package com.stress_detekt.services

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.stress_detekt.database.AppDatabase
import com.stress_detekt.database.SensorData
import com.stress_detekt.utils.SensorPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sqrt

class MultiSensorService(private val context: Context) : SensorEventListener {

    private val sensorManager: SensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val database = AppDatabase.getDatabase(context)
    private val capabilities = SensorCapabilities(context)
    private val sensorPrefs = SensorPreferences(context)

    private val _sensorDataLive = MutableLiveData<AllSensorReading>()
    val sensorDataLive: LiveData<AllSensorReading> = _sensorDataLive

    private val _isMonitoring = MutableLiveData(false)
    val isMonitoring: LiveData<Boolean> = _isMonitoring

    private val _sensorStatus = MutableLiveData<Map<String, SensorInfo>>()
    val sensorStatus: LiveData<Map<String, SensorInfo>> = _sensorStatus

    private var currentUserId: Long = -1
    private var dataPointsCollected = 0
    private var stepCounterStartValue: Int? = null

    private var currentData = AllSensorReading()

    data class AllSensorReading(
        var accelX: Float = 0f,
        var accelY: Float = 0f,
        var accelZ: Float = 0f,
        var accelMagnitude: Float = 0f,

        var gyroX: Float? = null,
        var gyroY: Float? = null,
        var gyroZ: Float? = null,

        var magX: Float? = null,
        var magY: Float? = null,
        var magZ: Float? = null,

        var lightLevel: Float? = null,
        var proximityDistance: Float? = null,
        var pressure: Float? = null,
        var temperature: Float? = null,
        var humidity: Float? = null,
        var heartRate: Float? = null,

        var stepCount: Int? = null,
        var stepCountStart: Int? = null,
        var stepsSinceStart: Int? = null,

        var gravityX: Float? = null,
        var gravityY: Float? = null,
        var gravityZ: Float? = null,

        var rotationX: Float? = null,
        var rotationY: Float? = null,
        var rotationZ: Float? = null,

        var activityType: String = "UNKNOWN",
        var dataPoints: Int = 0,
        var enabledSensorsCount: Int = 0
    )

    fun startMonitoring(userId: Long) {
        if (_isMonitoring.value == true) return

        currentUserId = userId
        dataPointsCollected = 0
        stepCounterStartValue = null

        val status = capabilities.getAllSensorsStatus()
        _sensorStatus.postValue(status)

        capabilities.logAvailableSensors()

        println("\n=== SENSOR PREFERENCES ===")

        var enabledCount = 0

        if (sensorPrefs.isAccelerometerEnabled()) {
            if (registerSensorIfAvailable(Sensor.TYPE_ACCELEROMETER)) enabledCount++
        }

        if (sensorPrefs.isGyroscopeEnabled()) {
            if (registerSensorIfAvailable(Sensor.TYPE_GYROSCOPE)) enabledCount++
        }

        if (sensorPrefs.isMagnetometerEnabled()) {
            if (registerSensorIfAvailable(Sensor.TYPE_MAGNETIC_FIELD)) enabledCount++
        }

        if (sensorPrefs.isLightEnabled()) {
            if (registerSensorIfAvailable(Sensor.TYPE_LIGHT)) enabledCount++
        }

        if (sensorPrefs.isProximityEnabled()) {
            if (registerSensorIfAvailable(Sensor.TYPE_PROXIMITY)) enabledCount++
        }

        if (sensorPrefs.isPressureEnabled()) {
            if (registerSensorIfAvailable(Sensor.TYPE_PRESSURE)) enabledCount++
        }

        if (sensorPrefs.isTemperatureEnabled()) {
            if (registerSensorIfAvailable(Sensor.TYPE_AMBIENT_TEMPERATURE)) enabledCount++
        }

        if (sensorPrefs.isHumidityEnabled()) {
            if (registerSensorIfAvailable(Sensor.TYPE_RELATIVE_HUMIDITY)) enabledCount++
        }

        if (sensorPrefs.isHeartRateEnabled()) {
            if (registerSensorIfAvailable(Sensor.TYPE_HEART_RATE)) enabledCount++
        }

        if (sensorPrefs.isStepCounterEnabled()) {
            if (registerSensorIfAvailable(Sensor.TYPE_STEP_COUNTER)) enabledCount++
        }

        if (sensorPrefs.isGravityEnabled()) {
            if (registerSensorIfAvailable(Sensor.TYPE_GRAVITY)) enabledCount++
        }

        if (sensorPrefs.isRotationEnabled()) {
            if (registerSensorIfAvailable(Sensor.TYPE_ROTATION_VECTOR)) enabledCount++
        }

        currentData.enabledSensorsCount = enabledCount
        println("✓ Total enabled sensors: $enabledCount")
        println("=========================\n")

        _isMonitoring.postValue(true)
    }

