package com.stress_detekt.utils

import com.stress_detekt.config.AppConfig
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * SensorRingBuffer - Кільцевий буфер для sensor readings
 *
 * Зберігає останні N секунд даних в RAM для швидкого доступу
 * при ML inference. Thread-safe.
 *
 * Особливості:
 * - Фіксований розмір (capacity)
 * - FIFO (First In, First Out)
 * - Автоматично видаляє старі записи
 * - Thread-safe з ReentrantLock
 * - Підтримує різні типи запитів (by index, by timestamp)
 *
 * Використання:
 * ```
 * val buffer = SensorRingBuffer()
 * buffer.add(reading)
 * val lastFive = buffer.getWindow(5)  // Останні 5 записів
 * ```
 */
class SensorRingBuffer(
    /**
     * Місткість буфера (кількість записів)
     * За замовчуванням: AppConfig.BUFFER_SIZE_RECORDS (15 записів при 2 сек інтервалі)
     */
    private val capacity: Int = AppConfig.BUFFER_SIZE_RECORDS
) {

    // Використовуємо ArrayDeque для ефективності
    private val buffer = ArrayDeque<SensorReading>(capacity)

    // Lock для thread-safety
    private val lock = ReentrantLock()

    /**
     * Data class для одного sensor reading
     * Містить всі дані з датчиків за один момент часу
     */
    data class SensorReading(
        val timestamp: Long,

        // Accelerometer
        val accelX: Float,
        val accelY: Float,
        val accelZ: Float,
        val accelMagnitude: Float,

        // Gyroscope
        val gyroX: Float?,
        val gyroY: Float?,
        val gyroZ: Float?,

        // Magnetometer
        val magX: Float?,
        val magY: Float?,
        val magZ: Float?,

        // Environment
        val lightLevel: Float?,
        val proximityDistance: Float?,
        val pressure: Float?,
        val temperature: Float?,
        val humidity: Float?,

        // Biometric
        val heartRate: Float?,
        val stepCount: Int?,

        // Motion
        val gravityX: Float?,
        val gravityY: Float?,
        val gravityZ: Float?,

        val rotationX: Float?,
        val rotationY: Float?,
        val rotationZ: Float?,

        // Metadata
        val activityType: String
    ) {
        /**
         * Конвертувати в Map для feature extraction
         */
        fun toMap(): Map<String, Any?> {
            return mapOf(
                "timestamp" to timestamp,
                "accelX" to accelX,
                "accelY" to accelY,
                "accelZ" to accelZ,
                "accelMagnitude" to accelMagnitude,
                "gyroX" to gyroX,
                "gyroY" to gyroY,
                "gyroZ" to gyroZ,
                "magX" to magX,
                "magY" to magY,
                "magZ" to magZ,
                "lightLevel" to lightLevel,
                "proximityDistance" to proximityDistance,
                "pressure" to pressure,
                "temperature" to temperature,
                "humidity" to humidity,
                "heartRate" to heartRate,
                "stepCount" to stepCount,
                "gravityX" to gravityX,
                "gravityY" to gravityY,
                "gravityZ" to gravityZ,
                "rotationX" to rotationX,
                "rotationY" to rotationY,
                "rotationZ" to rotationZ,
                "activityType" to activityType
            )
        }
    }

    /**
     * Додати новий запис в буфер
     *
     * Якщо буфер повний - видаляє найстаріший запис
     *
     * @param reading Sensor reading для додавання
     */
    fun add(reading: SensorReading) {
        lock.withLock {
            // Якщо досягли capacity - видаляємо найстаріший
            if (buffer.size >= capacity) {
                buffer.removeFirst()
            }

            // Додаємо новий в кінець
            buffer.addLast(reading)

            if (AppConfig.DEBUG_LOGGING && buffer.size % 5 == 0) {
                println("🔄 Buffer size: ${buffer.size}/$capacity")
            }
        }
    }

    /**
     * Отримати останні N записів
     *
     * @param windowSize Кількість записів (наприклад, 5)
     * @return List останніх windowSize записів, або порожній список якщо недостатньо
     */
    fun getWindow(windowSize: Int): List<SensorReading> {
        lock.withLock {
            return if (buffer.size < windowSize) {
                if (AppConfig.DEBUG_LOGGING) {
                    println("⚠️ Buffer має ${buffer.size} записів, потрібно $windowSize")
                }
                emptyList()
            } else {
                buffer.takeLast(windowSize).toList()
            }
        }
    }

    /**
     * Отримати записи по діапазону timestamp
     *
     * @param startTimestamp Початковий timestamp (включно)
     * @param endTimestamp Кінцевий timestamp (включно)
     * @return List записів в діапазоні
     */
    fun getWindowByTimestamp(
        startTimestamp: Long,
        endTimestamp: Long
    ): List<SensorReading> {
        lock.withLock {
            return buffer.filter {
                it.timestamp >= startTimestamp && it.timestamp <= endTimestamp
            }.toList()
        }
    }

    /**
     * Отримати записи за останні N мілісекунд
     *
     * @param milliseconds Скільки мілісекунд назад шукати
     * @return List записів за останні N мс
     */
    fun getLastMilliseconds(milliseconds: Long): List<SensorReading> {
        lock.withLock {
            if (buffer.isEmpty()) {
                return emptyList()
            }

            val now = buffer.last().timestamp
            val threshold = now - milliseconds

            return buffer.filter { it.timestamp >= threshold }.toList()
        }
    }

    /**
     * Отримати записи за останні N секунд
     *
     * @param seconds Скільки секунд назад шукати
     * @return List записів за останні N секунд
     */
    fun getLastSeconds(seconds: Int): List<SensorReading> {
        return getLastMilliseconds(seconds * 1000L)
    }

    /**
     * Отримати всі записи з буфера
     *
     * @return List всіх записів
     */
    fun getAll(): List<SensorReading> {
        lock.withLock {
            return buffer.toList()
        }
    }

    /**
     * Отримати запис по індексу
     *
     * @param index Індекс (0 = найстаріший, size-1 = найновіший)
     * @return SensorReading або null якщо індекс недійсний
     */
    fun get(index: Int): SensorReading? {
        lock.withLock {
            return if (index in 0 until buffer.size) {
                buffer[index]
            } else {
                null
            }
        }
    }

    /**
     * Отримати останній (найновіший) запис
     *
     * @return Останній SensorReading або null якщо буфер порожній
     */
    fun getLast(): SensorReading? {
        lock.withLock {
            return buffer.lastOrNull()
        }
    }

    /**
     * Отримати перший (найстаріший) запис
     *
     * @return Перший SensorReading або null якщо буфер порожній
     */
    fun getFirst(): SensorReading? {
        lock.withLock {
            return buffer.firstOrNull()
        }
    }

    /**
     * Поточний розмір буфера
     *
     * @return Кількість записів в буфері
     */
    fun size(): Int {
        lock.withLock {
            return buffer.size
        }
    }

    /**
     * Чи повний буфер
     *
     * @return true якщо size == capacity
     */
    fun isFull(): Boolean {
        lock.withLock {
            return buffer.size >= capacity
        }
    }

    /**
     * Чи порожній буфер
     *
     * @return true якщо size == 0
     */
    fun isEmpty(): Boolean {
        lock.withLock {
            return buffer.isEmpty()
        }
    }

    /**
     * Очистити буфер
     */
    fun clear() {
        lock.withLock {
            buffer.clear()

            if (AppConfig.DEBUG_LOGGING) {
                println("🗑️ Buffer cleared")
            }
        }
    }

    /**
     * Отримати статистику буфера
     */
    fun getStats(): BufferStats {
        lock.withLock {
            return BufferStats(
                capacity = capacity,
                currentSize = buffer.size,
                isFull = buffer.size >= capacity,
                isEmpty = buffer.isEmpty(),
                oldestTimestamp = buffer.firstOrNull()?.timestamp,
                newestTimestamp = buffer.lastOrNull()?.timestamp,
                timeSpanMs = if (buffer.size >= 2) {
                    buffer.last().timestamp - buffer.first().timestamp
                } else {
                    0L
                }
            )
        }
    }

    data class BufferStats(
        val capacity: Int,
        val currentSize: Int,
        val isFull: Boolean,
        val isEmpty: Boolean,
        val oldestTimestamp: Long?,
        val newestTimestamp: Long?,
        val timeSpanMs: Long
    ) {
        /**
         * Час в секундах між найстарішим та найновішим записом
         */
        val timeSpanSeconds: Float
            get() = timeSpanMs / 1000f

        /**
         * Відсоток заповненості буфера
         */
        val fillPercentage: Float
            get() = (currentSize.toFloat() / capacity) * 100f
    }

    /**
     * Експортувати буфер в JSON-подібну структуру
     * Корисно для debugging
     */
    fun toDebugString(): String {
        lock.withLock {
            val stats = getStats()
            return buildString {
                appendLine("═══ RING BUFFER DEBUG ═══")
                appendLine("Capacity: ${stats.capacity}")
                appendLine("Current size: ${stats.currentSize}")
                appendLine("Fill: ${String.format("%.1f", stats.fillPercentage)}%")
                appendLine("Time span: ${String.format("%.1f", stats.timeSpanSeconds)}s")
                appendLine("Oldest: ${stats.oldestTimestamp}")
                appendLine("Newest: ${stats.newestTimestamp}")
                appendLine("═══════════════════════")
            }
        }
    }

    /**
     * Перевірити чи готовий буфер для ML inference
     *
     * @param minRecords Мінімальна кількість записів (за замовчуванням ML_WINDOW_SIZE)
     * @return true якщо достатньо даних
     */
    fun isReadyForInference(minRecords: Int = AppConfig.ML_WINDOW_SIZE): Boolean {
        lock.withLock {
            return buffer.size >= minRecords
        }
    }

    /**
     * Отримати вікно для ML inference
     *
     * Завжди повертає останні ML_WINDOW_SIZE записів
     * або null якщо недостатньо даних
     *
     * @return List для ML або null
     */
    fun getInferenceWindow(): List<SensorReading>? {
        return if (isReadyForInference()) {
            getWindow(AppConfig.ML_WINDOW_SIZE)
        } else {
            null
        }
    }

    /**
     * Отримати статистики для вікна даних
     * Корисно для feature extraction
     */
    fun getWindowStatistics(window: List<SensorReading>): WindowStatistics {
        if (window.isEmpty()) {
            return WindowStatistics()
        }

        // Accelerometer statistics
        val accelMagnitudes = window.map { it.accelMagnitude }

        return WindowStatistics(
            count = window.size,
            timeSpanMs = window.last().timestamp - window.first().timestamp,

            // Accelerometer
            accelMean = accelMagnitudes.average().toFloat(),
            accelStd = calculateStd(accelMagnitudes),
            accelMin = accelMagnitudes.minOrNull() ?: 0f,
            accelMax = accelMagnitudes.maxOrNull() ?: 0f,

            // Activity distribution
            activityCounts = window.groupingBy { it.activityType }.eachCount()
        )
    }

    /**
     * Розрахувати стандартне відхилення
     */
    private fun calculateStd(values: List<Float>): Float {
        if (values.isEmpty()) return 0f

        val mean = values.average()
        val variance = values.map { (it - mean) * (it - mean) }.average()
        return kotlin.math.sqrt(variance).toFloat()
    }

    data class WindowStatistics(
        val count: Int = 0,
        val timeSpanMs: Long = 0,

        val accelMean: Float = 0f,
        val accelStd: Float = 0f,
        val accelMin: Float = 0f,
        val accelMax: Float = 0f,

        val activityCounts: Map<String, Int> = emptyMap()
    ) {
        val accelRange: Float
            get() = accelMax - accelMin

        val dominantActivity: String?
            get() = activityCounts.maxByOrNull { it.value }?.key
    }
}