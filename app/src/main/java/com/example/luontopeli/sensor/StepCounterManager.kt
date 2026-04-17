package com.example.luontopeli.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

class StepCounterManager(context: Context) {

    // SensorManager on Android-järjestelmäpalvelu sensorien käyttöön
    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    // Haetaan STEP_DETECTOR-sensori (tapahtuma per askel)
    private val stepSensor: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

    // Gyroskooppisensori kääntöliikkeen tunnistukseen
    private val gyroSensor: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

    private var stepListener: SensorEventListener? = null
    private var gyroListener: SensorEventListener? = null

    // Ravistuksen tunnistuksen muuttujat
    private var lastShakeTime = 0L
    private val SHAKE_THRESHOLD = 5.0f  // rad/s – kuinka voimakas ravistus
    private val SHAKE_COOLDOWN = 1000L  // ms – ei tunnisteta useita kertoja peräkkäin

    // Aloita askelmittaus
    fun startStepCounting(onStep: () -> Unit) {
        stepListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_STEP_DETECTOR) {
                    onStep()
                }
            }
            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
        }

        stepSensor?.let {
            sensorManager.registerListener(
                stepListener,
                it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    fun stopStepCounting() {
        stepListener?.let { sensorManager.unregisterListener(it) }
        stepListener = null
    }

    /**
     * Aloittaa ravistuksen tunnistuksen gyroskoopin avulla.
     * @param onShake kutsuu tätä funktiota kun ravistus havaitaan.
     */
    fun startShakeDetection(onShake: () -> Unit) {
        gyroListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_GYROSCOPE) {
                    val x = event.values[0]
                    val y = event.values[1]
                    val z = event.values[2]
                    
                    if (detectShake(x, y, z)) {
                        onShake()
                    }
                }
            }
            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
        }

        gyroSensor?.let {
            sensorManager.registerListener(
                gyroListener,
                it,
                SensorManager.SENSOR_DELAY_GAME
            )
        }
    }

    /**
     * Laskee ravistuksen voimakkuuden ja tarkistaa ylittyykö kynnysarvo.
     */
    private fun detectShake(x: Float, y: Float, z: Float): Boolean {
        // Laske pyörimisnopeuden suuruus (vektorimagnitudi)
        val magnitude = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
        val now = System.currentTimeMillis()

        // Tunnista ravistus jos yli kynnysarvon ja cooldown kulunut
        if (magnitude > SHAKE_THRESHOLD && now - lastShakeTime > SHAKE_COOLDOWN) {
            lastShakeTime = now
            return true
        }
        return false
    }

    fun stopGyroscope() {
        gyroListener?.let { sensorManager.unregisterListener(it) }
        gyroListener = null
    }

    fun stopAll() {
        stopStepCounting()
        stopGyroscope()
    }

    fun isStepSensorAvailable(): Boolean = stepSensor != null
}

const val STEP_LENGTH_METERS = 0.74f
