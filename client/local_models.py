# client/local_models.py
from sqlalchemy import (
    Column, Integer, ForeignKey, String, Float, DateTime, Boolean,
    JSON, BigInteger, PrimaryKeyConstraint, Index, Text, func
)
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import relationship
import json
from sqlalchemy.types import TypeDecorator, Text



Base = declarative_base()
class JSONEncodedDict(TypeDecorator):
    impl = Text
    def process_bind_param(self, value, dialect):
        return json.dumps(value) if value is not None else None
    def process_result_value(self, value, dialect):
        return json.loads(value) if value is not None else None


class User(Base):
    __tablename__ = "users"
    id = Column(Integer, primary_key=True, index=True)
    username = Column(String(50), unique=True, index=True, nullable=False)
    email = Column(String(100), unique=True, index=True, nullable=False)
    hashed_password = Column(String(255), nullable=False)
    user_type = Column(String(20), default="free")
    subscription_end = Column(DateTime(timezone=True))
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    updated_at = Column(DateTime(timezone=True), server_default=func.now(), onupdate=func.now())
    is_active = Column(Boolean, default=True)

    devices = relationship("Device", back_populates="user")
    vectors = relationship("SensorVector", back_populates="user")


class Device(Base):
    __tablename__ = "devices"
    id = Column(Integer, primary_key=True)
    user_id = Column(Integer, ForeignKey("users.id"), nullable=False)
    device_name = Column(String(100), nullable=False)
    device_type = Column(String(20))
    device_id = Column(String(100), unique=True, nullable=False)
    os_version = Column(String(50))
    app_version = Column(String(20))
    last_seen = Column(DateTime(timezone=True), server_default=func.now())
    is_active = Column(Boolean, default=True)
    created_at = Column(DateTime(timezone=True), server_default=func.now())

    user = relationship("User", back_populates="devices")
    vectors = relationship("SensorVector", back_populates="device")


class SensorVector(Base):
    __tablename__ = "a_sensor_vectors"

    id = Column(BigInteger, primary_key=True)
    user_id = Column(Integer, ForeignKey("users.id"), nullable=False, index=True)
    device_id = Column(Integer, ForeignKey("devices.id"), nullable=False, index=True)
    timestamp = Column(DateTime(timezone=True), nullable=False, index=True)

    # Биометрия
    heart_rate = Column(Integer)
    hrv_rmssd = Column(Float)
    hrv_sdnn = Column(Float)
    spo2 = Column(Integer)
    skin_temperature = Column(Float)

    # Движение
    accel_x = Column(Float)
    accel_y = Column(Float)
    accel_z = Column(Float)
    gyro_x = Column(Float)
    gyro_y = Column(Float)
    gyro_z = Column(Float)
    steps_count = Column(Integer, default=0)

    # Аудио
    noise_level_db = Column(Float)
    breathing_rate = Column(Integer)

    # Контекст
    activity_type = Column(Text)
    location_type = Column(Text)
    battery_level = Column(Integer)

    # ML Результаты
    stress_level = Column(Float)
    energy_level = Column(Float)
    focus_level = Column(Float)
    model_version = Column(Text, nullable=False)
    confidence_score = Column(Float)

    # Гибкое хранение
    raw_features = Column(JSONEncodedDict)
    lora_weights = Column(JSONEncodedDict)

    # raw_features = Column(JSON)
    # lora_weights = Column(JSON)
    signal_quality = Column(Integer)

    created_at = Column(DateTime(timezone=True), server_default=func.now())

    user = relationship("User", back_populates="vectors")
    device = relationship("Device", back_populates="vectors")

    __table_args__ = (
        PrimaryKeyConstraint('id', 'timestamp', 'user_id'),
        Index('ix_sensor_vectors_user_timestamp', 'user_id', 'timestamp'),
    )