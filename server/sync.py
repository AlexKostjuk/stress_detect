from fastapi import APIRouter, Depends
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import insert
from models import SensorVector
from database import get_db

router = APIRouter()

@router.post("/")
async def sync_vectors(vectors: list[dict], db: AsyncSession = Depends(get_db)):
    inserted = 0
    errors = []

    for v in vectors:
        try:
            # Логируем входящие данные
            print("Вставка записи:", v)

            # Вставляем только поля, которые реально есть в модели
            data = {
                "id": v.get("id"),
                "user_id": v.get("user_id"),
                "device_id": v.get("device_id"),
                "timestamp": v.get("timestamp"),
                "heart_rate": v.get("heart_rate"),
                "hrv_rmssd": v.get("hrv_rmssd"),
                "hrv_sdnn": v.get("hrv_sdnn"),
                "spo2": v.get("spo2"),
                "skin_temperature": v.get("skin_temperature"),
                "accel_x": v.get("accel_x"),
                "accel_y": v.get("accel_y"),
                "accel_z": v.get("accel_z"),
                "gyro_x": v.get("gyro_x"),
                "gyro_y": v.get("gyro_y"),
                "gyro_z": v.get("gyro_z"),
                "steps_count": v.get("steps_count"),
                "noise_level_db": v.get("noise_level_db"),
                "breathing_rate": v.get("breathing_rate"),
                "activity_type": v.get("activity_type"),
                "location_type": v.get("location_type"),
                "battery_level": v.get("battery_level"),
                "stress_level": v.get("stress_level"),
                "energy_level": v.get("energy_level"),
                "focus_level": v.get("focus_level"),
                "model_version": v.get("model_version"),
                "confidence_score": v.get("confidence_score"),
                "raw_features": v.get("raw_features"),
                "lora_weights": v.get("lora_weights"),
                "signal_quality": v.get("signal_quality"),
            }

            await db.execute(insert(SensorVector).values(**data))
            inserted += 1
        except Exception as e:
            errors.append(str(e))
            print("Ошибка вставки:", e)

    try:
        await db.commit()
    except Exception as e:
        await db.rollback()
        errors.append(f"Ошибка коммита: {e}")
        print("Ошибка коммита:", e)

    return {
        "count": inserted,
        "errors": errors
    }