package com.stress_detekt.services

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.stress_detekt.config.AppConfig
import com.stress_detekt.database.AppDatabase
import com.stress_detekt.database.SensorData
import com.stress_detekt.utils.SensorPreferences
import com.stress_detekt.utils.SensorRingBuffer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sqrt

/**
 * MultiSensorService - Збір даних з усіх датчиків телефону
 *
 * Версія: 2.0 (з AppConfig та інтервалом 2 секунди)
 *
 * Особливості:
 * - Використовує AppConfig для всіх налаштувань
 * - Accumulator pattern: накопичує samples та усереднює
 * - Зберігає кожні AppConfig.AGGREGATION_INTERVAL_MS (2 сек)
 * - Підтримує 12 типів датчиків
 * - Thread-safe
 */
class MultiSensorService(private val context: Context) : SensorEventListener {

    private val sensorManager: SensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val database = AppDatabase.getDatabase(context)
    private val capabilities = SensorCapabilities(context)
    private val sensorPrefs = SensorPreferences(context)

    // Ring buffer для ML inference (останні 30 секунд)
    private val sensorBuffer = SensorRingBuffer()

    // LiveData для UI
    private val _sensorDataLive = MutableLiveData<AllSensorReading>()
    val sensorDataLive: LiveData<AllSensorReading> = _sensorDataLive

    private val _isMonitoring = MutableLiveData(false)
    val isMonitoring: LiveData<Boolean> = _isMonitoring

    private val _sensorStatus = MutableLiveData<Map<String, SensorInfo>>()
    val sensorStatus: LiveData<Map<String, SensorInfo>> = _sensorStatus

    // Стан моніторингу
    private var currentUserId: Long = -1
    private var dataPointsCollected = 0
    private var stepCounterStartValue: Int? = null

    // Таймер для aggregation
    private var lastAggregationTime = 0L

    // ═══════════════════════════════════════════════════════════════════════
    // ACCUMULATOR - для усереднення samples
    // ═══════════════════════════════════════════════════════════════════════

    // Accelerometer
    private var accelXSum = 0f
    private var accelYSum = 0f
    private var accelZSum = 0f
    private var accelCount = 0

    // Gyroscope
    private var gyroXSum = 0f
    private var gyroYSum = 0f
    private var gyroZSum = 0f
    private var gyroCount = 0

    // Magnetometer
    private var magXSum = 0f
    private var magYSum = 0f
    private var magZSum = 0f
    private var magCount = 0

    // Light
    private var lightLevelSum = 0f
    private var lightCount = 0

    // Proximity
    private var proximitySum = 0f
    private var proximityCount = 0

    // Pressure
    private var pressureSum = 0f
    private var pressureCount = 0

    // Temperature
    private var temperatureSum = 0f
    private var temperatureCount = 0

    // Humidity
    private var humiditySum = 0f
    private var humidityCount = 0

    // Heart Rate
    private var heartRateSum = 0f
    private var heartRateCount = 0

    // Gravity
    private var gravityXSum = 0f
    private var gravityYSum = 0f
    private var gravityZSum = 0f
    private var gravityCount = 0

    // Rotation Vector
    private var rotationXSum = 0f
    private var rotationYSum = 0f
    private var rotationZSum = 0f
    private var rotationCount = 0

    // Поточні дані для UI
    private var currentData = AllSensorReading()

    /**
     * Data class для всіх sensor readings
     */
    data class AllSensorReading(
        // Accelerometer
        var accelX: Float = 0f,
        var accelY: Float = 0f,
        var accelZ: Float = 0f,
        var accelMagnitude: Float = 0f,

        // Gyroscope
        var gyroX: Float? = null,
        var gyroY: Float? = null,
        var gyroZ: Float? = null,

        // Magnetometer
        var magX: Float? = null,
        var magY: Float? = null,
        var magZ: Float? = null,

        // Environment
        var lightLevel: Float? = null,
        var proximityDistance: Float? = null,
        var pressure: Float? = null,
        var temperature: Float? = null,
        var humidity: Float? = null,

        // Biometric
        var heartRate: Float? = null,

        // Step Counter
        var stepCount: Int? = null,
        var stepCountStart: Int? = null,
        var stepsSinceStart: Int? = null,

        // Motion
        var gravityX: Float? = null,
        var gravityY: Float? = null,
        var gravityZ: Float? = null,

        var rotationX: Float? = null,
        var rotationY: Float? = null,
        var rotationZ: Float? = null,

        // Metadata
        var activityType: String = "UNKNOWN",
        var dataPoints: Int = 0,
        var enabledSensorsCount: Int = 0
    )

