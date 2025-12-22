package com.stress_detekt.config

/**
 * Централізована конфігурація проєкту Stress Detekt
 * 
 * Всі налаштування інтервалів, розмірів буферів, timeouts
 * знаходяться в одному місці для легкого налаштування.
 * 
 * ВАЖЛИВО: Після зміни будь-якого значення потрібно:
 * 1. Rebuild проєкту
 * 2. Перезапустити додаток
 * 3. Протестувати зміни
 */
object AppConfig {
    
    // ═══════════════════════════════════════════════════════════════════════
    // SENSOR SAMPLING CONFIGURATION
    // ═══════════════════════════════════════════════════════════════════════
    
    /**
     * Частота зчитування з датчиків
     * 
     * SENSOR_DELAY_NORMAL = ~200ms між зчитуваннями (5 Hz)
     * SENSOR_DELAY_UI = ~60ms (16 Hz) - для UI
     * SENSOR_DELAY_GAME = ~20ms (50 Hz) - для ігор
     * SENSOR_DELAY_FASTEST = ~0ms - максимальна швидкість
     * 
     * Рекомендація: SENSOR_DELAY_NORMAL для балансу між
     * точністю та споживанням батареї
     */
    const val SENSOR_DELAY = android.hardware.SensorManager.SENSOR_DELAY_NORMAL
    
    /**
     * Інтервал агрегації та збереження в БД
     * 
     * Варіанти:
     * - 1000ms (1 сек) = 86,400 записів/день, ~10.8 MB/день
     * - 2000ms (2 сек) = 43,200 записів/день, ~6.5 MB/день ⭐ РЕКОМЕНДОВАНО
     * - 5000ms (5 сек) = 17,280 записів/день, ~2.6 MB/день
     * 
     * При SENSOR_DELAY_NORMAL (~200ms) маємо ~5 зчитувань/сек
     * За 2 секунди накопичується ~10 зчитувань для усереднення
     */
    const val AGGREGATION_INTERVAL_MS = 2000L  // 2 секунди
    
    /**
     * Розмір буфера для ML inference (в секундах)
     * 
     * Буфер тримає останні N секунд даних в RAM
     * для швидкого доступу при ML inference
     * 
     * 30 секунд = достатньо для аналізу коротких трендів
     */
    const val BUFFER_SIZE_SECONDS = 30
    
    /**
     * Розмір буфера в записах
     * 
     * Розраховується автоматично:
     * 30 секунд / 2 секунди на запис = 15 записів
     */
    val BUFFER_SIZE_RECORDS: Int
        get() = (BUFFER_SIZE_SECONDS * 1000 / AGGREGATION_INTERVAL_MS).toInt()
    
    /**
     * Розмір вікна для ML inference (кількість записів)
     * 
     * Скільки записів використовувати для одного prediction
     * 5 записів × 2 сек = 10 секунд даних для аналізу
     * 
     * Для різних моделей:
     * - SVM/Random Forest: можна 1-5 записів
     * - LSTM/CNN: краще 5-10 записів (послідовність)
     */
    const val ML_WINDOW_SIZE = 5  // 5 записів
    
    /**
     * Мінімальний інтервал між ML inference
     * 
     * Не запускати inference частіше ніж кожні X мілісекунд
     * навіть якщо є достатньо даних
     * 
     * 5000ms = мінімум 5 секунд між predictions
     */
    const val MIN_INFERENCE_INTERVAL_MS = 5000L  // 5 секунд
    
    // ═══════════════════════════════════════════════════════════════════════
    // CAMERA CONFIGURATION (Facial Emotion Recognition)
    // ═══════════════════════════════════════════════════════════════════════
    
    /**
     * Як часто обробляти кадри з камери
     * 
     * Обробка кожного кадру дуже ресурсоємна:
     * - Face detection
     * - Face cropping
     * - FER model inference
     * 
     * 5000ms = кожні 5 секунд (рекомендовано)
     * 2000ms = кожні 2 секунди (якщо потужний телефон)
     */
    const val CAMERA_PROCESS_INTERVAL_MS = 5000L  // 5 секунд
    
