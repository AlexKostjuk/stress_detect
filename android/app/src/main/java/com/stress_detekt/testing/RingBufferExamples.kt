package com.stress_detekt.examples

import com.stress_detekt.config.AppConfig
import com.stress_detekt.services.MultiSensorService
import com.stress_detekt.utils.SensorRingBuffer

/**
 * Приклади використання SensorRingBuffer
 * 
 * Цей файл показує як працювати з RingBuffer для ML inference
 */
class RingBufferExamples {
    
    /**
     * Приклад 1: Отримати останні 5 записів для ML
     */
    fun example1_GetInferenceWindow(sensorService: MultiSensorService) {
        // Перевірити чи готовий буфер
        if (sensorService.isBufferReadyForInference()) {
            // Отримати вікно для ML (останні 5 записів)
            val window = sensorService.getInferenceWindow()
            
            if (window != null) {
                println("✅ Got ${window.size} records for ML inference")
                
                // Використати для prediction
                window.forEach { reading ->
                    println("  Timestamp: ${reading.timestamp}")
                    println("  Accel magnitude: ${reading.accelMagnitude}")
                    println("  Activity: ${reading.activityType}")
                    println("  ---")
                }
            }
        } else {
            println("⏳ Buffer not ready yet (need ${AppConfig.ML_WINDOW_SIZE} records)")
        }
    }
    
    /**
     * Приклад 2: Отримати останні 10 секунд даних
     */
    fun example2_GetLastSeconds(sensorService: MultiSensorService) {
        val buffer = sensorService.getBuffer()
        val last10Sec = buffer.getLastSeconds(10)
        
        println("📊 Got ${last10Sec.size} records from last 10 seconds")
        
        // Обчислити середню magnitude
        val avgMagnitude = last10Sec.map { it.accelMagnitude }.average()
        println("   Average magnitude: ${String.format("%.2f", avgMagnitude)}")
    }
    
    /**
     * Приклад 3: Отримати статистику буфера
     */
    fun example3_GetBufferStats(sensorService: MultiSensorService) {
        val stats = sensorService.getBufferStats()
        
        println("═══ BUFFER STATISTICS ═══")
        println("Capacity: ${stats.capacity}")
        println("Current size: ${stats.currentSize}")
        println("Fill: ${String.format("%.1f", stats.fillPercentage)}%")
        println("Time span: ${String.format("%.1f", stats.timeSpanSeconds)}s")
        println("Full: ${stats.isFull}")
        println("Empty: ${stats.isEmpty}")
        println("═════════════════════════")
    }
    
    /**
     * Приклад 4: Отримати статистики вікна для feature extraction
     */
    fun example4_WindowStatistics(sensorService: MultiSensorService) {
        val buffer = sensorService.getBuffer()
        val window = buffer.getInferenceWindow()
        
        if (window != null) {
            val windowStats = buffer.getWindowStatistics(window)
            
            println("═══ WINDOW STATISTICS ═══")
            println("Count: ${windowStats.count}")
            println("Time span: ${windowStats.timeSpanMs}ms")
            println("")
            println("Accelerometer:")
            println("  Mean: ${String.format("%.2f", windowStats.accelMean)}")
            println("  Std: ${String.format("%.2f", windowStats.accelStd)}")
            println("  Min: ${String.format("%.2f", windowStats.accelMin)}")
            println("  Max: ${String.format("%.2f", windowStats.accelMax)}")
            println("  Range: ${String.format("%.2f", windowStats.accelRange)}")
            println("")
            println("Activity distribution:")
            windowStats.activityCounts.forEach { (activity, count) ->
                val percentage = (count.toFloat() / windowStats.count) * 100
                println("  $activity: $count (${String.format("%.0f", percentage)}%)")
            }
            println("  Dominant: ${windowStats.dominantActivity}")
            println("═════════════════════════")
        }
    }
    
    /**
     * Приклад 5: Отримати дані по timestamp range
     */
    fun example5_GetByTimestamp(sensorService: MultiSensorService) {
        val buffer = sensorService.getBuffer()
        val now = System.currentTimeMillis()
        val fiveSecondsAgo = now - 5000
        
        val records = buffer.getWindowByTimestamp(fiveSecondsAgo, now)
        
        println("📊 Got ${records.size} records from last 5 seconds (by timestamp)")
    }
    
    /**
     * Приклад 6: Перевірити чи готовий для ML з custom window size
     */
    fun example6_CustomWindowSize(sensorService: MultiSensorService) {
        val buffer = sensorService.getBuffer()
        
        // Перевірити чи є 10 записів
        if (buffer.isReadyForInference(minRecords = 10)) {
            val window = buffer.getWindow(10)
            println("✅ Got 10 records for custom ML model")
        } else {
            println("⏳ Need more data (${buffer.size()}/10)")
        }
    }
    
    /**
     * Приклад 7: Конвертувати в Map для feature extraction
     */
    fun example7_ConvertToMap(sensorService: MultiSensorService) {
        val window = sensorService.getInferenceWindow()
        
        window?.forEach { reading ->
            val map = reading.toMap()
            
            // Тепер можна використовувати як Map
            println("Timestamp: ${map["timestamp"]}")
            println("Accel X: ${map["accelX"]}")
            println("Activity: ${map["activityType"]}")
        }
    }
    
    /**
     * Приклад 8: Debug info
     */
    fun example8_DebugInfo(sensorService: MultiSensorService) {
        val buffer = sensorService.getBuffer()
        println(buffer.toDebugString())
    }
    
    /**
     * Приклад 9: Цикл ML inference
     * 
     * Цей приклад показує як виглядає типовий ML inference loop
     */
    fun example9_InferenceLoop(sensorService: MultiSensorService) {
        // Запускати кожні 5 секунд
        if (sensorService.isBufferReadyForInference()) {
            val window = sensorService.getInferenceWindow()
            
            if (window != null) {
                // 1. Feature extraction
                val buffer = sensorService.getBuffer()
                val stats = buffer.getWindowStatistics(window)
                
                // 2. Створити feature vector
                val features = floatArrayOf(
                    stats.accelMean,
                    stats.accelStd,
                    stats.accelMin,
                    stats.accelMax,
                    stats.accelRange
                    // ... інші features
                )
                
                // 3. ML inference (псевдокод)
                // val prediction = mlModel.predict(features)
                
                // 4. Використати prediction
                println("✅ Inference completed")
                println("   Features: ${features.joinToString(", ") { String.format("%.2f", it) }}")
                // println("   Prediction: $prediction")
            }
        }
    }
    
    /**
     * Приклад 10: Отримати всі дані з буфера
     */
    fun example10_GetAllData(sensorService: MultiSensorService) {
        val buffer = sensorService.getBuffer()
        val allData = buffer.getAll()
        
        println("📊 Total records in buffer: ${allData.size}")
        
        if (allData.isNotEmpty()) {
            val first = allData.first()
            val last = allData.last()
            val timeSpan = last.timestamp - first.timestamp
            
            println("   First record: ${first.timestamp}")
            println("   Last record: ${last.timestamp}")
            println("   Time span: ${timeSpan}ms (${timeSpan / 1000}s)")
        }
    }
}