    private fun registerSensorIfAvailable(sensorType: Int): Boolean {
        val sensor = sensorManager.getDefaultSensor(sensorType)
        if (sensor != null) {
            sensorManager.registerListener(
                this,
                sensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
            println("✓ Registered: ${sensor.name}")
            return true
        } else {
            val sensorName = capabilities.checkSensor(sensorType).name
            println("✗ Not available: $sensorName")
            return false
        }
    }

    fun stopMonitoring() {
        sensorManager.unregisterListener(this)
        _isMonitoring.postValue(false)
        stepCounterStartValue = null
        println("✓ All sensors unregistered")
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return

        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                if (sensorPrefs.isAccelerometerEnabled()) {
                    currentData.accelX = event.values[0]
                    currentData.accelY = event.values[1]
                    currentData.accelZ = event.values[2]
                    currentData.accelMagnitude = sqrt(
                        currentData.accelX * currentData.accelX +
                                currentData.accelY * currentData.accelY +
                                currentData.accelZ * currentData.accelZ
                    )
                    currentData.activityType = detectActivity(currentData.accelMagnitude)
                }
            }

            Sensor.TYPE_GYROSCOPE -> {
                if (sensorPrefs.isGyroscopeEnabled()) {
                    currentData.gyroX = event.values[0]
                    currentData.gyroY = event.values[1]
                    currentData.gyroZ = event.values[2]
                }
            }

            Sensor.TYPE_MAGNETIC_FIELD -> {
                if (sensorPrefs.isMagnetometerEnabled()) {
                    currentData.magX = event.values[0]
                    currentData.magY = event.values[1]
                    currentData.magZ = event.values[2]
                }
            }

            Sensor.TYPE_LIGHT -> {
                if (sensorPrefs.isLightEnabled()) {
                    currentData.lightLevel = event.values[0]
                }
            }

            Sensor.TYPE_PROXIMITY -> {
                if (sensorPrefs.isProximityEnabled()) {
                    currentData.proximityDistance = event.values[0]
                }
            }

            Sensor.TYPE_PRESSURE -> {
                if (sensorPrefs.isPressureEnabled()) {
                    currentData.pressure = event.values[0]
                }
            }

            Sensor.TYPE_AMBIENT_TEMPERATURE -> {
                if (sensorPrefs.isTemperatureEnabled()) {
                    currentData.temperature = event.values[0]
                }
            }

            Sensor.TYPE_RELATIVE_HUMIDITY -> {
                if (sensorPrefs.isHumidityEnabled()) {
                    currentData.humidity = event.values[0]
                }
            }

            Sensor.TYPE_HEART_RATE -> {
                if (sensorPrefs.isHeartRateEnabled()) {
                    currentData.heartRate = event.values[0]
                }
            }

            Sensor.TYPE_STEP_COUNTER -> {
                if (sensorPrefs.isStepCounterEnabled()) {
                    val totalSteps = event.values[0].toInt()

                    if (stepCounterStartValue == null) {
                        stepCounterStartValue = totalSteps
                        currentData.stepCountStart = totalSteps
                    }

                    currentData.stepCount = totalSteps
                    currentData.stepsSinceStart = totalSteps - (stepCounterStartValue ?: totalSteps)

                    println("Step Counter: total=$totalSteps, start=$stepCounterStartValue, diff=${currentData.stepsSinceStart}")
                }
            }

            Sensor.TYPE_GRAVITY -> {
                if (sensorPrefs.isGravityEnabled()) {
                    currentData.gravityX = event.values[0]
                    currentData.gravityY = event.values[1]
                    currentData.gravityZ = event.values[2]
                }
            }

            Sensor.TYPE_ROTATION_VECTOR -> {
                if (sensorPrefs.isRotationEnabled()) {
                    currentData.rotationX = event.values[0]
                    currentData.rotationY = event.values[1]
                    currentData.rotationZ = event.values[2]
                }
            }
        }

