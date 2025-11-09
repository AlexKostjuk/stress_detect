# client/sync.py
import requests
import json
from PyQt6.QtWidgets import QApplication
from client.local_db import get_db
from client.local_models import SensorVector

def _get_window():
    app = QApplication.instance()
    if app and app.activeWindow():
        from client.main import HealthClient
        if isinstance(app.activeWindow(), HealthClient):
            return app.activeWindow()
    return None

def sync_to_server(jwt: str, url: str = "http://host.docker.internal:8000/sync/"):
    headers = {"Authorization": f"Bearer {jwt}"}
    db = next(get_db())
    try:
        vectors = db.query(SensorVector).all()
        if not vectors:
            return "Нет данных для отправки"

        data = []
        for v in vectors:
            item = {c.name: getattr(v, c.name) for c in v.__table__.columns}
            if item.get("timestamp"):
                item["timestamp"] = item["timestamp"].isoformat()
            if item.get("created_at"):
                item["created_at"] = item["created_at"].isoformat()
            data.append(item)

        # Показываем в терминале
        win = _get_window()
        if win:
            win.log(f"Отправка {len(data)} записей...", "DATA")
            preview = json.dumps(data, ensure_ascii=False, indent=2)
            if len(preview) > 1500:
                preview = preview[:1500] + "\n... (ещё данные)"
            win.log(preview, "DATA")

        r = requests.post(url, json=data, headers=headers, timeout=15)
        if r.status_code == 200:
            count = r.json().get("count", len(data))
            db.query(SensorVector).delete()
            db.commit()
            result = f"Успешно синхронизировано {count} записей"
            if win:
                win.log(result, "SUCCESS")
            return result
        else:
            error = f"Ошибка сервера: {r.status_code} {r.text}"
            if win:
                win.log(error, "ERROR")
            return error
    except Exception as e:
        if win:
            win.log(f"Исключение: {e}", "ERROR")
        return f"Ошибка: {e}"
    finally:
        db.close()