    /**
     * Розмір входу для FER моделі
     * 
     * Більшість FER моделей очікують 48x48 grayscale
     * або 224x224 RGB (залежить від моделі)
     */
    const val FER_INPUT_SIZE = 48  // 48x48 pixels
    
    /**
     * Мінімальний розмір обличчя для detection
     * 
     * Пікселі. Менші обличчя ігноруються (занадто далеко від камери)
     */
    const val FER_MIN_FACE_SIZE = 100  // 100x100 pixels
    
    /**
     * Confidence threshold для face detection
     * 
     * 0.0-1.0. Вищий = більш впевнені detection, але можуть пропускати
     */
    const val FER_CONFIDENCE_THRESHOLD = 0.7f
    
    // ═══════════════════════════════════════════════════════════════════════
    // MICROPHONE CONFIGURATION (Voice Stress Analysis)
    // ═══════════════════════════════════════════════════════════════════════
    
    /**
     * Sample rate для аудіо запису
     * 
     * 16000 Hz = стандарт для speech analysis
     * 44100 Hz = CD quality (надмірно для нас)
     * 8000 Hz = телефонна якість (замало)
     */
    const val AUDIO_SAMPLE_RATE = 16000  // 16 kHz
    
    /**
     * Тривалість audio chunk для аналізу (секунди)
     * 
     * 5 секунд = достатньо для аналізу емоцій в голосі
     * 3 секунди = мінімум для стабільної оцінки
     * 10 секунд = можна, але довго обробляти
     */
    const val AUDIO_CHUNK_DURATION_SEC = 5  // 5 секунд
    
    /**
     * Розмір аудіо буфера
     * 
     * Кількість samples в одному chunk
     * 16000 Hz × 5 сек = 80,000 samples
     */
    val AUDIO_CHUNK_SIZE: Int
        get() = AUDIO_SAMPLE_RATE * AUDIO_CHUNK_DURATION_SEC
    
    /**
     * Мінімальна енергія сигналу (dB) для аналізу
     * 
     * Нижче цього рівня вважаємо що людина мовчить
     * -40 dB = тиша
     * -20 dB = шепіт
     * -10 dB = нормальна розмова
     */
    const val AUDIO_MIN_ENERGY_DB = -30f
    
    // ═══════════════════════════════════════════════════════════════════════
    // BLUETOOTH WATCH CONFIGURATION (Samsung Galaxy Watch 5)
    // ═══════════════════════════════════════════════════════════════════════
    
    /**
     * Timeout для scan Bluetooth пристроїв
     * 
     * Як довго сканувати перед тим як зупинитися
     * 10 секунд = зазвичай достатньо
     */
    const val BT_SCAN_TIMEOUT_MS = 10000L  // 10 секунд
    
    /**
     * Timeout для reconnect при втраті з'єднання
     * 
     * Як довго чекати перед спробою reconnect
     * 5 секунд = не надто швидко, не надто повільно
     */
    const val BT_RECONNECT_TIMEOUT_MS = 5000L  // 5 секунд
    
    /**
     * Максимальна кількість спроб reconnect
     * 
     * Після цього припиняємо намагатися
     */
    const val BT_MAX_RECONNECT_ATTEMPTS = 3
    
    /**
     * Keep-alive interval для Bluetooth з'єднання
     * 
     * Періодичні ping для підтримки з'єднання живим
     * 30 секунд = стандарт
     */
    const val BT_KEEPALIVE_INTERVAL_MS = 30000L  // 30 секунд
    
    // ═══════════════════════════════════════════════════════════════════════
    // DATABASE CONFIGURATION
    // ═══════════════════════════════════════════════════════════════════════
    
    /**
     * Retention period для локальних даних (дні)
     * 
     * FREE users: 30 днів
     * PREMIUM users: 30 днів локально + sync на сервер
     * 
     * Після 30 днів старі дані автоматично видаляються
     */
    const val DATA_RETENTION_DAYS = 30
    
