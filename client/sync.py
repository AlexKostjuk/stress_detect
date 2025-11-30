import requests
import json
from datetime import datetime
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


def to_iso(value):
    """Конвертация datetime → ISO 8601 для FastAPI"""
    if isinstance(value, datetime):
        return value.isoformat()
    return value


def sync_to_server(jwt: str, url: str = "http://127.0.0.1:8000/sync/"):
    headers = {
        "Authorization": f"Bearer {jwt}",
        "Content-Type": "application/json"
    }

    win = _get_window()

    with get_db() as db:
        try:
            vectors = db.query(SensorVector).all()
            if not vectors:
                return "Нет данных для отправки"

            # Подготовка данных
            data = []
            for v in vectors:
                row = {}
                for c in v.__table__.columns:
                    value = getattr(v, c.name)
                    row[c.name] = to_iso(value)
                data.append(row)

            # Логирование
            if win:
                win.log(f"Отправка {len(data)} записей...", "DATA")
                preview = json.dumps(data, ensure_ascii=False, indent=2)
                if len(preview) > 1500:
                    preview = preview[:1500] + "\n... (ещё данные)"
                win.log(preview, "DATA")

            # Важно: отправляем через json=
            r = requests.post(url, json=data, headers=headers, timeout=20)

            if r.status_code == 200:
                resp = r.json()
                count = resp.get("count", 0)
                errors = resp.get("errors", [])

                result = f"Успешно синхронизировано {count} записей"
                if errors:
                    result += f" (ошибки: {len(errors)})"

                if win:
                    win.log(result, "SUCCESS")

                # ❗ ЛОКАЛЬНЫЕ ДАННЫЕ НЕ УДАЛЯЕМ
                # они должны храниться 30 дней для ML

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


# import requests
# import json
# from PyQt6.QtWidgets import QApplication
# from client.local_db import get_db
# from client.local_models import SensorVector
#
# def _get_window():
#     app = QApplication.instance()
#     if app and app.activeWindow():
#         from client.main import HealthClient
#         if isinstance(app.activeWindow(), HealthClient):
#             return app.activeWindow()
#     return None
#
# def sync_to_server(jwt: str, url: str = "http://127.0.0.1:8000/sync/"):
#     headers = {
#         "Authorization": f"Bearer {jwt}",
#         "Content-Type": "application/json"
#     }
#
#     with get_db() as db:
#         try:
#             vectors = db.query(SensorVector).all()
#             if not vectors:
#                 return "Нет данных для отправки"
#
#             data = []
#             for v in vectors:
#                 item = {c.name: getattr(v, c.name) for c in v.__table__.columns}
#                 data.append(item)
#
#             win = _get_window()
#             if win:
#                 win.log(f"Отправка {len(data)} записей...", "DATA")
#                 preview = json.dumps(data, default=str, ensure_ascii=False, indent=2)
#                 if len(preview) > 1500:
#                     preview = preview[:1500] + "\n... (ещё данные)"
#                 win.log(preview, "DATA")
#
#             # ✅ сериализуем вручную, чтобы datetime не ломал JSON
#             payload = json.dumps(data, default=str)
#             r = requests.post(url, data=payload, headers=headers, timeout=15)
#
#             if r.status_code == 200:
#                 count = r.json().get("count", len(data))
#                 result = f"Успешно синхронизировано {count} записей"
#                 if win:
#                     win.log(result, "SUCCESS")
#                 return result
#             else:
#                 error = f"Ошибка сервера: {r.status_code} {r.text}"
#                 if win:
#                     win.log(error, "ERROR")
#                 return error
#
#         except Exception as e:
#             win = _get_window()
#             if win:
#                 win.log(f"Исключение: {e}", "ERROR")
#             return f"Ошибка: {e}"