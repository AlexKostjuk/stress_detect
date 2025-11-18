# client/cleanup.py
from datetime import datetime, timedelta
from sqlalchemy import delete
from .local_models import SensorVector
from .local_db import get_db

FREE_DAYS = 60

# def cleanup_old_data():
#     cutoff = datetime.utcnow() - timedelta(days=FREE_DAYS)
#     db = next(get_db())
#     try:
#         result = db.execute(delete(SensorVector).where(SensorVector.timestamp < cutoff))
#         db.commit()
#         print(f"[CLEANUP] Удалено {result.rowcount} записей старше {FREE_DAYS} дней")
#     except Exception as e:
#         db.rollback()
#         print(f"[ERROR] Очистка: {e}")
#     finally:
#         db.close()
# client/cleanup.py
from contextlib import contextmanager

def cleanup_old_data():
    cutoff = datetime.utcnow() - timedelta(days=FREE_DAYS)
    with get_db() as db:
        try:
            result = db.execute(delete(SensorVector).where(SensorVector.timestamp < cutoff))
            db.commit()
            deleted = result.rowcount if result.rowcount != -1 else db.query(SensorVector).filter(SensorVector.timestamp < cutoff).count()
            print(f"[CLEANUP] Удалено {deleted} записей старше {FREE_DAYS} дней")
        except Exception as e:
            db.rollback()
            print(f"[ERROR] Очистка: {e}")