    /**
     * Batch size для sync на сервер
     * 
     * Скільки записів відправляти за один раз
     * 1000 = баланс між швидкістю та розміром запиту
     */
    const val SYNC_BATCH_SIZE = 1000
    
    /**
     * Як часто запускати cleanup старих даних
     * 
     * 1 = щоденно (рекомендовано)
     * 7 = щотижня
     */
    const val CLEANUP_INTERVAL_DAYS = 1
    
    // ═══════════════════════════════════════════════════════════════════════
    // BLUETOOTH P2P SYNC CONFIGURATION (Phone ↔ Laptop)
    // ═══════════════════════════════════════════════════════════════════════
    
    /**
     * Як часто синхронізувати predictions між пристроями
     * 
     * 5 секунд = real-time sync (рекомендовано)
     */
    const val P2P_SYNC_INTERVAL_MS = 5000L  // 5 секунд
    
    /**
     * Timeout для P2P connection
     */
    const val P2P_CONNECTION_TIMEOUT_MS = 10000L  // 10 секунд
    
    // ═══════════════════════════════════════════════════════════════════════
    // CALCULATED VALUES (НЕ ЗМІНЮВАТИ! Розраховуються автоматично)
    // ═══════════════════════════════════════════════════════════════════════
    
    /**
     * Скільки samples накопичується за один aggregation interval
     * 
     * При SENSOR_DELAY_NORMAL (~200ms) та AGGREGATION_INTERVAL_MS = 2000ms:
     * 2000ms / 200ms = 10 samples
     */
    val SAMPLES_PER_AGGREGATION: Int
        get() = (AGGREGATION_INTERVAL_MS / 200).toInt()
    
    /**
     * Очікувана кількість записів за день
     * 
     * При AGGREGATION_INTERVAL_MS = 2000ms:
     * 24 години × 60 хв × 60 сек / 2 сек = 43,200 записів
     */
    val RECORDS_PER_DAY: Long
        get() = (24 * 60 * 60 * 1000L) / AGGREGATION_INTERVAL_MS
    
    /**
     * Очікуваний розмір одного запису (bytes)
     * 
     * Приблизно:
     * - userId: 8 bytes
     * - timestamp: 8 bytes
     * - ~30 Float fields × 4 bytes = 120 bytes
     * - String fields: ~20 bytes
     * Total: ~150 bytes per record
     */
    const val BYTES_PER_RECORD = 150
    
    /**
     * Очікуваний розмір БД за день (MB)
     * 
     * При AGGREGATION_INTERVAL_MS = 2000ms:
     * 43,200 records × 150 bytes ≈ 6.5 MB/день
     */
    val ESTIMATED_DB_SIZE_PER_DAY_MB: Long
        get() = (RECORDS_PER_DAY * BYTES_PER_RECORD) / (1024 * 1024)
    
    /**
     * Очікуваний розмір БД за 30 днів (MB)
     */
    val ESTIMATED_DB_SIZE_PER_MONTH_MB: Long
        get() = ESTIMATED_DB_SIZE_PER_DAY_MB * DATA_RETENTION_DAYS
    
    // ═══════════════════════════════════════════════════════════════════════
    // LOGGING & DEBUGGING
    // ═══════════════════════════════════════════════════════════════════════
    
    /**
     * Увімкнути детальне логування
     * 
     * true = багато println() в Logcat
     * false = тільки важливі повідомлення
     * 
     * ВАЖЛИВО: Вимкнути в production!
     */
    const val DEBUG_LOGGING = true
    
    /**
     * Логувати кожні N записів
     * 
     * Щоб не забивати Logcat, логуємо не кожен запис
     * а кожен 10-й або 100-й
     */
    const val LOG_EVERY_N_RECORDS = 10
    
    // ═══════════════════════════════════════════════════════════════════════
    // HELPER FUNCTIONS
    // ═══════════════════════════════════════════════════════════════════════
    
