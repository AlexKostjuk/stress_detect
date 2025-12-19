package com.stress_detekt.database;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile UserDao _userDao;

  private volatile SensorDao _sensorDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(3) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `users` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `email` TEXT NOT NULL, `password` TEXT NOT NULL, `name` TEXT NOT NULL, `createdAt` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `sensor_data` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `userId` INTEGER NOT NULL, `accelX` REAL, `accelY` REAL, `accelZ` REAL, `accelMagnitude` REAL, `gyroX` REAL, `gyroY` REAL, `gyroZ` REAL, `magX` REAL, `magY` REAL, `magZ` REAL, `lightLevel` REAL, `proximityDistance` REAL, `pressure` REAL, `altitude` REAL, `temperature` REAL, `humidity` REAL, `heartRate` REAL, `stepCount` INTEGER, `gravityX` REAL, `gravityY` REAL, `gravityZ` REAL, `rotationX` REAL, `rotationY` REAL, `rotationZ` REAL, `latitude` REAL, `longitude` REAL, `gpsSpeed` REAL, `gpsAccuracy` REAL, `gpsAltitude` REAL, `activityType` TEXT NOT NULL, `timestamp` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '28995d12fa41b064a31dcb350f2a3411')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `users`");
        db.execSQL("DROP TABLE IF EXISTS `sensor_data`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsUsers = new HashMap<String, TableInfo.Column>(5);
        _columnsUsers.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("email", new TableInfo.Column("email", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("password", new TableInfo.Column("password", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysUsers = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesUsers = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoUsers = new TableInfo("users", _columnsUsers, _foreignKeysUsers, _indicesUsers);
        final TableInfo _existingUsers = TableInfo.read(db, "users");
        if (!_infoUsers.equals(_existingUsers)) {
          return new RoomOpenHelper.ValidationResult(false, "users(com.stress_detekt.database.User).\n"
                  + " Expected:\n" + _infoUsers + "\n"
                  + " Found:\n" + _existingUsers);
        }
        final HashMap<String, TableInfo.Column> _columnsSensorData = new HashMap<String, TableInfo.Column>(33);
        _columnsSensorData.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSensorData.put("userId", new TableInfo.Column("userId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSensorData.put("accelX", new TableInfo.Column("accelX", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSensorData.put("accelY", new TableInfo.Column("accelY", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSensorData.put("accelZ", new TableInfo.Column("accelZ", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSensorData.put("accelMagnitude", new TableInfo.Column("accelMagnitude", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSensorData.put("gyroX", new TableInfo.Column("gyroX", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSensorData.put("gyroY", new TableInfo.Column("gyroY", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSensorData.put("gyroZ", new TableInfo.Column("gyroZ", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSensorData.put("magX", new TableInfo.Column("magX", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSensorData.put("magY", new TableInfo.Column("magY", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSensorData.put("magZ", new TableInfo.Column("magZ", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSensorData.put("lightLevel", new TableInfo.Column("lightLevel", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSensorData.put("proximityDistance", new TableInfo.Column("proximityDistance", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSensorData.put("pressure", new TableInfo.Column("pressure", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSensorData.put("altitude", new TableInfo.Column("altitude", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSensorData.put("temperature", new TableInfo.Column("temperature", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSensorData.put("humidity", new TableInfo.Column("humidity", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSensorData.put("heartRate", new TableInfo.Column("heartRate", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSensorData.put("stepCount", new TableInfo.Column("stepCount", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSensorData.put("gravityX", new TableInfo.Column("gravityX", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSensorData.put("gravityY", new TableInfo.Column("gravityY", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSensorData.put("gravityZ", new TableInfo.Column("gravityZ", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSensorData.put("rotationX", new TableInfo.Column("rotationX", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSensorData.put("rotationY", new TableInfo.Column("rotationY", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSensorData.put("rotationZ", new TableInfo.Column("rotationZ", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSensorData.put("latitude", new TableInfo.Column("latitude", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSensorData.put("longitude", new TableInfo.Column("longitude", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSensorData.put("gpsSpeed", new TableInfo.Column("gpsSpeed", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSensorData.put("gpsAccuracy", new TableInfo.Column("gpsAccuracy", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSensorData.put("gpsAltitude", new TableInfo.Column("gpsAltitude", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSensorData.put("activityType", new TableInfo.Column("activityType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSensorData.put("timestamp", new TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysSensorData = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesSensorData = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoSensorData = new TableInfo("sensor_data", _columnsSensorData, _foreignKeysSensorData, _indicesSensorData);
        final TableInfo _existingSensorData = TableInfo.read(db, "sensor_data");
        if (!_infoSensorData.equals(_existingSensorData)) {
          return new RoomOpenHelper.ValidationResult(false, "sensor_data(com.stress_detekt.database.SensorData).\n"
                  + " Expected:\n" + _infoSensorData + "\n"
                  + " Found:\n" + _existingSensorData);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "28995d12fa41b064a31dcb350f2a3411", "2e3901aa37e54092f548dc4b8f385964");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "users","sensor_data");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `users`");
      _db.execSQL("DELETE FROM `sensor_data`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(UserDao.class, UserDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(SensorDao.class, SensorDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public UserDao userDao() {
    if (_userDao != null) {
      return _userDao;
    } else {
      synchronized(this) {
        if(_userDao == null) {
          _userDao = new UserDao_Impl(this);
        }
        return _userDao;
      }
    }
  }

  @Override
  public SensorDao sensorDao() {
    if (_sensorDao != null) {
      return _sensorDao;
    } else {
      synchronized(this) {
        if(_sensorDao == null) {
          _sensorDao = new SensorDao_Impl(this);
        }
        return _sensorDao;
      }
    }
  }
}
