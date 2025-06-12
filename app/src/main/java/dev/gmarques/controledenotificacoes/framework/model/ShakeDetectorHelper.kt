package dev.gmarques.controledenotificacoes.framework.model

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.math.sqrt

/**
 * Criado por Gilian Marques
 * Em quinta-feira, 12 de junho de 2025 as 10:49.
 */
class ShakeDetectorHelper @Inject constructor(
    @ApplicationContext context: Context,
) : SensorEventListener {
    private lateinit var onShake: () -> Unit

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private var accel = 0f
    private var accelCurrent = SensorManager.GRAVITY_EARTH
    private var accelLast = SensorManager.GRAVITY_EARTH

    private val threshold = 4f

    fun start(onShake: () -> Unit) {
        this.onShake = onShake
        accelerometer?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        accelLast = accelCurrent
        accelCurrent = sqrt(x * x + y * y + z * z)
        val delta = accelCurrent - accelLast
        accel = accel * 0.9f + delta

        if (accel > threshold) {
            onShake()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Ignorado
    }
}