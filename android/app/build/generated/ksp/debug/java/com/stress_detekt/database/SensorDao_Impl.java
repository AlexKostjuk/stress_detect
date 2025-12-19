package com.stress_detekt.database;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Double;
import java.lang.Exception;
import java.lang.Float;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class SensorDao_Impl implements SensorDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<SensorData> __insertionAdapterOfSensorData;

  private final SharedSQLiteStatement __preparedStmtOfDeleteUserData;

  public SensorDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfSensorData = new EntityInsertionAdapter<SensorData>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `sensor_data` (`id`,`userId`,`accelX`,`accelY`,`accelZ`,`accelMagnitude`,`gyroX`,`gyroY`,`gyroZ`,`magX`,`magY`,`magZ`,`lightLevel`,`proximityDistance`,`pressure`,`altitude`,`temperature`,`humidity`,`heartRate`,`stepCount`,`gravityX`,`gravityY`,`gravityZ`,`rotationX`,`rotationY`,`rotationZ`,`latitude`,`longitude`,`gpsSpeed`,`gpsAccuracy`,`gpsAltitude`,`activityType`,`timestamp`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SensorData entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getUserId());
        if (entity.getAccelX() == null) {
          statement.bindNull(3);
        } else {
          statement.bindDouble(3, entity.getAccelX());
        }
        if (entity.getAccelY() == null) {
          statement.bindNull(4);
        } else {
          statement.bindDouble(4, entity.getAccelY());
        }
        if (entity.getAccelZ() == null) {
          statement.bindNull(5);
        } else {
          statement.bindDouble(5, entity.getAccelZ());
        }
        if (entity.getAccelMagnitude() == null) {
          statement.bindNull(6);
        } else {
          statement.bindDouble(6, entity.getAccelMagnitude());
        }
        if (entity.getGyroX() == null) {
          statement.bindNull(7);
        } else {
          statement.bindDouble(7, entity.getGyroX());
        }
        if (entity.getGyroY() == null) {
          statement.bindNull(8);
        } else {
          statement.bindDouble(8, entity.getGyroY());
        }
        if (entity.getGyroZ() == null) {
          statement.bindNull(9);
        } else {
          statement.bindDouble(9, entity.getGyroZ());
        }
        if (entity.getMagX() == null) {
          statement.bindNull(10);
        } else {
          statement.bindDouble(10, entity.getMagX());
        }
        if (entity.getMagY() == null) {
          statement.bindNull(11);
        } else {
          statement.bindDouble(11, entity.getMagY());
        }
        if (entity.getMagZ() == null) {
          statement.bindNull(12);
        } else {
          statement.bindDouble(12, entity.getMagZ());
        }
        if (entity.getLightLevel() == null) {
          statement.bindNull(13);
        } else {
          statement.bindDouble(13, entity.getLightLevel());
        }
        if (entity.getProximityDistance() == null) {
          statement.bindNull(14);
        } else {
          statement.bindDouble(14, entity.getProximityDistance());
        }
        if (entity.getPressure() == null) {
          statement.bindNull(15);
        } else {
          statement.bindDouble(15, entity.getPressure());
        }
        if (entity.getAltitude() == null) {
          statement.bindNull(16);
        } else {
          statement.bindDouble(16, entity.getAltitude());
        }
        if (entity.getTemperature() == null) {
          statement.bindNull(17);
        } else {
          statement.bindDouble(17, entity.getTemperature());
        }
        if (entity.getHumidity() == null) {
          statement.bindNull(18);
        } else {
          statement.bindDouble(18, entity.getHumidity());
        }
        if (entity.getHeartRate() == null) {
          statement.bindNull(19);
        } else {
          statement.bindDouble(19, entity.getHeartRate());
        }
        if (entity.getStepCount() == null) {
          statement.bindNull(20);
        } else {
          statement.bindLong(20, entity.getStepCount());
        }
        if (entity.getGravityX() == null) {
          statement.bindNull(21);
        } else {
          statement.bindDouble(21, entity.getGravityX());
        }
        if (entity.getGravityY() == null) {
          statement.bindNull(22);
        } else {
          statement.bindDouble(22, entity.getGravityY());
        }
        if (entity.getGravityZ() == null) {
          statement.bindNull(23);
        } else {
          statement.bindDouble(23, entity.getGravityZ());
        }
        if (entity.getRotationX() == null) {
          statement.bindNull(24);
        } else {
          statement.bindDouble(24, entity.getRotationX());
        }
        if (entity.getRotationY() == null) {
          statement.bindNull(25);
        } else {
          statement.bindDouble(25, entity.getRotationY());
        }
        if (entity.getRotationZ() == null) {
          statement.bindNull(26);
        } else {
          statement.bindDouble(26, entity.getRotationZ());
        }
        if (entity.getLatitude() == null) {
          statement.bindNull(27);
        } else {
          statement.bindDouble(27, entity.getLatitude());
        }
        if (entity.getLongitude() == null) {
          statement.bindNull(28);
        } else {
          statement.bindDouble(28, entity.getLongitude());
        }
        if (entity.getGpsSpeed() == null) {
          statement.bindNull(29);
        } else {
          statement.bindDouble(29, entity.getGpsSpeed());
        }
        if (entity.getGpsAccuracy() == null) {
          statement.bindNull(30);
        } else {
          statement.bindDouble(30, entity.getGpsAccuracy());
        }
        if (entity.getGpsAltitude() == null) {
          statement.bindNull(31);
        } else {
          statement.bindDouble(31, entity.getGpsAltitude());
        }
        statement.bindString(32, entity.getActivityType());
        statement.bindLong(33, entity.getTimestamp());
      }
    };
    this.__preparedStmtOfDeleteUserData = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM sensor_data WHERE userId = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertSensorData(final SensorData data,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfSensorData.insertAndReturnId(data);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteUserData(final long userId, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteUserData.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, userId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteUserData.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object getRecentData(final long userId, final int limit,
      final Continuation<? super List<SensorData>> $completion) {
    final String _sql = "SELECT * FROM sensor_data WHERE userId = ? ORDER BY timestamp DESC LIMIT ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, userId);
    _argIndex = 2;
    _statement.bindLong(_argIndex, limit);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<SensorData>>() {
      @Override
      @NonNull
      public List<SensorData> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfAccelX = CursorUtil.getColumnIndexOrThrow(_cursor, "accelX");
          final int _cursorIndexOfAccelY = CursorUtil.getColumnIndexOrThrow(_cursor, "accelY");
          final int _cursorIndexOfAccelZ = CursorUtil.getColumnIndexOrThrow(_cursor, "accelZ");
          final int _cursorIndexOfAccelMagnitude = CursorUtil.getColumnIndexOrThrow(_cursor, "accelMagnitude");
          final int _cursorIndexOfGyroX = CursorUtil.getColumnIndexOrThrow(_cursor, "gyroX");
          final int _cursorIndexOfGyroY = CursorUtil.getColumnIndexOrThrow(_cursor, "gyroY");
          final int _cursorIndexOfGyroZ = CursorUtil.getColumnIndexOrThrow(_cursor, "gyroZ");
          final int _cursorIndexOfMagX = CursorUtil.getColumnIndexOrThrow(_cursor, "magX");
          final int _cursorIndexOfMagY = CursorUtil.getColumnIndexOrThrow(_cursor, "magY");
          final int _cursorIndexOfMagZ = CursorUtil.getColumnIndexOrThrow(_cursor, "magZ");
          final int _cursorIndexOfLightLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "lightLevel");
          final int _cursorIndexOfProximityDistance = CursorUtil.getColumnIndexOrThrow(_cursor, "proximityDistance");
          final int _cursorIndexOfPressure = CursorUtil.getColumnIndexOrThrow(_cursor, "pressure");
          final int _cursorIndexOfAltitude = CursorUtil.getColumnIndexOrThrow(_cursor, "altitude");
          final int _cursorIndexOfTemperature = CursorUtil.getColumnIndexOrThrow(_cursor, "temperature");
          final int _cursorIndexOfHumidity = CursorUtil.getColumnIndexOrThrow(_cursor, "humidity");
          final int _cursorIndexOfHeartRate = CursorUtil.getColumnIndexOrThrow(_cursor, "heartRate");
          final int _cursorIndexOfStepCount = CursorUtil.getColumnIndexOrThrow(_cursor, "stepCount");
          final int _cursorIndexOfGravityX = CursorUtil.getColumnIndexOrThrow(_cursor, "gravityX");
          final int _cursorIndexOfGravityY = CursorUtil.getColumnIndexOrThrow(_cursor, "gravityY");
          final int _cursorIndexOfGravityZ = CursorUtil.getColumnIndexOrThrow(_cursor, "gravityZ");
          final int _cursorIndexOfRotationX = CursorUtil.getColumnIndexOrThrow(_cursor, "rotationX");
          final int _cursorIndexOfRotationY = CursorUtil.getColumnIndexOrThrow(_cursor, "rotationY");
          final int _cursorIndexOfRotationZ = CursorUtil.getColumnIndexOrThrow(_cursor, "rotationZ");
          final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
          final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
          final int _cursorIndexOfGpsSpeed = CursorUtil.getColumnIndexOrThrow(_cursor, "gpsSpeed");
          final int _cursorIndexOfGpsAccuracy = CursorUtil.getColumnIndexOrThrow(_cursor, "gpsAccuracy");
          final int _cursorIndexOfGpsAltitude = CursorUtil.getColumnIndexOrThrow(_cursor, "gpsAltitude");
          final int _cursorIndexOfActivityType = CursorUtil.getColumnIndexOrThrow(_cursor, "activityType");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final List<SensorData> _result = new ArrayList<SensorData>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SensorData _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpUserId;
            _tmpUserId = _cursor.getLong(_cursorIndexOfUserId);
            final Float _tmpAccelX;
            if (_cursor.isNull(_cursorIndexOfAccelX)) {
              _tmpAccelX = null;
            } else {
              _tmpAccelX = _cursor.getFloat(_cursorIndexOfAccelX);
            }
            final Float _tmpAccelY;
            if (_cursor.isNull(_cursorIndexOfAccelY)) {
              _tmpAccelY = null;
            } else {
              _tmpAccelY = _cursor.getFloat(_cursorIndexOfAccelY);
            }
            final Float _tmpAccelZ;
            if (_cursor.isNull(_cursorIndexOfAccelZ)) {
              _tmpAccelZ = null;
            } else {
              _tmpAccelZ = _cursor.getFloat(_cursorIndexOfAccelZ);
            }
            final Float _tmpAccelMagnitude;
            if (_cursor.isNull(_cursorIndexOfAccelMagnitude)) {
              _tmpAccelMagnitude = null;
            } else {
              _tmpAccelMagnitude = _cursor.getFloat(_cursorIndexOfAccelMagnitude);
            }
            final Float _tmpGyroX;
            if (_cursor.isNull(_cursorIndexOfGyroX)) {
              _tmpGyroX = null;
            } else {
              _tmpGyroX = _cursor.getFloat(_cursorIndexOfGyroX);
            }
            final Float _tmpGyroY;
            if (_cursor.isNull(_cursorIndexOfGyroY)) {
              _tmpGyroY = null;
            } else {
              _tmpGyroY = _cursor.getFloat(_cursorIndexOfGyroY);
            }
            final Float _tmpGyroZ;
            if (_cursor.isNull(_cursorIndexOfGyroZ)) {
              _tmpGyroZ = null;
            } else {
              _tmpGyroZ = _cursor.getFloat(_cursorIndexOfGyroZ);
            }
            final Float _tmpMagX;
            if (_cursor.isNull(_cursorIndexOfMagX)) {
              _tmpMagX = null;
            } else {
              _tmpMagX = _cursor.getFloat(_cursorIndexOfMagX);
            }
            final Float _tmpMagY;
            if (_cursor.isNull(_cursorIndexOfMagY)) {
              _tmpMagY = null;
            } else {
              _tmpMagY = _cursor.getFloat(_cursorIndexOfMagY);
            }
            final Float _tmpMagZ;
            if (_cursor.isNull(_cursorIndexOfMagZ)) {
              _tmpMagZ = null;
            } else {
              _tmpMagZ = _cursor.getFloat(_cursorIndexOfMagZ);
            }
            final Float _tmpLightLevel;
            if (_cursor.isNull(_cursorIndexOfLightLevel)) {
              _tmpLightLevel = null;
            } else {
              _tmpLightLevel = _cursor.getFloat(_cursorIndexOfLightLevel);
            }
            final Float _tmpProximityDistance;
            if (_cursor.isNull(_cursorIndexOfProximityDistance)) {
              _tmpProximityDistance = null;
            } else {
              _tmpProximityDistance = _cursor.getFloat(_cursorIndexOfProximityDistance);
            }
            final Float _tmpPressure;
            if (_cursor.isNull(_cursorIndexOfPressure)) {
              _tmpPressure = null;
            } else {
              _tmpPressure = _cursor.getFloat(_cursorIndexOfPressure);
            }
            final Float _tmpAltitude;
            if (_cursor.isNull(_cursorIndexOfAltitude)) {
              _tmpAltitude = null;
            } else {
              _tmpAltitude = _cursor.getFloat(_cursorIndexOfAltitude);
            }
            final Float _tmpTemperature;
            if (_cursor.isNull(_cursorIndexOfTemperature)) {
              _tmpTemperature = null;
            } else {
              _tmpTemperature = _cursor.getFloat(_cursorIndexOfTemperature);
            }
            final Float _tmpHumidity;
            if (_cursor.isNull(_cursorIndexOfHumidity)) {
              _tmpHumidity = null;
            } else {
              _tmpHumidity = _cursor.getFloat(_cursorIndexOfHumidity);
            }
            final Float _tmpHeartRate;
            if (_cursor.isNull(_cursorIndexOfHeartRate)) {
              _tmpHeartRate = null;
            } else {
              _tmpHeartRate = _cursor.getFloat(_cursorIndexOfHeartRate);
            }
            final Integer _tmpStepCount;
            if (_cursor.isNull(_cursorIndexOfStepCount)) {
              _tmpStepCount = null;
            } else {
              _tmpStepCount = _cursor.getInt(_cursorIndexOfStepCount);
            }
            final Float _tmpGravityX;
            if (_cursor.isNull(_cursorIndexOfGravityX)) {
              _tmpGravityX = null;
            } else {
              _tmpGravityX = _cursor.getFloat(_cursorIndexOfGravityX);
            }
            final Float _tmpGravityY;
            if (_cursor.isNull(_cursorIndexOfGravityY)) {
              _tmpGravityY = null;
            } else {
              _tmpGravityY = _cursor.getFloat(_cursorIndexOfGravityY);
            }
            final Float _tmpGravityZ;
            if (_cursor.isNull(_cursorIndexOfGravityZ)) {
              _tmpGravityZ = null;
            } else {
              _tmpGravityZ = _cursor.getFloat(_cursorIndexOfGravityZ);
            }
            final Float _tmpRotationX;
            if (_cursor.isNull(_cursorIndexOfRotationX)) {
              _tmpRotationX = null;
            } else {
              _tmpRotationX = _cursor.getFloat(_cursorIndexOfRotationX);
            }
            final Float _tmpRotationY;
            if (_cursor.isNull(_cursorIndexOfRotationY)) {
              _tmpRotationY = null;
            } else {
              _tmpRotationY = _cursor.getFloat(_cursorIndexOfRotationY);
            }
            final Float _tmpRotationZ;
            if (_cursor.isNull(_cursorIndexOfRotationZ)) {
              _tmpRotationZ = null;
            } else {
              _tmpRotationZ = _cursor.getFloat(_cursorIndexOfRotationZ);
            }
            final Double _tmpLatitude;
            if (_cursor.isNull(_cursorIndexOfLatitude)) {
              _tmpLatitude = null;
            } else {
              _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
            }
            final Double _tmpLongitude;
            if (_cursor.isNull(_cursorIndexOfLongitude)) {
              _tmpLongitude = null;
            } else {
              _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
            }
            final Float _tmpGpsSpeed;
            if (_cursor.isNull(_cursorIndexOfGpsSpeed)) {
              _tmpGpsSpeed = null;
            } else {
              _tmpGpsSpeed = _cursor.getFloat(_cursorIndexOfGpsSpeed);
            }
            final Float _tmpGpsAccuracy;
            if (_cursor.isNull(_cursorIndexOfGpsAccuracy)) {
              _tmpGpsAccuracy = null;
            } else {
              _tmpGpsAccuracy = _cursor.getFloat(_cursorIndexOfGpsAccuracy);
            }
            final Double _tmpGpsAltitude;
            if (_cursor.isNull(_cursorIndexOfGpsAltitude)) {
              _tmpGpsAltitude = null;
            } else {
              _tmpGpsAltitude = _cursor.getDouble(_cursorIndexOfGpsAltitude);
            }
            final String _tmpActivityType;
            _tmpActivityType = _cursor.getString(_cursorIndexOfActivityType);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            _item = new SensorData(_tmpId,_tmpUserId,_tmpAccelX,_tmpAccelY,_tmpAccelZ,_tmpAccelMagnitude,_tmpGyroX,_tmpGyroY,_tmpGyroZ,_tmpMagX,_tmpMagY,_tmpMagZ,_tmpLightLevel,_tmpProximityDistance,_tmpPressure,_tmpAltitude,_tmpTemperature,_tmpHumidity,_tmpHeartRate,_tmpStepCount,_tmpGravityX,_tmpGravityY,_tmpGravityZ,_tmpRotationX,_tmpRotationY,_tmpRotationZ,_tmpLatitude,_tmpLongitude,_tmpGpsSpeed,_tmpGpsAccuracy,_tmpGpsAltitude,_tmpActivityType,_tmpTimestamp);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getDataSince(final long userId, final long startTime,
      final Continuation<? super List<SensorData>> $completion) {
    final String _sql = "SELECT * FROM sensor_data WHERE userId = ? AND timestamp >= ? ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, userId);
    _argIndex = 2;
    _statement.bindLong(_argIndex, startTime);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<SensorData>>() {
      @Override
      @NonNull
      public List<SensorData> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfAccelX = CursorUtil.getColumnIndexOrThrow(_cursor, "accelX");
          final int _cursorIndexOfAccelY = CursorUtil.getColumnIndexOrThrow(_cursor, "accelY");
          final int _cursorIndexOfAccelZ = CursorUtil.getColumnIndexOrThrow(_cursor, "accelZ");
          final int _cursorIndexOfAccelMagnitude = CursorUtil.getColumnIndexOrThrow(_cursor, "accelMagnitude");
          final int _cursorIndexOfGyroX = CursorUtil.getColumnIndexOrThrow(_cursor, "gyroX");
          final int _cursorIndexOfGyroY = CursorUtil.getColumnIndexOrThrow(_cursor, "gyroY");
          final int _cursorIndexOfGyroZ = CursorUtil.getColumnIndexOrThrow(_cursor, "gyroZ");
          final int _cursorIndexOfMagX = CursorUtil.getColumnIndexOrThrow(_cursor, "magX");
          final int _cursorIndexOfMagY = CursorUtil.getColumnIndexOrThrow(_cursor, "magY");
          final int _cursorIndexOfMagZ = CursorUtil.getColumnIndexOrThrow(_cursor, "magZ");
          final int _cursorIndexOfLightLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "lightLevel");
          final int _cursorIndexOfProximityDistance = CursorUtil.getColumnIndexOrThrow(_cursor, "proximityDistance");
          final int _cursorIndexOfPressure = CursorUtil.getColumnIndexOrThrow(_cursor, "pressure");
          final int _cursorIndexOfAltitude = CursorUtil.getColumnIndexOrThrow(_cursor, "altitude");
          final int _cursorIndexOfTemperature = CursorUtil.getColumnIndexOrThrow(_cursor, "temperature");
          final int _cursorIndexOfHumidity = CursorUtil.getColumnIndexOrThrow(_cursor, "humidity");
          final int _cursorIndexOfHeartRate = CursorUtil.getColumnIndexOrThrow(_cursor, "heartRate");
          final int _cursorIndexOfStepCount = CursorUtil.getColumnIndexOrThrow(_cursor, "stepCount");
          final int _cursorIndexOfGravityX = CursorUtil.getColumnIndexOrThrow(_cursor, "gravityX");
          final int _cursorIndexOfGravityY = CursorUtil.getColumnIndexOrThrow(_cursor, "gravityY");
          final int _cursorIndexOfGravityZ = CursorUtil.getColumnIndexOrThrow(_cursor, "gravityZ");
          final int _cursorIndexOfRotationX = CursorUtil.getColumnIndexOrThrow(_cursor, "rotationX");
          final int _cursorIndexOfRotationY = CursorUtil.getColumnIndexOrThrow(_cursor, "rotationY");
          final int _cursorIndexOfRotationZ = CursorUtil.getColumnIndexOrThrow(_cursor, "rotationZ");
          final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
          final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
          final int _cursorIndexOfGpsSpeed = CursorUtil.getColumnIndexOrThrow(_cursor, "gpsSpeed");
          final int _cursorIndexOfGpsAccuracy = CursorUtil.getColumnIndexOrThrow(_cursor, "gpsAccuracy");
          final int _cursorIndexOfGpsAltitude = CursorUtil.getColumnIndexOrThrow(_cursor, "gpsAltitude");
          final int _cursorIndexOfActivityType = CursorUtil.getColumnIndexOrThrow(_cursor, "activityType");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final List<SensorData> _result = new ArrayList<SensorData>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SensorData _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpUserId;
            _tmpUserId = _cursor.getLong(_cursorIndexOfUserId);
            final Float _tmpAccelX;
            if (_cursor.isNull(_cursorIndexOfAccelX)) {
              _tmpAccelX = null;
            } else {
              _tmpAccelX = _cursor.getFloat(_cursorIndexOfAccelX);
            }
            final Float _tmpAccelY;
            if (_cursor.isNull(_cursorIndexOfAccelY)) {
              _tmpAccelY = null;
            } else {
              _tmpAccelY = _cursor.getFloat(_cursorIndexOfAccelY);
            }
            final Float _tmpAccelZ;
            if (_cursor.isNull(_cursorIndexOfAccelZ)) {
              _tmpAccelZ = null;
            } else {
              _tmpAccelZ = _cursor.getFloat(_cursorIndexOfAccelZ);
            }
            final Float _tmpAccelMagnitude;
            if (_cursor.isNull(_cursorIndexOfAccelMagnitude)) {
              _tmpAccelMagnitude = null;
            } else {
              _tmpAccelMagnitude = _cursor.getFloat(_cursorIndexOfAccelMagnitude);
            }
            final Float _tmpGyroX;
            if (_cursor.isNull(_cursorIndexOfGyroX)) {
              _tmpGyroX = null;
            } else {
              _tmpGyroX = _cursor.getFloat(_cursorIndexOfGyroX);
            }
            final Float _tmpGyroY;
            if (_cursor.isNull(_cursorIndexOfGyroY)) {
              _tmpGyroY = null;
            } else {
              _tmpGyroY = _cursor.getFloat(_cursorIndexOfGyroY);
            }
            final Float _tmpGyroZ;
            if (_cursor.isNull(_cursorIndexOfGyroZ)) {
              _tmpGyroZ = null;
            } else {
              _tmpGyroZ = _cursor.getFloat(_cursorIndexOfGyroZ);
            }
            final Float _tmpMagX;
            if (_cursor.isNull(_cursorIndexOfMagX)) {
              _tmpMagX = null;
            } else {
              _tmpMagX = _cursor.getFloat(_cursorIndexOfMagX);
            }
            final Float _tmpMagY;
            if (_cursor.isNull(_cursorIndexOfMagY)) {
              _tmpMagY = null;
            } else {
              _tmpMagY = _cursor.getFloat(_cursorIndexOfMagY);
            }
            final Float _tmpMagZ;
            if (_cursor.isNull(_cursorIndexOfMagZ)) {
              _tmpMagZ = null;
            } else {
              _tmpMagZ = _cursor.getFloat(_cursorIndexOfMagZ);
            }
            final Float _tmpLightLevel;
            if (_cursor.isNull(_cursorIndexOfLightLevel)) {
              _tmpLightLevel = null;
            } else {
              _tmpLightLevel = _cursor.getFloat(_cursorIndexOfLightLevel);
            }
            final Float _tmpProximityDistance;
            if (_cursor.isNull(_cursorIndexOfProximityDistance)) {
              _tmpProximityDistance = null;
            } else {
              _tmpProximityDistance = _cursor.getFloat(_cursorIndexOfProximityDistance);
            }
            final Float _tmpPressure;
            if (_cursor.isNull(_cursorIndexOfPressure)) {
              _tmpPressure = null;
            } else {
              _tmpPressure = _cursor.getFloat(_cursorIndexOfPressure);
            }
            final Float _tmpAltitude;
            if (_cursor.isNull(_cursorIndexOfAltitude)) {
              _tmpAltitude = null;
            } else {
              _tmpAltitude = _cursor.getFloat(_cursorIndexOfAltitude);
            }
            final Float _tmpTemperature;
            if (_cursor.isNull(_cursorIndexOfTemperature)) {
              _tmpTemperature = null;
            } else {
              _tmpTemperature = _cursor.getFloat(_cursorIndexOfTemperature);
            }
            final Float _tmpHumidity;
            if (_cursor.isNull(_cursorIndexOfHumidity)) {
              _tmpHumidity = null;
            } else {
              _tmpHumidity = _cursor.getFloat(_cursorIndexOfHumidity);
            }
            final Float _tmpHeartRate;
            if (_cursor.isNull(_cursorIndexOfHeartRate)) {
              _tmpHeartRate = null;
            } else {
              _tmpHeartRate = _cursor.getFloat(_cursorIndexOfHeartRate);
            }
            final Integer _tmpStepCount;
            if (_cursor.isNull(_cursorIndexOfStepCount)) {
              _tmpStepCount = null;
            } else {
              _tmpStepCount = _cursor.getInt(_cursorIndexOfStepCount);
            }
            final Float _tmpGravityX;
            if (_cursor.isNull(_cursorIndexOfGravityX)) {
              _tmpGravityX = null;
            } else {
              _tmpGravityX = _cursor.getFloat(_cursorIndexOfGravityX);
            }
            final Float _tmpGravityY;
            if (_cursor.isNull(_cursorIndexOfGravityY)) {
              _tmpGravityY = null;
            } else {
              _tmpGravityY = _cursor.getFloat(_cursorIndexOfGravityY);
            }
            final Float _tmpGravityZ;
            if (_cursor.isNull(_cursorIndexOfGravityZ)) {
              _tmpGravityZ = null;
            } else {
              _tmpGravityZ = _cursor.getFloat(_cursorIndexOfGravityZ);
            }
            final Float _tmpRotationX;
            if (_cursor.isNull(_cursorIndexOfRotationX)) {
              _tmpRotationX = null;
            } else {
              _tmpRotationX = _cursor.getFloat(_cursorIndexOfRotationX);
            }
            final Float _tmpRotationY;
            if (_cursor.isNull(_cursorIndexOfRotationY)) {
              _tmpRotationY = null;
            } else {
              _tmpRotationY = _cursor.getFloat(_cursorIndexOfRotationY);
            }
            final Float _tmpRotationZ;
            if (_cursor.isNull(_cursorIndexOfRotationZ)) {
              _tmpRotationZ = null;
            } else {
              _tmpRotationZ = _cursor.getFloat(_cursorIndexOfRotationZ);
            }
            final Double _tmpLatitude;
            if (_cursor.isNull(_cursorIndexOfLatitude)) {
              _tmpLatitude = null;
            } else {
              _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
            }
            final Double _tmpLongitude;
            if (_cursor.isNull(_cursorIndexOfLongitude)) {
              _tmpLongitude = null;
            } else {
              _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
            }
            final Float _tmpGpsSpeed;
            if (_cursor.isNull(_cursorIndexOfGpsSpeed)) {
              _tmpGpsSpeed = null;
            } else {
              _tmpGpsSpeed = _cursor.getFloat(_cursorIndexOfGpsSpeed);
            }
            final Float _tmpGpsAccuracy;
            if (_cursor.isNull(_cursorIndexOfGpsAccuracy)) {
              _tmpGpsAccuracy = null;
            } else {
              _tmpGpsAccuracy = _cursor.getFloat(_cursorIndexOfGpsAccuracy);
            }
            final Double _tmpGpsAltitude;
            if (_cursor.isNull(_cursorIndexOfGpsAltitude)) {
              _tmpGpsAltitude = null;
            } else {
              _tmpGpsAltitude = _cursor.getDouble(_cursorIndexOfGpsAltitude);
            }
            final String _tmpActivityType;
            _tmpActivityType = _cursor.getString(_cursorIndexOfActivityType);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            _item = new SensorData(_tmpId,_tmpUserId,_tmpAccelX,_tmpAccelY,_tmpAccelZ,_tmpAccelMagnitude,_tmpGyroX,_tmpGyroY,_tmpGyroZ,_tmpMagX,_tmpMagY,_tmpMagZ,_tmpLightLevel,_tmpProximityDistance,_tmpPressure,_tmpAltitude,_tmpTemperature,_tmpHumidity,_tmpHeartRate,_tmpStepCount,_tmpGravityX,_tmpGravityY,_tmpGravityZ,_tmpRotationX,_tmpRotationY,_tmpRotationZ,_tmpLatitude,_tmpLongitude,_tmpGpsSpeed,_tmpGpsAccuracy,_tmpGpsAltitude,_tmpActivityType,_tmpTimestamp);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getDataCount(final long userId, final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM sensor_data WHERE userId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, userId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getAverageMagnitude(final long userId, final long startTime,
      final Continuation<? super Float> $completion) {
    final String _sql = "SELECT AVG(accelMagnitude) FROM sensor_data WHERE userId = ? AND timestamp >= ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, userId);
    _argIndex = 2;
    _statement.bindLong(_argIndex, startTime);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Float>() {
      @Override
      @Nullable
      public Float call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Float _result;
          if (_cursor.moveToFirst()) {
            final Float _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getFloat(0);
            }
            _result = _tmp;
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
