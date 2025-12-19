package com.stress_detekt.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface SensorDao {

    @Insert
    suspend fun insertSensorData(data: SensorData): Long

    @Query("SELECT * FROM sensor_data WHERE userId = :userId ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentData(userId: Long, limit: Int = 100): List<SensorData>

    @Query("SELECT * FROM sensor_data WHERE userId = :userId AND timestamp >= :startTime ORDER BY timestamp DESC")
    suspend fun getDataSince(userId: Long, startTime: Long): List<SensorData>

    @Query("DELETE FROM sensor_data WHERE userId = :userId")
    suspend fun deleteUserData(userId: Long)

    @Query("SELECT COUNT(*) FROM sensor_data WHERE userId = :userId")
    suspend fun getDataCount(userId: Long): Int


    @Query("SELECT AVG(accelMagnitude) FROM sensor_data WHERE userId = :userId AND timestamp >= :startTime")
    suspend fun getAverageMagnitude(userId: Long, startTime: Long): Float?
}