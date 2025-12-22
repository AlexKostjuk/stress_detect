package com.stress_detekt.testing

import android.os.Handler
import android.os.Looper
import com.stress_detekt.config.AppConfig
import com.stress_detekt.services.MultiSensorService
import com.stress_detekt.utils.SensorRingBuffer
import java.text.SimpleDateFormat
import java.util.*

/**
 * AutoTestRunner - Автоматичний запуск тестів з виводом в Terminal
 * 
 * Використання:
 * ```
 * val testRunner = AutoTestRunner(sensorService) { output ->
 *     terminalViewModel.addOutput(output)
 * }
 * testRunner.runAllTests()
 * ```
 */
class AutoTestRunner(
    private val sensorService: MultiSensorService,
    private val outputCallback: (String) -> Unit
) {
    
    private val handler = Handler(Looper.getMainLooper())
    private val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    
    /**
     * Вивести в термінал з timestamp
     */
    private fun println(text: String) {
        val timestamp = dateFormat.format(Date())
        outputCallback("[$timestamp] $text")
    }
    
    /**
     * Вивести без timestamp
     */
    private fun print(text: String) {
        outputCallback(text)
    }
    
    /**
     * Розділювач
     */
    private fun separator(char: String = "═", length: Int = 50) {
        print(char.repeat(length))
    }
    
    /**
     * Заголовок
     */
    private fun header(text: String) {
        separator()
        println("  $text")
        separator()
    }
    
    // ═══════════════════════════════════════════════════════════════════════
    // ОСНОВНІ ТЕСТИ
    // ═══════════════════════════════════════════════════════════════════════
    
    /**
     * Запустити всі тести послідовно
     */
    fun runAllTests() {
        header("🧪 AUTO TEST SUITE")
        println("Starting automated tests...")
        println("")
        
        // Тест 1: Конфігурація (одразу)
        test1_Configuration()
        
        // Тест 2: Статус датчиків (одразу)
        handler.postDelayed({ test2_SensorStatus() }, 1000)
        
        // Тест 3: Буфер через 5 сек
        handler.postDelayed({ test3_BufferAfter5Sec() }, 5000)
        
        // Тест 4: Буфер через 10 сек
        handler.postDelayed({ test4_BufferAfter10Sec() }, 10000)
        
        // Тест 5: ML Readiness через 15 сек
        handler.postDelayed({ test5_MLReadiness() }, 15000)
        
        // Тест 6: Window Statistics через 20 сек
        handler.postDelayed({ test6_WindowStatistics() }, 20000)
        
        // Тест 7: Collection Stats через 25 сек
        handler.postDelayed({ test7_CollectionStats() }, 25000)
        
        // Тест 8: Buffer Full через 35 сек
        handler.postDelayed({ test8_BufferFull() }, 35000)
        
        // Фінальний звіт через 40 сек
        handler.postDelayed({ finalReport() }, 40000)
    }
    
    /**
     * Тест 1: Перевірка конфігурації
     */
    private fun test1_Configuration() {
        header("TEST 1: Configuration Check")
        
        try {
            // Валідація
            val error = AppConfig.validateConfig()
            if (error != null) {
                println("⚠️  Configuration warning:")
                println("   $error")
            } else {
                println("✅ Configuration valid")
            }
            
            println("")
            println("📊 Key Settings:")
            println("   Aggregation interval: ${AppConfig.AGGREGATION_INTERVAL_MS}ms")
            println("   Buffer size: ${AppConfig.BUFFER_SIZE_RECORDS} records")
            println("   ML window size: ${AppConfig.ML_WINDOW_SIZE} records")
            println("   Min inference interval: ${AppConfig.MIN_INFERENCE_INTERVAL_MS}ms")
            
            println("")
            println("💾 Expected Data:")
            println("   Records/day: ${AppConfig.RECORDS_PER_DAY}")
            println("   Size/day: ${AppConfig.ESTIMATED_DB_SIZE_PER_DAY_MB} MB")
            println("   Size/month: ${AppConfig.ESTIMATED_DB_SIZE_PER_MONTH_MB} MB")
            
            println("")
            println("✅ TEST 1 PASSED")
            
        } catch (e: Exception) {
            println("❌ TEST 1 FAILED: ${e.message}")
        }
        
        println("")
    }
    
    /**
     * Тест 2: Статус датчиків
     */
    private fun test2_SensorStatus() {
        header("TEST 2: Sensor Status")
        
        try {
            val stats = sensorService.getCollectionStats()
            
            println("📡 Monitoring Status:")
            println("   Is monitoring: ${if (stats.isMonitoring) "✅ YES" else "❌ NO"}")
            println("   Enabled sensors: ${stats.enabledSensorsCount}")
            println("   Current activity: ${stats.currentActivity}")
            
            if (stats.isMonitoring) {
                println("")
                println("📊 Collection Progress:")
                println("   Data points: ${stats.dataPointsCollected}")
                val minutes = stats.runningTimeMs / 60000
                val seconds = (stats.runningTimeMs % 60000) / 1000
                println("   Running time: ${minutes}m ${seconds}s")
                
                println("")
                println("✅ TEST 2 PASSED")
            } else {
                println("")
                println("⚠️  Monitoring not started!")
                println("   Please start monitoring from Dashboard")
            }
            
        } catch (e: Exception) {
            println("❌ TEST 2 FAILED: ${e.message}")
        }
        
        println("")
    }
    
    /**
     * Тест 3: Стан буфера через 5 секунд
     */
    private fun test3_BufferAfter5Sec() {
        header("TEST 3: Buffer Check (5 sec)")
        
        try {
            val buffer = sensorService.getBuffer()
            val stats = buffer.getStats()
            
            println("🔄 Buffer Status:")
            println("   Size: ${stats.currentSize}/${stats.capacity}")
            println("   Fill: ${String.format("%.1f", stats.fillPercentage)}%")
            println("   Time span: ${String.format("%.1f", stats.timeSpanSeconds)}s")
            
            // Очікуємо ~2-3 записи через 5 сек (при інтервалі 2 сек)
            val expectedMin = 2
            val expectedMax = 3
            
            println("")
            if (stats.currentSize in expectedMin..expectedMax) {
                println("✅ TEST 3 PASSED")
                println("   Buffer size matches expectation (${expectedMin}-${expectedMax})")
            } else if (stats.currentSize < expectedMin) {
                println("⚠️  TEST 3 WARNING")
                println("   Buffer has less data than expected")
                println("   Expected: ${expectedMin}-${expectedMax}, Got: ${stats.currentSize}")
            } else {
                println("✅ TEST 3 PASSED")
                println("   Buffer filling correctly")
            }
            
        } catch (e: Exception) {
            println("❌ TEST 3 FAILED: ${e.message}")
        }
        
        println("")
    }
    
    /**
     * Тест 4: Стан буфера через 10 секунд
     */
    private fun test4_BufferAfter10Sec() {
        header("TEST 4: Buffer Check (10 sec)")
        
        try {
            val buffer = sensorService.getBuffer()
            val stats = buffer.getStats()
            
            println("🔄 Buffer Status:")
            println("   Size: ${stats.currentSize}/${stats.capacity}")
            println("   Fill: ${String.format("%.1f", stats.fillPercentage)}%")
            println("   Time span: ${String.format("%.1f", stats.timeSpanSeconds)}s")
            
            // Очікуємо ~5 записів через 10 сек
            val expected = 5
            
            println("")
            if (stats.currentSize >= expected) {
                println("✅ TEST 4 PASSED")
                println("   Buffer has sufficient data (>= $expected)")
            } else {
                println("⚠️  TEST 4 WARNING")
                println("   Buffer has ${stats.currentSize} records, expected >= $expected")
            }
            
            // Показати останній запис
            val last = buffer.getLast()
            if (last != null) {
                println("")
                println("📊 Latest Reading:")
                println("   Accel magnitude: ${String.format("%.2f", last.accelMagnitude)} m/s²")
                println("   Activity: ${last.activityType}")
                if (last.heartRate != null) {
                    println("   Heart rate: ${String.format("%.0f", last.heartRate)} BPM")
                }
            }
            
        } catch (e: Exception) {
            println("❌ TEST 4 FAILED: ${e.message}")
        }
        
        println("")
    }
    
    /**
     * Тест 5: Готовність до ML inference
     */
    private fun test5_MLReadiness() {
        header("TEST 5: ML Inference Readiness")
        
        try {
            val isReady = sensorService.isBufferReadyForInference()
            
            println("🤖 ML Inference Status:")
            println("   Ready: ${if (isReady) "✅ YES" else "❌ NO"}")
            println("   Required records: ${AppConfig.ML_WINDOW_SIZE}")
            println("   Available records: ${sensorService.getBuffer().size()}")
            
            if (isReady) {
                val window = sensorService.getInferenceWindow()
                
                println("")
                println("📊 Inference Window:")
                println("   Size: ${window?.size ?: 0} records")
                
                if (window != null && window.isNotEmpty()) {
                    val timeSpan = window.last().timestamp - window.first().timestamp
                    println("   Time span: ${timeSpan}ms (${timeSpan / 1000}s)")
                    
                    // Показати activity distribution
                    val activities = window.groupingBy { it.activityType }.eachCount()
                    println("")
                    println("   Activity distribution:")
                    activities.forEach { (activity, count) ->
                        val pct = (count.toFloat() / window.size) * 100
                        println("     $activity: $count (${String.format("%.0f", pct)}%)")
                    }
                }
                
                println("")
                println("✅ TEST 5 PASSED")
                println("   Buffer ready for ML inference")
                
            } else {
                println("")
                println("⏳ TEST 5 PENDING")
                println("   Need more data for ML inference")
                println("   Current: ${sensorService.getBuffer().size()} / ${AppConfig.ML_WINDOW_SIZE}")
            }
            
        } catch (e: Exception) {
            println("❌ TEST 5 FAILED: ${e.message}")
        }
        
        println("")
    }
    
    /**
     * Тест 6: Статистики вікна
     */
    private fun test6_WindowStatistics() {
        header("TEST 6: Window Statistics")
        
        try {
            val buffer = sensorService.getBuffer()
            val window = buffer.getInferenceWindow()
            
            if (window != null) {
                val stats = buffer.getWindowStatistics(window)
                
                println("📊 Window Statistics:")
                println("   Records: ${stats.count}")
                println("   Time span: ${stats.timeSpanMs}ms")
                
                println("")
                println("📈 Accelerometer:")
                println("   Mean: ${String.format("%.2f", stats.accelMean)} m/s²")
                println("   Std Dev: ${String.format("%.2f", stats.accelStd)} m/s²")
                println("   Min: ${String.format("%.2f", stats.accelMin)} m/s²")
                println("   Max: ${String.format("%.2f", stats.accelMax)} m/s²")
                println("   Range: ${String.format("%.2f", stats.accelRange)} m/s²")
                
                println("")
                println("🎯 Activity Analysis:")
                println("   Dominant: ${stats.dominantActivity}")
                stats.activityCounts.forEach { (activity, count) ->
                    val pct = (count.toFloat() / stats.count) * 100
                    println("   $activity: $count (${String.format("%.0f", pct)}%)")
                }
                
                println("")
                
                // Оцінка якості даних
                if (stats.accelStd < 0.5f) {
                    println("✅ Data quality: STABLE (low variance)")
                } else if (stats.accelStd < 2.0f) {
                    println("✅ Data quality: MODERATE (normal variance)")
                } else {
                    println("⚠️  Data quality: VARIABLE (high variance)")
                }
                
                println("")
                println("✅ TEST 6 PASSED")
                
            } else {
                println("⏳ TEST 6 PENDING")
                println("   Not enough data for statistics")
            }
            
        } catch (e: Exception) {
            println("❌ TEST 6 FAILED: ${e.message}")
        }
        
        println("")
    }
    
    /**
     * Тест 7: Статистика збору даних
     */
    private fun test7_CollectionStats() {
        header("TEST 7: Collection Statistics")
        
        try {
            val stats = sensorService.getCollectionStats()
            
            println("📊 Collection Summary:")
            println("   Total records: ${stats.dataPointsCollected}")
            
            val minutes = stats.runningTimeMs / 60000
            val seconds = (stats.runningTimeMs % 60000) / 1000
            println("   Running time: ${minutes}m ${seconds}s")
            
            println("   Enabled sensors: ${stats.enabledSensorsCount}")
            println("   Current activity: ${stats.currentActivity}")
            
            // Обчислити швидкість збору
            if (stats.runningTimeMs > 0) {
                val recordsPerMin = (stats.dataPointsCollected.toFloat() / stats.runningTimeMs) * 60000
                println("   Collection rate: ${String.format("%.1f", recordsPerMin)} records/min")
                
                // Очікувана швидкість: 60000ms / 2000ms = 30 records/min
                val expectedRate = 30f
                val diff = kotlin.math.abs(recordsPerMin - expectedRate)
                
                println("")
                if (diff < 3f) {
                    println("✅ Collection rate is accurate")
                    println("   Expected: ${expectedRate.toInt()} rec/min")
                    println("   Actual: ${String.format("%.1f", recordsPerMin)} rec/min")
                } else {
                    println("⚠️  Collection rate differs from expected")
                    println("   Expected: ${expectedRate.toInt()} rec/min")
                    println("   Actual: ${String.format("%.1f", recordsPerMin)} rec/min")
                    println("   Difference: ${String.format("%.1f", diff)} rec/min")
                }
            }
            
            println("")
            println("✅ TEST 7 PASSED")
            
        } catch (e: Exception) {
            println("❌ TEST 7 FAILED: ${e.message}")
        }
        
        println("")
    }
    
    /**
     * Тест 8: Перевірка повного буфера
     */
    private fun test8_BufferFull() {
        header("TEST 8: Buffer Full Check")
        
        try {
            val buffer = sensorService.getBuffer()
            val stats = buffer.getStats()
            
            println("🔄 Buffer Status:")
            println("   Size: ${stats.currentSize}/${stats.capacity}")
            println("   Fill: ${String.format("%.1f", stats.fillPercentage)}%")
            println("   Is full: ${if (stats.isFull) "✅ YES" else "❌ NO"}")
            println("   Time span: ${String.format("%.1f", stats.timeSpanSeconds)}s")
            
            println("")
            
            // Через 35 сек при інтервалі 2 сек буфер повинен бути повний
            if (stats.isFull || stats.currentSize >= AppConfig.BUFFER_SIZE_RECORDS - 1) {
                println("✅ TEST 8 PASSED")
                println("   Buffer reached full capacity")
                
                // Перевірити що старі записи видаляються
                println("")
                println("🔄 Buffer Rotation:")
                val oldestAge = if (stats.newestTimestamp != null && stats.oldestTimestamp != null) {
                    (stats.newestTimestamp - stats.oldestTimestamp) / 1000f
                } else 0f
                println("   Oldest record age: ${String.format("%.1f", oldestAge)}s")
                
                val expectedAge = AppConfig.BUFFER_SIZE_SECONDS.toFloat()
                if (kotlin.math.abs(oldestAge - expectedAge) < 5f) {
                    println("   ✅ Rotation working correctly")
                } else {
                    println("   ⚠️  Age differs from expected ${expectedAge}s")
                }
                
            } else {
                println("⚠️  TEST 8 WARNING")
                println("   Buffer not full yet (${stats.currentSize}/${stats.capacity})")
                println("   This might be expected if monitoring just started")
            }
            
        } catch (e: Exception) {
            println("❌ TEST 8 FAILED: ${e.message}")
        }
        
        println("")
    }
    
    /**
     * Фінальний звіт
     */
    private fun finalReport() {
        separator("═", 60)
        println("  🏁 FINAL TEST REPORT")
        separator("═", 60)
        
        try {
            val collectionStats = sensorService.getCollectionStats()
            val bufferStats = sensorService.getBuffer().getStats()
            
            println("")
            println("📊 Overall Status:")
            println("   Monitoring: ${if (collectionStats.isMonitoring) "✅ ACTIVE" else "❌ INACTIVE"}")
            println("   Total records collected: ${collectionStats.dataPointsCollected}")
            println("   Buffer status: ${bufferStats.currentSize}/${bufferStats.capacity} " +
                    "(${String.format("%.0f", bufferStats.fillPercentage)}%)")
            
            val minutes = collectionStats.runningTimeMs / 60000
            val seconds = (collectionStats.runningTimeMs % 60000) / 1000
            println("   Total running time: ${minutes}m ${seconds}s")
            
            println("")
            println("🎯 System Health:")
            
            // Перевірка 1: Monitoring active
            val check1 = collectionStats.isMonitoring
            println("   ${if (check1) "✅" else "❌"} Monitoring active")
            
            // Перевірка 2: Data collection
            val check2 = collectionStats.dataPointsCollected > 0
            println("   ${if (check2) "✅" else "❌"} Data collection working")
            
            // Перевірка 3: Buffer operational
            val check3 = bufferStats.currentSize > 0
            println("   ${if (check3) "✅" else "❌"} Buffer operational")
            
            // Перевірка 4: ML ready
            val check4 = bufferStats.currentSize >= AppConfig.ML_WINDOW_SIZE
            println("   ${if (check4) "✅" else "⏳"} ML inference ready")
            
            // Перевірка 5: Buffer rotation
            val check5 = bufferStats.isFull || collectionStats.runningTimeMs < 30000
            println("   ${if (check5) "✅" else "⚠️ "} Buffer rotation")
            
            println("")
            
            val allPassed = check1 && check2 && check3
            if (allPassed) {
                println("🎉 ALL CRITICAL TESTS PASSED!")
                println("   System is operational and ready for ML")
            } else {
                println("⚠️  SOME TESTS FAILED")
                println("   Please review the test results above")
            }
            
            println("")
            println("💡 Next Steps:")
            if (!check4) {
                println("   • Wait for buffer to fill (need ${AppConfig.ML_WINDOW_SIZE - bufferStats.currentSize} more records)")
            }
            println("   • Start testing ML inference")
            println("   • Enable camera/microphone sensors")
            println("   • Connect Galaxy Watch 5")
            
        } catch (e: Exception) {
            println("❌ FINAL REPORT FAILED: ${e.message}")
        }
        
        println("")
        separator("═", 60)
        println("  Test suite completed at ${dateFormat.format(Date())}")
        separator("═", 60)
        println("")
    }
    
    // ═══════════════════════════════════════════════════════════════════════
    // ІНДИВІДУАЛЬНІ ТЕСТИ (для запуску вручну)
    // ═══════════════════════════════════════════════════════════════════════
    
    /**
     * Запустити тільки тест конфігурації
     */
    fun runConfigTest() {
        test1_Configuration()
    }
    
    /**
     * Запустити тільки тест статусу
     */
    fun runStatusTest() {
        test2_SensorStatus()
    }
    
    /**
     * Запустити тільки тест буфера
     */
    fun runBufferTest() {
        header("BUFFER TEST")
        val buffer = sensorService.getBuffer()
        println(buffer.toDebugString())
        println("")
    }
    
    /**
     * Запустити швидкий тест (без затримок)
     */
    fun runQuickTest() {
        header("🚀 QUICK TEST")
        test1_Configuration()
        test2_SensorStatus()
        
        val buffer = sensorService.getBuffer()
        if (buffer.size() > 0) {
            println("")
            println(buffer.toDebugString())
        }
        println("")
    }
}
