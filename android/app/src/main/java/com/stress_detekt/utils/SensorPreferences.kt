package com.stress_detekt.utils

import android.content.Context
import android.content.SharedPreferences

class SensorPreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        "sensor_preferences",
        Context.MODE_PRIVATE
    )

    companion object {
        // Датчики руху
        private const val KEY_ACCELEROMETER = "sensor_accelerometer"
        private const val KEY_GYROSCOPE = "sensor_gyroscope"
        private const val KEY_MAGNETOMETER = "sensor_magnetometer"
        private const val KEY_GRAVITY = "sensor_gravity"
        private const val KEY_ROTATION = "sensor_rotation"

        // Датчики середовища
        private const val KEY_LIGHT = "sensor_light"
        private const val KEY_PROXIMITY = "sensor_proximity"
        private const val KEY_PRESSURE = "sensor_pressure"
        private const val KEY_TEMPERATURE = "sensor_temperature"
        private const val KEY_HUMIDITY = "sensor_humidity"

        // Біометричні
        private const val KEY_HEART_RATE = "sensor_heart_rate"
        private const val KEY_STEP_COUNTER = "sensor_step_counter"

        // Локація
        private const val KEY_GPS = "sensor_gps"

        // Камера та мікрофон
        private const val KEY_CAMERA = "sensor_camera"
        private const val KEY_MICROPHONE = "sensor_microphone"
    }

    // Геттери
    fun isAccelerometerEnabled() = prefs.getBoolean(KEY_ACCELEROMETER, true)
    fun isGyroscopeEnabled() = prefs.getBoolean(KEY_GYROSCOPE, true)
    fun isMagnetometerEnabled() = prefs.getBoolean(KEY_MAGNETOMETER, true)
    fun isGravityEnabled() = prefs.getBoolean(KEY_GRAVITY, true)
    fun isRotationEnabled() = prefs.getBoolean(KEY_ROTATION, true)

    fun isLightEnabled() = prefs.getBoolean(KEY_LIGHT, true)
    fun isProximityEnabled() = prefs.getBoolean(KEY_PROXIMITY, true)
    fun isPressureEnabled() = prefs.getBoolean(KEY_PRESSURE, true)
    fun isTemperatureEnabled() = prefs.getBoolean(KEY_TEMPERATURE, true)
    fun isHumidityEnabled() = prefs.getBoolean(KEY_HUMIDITY, true)

    fun isHeartRateEnabled() = prefs.getBoolean(KEY_HEART_RATE, false) // OFF by default
    fun isStepCounterEnabled() = prefs.getBoolean(KEY_STEP_COUNTER, true)

    fun isGpsEnabled() = prefs.getBoolean(KEY_GPS, false) // OFF by default (privacy)

    fun isCameraEnabled() = prefs.getBoolean(KEY_CAMERA, false) // OFF by default
    fun isMicrophoneEnabled() = prefs.getBoolean(KEY_MICROPHONE, false) // OFF by default

    // Сеттери
    fun setAccelerometerEnabled(enabled: Boolean) =
        prefs.edit().putBoolean(KEY_ACCELEROMETER, enabled).apply()

    fun setGyroscopeEnabled(enabled: Boolean) =
        prefs.edit().putBoolean(KEY_GYROSCOPE, enabled).apply()

    fun setMagnetometerEnabled(enabled: Boolean) =
        prefs.edit().putBoolean(KEY_MAGNETOMETER, enabled).apply()

    fun setGravityEnabled(enabled: Boolean) =
        prefs.edit().putBoolean(KEY_GRAVITY, enabled).apply()

    fun setRotationEnabled(enabled: Boolean) =
        prefs.edit().putBoolean(KEY_ROTATION, enabled).apply()

    fun setLightEnabled(enabled: Boolean) =
        prefs.edit().putBoolean(KEY_LIGHT, enabled).apply()

    fun setProximityEnabled(enabled: Boolean) =
        prefs.edit().putBoolean(KEY_PROXIMITY, enabled).apply()

    fun setPressureEnabled(enabled: Boolean) =
        prefs.edit().putBoolean(KEY_PRESSURE, enabled).apply()

    fun setTemperatureEnabled(enabled: Boolean) =
        prefs.edit().putBoolean(KEY_TEMPERATURE, enabled).apply()

    fun setHumidityEnabled(enabled: Boolean) =
        prefs.edit().putBoolean(KEY_HUMIDITY, enabled).apply()

    fun setHeartRateEnabled(enabled: Boolean) =
        prefs.edit().putBoolean(KEY_HEART_RATE, enabled).apply()

    fun setStepCounterEnabled(enabled: Boolean) =
        prefs.edit().putBoolean(KEY_STEP_COUNTER, enabled).apply()

    fun setGpsEnabled(enabled: Boolean) =
        prefs.edit().putBoolean(KEY_GPS, enabled).apply()

    fun setCameraEnabled(enabled: Boolean) =
        prefs.edit().putBoolean(KEY_CAMERA, enabled).apply()

    fun setMicrophoneEnabled(enabled: Boolean) =
        prefs.edit().putBoolean(KEY_MICROPHONE, enabled).apply()

    // Скинути всі налаштування
    fun resetToDefaults() {
        prefs.edit().clear().apply()
    }

    // Отримати всі налаштування як Map
    fun getAllSettings(): Map<String, Boolean> {
        return mapOf(
            "accelerometer" to isAccelerometerEnabled(),
            "gyroscope" to isGyroscopeEnabled(),
            "magnetometer" to isMagnetometerEnabled(),
            "gravity" to isGravityEnabled(),
            "rotation" to isRotationEnabled(),
            "light" to isLightEnabled(),
            "proximity" to isProximityEnabled(),
            "pressure" to isPressureEnabled(),
            "temperature" to isTemperatureEnabled(),
            "humidity" to isHumidityEnabled(),
            "heartRate" to isHeartRateEnabled(),
            "stepCounter" to isStepCounterEnabled(),
            "gps" to isGpsEnabled(),
            "camera" to isCameraEnabled(),
            "microphone" to isMicrophoneEnabled()
        )
    }

    // Отримати кількість увімкнених датчиків
    fun getEnabledCount(): Int {
        return getAllSettings().values.count { it }
    }
}