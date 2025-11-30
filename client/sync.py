# client/sync.py
import requests
import json
from datetime import datetime, timezone
from PyQt6.QtWidgets import QApplication
from client.local_db import get_db
from client.local_models import SensorVector


def _get_window():
    """Отримує активне вікно клієнта для логування."""
    app = QApplication.instance()
    if app and app.activeWindow():
        from client.main import HealthClient
        if isinstance(app.activeWindow(), HealthClient):
            return app.activeWindow()
    return None


def to_iso(value):
    """
    Конвертує datetime → ISO 8601 з timezone для FastAPI.

    Формати:
    - datetime → '2025-11-19T22:19:27+00:00'
    - None → None
    - інше → повертає як є
    """
    if isinstance(value, datetime):
        # Додаємо UTC timezone якщо його немає
        if value.tzinfo is None:
            value = value.replace(tzinfo=timezone.utc)
        return value.isoformat()
    return value


def serialize_json_fields(value):
    """
    Серіалізує JSON поля (raw_features, lora_weights).
    SQLite зберігає їх як TEXT, потрібно розпарсити.
    """
    if value is None:
        return None

    if isinstance(value, str):
        try:
            return json.loads(value)
        except:
            return value

    return value


def sync_to_server(jwt: str, url: str = "http://127.0.0.1:8000/sync/", delete_after_sync: bool = False) -> str:
    """
    Синхронізує локальні дані сенсорів з сервером.

    Args:
        jwt: JWT токен авторизації
        url: URL endpoint синхронізації
        delete_after_sync: Видалити дані після успішної синхронізації (тільки для PREMIUM)

    Returns:
        str: Повідомлення про результат синхронізації
    """

    headers = {
        "Authorization": f"Bearer {jwt}",
        "Content-Type": "application/json"
    }

    win = _get_window()

    with get_db() as db:
        try:
            # ========================================
            # 1. ОТРИМАННЯ ЛОКАЛЬНИХ ДАНИХ
            # ========================================
            vectors = db.query(SensorVector).all()

            if not vectors:
                msg = "Немає даних для відправки"
                if win:
                    win.log(msg, "WARN")
                return msg

            # ========================================
            # 2. ПІДГОТОВКА ДАНИХ
            # ========================================
            data = []
            device_info = {}  # ✅ ДОДАНО: Інформація про пристрій

            for v in vectors:
                row = {}

                for c in v.__table__.columns:
                    value = getattr(v, c.name)

                    # Спеціальна обробка різних типів
                    if c.name in ["timestamp", "created_at"]:
                        # datetime → ISO string з timezone
                        row[c.name] = to_iso(value)

                    elif c.name in ["raw_features", "lora_weights"]:
                        # JSON поля (можуть бути строками в SQLite)
                        row[c.name] = serialize_json_fields(value)

                    else:
                        # Інші поля як є
                        row[c.name] = value

                data.append(row)

            # ✅ ДОДАНО: Отримуємо інформацію про пристрій
            if vectors:
                first_vector = vectors[0]
                device_id = first_vector.device_id

                # Шукаємо Device в локальній БД
                from client.local_models import Device
                device = db.query(Device).filter(Device.id == device_id).first()

                if device:
                    device_info = {
                        "device_id": device.id,
                        "device_name": device.device_name,
                        "device_type": device.device_type,
                        "device_id_str": device.device_id
                    }

            # ========================================
            # 3. ЛОГУВАННЯ
            # ========================================
            if win:
                win.log(f"📤 Відправка {len(data)} записів на сервер...", "SYNC")

                # Показуємо приклад даних (перший запис)
                if data:
                    preview = json.dumps(data[0], ensure_ascii=False, indent=2)
                    if len(preview) > 800:
                        preview = preview[:800] + "\n... (скорочено)"
                    win.log(f"Приклад даних:\n{preview}", "DATA")

            # ========================================
            # 4. ВІДПРАВКА НА СЕРВЕР
            # ========================================
            try:
                # ✅ ДОДАНО: Відправляємо дані + інформацію про пристрій
                payload = {
                    "vectors": data,
                    "device_info": device_info
                }

                # Використовуємо json= щоб FastAPI автоматично розпарсив
                response = requests.post(
                    url,
                    json=payload,
                    headers=headers,
                    timeout=30  # Збільшено для великих обсягів
                )

                # ========================================
                # 5. ОБРОБКА ВІДПОВІДІ
                # ========================================
                if response.status_code == 200:
                    resp_data = response.json()

                    inserted = resp_data.get("inserted", 0)
                    skipped = resp_data.get("skipped", 0)
                    total = resp_data.get("total", 0)
                    errors = resp_data.get("errors", [])
                    retention_days = resp_data.get("retention_days", "?")

                    # Формуємо результат
                    result_parts = [
                        f"✅ Синхронізація завершена:",
                        f"   Вставлено: {inserted}/{total}",
                    ]

                    if skipped > 0:
                        result_parts.append(f"   Пропущено: {skipped} (застарілі дані)")

                    result_parts.append(f"   Retention: {retention_days} днів")

                    result = "\n".join(result_parts)

                    if win:
                        win.log(result, "SUCCESS")

                        # Показуємо помилки якщо є
                        if errors:
                            win.log(f"⚠ Помилки ({len(errors)}):", "WARN")
                            for err in errors[:5]:  # Перші 5
                                win.log(f"   - {err}", "ERROR")
                            if len(errors) > 5:
                                win.log(f"   ... і ще {len(errors) - 5}", "ERROR")

                    # ❗ ЛОКАЛЬНІ ДАНІ
                    if delete_after_sync and inserted > 0:
                        # ✅ ВИДАЛЯЄМО тільки синхронізовані записи (для PREMIUM)
                        try:
                            synced_ids = [row['id'] for row in data]
                            db.query(SensorVector).filter(
                                SensorVector.id.in_(synced_ids)
                            ).delete(synchronize_session=False)
                            db.commit()

                            if win:
                                win.log(f"🗑 Видалено {len(synced_ids)} синхронізованих записів", "INFO")
                        except Exception as del_err:
                            if win:
                                win.log(f"⚠ Помилка видалення: {del_err}", "WARN")
                    else:
                        # FREE: зберігаємо 60 днів (cleanup_old_data)
                        # PREMIUM: можна зберігати для ML аналізу
                        pass

                    return f"Синхронізовано {inserted}/{total} записів"

                else:
                    # Помилка сервера
                    error = f"❌ Помилка сервера: {response.status_code}"
                    try:
                        error_detail = response.json().get("detail", response.text)
                        error += f"\n{error_detail}"
                    except:
                        error += f"\n{response.text[:200]}"

                    if win:
                        win.log(error, "ERROR")

                    return error

            except requests.exceptions.Timeout:
                error = "❌ Timeout: Сервер не відповідає (>30 сек)"
                if win:
                    win.log(error, "ERROR")
                return error

            except requests.exceptions.ConnectionError:
                error = "❌ Помилка з'єднання: Сервер недоступний"
                if win:
                    win.log(error, "ERROR")
                return error

        except Exception as e:
            error = f"❌ Виняток: {str(e)}"
            if win:
                win.log(error, "ERROR")
            return error


def auto_sync_if_premium(jwt: str, user_type: str, url: str = "http://127.0.0.1:8000/sync/"):
    """
    Автоматична синхронізація для PREMIUM користувачів.
    Викликається таймером кожні N хвилин.
    """
    if user_type != "premium":
        return

    win = _get_window()
    if win:
        win.log("🔄 Автосинхронізація (PREMIUM)...", "SYNC")

    result = sync_to_server(jwt, url)

    if win and "Синхронізовано" in result:
        win.log(result, "SUCCESS")