    /**
     * Почати моніторинг
     */
    fun startMonitoring(userId: Long) {
        if (_isMonitoring.value == true) {
            if (AppConfig.DEBUG_LOGGING) {
                println("⚠️ Моніторинг вже запущено")
            }
            return
        }

        currentUserId = userId
        dataPointsCollected = 0
        stepCounterStartValue = null
        lastAggregationTime = System.currentTimeMillis()

        // Скинути accumulators
        resetAccumulators()

        // Вивести конфігурацію
        AppConfig.printConfig()

        // Валідувати конфігурацію
        val validationError = AppConfig.validateConfig()
        if (validationError != null) {
            println(validationError)
        }

        // Отримати статус датчиків
        val status = capabilities.getAllSensorsStatus()
        _sensorStatus.postValue(status)

        if (AppConfig.DEBUG_LOGGING) {
            capabilities.logAvailableSensors()
            println("\n═══ STARTING MONITORING ═══")
            println("User ID: $userId")
            println("Aggregation interval: ${AppConfig.AGGREGATION_INTERVAL_MS}ms")
            println("Expected samples per aggregation: ${AppConfig.SAMPLES_PER_AGGREGATION}")
            println("Expected records per day: ${AppConfig.RECORDS_PER_DAY}")
            println("═══════════════════════════════\n")
        }

        var enabledCount = 0

        // Реєструємо всі увімкнені датчики
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

        if (AppConfig.DEBUG_LOGGING) {
            println("✅ Enabled sensors: $enabledCount")
        }

        _isMonitoring.postValue(true)
    }

    /**
     * Зареєструвати датчик якщо доступний
     */
    private fun registerSensorIfAvailable(sensorType: Int): Boolean {
        val sensor = sensorManager.getDefaultSensor(sensorType)
        if (sensor != null) {
            val success = sensorManager.registerListener(
                this,
                sensor,
                AppConfig.SENSOR_DELAY  // Використовуємо з AppConfig
            )

            if (success && AppConfig.DEBUG_LOGGING) {
                println("✓ Registered: ${sensor.name}")
            }

            return success
        } else {
            if (AppConfig.DEBUG_LOGGING) {
                val sensorName = capabilities.checkSensor(sensorType).name
                println("✗ Not available: $sensorName")
            }
            return false
        }
    }

    /**
     * Зупинити моніторинг
     */
    fun stopMonitoring() {
        sensorManager.unregisterListener(this)
        _isMonitoring.postValue(false)
        stepCounterStartValue = null
        resetAccumulators()

        // Очищаємо buffer при зупинці
        sensorBuffer.clear()

        if (AppConfig.DEBUG_LOGGING) {
            println("✓ All sensors unregistered")
            println("📊 Total data points collected: $dataPointsCollected")
        }
    }

    /**
     * Обробка нових даних з датчика
     */
    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return

