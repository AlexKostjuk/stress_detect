# server/routes/sync.py
from fastapi import APIRouter, Depends
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import insert, select
from datetime import datetime
from models import SensorVector, Device
from database import get_db

router = APIRouter()

def parse_timestamp(value):
    """Преобразует строку в datetime, если нужно."""
    if isinstance(value, datetime):
        return value
    if isinstance(value, str):
        try:
            return datetime.fromisoformat(value.replace("Z", ""))
        except Exception:
            pass
    return None


@router.post("/")
async def sync_vectors(vectors: list[dict], db: AsyncSession = Depends(get_db)):
    inserted = 0
    errors = []

    for v in vectors:
        try:
            # -------------------------------
            # 1 ВАЛИДАЦИЯ timestamp
            # -------------------------------
            ts = parse_timestamp(v.get("timestamp"))
            if ts is None:
                errors.append(f"Invalid timestamp: {v.get('timestamp')}")
                print("❌ Invalid timestamp:", v.get("timestamp"))
                continue

            # -------------------------------
            # 2 ПРОВЕРКА СУЩЕСТВОВАНИЯ УСТРОЙСТВА
            # -------------------------------
            device_id = v.get("device_id")
            user_id = v.get("user_id")

            result = await db.execute(select(Device).where(Device.id == device_id))
            device = result.scalars().first()

            if not device:
                # Автоматически создаём устройство
                new_device = Device(
                    id=device_id,
                    user_id=user_id,
                    device_name=f"Auto-{device_id}",
                    device_type="auto",
                    device_id=f"auto-{device_id}",
                    is_active=True
                )
                db.add(new_device)
                await db.flush()  # важно!

                print(f"⚙ Авто-создано устройство ID={device_id}")

            # -------------------------------
            # 3 ПОДГОТОВКА ДАННЫХ ДЛЯ INSERT
            # -------------------------------
            allowed_fields = {col.name for col in SensorVector.__table__.columns}

            data = {
                key: (ts if key == "timestamp" else value)
                for key, value in v.items()
                if key in allowed_fields
            }

            # -------------------------------
            # 4 ВСТАВКА
            # -------------------------------
            await db.execute(insert(SensorVector).values(**data))
            inserted += 1

        except Exception as e:
            errors.append(str(e))
            print("❌ Ошибка вставки:", e)

    # -------------------------------
    # 5 КОММИТ
    # -------------------------------
    try:
        await db.commit()
    except Exception as e:
        await db.rollback()
        errors.append(f"commit error: {e}")
        print("❌ Commit error:", e)

    return {
        "inserted": inserted,
        "errors": errors,
    }




# from fastapi import APIRouter, Depends
# from sqlalchemy.ext.asyncio import AsyncSession
# from sqlalchemy import insert
# from database import get_db
# from models import SensorVector
# from schemas import SensorVectorSync
#
# router = APIRouter()
#
# @router.post("/")
# async def sync_vectors(vectors: list[SensorVectorSync], db: AsyncSession = Depends(get_db)):
#     inserted = 0
#     errors = []
#
#     for v in vectors:
#         try:
#             obj = v.dict()
#
#             # created_at из локальной БД — лишний (у сервера своё поле)
#             obj.pop("created_at", None)
#
#             await db.execute(insert(SensorVector).values(**obj))
#             inserted += 1
#
#         except Exception as e:
#             errors.append(str(e))
#             print("Ошибка вставки:", e)
#
#     try:
#         await db.commit()
#     except Exception as e:
#         await db.rollback()
#         errors.append(f"Ошибка коммита: {e}")
#         print("Ошибка коммита:", e)
#
#     return {
#         "count": inserted,
#         "errors": errors
#     }
#