        currentData.dataPoints = ++dataPointsCollected
        _sensorDataLive.postValue(currentData.copy())

        if (dataPointsCollected % 10 == 0) {
            saveToDB()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    private fun detectActivity(magnitude: Float): String {
        return when {
            magnitude < 10.5 -> "STILL"
            magnitude < 11.5 -> "WALKING"
            else -> "RUNNING"
        }
    }

    private fun saveToDB() {
        if (currentUserId == -1L) return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val data = SensorData(
                    userId = currentUserId,
                    accelX = if (sensorPrefs.isAccelerometerEnabled()) currentData.accelX else null,
                    accelY = if (sensorPrefs.isAccelerometerEnabled()) currentData.accelY else null,
                    accelZ = if (sensorPrefs.isAccelerometerEnabled()) currentData.accelZ else null,
                    accelMagnitude = if (sensorPrefs.isAccelerometerEnabled()) currentData.accelMagnitude else null,
                    gyroX = if (sensorPrefs.isGyroscopeEnabled()) currentData.gyroX else null,
                    gyroY = if (sensorPrefs.isGyroscopeEnabled()) currentData.gyroY else null,
                    gyroZ = if (sensorPrefs.isGyroscopeEnabled()) currentData.gyroZ else null,
                    magX = if (sensorPrefs.isMagnetometerEnabled()) currentData.magX else null,
                    magY = if (sensorPrefs.isMagnetometerEnabled()) currentData.magY else null,
                    magZ = if (sensorPrefs.isMagnetometerEnabled()) currentData.magZ else null,
                    lightLevel = if (sensorPrefs.isLightEnabled()) currentData.lightLevel else null,
                    proximityDistance = if (sensorPrefs.isProximityEnabled()) currentData.proximityDistance else null,
                    pressure = if (sensorPrefs.isPressureEnabled()) currentData.pressure else null,
                    temperature = if (sensorPrefs.isTemperatureEnabled()) currentData.temperature else null,
                    humidity = if (sensorPrefs.isHumidityEnabled()) currentData.humidity else null,
                    heartRate = if (sensorPrefs.isHeartRateEnabled()) currentData.heartRate else null,
                    stepCount = if (sensorPrefs.isStepCounterEnabled()) currentData.stepsSinceStart else null,
                    gravityX = if (sensorPrefs.isGravityEnabled()) currentData.gravityX else null,
                    gravityY = if (sensorPrefs.isGravityEnabled()) currentData.gravityY else null,
                    gravityZ = if (sensorPrefs.isGravityEnabled()) currentData.gravityZ else null,
                    rotationX = if (sensorPrefs.isRotationEnabled()) currentData.rotationX else null,
                    rotationY = if (sensorPrefs.isRotationEnabled()) currentData.rotationY else null,
                    rotationZ = if (sensorPrefs.isRotationEnabled()) currentData.rotationZ else null,
                    activityType = currentData.activityType
                )
                database.sensorDao().insertSensorData(data)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getAverageActivity(userId: Long, callback: (String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val fiveMinutesAgo = System.currentTimeMillis() - (5 * 60 * 1000)
                val avgMag = database.sensorDao().getAverageMagnitude(userId, fiveMinutesAgo)

                val activity = when {
                    avgMag == null -> "NO DATA"
                    avgMag < 10.5 -> "MOSTLY STILL"
                    avgMag < 11.5 -> "MOSTLY WALKING"
                    else -> "MOSTLY ACTIVE"
                }

                callback(activity)
            } catch (e: Exception) {
                callback("ERROR")
            }
        }
    }
}