package com.example.luontopeli.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.luontopeli.data.local.AppDatabase
import com.example.luontopeli.data.local.entity.WalkSession
import com.example.luontopeli.sensor.STEP_LENGTH_METERS
import com.example.luontopeli.sensor.StepCounterManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class WalkViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val stepManager = StepCounterManager(application)

    // Aktiivisen sessoin tila
    private val _currentSession = MutableStateFlow<WalkSession?>(null)
    val currentSession: StateFlow<WalkSession?> = _currentSession.asStateFlow()

    // Onko kävely käynnissä
    private val _isWalking = MutableStateFlow(false)
    val isWalking: StateFlow<Boolean> = _isWalking.asStateFlow()

    // Aloita uusi kävelysessio
    fun startWalk() {
        if (_isWalking.value) return

        val session = WalkSession()
        _currentSession.value = session
        _isWalking.value = true

        stepManager.startStepCounting {
            _currentSession.update { current ->
                current?.copy(
                    stepCount = current.stepCount + 1,
                    distanceMeters = current.distanceMeters + STEP_LENGTH_METERS
                )
            }
        }
    }

    // Lopeta kävely ja tallenna sessio
    fun stopWalk() {
        stepManager.stopStepCounting()
        _isWalking.value = false
        _currentSession.update { it?.copy(
            endTime = System.currentTimeMillis(),
            isActive = false
        )}

        // Tallenna päättynyt sessio Room-tietokantaan
        viewModelScope.launch {
            _currentSession.value?.let { session ->
                db.walkSessionDao().insert(session)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stepManager.stopAll()
    }
}