    /**
     * Вивести поточну конфігурацію в Logcat
     * 
     * Корисно для debugging та перевірки налаштувань
     */
    fun printConfig() {
        if (!DEBUG_LOGGING) return
        
        println("════════════════════════════════════════════════════")
        println("         STRESS DETEKT CONFIGURATION")
        println("════════════════════════════════════════════════════")
        println("📊 SENSOR SAMPLING:")
        println("   Aggregation interval: ${AGGREGATION_INTERVAL_MS}ms")
        println("   Samples per aggregation: $SAMPLES_PER_AGGREGATION")
        println("   Records per day: $RECORDS_PER_DAY")
        println("")
        println("💾 DATABASE:")
        println("   Estimated size per day: ${ESTIMATED_DB_SIZE_PER_DAY_MB} MB")
        println("   Estimated size per month: ${ESTIMATED_DB_SIZE_PER_MONTH_MB} MB")
        println("   Retention: $DATA_RETENTION_DAYS days")
        println("")
        println("🧠 ML INFERENCE:")
        println("   Buffer size: $BUFFER_SIZE_RECORDS records ($BUFFER_SIZE_SECONDS sec)")
        println("   ML window size: $ML_WINDOW_SIZE records")
        println("   Min inference interval: ${MIN_INFERENCE_INTERVAL_MS}ms")
        println("")
        println("📷 CAMERA:")
        println("   Process interval: ${CAMERA_PROCESS_INTERVAL_MS}ms")
        println("   Input size: ${FER_INPUT_SIZE}x${FER_INPUT_SIZE}")
        println("")
        println("🎤 MICROPHONE:")
        println("   Sample rate: ${AUDIO_SAMPLE_RATE} Hz")
        println("   Chunk duration: ${AUDIO_CHUNK_DURATION_SEC} sec")
        println("   Chunk size: $AUDIO_CHUNK_SIZE samples")
        println("")
        println("⌚ BLUETOOTH:")
        println("   Scan timeout: ${BT_SCAN_TIMEOUT_MS}ms")
        println("   Reconnect timeout: ${BT_RECONNECT_TIMEOUT_MS}ms")
        println("   Max reconnect attempts: $BT_MAX_RECONNECT_ATTEMPTS")
        println("════════════════════════════════════════════════════")
    }
    
    /**
     * Валідація конфігурації
     * 
     * Перевіряє чи всі значення в допустимих межах
     * 
     * @return null якщо все ок, або текст помилки
     */
    fun validateConfig(): String? {
        // Перевірка aggregation interval
        if (AGGREGATION_INTERVAL_MS < 1000) {
            return "⚠️ AGGREGATION_INTERVAL_MS надто малий (< 1 sec). Рекомендується >= 1000ms"
        }
        if (AGGREGATION_INTERVAL_MS > 10000) {
            return "⚠️ AGGREGATION_INTERVAL_MS надто великий (> 10 sec). Рекомендується <= 5000ms"
        }
        
        // Перевірка buffer size
        if (BUFFER_SIZE_RECORDS < ML_WINDOW_SIZE) {
            return "❌ BUFFER_SIZE_RECORDS ($BUFFER_SIZE_RECORDS) має бути >= ML_WINDOW_SIZE ($ML_WINDOW_SIZE)"
        }
        
        // Перевірка ML window
        if (ML_WINDOW_SIZE < 1) {
            return "❌ ML_WINDOW_SIZE має бути >= 1"
        }
        if (ML_WINDOW_SIZE > 20) {
            return "⚠️ ML_WINDOW_SIZE надто великий (> 20). Буде повільний inference"
        }
        
        // Перевірка camera interval
        if (CAMERA_PROCESS_INTERVAL_MS < 1000) {
            return "⚠️ CAMERA_PROCESS_INTERVAL_MS надто малий (< 1 sec). Буде споживати багато батареї"
        }
        
        // Перевірка audio
        if (AUDIO_SAMPLE_RATE < 8000 || AUDIO_SAMPLE_RATE > 48000) {
            return "⚠️ AUDIO_SAMPLE_RATE має бути між 8000-48000 Hz"
        }
        
        // Все добре
        return null
    }
}
