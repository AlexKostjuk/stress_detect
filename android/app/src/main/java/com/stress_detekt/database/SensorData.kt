package com.stress_detekt.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sensor_data")
data class SensorData(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val userId: Long,

    // Accelerometer (завжди є)
    val accelX: Float? = null,
    val accelY: Float? = null,
    val accelZ: Float? = null,
    val accelMagnitude: Float? = null,

    // Gyroscope (майже завжди є)
    val gyroX: Float? = null,
    val gyroY: Float? = null,
    val gyroZ: Float? = null,

    // Magnetometer (майже завжди є)
    val magX: Float? = null,
    val magY: Float? = null,
    val magZ: Float? = null,

    // Light (майже завжди є)
    val lightLevel: Float? = null,

    // Proximity (майже завжди є)
    val proximityDistance: Float? = null,

    // Pressure/Barometer (часто є)
    val pressure: Float? = null,
    val altitude: Float? = null,

    // Temperature (рідко є на телефонах)
    val temperature: Float? = null,

    // Humidity (рідко є на телефонах)
    val humidity: Float? = null,

    // Heart Rate (дуже рідко)
    val heartRate: Float? = null,

    // Step Counter (майже завжди є)
    val stepCount: Int? = null,

    // Gravity (майже завжди є)
    val gravityX: Float? = null,
    val gravityY: Float? = null,
    val gravityZ: Float? = null,

    // Rotation Vector (майже завжди є)
    val rotationX: Float? = null,
    val rotationY: Float? = null,
    val rotationZ: Float? = null,

    // GPS (додамо окремо)
    val latitude: Double? = null,
    val longitude: Double? = null,
    val gpsSpeed: Float? = null,
    val gpsAccuracy: Float? = null,
    val gpsAltitude: Double? = null,

    // Activity type
    val activityType: String = "UNKNOWN",

    // Timestamp
    val timestamp: Long = System.currentTimeMillis()
)