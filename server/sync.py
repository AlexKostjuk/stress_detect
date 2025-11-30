# server/sync.py
from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select, text
from datetime import datetime
from models import SensorVector, Device, User
from database import get_db
from auth import verify_token
from typing import List, Dict, Any

router = APIRouter()


def parse_timestamp(value) -> datetime:
    """
    Парсить ISO строку або datetime об'єкт в datetime з timezone.
    Підтримує формати:
    - 2025-11-19T22:19:27
    - 2025-11-19T22:19:27Z
    - 2025-11-19T22:19:27+00:00
    """
    if isinstance(value, datetime):
        return value

    if isinstance(value, str):
        try:
            # Видаляємо 'Z' і замінюємо на +00:00
            clean_value = value.replace("Z", "+00:00")

            # Якщо немає timezone, додаємо UTC
            if "+" not in clean_value and len(clean_value) == 19:
                clean_value += "+00:00"

            return datetime.fromisoformat(clean_value)
        except ValueError as e:
            print(f"❌ Помилка парсингу timestamp '{value}': {e}")
            return None

    return None


async def ensure_device_exists(
        db: AsyncSession,
        device_id: int,
        user_id: int,
        device_name: str = None,
        device_type: str = None,
        device_id_str: str = None
) -> bool:
    """
    Перевіряє чи існує пристрій, якщо ні - створює з даними клієнта.
    Якщо існує - оновлює дані.
    Повертає True якщо успішно.
    """
    try:
        result = await db.execute(
            select(Device).where(Device.id == device_id)
        )
        device = result.scalars().first()

        if not device:
            # Створюємо новий пристрій з даними клієнта
            new_device = Device(
                id=device_id,
                user_id=user_id,
                device_name=device_name or f"Device-{device_id}",
                device_type=device_type or "unknown",
                device_id=device_id_str or f"device-{device_id}",
                is_active=True
            )
            db.add(new_device)
            await db.flush()
            print(f"✅ Створено пристрій ID={device_id} ({device_name})")
        else:
            # Оновлюємо дані якщо вони змінились
            updated = False
            if device_name and device.device_name != device_name:
                device.device_name = device_name
                updated = True
            if device_type and device.device_type != device_type:
                device.device_type = device_type
                updated = True
            if device_id_str and device.device_id != device_id_str:
                device.device_id = device_id_str
                updated = True

            if updated:
                await db.flush()
                print(f"🔄 Оновлено пристрій ID={device_id}")

        return True
    except Exception as e:
        print(f"❌ Помилка створення/оновлення пристрою: {e}")
        return False


async def validate_user_retention(
        db: AsyncSession,
        user_id: int
) -> Dict[str, Any]:
    """
    Перевіряє retention policy користувача.
    Повертає інформацію про користувача та його retention.
    """
    try:
        # Отримуємо користувача
        result = await db.execute(
            text("""
                 SELECT u.id, u.user_type, ur.retention_days
                 FROM users u
                          LEFT JOIN user_retention ur ON ur.user_id = u.id
                 WHERE u.id = :user_id
                 """),
            {"user_id": user_id}
        )
        row = result.first()

        if not row:
            return {"exists": False, "error": f"Користувач {user_id} не знайдено"}

        return {
            "exists": True,
            "user_type": row[1],
            "retention_days": row[2] or 60  # За замовчуванням FREE
        }
    except Exception as e:
        print(f"❌ Помилка перевірки retention: {e}")
        return {"exists": False, "error": str(e)}