        // Накопичуємо значення в accumulators
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                if (sensorPrefs.isAccelerometerEnabled()) {
                    accelXSum += event.values[0]
                    accelYSum += event.values[1]
                    accelZSum += event.values[2]
                    accelCount++
                }
            }

            Sensor.TYPE_GYROSCOPE -> {
                if (sensorPrefs.isGyroscopeEnabled()) {
                    gyroXSum += event.values[0]
                    gyroYSum += event.values[1]
                    gyroZSum += event.values[2]
                    gyroCount++
                }
            }

            Sensor.TYPE_MAGNETIC_FIELD -> {
                if (sensorPrefs.isMagnetometerEnabled()) {
                    magXSum += event.values[0]
                    magYSum += event.values[1]
                    magZSum += event.values[2]
                    magCount++
                }
            }

            Sensor.TYPE_LIGHT -> {
                if (sensorPrefs.isLightEnabled()) {
                    lightLevelSum += event.values[0]
                    lightCount++
                }
            }

            Sensor.TYPE_PROXIMITY -> {
                if (sensorPrefs.isProximityEnabled()) {
                    proximitySum += event.values[0]
                    proximityCount++
                }
            }

            Sensor.TYPE_PRESSURE -> {
                if (sensorPrefs.isPressureEnabled()) {
                    pressureSum += event.values[0]
                    pressureCount++
                }
            }

            Sensor.TYPE_AMBIENT_TEMPERATURE -> {
                if (sensorPrefs.isTemperatureEnabled()) {
                    temperatureSum += event.values[0]
                    temperatureCount++
                }
            }

            Sensor.TYPE_RELATIVE_HUMIDITY -> {
                if (sensorPrefs.isHumidityEnabled()) {
                    humiditySum += event.values[0]
                    humidityCount++
                }
            }

            Sensor.TYPE_HEART_RATE -> {
                if (sensorPrefs.isHeartRateEnabled()) {
                    heartRateSum += event.values[0]
                    heartRateCount++
                }
            }

            Sensor.TYPE_STEP_COUNTER -> {
                if (sensorPrefs.isStepCounterEnabled()) {
                    val totalSteps = event.values[0].toInt()

                    if (stepCounterStartValue == null) {
                        stepCounterStartValue = totalSteps
                        currentData.stepCountStart = totalSteps

                        if (AppConfig.DEBUG_LOGGING) {
                            println("🚶 Step Counter initialized: $totalSteps")
                        }
                    }

                    currentData.stepCount = totalSteps
                    currentData.stepsSinceStart = totalSteps - (stepCounterStartValue ?: totalSteps)
                }
            }

            Sensor.TYPE_GRAVITY -> {
                if (sensorPrefs.isGravityEnabled()) {
                    gravityXSum += event.values[0]
                    gravityYSum += event.values[1]
                    gravityZSum += event.values[2]
                    gravityCount++
                }
            }

            Sensor.TYPE_ROTATION_VECTOR -> {
                if (sensorPrefs.isRotationEnabled()) {
                    rotationXSum += event.values[0]
                    rotationYSum += event.values[1]
                    rotationZSum += event.values[2]
                    rotationCount++
                }
            }
        }

        // Перевіряємо чи пройшов aggregation interval
        val currentTime = System.currentTimeMillis()
        val timeSinceLastAggregation = currentTime - lastAggregationTime

        if (timeSinceLastAggregation >= AppConfig.AGGREGATION_INTERVAL_MS) {
            // Час усереднити та зберегти
            aggregateAndSave()
            lastAggregationTime = currentTime
        }

        // Оновлюємо UI (з поточними accumulator значеннями для preview)
        updateCurrentDataForUI()
        _sensorDataLive.postValue(currentData.copy())
    }

    /**
     * Оновити currentData для відображення в UI
     * (показуємо поточні середні значення)
     */
    private fun updateCurrentDataForUI() {
        // Accelerometer
        if (accelCount > 0) {
            currentData.accelX = accelXSum / accelCount
            currentData.accelY = accelYSum / accelCount
            currentData.accelZ = accelZSum / accelCount
            currentData.accelMagnitude = sqrt(
                currentData.accelX * currentData.accelX +
                        currentData.accelY * currentData.accelY +
                        currentData.accelZ * currentData.accelZ
            )
            currentData.activityType = detectActivity(currentData.accelMagnitude)
        }

        // Gyroscope
        if (gyroCount > 0) {
            currentData.gyroX = gyroXSum / gyroCount
            currentData.gyroY = gyroYSum / gyroCount
            currentData.gyroZ = gyroZSum / gyroCount
        }

        // Magnetometer
        if (magCount > 0) {
            currentData.magX = magXSum / magCount
            currentData.magY = magYSum / magCount
            currentData.magZ = magZSum / magCount
        }

        // Light
        if (lightCount > 0) {
            currentData.lightLevel = lightLevelSum / lightCount
        }

        // Proximity
        if (proximityCount > 0) {
            currentData.proximityDistance = proximitySum / proximityCount
        }

        // Pressure
        if (pressureCount > 0) {
            currentData.pressure = pressureSum / pressureCount
        }

        // Temperature
        if (temperatureCount > 0) {
            currentData.temperature = temperatureSum / temperatureCount
        }

        // Humidity
        if (humidityCount > 0) {
            currentData.humidity = humiditySum / humidityCount
        }

        // Heart Rate
        if (heartRateCount > 0) {
            currentData.heartRate = heartRateSum / heartRateCount
        }

        // Gravity
        if (gravityCount > 0) {
            currentData.gravityX = gravityXSum / gravityCount
            currentData.gravityY = gravityYSum / gravityCount
            currentData.gravityZ = gravityZSum / gravityCount
        }

        // Rotation
        if (rotationCount > 0) {
            currentData.rotationX = rotationXSum / rotationCount
            currentData.rotationY = rotationYSum / rotationCount
            currentData.rotationZ = rotationZSum / rotationCount
        }

        currentData.dataPoints = dataPointsCollected
    }

    /**
     * Усереднити накопичені дані та зберегти в БД
     */
    private fun aggregateAndSave() {
        // Оновлюємо currentData з усередненими значеннями
        updateCurrentDataForUI()

        // Збільшуємо лічильник
        dataPointsCollected++
        currentData.dataPoints = dataPointsCollected

        // Зберігаємо в БД
        saveToDB()

        // Додаємо в RingBuffer для ML inference
        addToBuffer()

        // Скидаємо accumulators
        resetAccumulators()

        // Debug log кожні N записів
        if (AppConfig.DEBUG_LOGGING &&
            dataPointsCollected % AppConfig.LOG_EVERY_N_RECORDS == 0) {
            val minutes = (dataPointsCollected * AppConfig.AGGREGATION_INTERVAL_MS) / 60000
            println("📊 Saved $dataPointsCollected records (~$minutes minutes of data)")

            // Логуємо статистику буфера
            val bufferStats = sensorBuffer.getStats()
            println("🔄 Buffer: ${bufferStats.currentSize}/${bufferStats.capacity} " +
                    "(${String.format("%.0f", bufferStats.fillPercentage)}% full, " +
                    "${String.format("%.1f", bufferStats.timeSpanSeconds)}s span)")
        }
    }

    /**
     * Скинути всі accumulators
     */
    private fun resetAccumulators() {
        accelXSum = 0f
        accelYSum = 0f
        accelZSum = 0f
        accelCount = 0

        gyroXSum = 0f
        gyroYSum = 0f
        gyroZSum = 0f
        gyroCount = 0

        magXSum = 0f
        magYSum = 0f
        magZSum = 0f
        magCount = 0

        lightLevelSum = 0f
        lightCount = 0

        proximitySum = 0f
        proximityCount = 0

        pressureSum = 0f
        pressureCount = 0

        temperatureSum = 0f
        temperatureCount = 0

        humiditySum = 0f
        humidityCount = 0

        heartRateSum = 0f
        heartRateCount = 0

        gravityXSum = 0f
        gravityYSum = 0f
        gravityZSum = 0f
        gravityCount = 0

        rotationXSum = 0f
        rotationYSum = 0f
        rotationZSum = 0f
        rotationCount = 0
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Можна логувати зміни точності
        if (AppConfig.DEBUG_LOGGING && sensor != null) {
            val accuracyStr = when (accuracy) {
                SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> "HIGH"
                SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> "MEDIUM"
                SensorManager.SENSOR_STATUS_ACCURACY_LOW -> "LOW"
                SensorManager.SENSOR_STATUS_UNRELIABLE -> "UNRELIABLE"
                else -> "UNKNOWN"
            }
            // Логуємо тільки якщо точність низька
            if (accuracy <= SensorManager.SENSOR_STATUS_ACCURACY_LOW) {
                println("⚠️ ${sensor.name} accuracy: $accuracyStr")
            }
        }
    }

    /**
     * Визначити тип активності по magnitude
     */
    private fun detectActivity(magnitude: Float): String {
        return when {
            magnitude < 10.5 -> "STILL"
            magnitude < 11.5 -> "WALKING"
            else -> "RUNNING"
        }
    }

    /**
     * Додати поточні дані в RingBuffer
     */
    private fun addToBuffer() {
        val reading = SensorRingBuffer.SensorReading(
            timestamp = System.currentTimeMillis(),

            // Accelerometer
            accelX = currentData.accelX,
            accelY = currentData.accelY,
            accelZ = currentData.accelZ,
            accelMagnitude = currentData.accelMagnitude,

            // Gyroscope
            gyroX = currentData.gyroX,
            gyroY = currentData.gyroY,
            gyroZ = currentData.gyroZ,

            // Magnetometer
            magX = currentData.magX,
            magY = currentData.magY,
            magZ = currentData.magZ,

            // Environment
            lightLevel = currentData.lightLevel,
            proximityDistance = currentData.proximityDistance,
            pressure = currentData.pressure,
            temperature = currentData.temperature,
            humidity = currentData.humidity,

            // Biometric
            heartRate = currentData.heartRate,
            stepCount = currentData.stepsSinceStart,

            // Motion
            gravityX = currentData.gravityX,
            gravityY = currentData.gravityY,
            gravityZ = currentData.gravityZ,

            rotationX = currentData.rotationX,
            rotationY = currentData.rotationY,
            rotationZ = currentData.rotationZ,

            // Metadata
            activityType = currentData.activityType
        )

        sensorBuffer.add(reading)
    }

    /**
     * Зберегти в БД
     */
    private fun saveToDB() {
        if (currentUserId == -1L) return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val data = SensorData(
                    userId = currentUserId,
                    timestamp = System.currentTimeMillis(),

                    // Accelerometer
                    accelX = if (sensorPrefs.isAccelerometerEnabled()) currentData.accelX else null,
                    accelY = if (sensorPrefs.isAccelerometerEnabled()) currentData.accelY else null,
                    accelZ = if (sensorPrefs.isAccelerometerEnabled()) currentData.accelZ else null,
                    accelMagnitude = if (sensorPrefs.isAccelerometerEnabled()) currentData.accelMagnitude else null,

                    // Gyroscope
                    gyroX = if (sensorPrefs.isGyroscopeEnabled()) currentData.gyroX else null,
                    gyroY = if (sensorPrefs.isGyroscopeEnabled()) currentData.gyroY else null,
                    gyroZ = if (sensorPrefs.isGyroscopeEnabled()) currentData.gyroZ else null,

                    // Magnetometer
                    magX = if (sensorPrefs.isMagnetometerEnabled()) currentData.magX else null,
                    magY = if (sensorPrefs.isMagnetometerEnabled()) currentData.magY else null,
                    magZ = if (sensorPrefs.isMagnetometerEnabled()) currentData.magZ else null,

                    // Environment
                    lightLevel = if (sensorPrefs.isLightEnabled()) currentData.lightLevel else null,
                    proximityDistance = if (sensorPrefs.isProximityEnabled()) currentData.proximityDistance else null,
                    pressure = if (sensorPrefs.isPressureEnabled()) currentData.pressure else null,
                    temperature = if (sensorPrefs.isTemperatureEnabled()) currentData.temperature else null,
                    humidity = if (sensorPrefs.isHumidityEnabled()) currentData.humidity else null,

                    // Biometric
                    heartRate = if (sensorPrefs.isHeartRateEnabled()) currentData.heartRate else null,
                    stepCount = if (sensorPrefs.isStepCounterEnabled()) currentData.stepsSinceStart else null,

                    // Motion
                    gravityX = if (sensorPrefs.isGravityEnabled()) currentData.gravityX else null,
                    gravityY = if (sensorPrefs.isGravityEnabled()) currentData.gravityY else null,
                    gravityZ = if (sensorPrefs.isGravityEnabled()) currentData.gravityZ else null,

                    rotationX = if (sensorPrefs.isRotationEnabled()) currentData.rotationX else null,
                    rotationY = if (sensorPrefs.isRotationEnabled()) currentData.rotationY else null,
                    rotationZ = if (sensorPrefs.isRotationEnabled()) currentData.rotationZ else null,

                    // Metadata
                    activityType = currentData.activityType
                )

                database.sensorDao().insertSensorData(data)

            } catch (e: Exception) {
                if (AppConfig.DEBUG_LOGGING) {
                    println("❌ Database save error: ${e.message}")
                    e.printStackTrace()
                }
            }
        }
    }

    /**
     * Отримати середню активність за останні N хвилин
     */
    fun getAverageActivity(userId: Long, minutesAgo: Int = 5, callback: (String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val timeAgo = System.currentTimeMillis() - (minutesAgo * 60 * 1000)
                val avgMag = database.sensorDao().getAverageMagnitude(userId, timeAgo)

                val activity = when {
                    avgMag == null -> "NO DATA"
                    avgMag < 10.5 -> "MOSTLY STILL"
                    avgMag < 11.5 -> "MOSTLY WALKING"
                    else -> "MOSTLY ACTIVE"
                }

                callback(activity)
            } catch (e: Exception) {
                if (AppConfig.DEBUG_LOGGING) {
                    println("❌ Average activity error: ${e.message}")
                }
                callback("ERROR")
            }
        }
    }

    /**
     * Отримати статистику збору даних
     */
    fun getCollectionStats(): CollectionStats {
        val runningTimeMs = if (_isMonitoring.value == true) {
            dataPointsCollected * AppConfig.AGGREGATION_INTERVAL_MS
        } else {
            0L
        }

        return CollectionStats(
            isMonitoring = _isMonitoring.value ?: false,
            dataPointsCollected = dataPointsCollected,
            runningTimeMs = runningTimeMs,
            enabledSensorsCount = currentData.enabledSensorsCount,
            currentActivity = currentData.activityType,
            aggregationIntervalMs = AppConfig.AGGREGATION_INTERVAL_MS
        )
    }

    data class CollectionStats(
        val isMonitoring: Boolean,
        val dataPointsCollected: Int,
        val runningTimeMs: Long,
        val enabledSensorsCount: Int,
        val currentActivity: String,
        val aggregationIntervalMs: Long
    )

    // ═══════════════════════════════════════════════════════════════════════
    // RING BUFFER ACCESS (для ML inference)
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Отримати RingBuffer для ML inference
     *
     * @return SensorRingBuffer instance
     */
    fun getBuffer(): SensorRingBuffer {
        return sensorBuffer
    }

    /**
     * Перевірити чи готовий буфер для ML inference
     *
     * @return true якщо достатньо даних
     */
    fun isBufferReadyForInference(): Boolean {
        return sensorBuffer.isReadyForInference()
    }

    /**
     * Отримати вікно даних для ML inference
     *
     * @return List останніх ML_WINDOW_SIZE записів або null
     */
    fun getInferenceWindow(): List<SensorRingBuffer.SensorReading>? {
        return sensorBuffer.getInferenceWindow()
    }

    /**
     * Отримати статистику буфера
     */
    fun getBufferStats(): SensorRingBuffer.BufferStats {
        return sensorBuffer.getStats()
    }

    /**
     * Очистити буфер
     * Корисно при старті нового моніторингу
     */
    fun clearBuffer() {
        sensorBuffer.clear()
    }
}