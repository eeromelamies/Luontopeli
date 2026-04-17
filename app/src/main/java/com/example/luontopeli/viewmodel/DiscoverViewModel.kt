package com.example.luontopeli.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.luontopeli.data.local.entity.NatureSpot
import com.example.luontopeli.data.repository.NatureSpotRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel löytönäkymälle (DiscoverScreen).
 */
@HiltViewModel
class DiscoverViewModel @Inject constructor(
    application: Application,
    private val repository: NatureSpotRepository
) : AndroidViewModel(application) {

    private val _allSpots = MutableStateFlow<List<NatureSpot>>(emptyList())
    val allSpots: StateFlow<List<NatureSpot>> = _allSpots.asStateFlow()

    init {
        viewModelScope.launch {
            repository.allSpots.collect { spots ->
                _allSpots.value = spots
            }
        }
    }
}