@router.post("/")
async def sync_vectors(
        payload: Dict[str, Any],  # ✅ ЗМІНЕНО: Приймаємо dict замість list
        username: str = Depends(verify_token),
        db: AsyncSession = Depends(get_db)
):
    """
    Синхронізація даних сенсорів з клієнта.

    Payload:
    {
        "vectors": [...],  # Список векторів
        "device_info": {   # Інформація про пристрій
            "device_id": 1,
            "device_name": "Local-Headset",
            "device_type": "",
            "device_id_str": "LOCAL-001"
        }
    }

    Враховує:
    - Hypertable partitioning (timestamp + user_id)
    - Compression policy (7 днів)
    - Dynamic retention (FREE: 60, PREMIUM: 365)
    - Синхронізація пристроїв
    """

    # ✅ ДОДАНО: Розпаковуємо payload
    vectors = payload.get("vectors", [])
    device_info = payload.get("device_info", {})

    if not vectors:
        return {
            "inserted": 0,
            "total": 0,
            "errors": ["Порожній масив даних"],
            "username": username
        }

    inserted = 0
    skipped = 0
    errors = []

    # Отримуємо user_id з username
    result = await db.execute(select(User).where(User.username == username))
    current_user = result.scalars().first()

    if not current_user:
        raise HTTPException(status_code=404, detail="User not found")

    print(f"\n{'=' * 60}")
    print(f"🔄 Синхронізація від {username} (user_id={current_user.id})")
    print(f"📦 Отримано записів: {len(vectors)}")
    print(f"{'=' * 60}\n")

    # Перевіряємо retention
    retention_info = await validate_user_retention(db, current_user.id)
    if not retention_info["exists"]:
        errors.append(retention_info["error"])
        return {"inserted": 0, "total": len(vectors), "errors": errors}

    print(f"👤 Тип: {retention_info['user_type']}, Retention: {retention_info['retention_days']} днів\n")

    # Унікальні пристрої (перевіряємо один раз)
    device_ids = set(v.get("device_id") for v in vectors if v.get("device_id"))

    # ✅ ДОДАНО: Використовуємо device_info якщо є
    for device_id in device_ids:
        if device_info.get("device_id") == device_id:
            # Є інформація про цей пристрій
            await ensure_device_exists(
                db,
                device_id,
                current_user.id,
                device_name=device_info.get("device_name"),
                device_type=device_info.get("device_type"),
                device_id_str=device_info.get("device_id_str")
            )
        else:
            # Немає інформації - створюємо автоматично
            await ensure_device_exists(db, device_id, current_user.id)

    # Batch insert для швидкості
    batch_data = []

    for idx, v in enumerate(vectors):
        try:
            # ========================================
            # 1. ВАЛІДАЦІЯ ОБОВ'ЯЗКОВИХ ПОЛІВ
            # ========================================
            user_id = v.get("user_id")
            device_id = v.get("device_id")
            timestamp_raw = v.get("timestamp")

            if not all([user_id, device_id, timestamp_raw]):
                errors.append(f"Запис {idx}: відсутні обов'язкові поля")
                skipped += 1
                continue

            # Перевірка відповідності user_id
            if user_id != current_user.id:
                errors.append(f"Запис {idx}: user_id не співпадає з авторизованим користувачем")
                skipped += 1
                continue

            # ========================================
            # 2. ПАРСИНГ TIMESTAMP
            # ========================================
            timestamp = parse_timestamp(timestamp_raw)
            if timestamp is None:
                errors.append(f"Запис {idx}: неправильний timestamp '{timestamp_raw}'")
                skipped += 1
                continue

            # ========================================
            # 3. ПАРСИНГ created_at (опціонально)
            # ========================================
            created_at_raw = v.get("created_at")
            created_at = parse_timestamp(created_at_raw) if created_at_raw else None

            # ✅ ВИПРАВЛЕНО: Переконуємося що created_at має timezone
            if created_at and created_at.tzinfo is None:
                from datetime import timezone as tz
                created_at = created_at.replace(tzinfo=tz.utc)

            # ========================================
            # 4. ПЕРЕВІРКА RETENTION POLICY
            # ========================================
            # Не вставляємо дані старші за retention
            from datetime import timedelta, timezone as tz

            cutoff_date = datetime.now(tz.utc) - timedelta(days=retention_info["retention_days"])

            # ✅ ВИПРАВЛЕНО: Переконуємося що timestamp має timezone
            if timestamp.tzinfo is None:
                timestamp = timestamp.replace(tzinfo=tz.utc)

            if timestamp < cutoff_date:
                skipped += 1
                continue  # Дані вже застарілі, job їх видалить

            # ========================================
            # 5. ПІДГОТОВКА ДАНИХ
            # ========================================
            allowed_fields = {col.name for col in SensorVector.__table__.columns}

            data = {}
            for key, value in v.items():
                if key not in allowed_fields:
                    continue

                # Спеціальна обробка datetime полів
                if key == "timestamp":
                    data[key] = timestamp
                elif key == "created_at":
                    data[key] = created_at if created_at else datetime.utcnow()
                elif value is not None:  # Пропускаємо None
                    data[key] = value

            # Обов'язкові поля
            if "model_version" not in data:
                data["model_version"] = "v1.0"

            batch_data.append(data)

        except Exception as e:
            error_msg = f"Запис {idx}: {str(e)}"
            errors.append(error_msg)
            skipped += 1
            print(f"❌ {error_msg}")

    # ========================================
    # 6. BATCH INSERT (для hypertable)
    # ========================================
    if batch_data:
        try:
            # ✅ ВИПРАВЛЕНО: PostgreSQL-специфічний ON CONFLICT
            from sqlalchemy.dialects.postgresql import insert as pg_insert

            stmt = pg_insert(SensorVector).values(batch_data)
            stmt = stmt.on_conflict_do_nothing(
                index_elements=['id', 'timestamp', 'user_id']
            )

            await db.execute(stmt)
            inserted = len(batch_data)
            print(f"✅ Batch insert: {inserted} записів (дублікати пропущено)")

        except Exception as e:
            error_msg = f"Помилка batch insert: {str(e)}"
            errors.append(error_msg)
            print(f"❌ {error_msg}")

    # ========================================
    # 7. COMMIT
    # ========================================
    try:
        await db.commit()
        print(f"\n{'=' * 60}")
        print(f"✅ УСПІШНО:")
        print(f"   Вставлено: {inserted}")
        print(f"   Пропущено: {skipped}")
        print(f"   Помилок: {len(errors)}")
        print(f"{'=' * 60}\n")

    except Exception as e:
        await db.rollback()
        error_msg = f"Помилка commit: {str(e)}"
        errors.append(error_msg)
        print(f"❌ {error_msg}")
        raise HTTPException(status_code=500, detail=error_msg)

    return {
        "inserted": inserted,
        "skipped": skipped,
        "total": len(vectors),
        "errors": errors[:20],  # Макс 20 помилок
        "username": username,
        "retention_days": retention_info["retention_days"]
    }


# ========================================
# ДОДАТКОВИЙ ENDPOINT - СТАТИСТИКА
# ========================================
@router.get("/stats")
async def get_sync_stats(
        username: str = Depends(verify_token),
        db: AsyncSession = Depends(get_db)
):
    """
    Статистика синхронізованих даних.
    """
    result = await db.execute(select(User).where(User.username == username))
    user = result.scalars().first()

    if not user:
        raise HTTPException(status_code=404, detail="User not found")

    # Підрахунок записів
    count_result = await db.execute(
        text("SELECT COUNT(*) FROM a_sensor_vectors WHERE user_id = :user_id"),
        {"user_id": user.id}
    )
    total_records = count_result.scalar()

    # Retention інфо
    retention_result = await db.execute(
        text("""
             SELECT retention_days, updated_at
             FROM user_retention
             WHERE user_id = :user_id
             """),
        {"user_id": user.id}
    )
    retention_row = retention_result.first()

    return {
        "username": username,
        "user_type": user.user_type,
        "total_records": total_records,
        "retention_days": retention_row[0] if retention_row else 60,
        "retention_updated": retention_row[1].isoformat() if retention_row else None
    }