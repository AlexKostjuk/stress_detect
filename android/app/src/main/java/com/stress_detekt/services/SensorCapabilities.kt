package com.stress_detekt.services

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager

data class SensorInfo(
    val name: String,
    val type: Int,
    val isAvailable: Boolean,
    val vendor: String = "",
    val version: Int = 0,
    val power: Float = 0f,
    val maxRange: Float = 0f
)

class SensorCapabilities(context: Context) {

    private val sensorManager: SensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    // Перевірка наявності датчиків
    fun checkSensor(sensorType: Int): SensorInfo {
        val sensor = sensorManager.getDefaultSensor(sensorType)

        return if (sensor != null) {
            SensorInfo(
                name = sensor.name,
                type = sensorType,
                isAvailable = true,
                vendor = sensor.vendor,
                version = sensor.version,
                power = sensor.power,
                maxRange = sensor.maximumRange  // ← ВИПРАВЛЕНО
            )
        } else {
            SensorInfo(
                name = getSensorTypeName(sensorType),
                type = sensorType,
                isAvailable = false
            )
        }
    }

    // Отримати всі доступні датчики
    fun getAllSensorsStatus(): Map<String, SensorInfo> {
        return mapOf(
            "accelerometer" to checkSensor(Sensor.TYPE_ACCELEROMETER),
            "gyroscope" to checkSensor(Sensor.TYPE_GYROSCOPE),
            "magnetometer" to checkSensor(Sensor.TYPE_MAGNETIC_FIELD),
            "light" to checkSensor(Sensor.TYPE_LIGHT),
            "proximity" to checkSensor(Sensor.TYPE_PROXIMITY),
            "pressure" to checkSensor(Sensor.TYPE_PRESSURE),
            "temperature" to checkSensor(Sensor.TYPE_AMBIENT_TEMPERATURE),
            "humidity" to checkSensor(Sensor.TYPE_RELATIVE_HUMIDITY),
            "heartRate" to checkSensor(Sensor.TYPE_HEART_RATE),
            "stepCounter" to checkSensor(Sensor.TYPE_STEP_COUNTER),
            "gravity" to checkSensor(Sensor.TYPE_GRAVITY),
            "linearAccel" to checkSensor(Sensor.TYPE_LINEAR_ACCELERATION),
            "rotation" to checkSensor(Sensor.TYPE_ROTATION_VECTOR)
        )
    }

    private fun getSensorTypeName(type: Int): String {
        return when (type) {
            Sensor.TYPE_ACCELEROMETER -> "Accelerometer"
            Sensor.TYPE_GYROSCOPE -> "Gyroscope"
            Sensor.TYPE_MAGNETIC_FIELD -> "Magnetometer"
            Sensor.TYPE_LIGHT -> "Light Sensor"
            Sensor.TYPE_PROXIMITY -> "Proximity Sensor"
            Sensor.TYPE_PRESSURE -> "Barometer"
            Sensor.TYPE_AMBIENT_TEMPERATURE -> "Temperature"
            Sensor.TYPE_RELATIVE_HUMIDITY -> "Humidity"
            Sensor.TYPE_HEART_RATE -> "Heart Rate"
            Sensor.TYPE_STEP_COUNTER -> "Step Counter"
            Sensor.TYPE_GRAVITY -> "Gravity"
            Sensor.TYPE_LINEAR_ACCELERATION -> "Linear Acceleration"
            Sensor.TYPE_ROTATION_VECTOR -> "Rotation Vector"
            else -> "Unknown Sensor"
        }
    }

    // Вивести в консоль список датчиків
    fun logAvailableSensors() {
        val allSensors = sensorManager.getSensorList(Sensor.TYPE_ALL)
        println("=== AVAILABLE SENSORS (${allSensors.size}) ===")
        allSensors.forEach { sensor ->
            println("✓ ${sensor.name} (${sensor.vendor})")
            println("  Type: ${sensor.type}, Power: ${sensor.power}mA")
            println("  Range: ${sensor.maximumRange}, Version: ${sensor.version}")  // ← ВИПРАВЛЕНО
            println()
        }
    }
}