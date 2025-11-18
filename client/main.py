# client/main.py
import sys
import jwt
import requests
from datetime import datetime
from random import randint, uniform

from PyQt6.QtWidgets import (
    QApplication, QMainWindow, QLabel, QLineEdit, QPushButton,
    QVBoxLayout, QWidget, QTabWidget, QTextEdit, QMessageBox, QFrame
)
from PyQt6.QtCore import QTimer, Qt
from PyQt6.QtGui import QColor, QTextCursor

from client.local_db import init_db, get_db
from client.local_models import User, Device, SensorVector
from client.cleanup import cleanup_old_data
from client.sync import sync_to_server
from PyQt6.QtCore import QTimer
import json, os
from client.local_db import get_db
import os
from PyQt6.QtWidgets import QStackedWidget
from client.admin_page import UserAdminPage



class HealthClient(QMainWindow):
    def __init__(self):
        super().__init__()

        self.setWindowTitle("Health Monitor Pro")
        self.setGeometry(100, 100, 640, 760)

        self.jwt = None
        self.username = None
        self.user_id = None
        self.device_id = None
        self.user_type = "free"
        self.is_collecting = False

        self.BASE_URL = "http://host.docker.internal:8000"

        init_db()
        self.setup_ui()
        self.start_cleanup_timer()

    def add_admin_page(self):
        """Функция для подключения скрытой админской страницы"""
        self.admin_page = UserAdminPage(self.jwt, self.BASE_URL)
        self.tabs.addTab(self.admin_page, "Админ")
        self.log("Админская вкладка подключена (DEBUG mode)", "INFO")

    def setup_ui(self):
        self.tabs = QTabWidget()
        self.tabs.addTab(self.auth_tab(), "Авторизация")
        self.tabs.addTab(self.monitor_tab(), "Мониторинг")
        self.tabs.addTab(self.terminal_tab(), "Терминал")

        container = QWidget()
        layout = QVBoxLayout()
        layout.addWidget(self.tabs)
        container.setLayout(layout)
        self.setCentralWidget(container)

        # tabs = QTabWidget()
        # tabs.addTab(self.auth_tab(), "Авторизация")
        # tabs.addTab(self.monitor_tab(), "Мониторинг")
        # tabs.addTab(self.terminal_tab(), "Терминал")
        #
        # container = QWidget()
        # layout = QVBoxLayout()
        # layout.addWidget(tabs)
        # container.setLayout(layout)
        # self.setCentralWidget(container)

    def auth_tab(self):
        w = QWidget()
        l = QVBoxLayout()

        reg_frame = QFrame(); reg_frame.setFrameShape(QFrame.Shape.StyledPanel)
        reg_l = QVBoxLayout()
        self.reg_user = QLineEdit(); self.reg_user.setPlaceholderText("Логин")
        self.reg_email = QLineEdit(); self.reg_email.setPlaceholderText("Email")
        self.reg_pass = QLineEdit(); self.reg_pass.setPlaceholderText("Пароль")
        self.reg_pass.setEchoMode(QLineEdit.EchoMode.Password)
        reg_btn = QPushButton("Зарегистрироваться"); reg_btn.clicked.connect(self.register)
        reg_l.addWidget(QLabel("Регистрация"))
        reg_l.addWidget(self.reg_user); reg_l.addWidget(self.reg_email)
        reg_l.addWidget(self.reg_pass); reg_l.addWidget(reg_btn)
        reg_frame.setLayout(reg_l)

        login_frame = QFrame(); login_frame.setFrameShape(QFrame.Shape.StyledPanel)
        login_l = QVBoxLayout()
        self.login_user = QLineEdit(); self.login_user.setPlaceholderText("Логин")
        self.login_pass = QLineEdit(); self.login_pass.setPlaceholderText("Пароль")
        self.login_pass.setEchoMode(QLineEdit.EchoMode.Password)
        login_btn = QPushButton("Войти"); login_btn.clicked.connect(self.login)
        login_l.addWidget(QLabel("Вход"))
        login_l.addWidget(self.login_user); login_l.addWidget(self.login_pass)
        login_l.addWidget(login_btn)
        login_frame.setLayout(login_l)

        l.addWidget(reg_frame); l.addWidget(login_frame)
        w.setLayout(l)
        return w

    def monitor_tab(self):
        w = QWidget()
        l = QVBoxLayout()

        self.status = QLabel("Ожидание входа...")
        self.status.setAlignment(Qt.AlignmentFlag.AlignCenter)
        self.status.setStyleSheet("font-size: 16px; font-weight: bold;")

        self.premium_label = QLabel("Тип: FREE")
        self.premium_label.setAlignment(Qt.AlignmentFlag.AlignCenter)
        self.premium_label.setStyleSheet("color: orange;")

        self.start_stop_btn = QPushButton("Старт сбора")
        self.start_stop_btn.clicked.connect(self.toggle_collection)
        self.start_stop_btn.setEnabled(False)

        self.sync_btn = QPushButton("Синхронизировать")
        self.sync_btn.clicked.connect(self.sync)
        self.sync_btn.setEnabled(False)

        self.logout_btn = QPushButton("Логаут")
        self.logout_btn.clicked.connect(self.logout)
        self.logout_btn.setEnabled(False)

        self.local_count = QLabel("Локально: 0 записей")
        self.local_count.setAlignment(Qt.AlignmentFlag.AlignCenter)

        l.addWidget(self.status)
        l.addWidget(self.premium_label)
        l.addWidget(self.local_count)
        l.addWidget(self.start_stop_btn)
        l.addWidget(self.sync_btn)
        l.addWidget(self.logout_btn)
        l.addStretch()
        w.setLayout(l)
        return w

    def terminal_tab(self):
        w = QWidget()
        l = QVBoxLayout()
        self.terminal = QTextEdit()
        self.terminal.setReadOnly(True)
        self.terminal.setStyleSheet("font-family: Consolas; font-size: 10pt; background: #1e1e1e; color: #d4d4d4;")
        l.addWidget(QLabel("Терминал (логи и данные)"))
        l.addWidget(self.terminal)
        w.setLayout(l)
        return w

    # def start_cleanup_timer(self):
    #     QTimer.singleShot(86400000, cleanup_old_data)

    def start_cleanup_timer(self):
        self.cleanup_timer = QTimer()
        self.cleanup_timer.timeout.connect(cleanup_old_data)
        self.cleanup_timer.start(86400000)  # каждые 24 часа

    def save_jwt(self):
        if self.jwt:
            with open("jwt_token.json", "w") as f:
                json.dump({"token": self.jwt}, f)

    def load_jwt(self):
        if os.path.exists("jwt_token.json"):
            with open("jwt_token.json") as f:
                self.jwt = json.load(f).get("token")

    def log(self, msg: str, level: str = "INFO"):
        colors = {
            "INFO": "#00ff00", "WARN": "#ffff00", "ERROR": "#ff0000",
            "SUCCESS": "#00ffff", "DATA": "#ff00ff", "SYNC": "#00bfff"
        }
        t = datetime.now().strftime("%H:%M:%S")
        html = f"<span style='color: {colors.get(level, '#d4d4d4')};'>[{t}] [{level}] {msg}</span>"
        self.terminal.append(html)
        self.terminal.moveCursor(QTextCursor.MoveOperation.End)

    # def login(self):
    #     u, p = self.login_user.text().strip(), self.login_pass.text().strip()
    #     if not u or not p:
    #         self.log("Заполните поля", "WARN")
    #         return
    #     try:
    #         r = requests.post(f"{self.BASE_URL}/auth/login", json={"username": u, "password": p}, timeout=10)
    #         if r.status_code == 200:
    #             self.jwt = r.json()["access_token"]
    #             payload = jwt.decode(self.jwt, options={"verify_signature": False})
    #             self.username = payload.get("sub")
    #             self.fetch_user_info()
    #             self.setup_local_user(u)
    #
    #             self.start_stop_btn.setEnabled(True)
    #             self.sync_btn.setEnabled(self.user_type == "premium")
    #             self.logout_btn.setEnabled(True)
    #             self.status.setText(f"Пользователь: {u}")
    #             self.log("Вход успешен", "SUCCESS")
    #             if os.getenv("DEBUG", "true").lower() == "true":
    #                 self.add_admin_page()
    #
    #         else:
    #             self.log(f"Ошибка: {r.json().get('detail')}", "ERROR")
    #     except Exception as e:
    #         self.log(f"Сервер недоступен: {e}", "ERROR")

    def login(self):
        u, p = self.login_user.text().strip(), self.login_pass.text().strip()
        if not u or not p:
            self.log("Заполните поля", "WARN")
            return
        try:
            r = requests.post(f"{self.BASE_URL}/auth/login", json={"username": u, "password": p}, timeout=10)
            if r.status_code == 200:
                self.jwt = r.json()["access_token"]
                payload = jwt.decode(self.jwt, options={"verify_signature": False})
                self.username = payload.get("sub")

                # Шаг 2: получаем полную информацию о пользователе
                user_info = self.fetch_user_info()

                # передаём её в setup_local_user
                self.setup_local_user(user_info)

                self.start_stop_btn.setEnabled(True)
                self.sync_btn.setEnabled(self.user_type == "premium")
                self.logout_btn.setEnabled(True)
                self.status.setText(f"Пользователь: {u}")
                self.log("Вход успешен", "SUCCESS")

                if os.getenv("DEBUG", "true").lower() == "true":
                    self.add_admin_page()

            else:
                self.log(f"Ошибка: {r.json().get('detail')}", "ERROR")
        except Exception as e:
            self.log(f"Сервер недоступен: {e}", "ERROR")

    def register(self):
        u, e, p = self.reg_user.text().strip(), self.reg_email.text().strip(), self.reg_pass.text().strip()
        if not all([u, e, p]):
            self.log("Заполните поля", "WARN")
            return
        try:
            r = requests.post(f"{self.BASE_URL}/auth/register", json={"username": u, "email": e, "password": p}, timeout=10)
            if r.status_code == 200:
                self.log("Аккаунт создан!", "SUCCESS")
                self.reg_user.clear(); self.reg_email.clear(); self.reg_pass.clear()
            else:
                self.log(f"Ошибка: {r.json().get('detail')}", "ERROR")
        except Exception as e:
            self.log(f"Нет связи: {e}", "ERROR")

    # def fetch_user_info(self):
    #     try:
    #         r = requests.get(f"{self.BASE_URL}/auth/me", headers={"Authorization": f"Bearer {self.jwt}"}, timeout=5)
    #         if r.status_code == 200:
    #             self.user_type = r.json().get("user_type", "free")
    #             self.premium_label.setText(f"Тип: {self.user_type.upper()}")
    #             self.premium_label.setStyleSheet("color: #00ff00; font-weight: bold;" if self.user_type == "premium" else "color: orange;")
    #         else:
    #             self.log("Не удалось получить статус", "WARN")
    #     except Exception as e:
    #         self.log(f"Ошибка премиума: {e}", "ERROR")
    def fetch_user_info(self) -> dict:
        try:
            r = requests.get(f"{self.BASE_URL}/auth/me", headers={"Authorization": f"Bearer {self.jwt}"}, timeout=5)
            if r.status_code == 200:
                data = r.json()
                self.user_type = data.get("user_type", "free")
                self.premium_label.setText(f"Тип: {self.user_type.upper()}")
                self.premium_label.setStyleSheet(
                    "color: #00ff00; font-weight: bold;" if self.user_type == "premium" else "color: orange;"
                )
                return data
            else:
                self.log("Не удалось получить статус", "WARN")
        except Exception as e:
            self.log(f"Ошибка премиума: {e}", "ERROR")
        return {}

    #
    def setup_local_user(self, user_info: dict):
        username = user_info.get("username", self.username)
        email = user_info.get("email", f"{username}@local")
        user_type = user_info.get("user_type", "free")
        subscription_end = user_info.get("subscription_end")
        is_active = user_info.get("is_active", True)

        with get_db() as db:
            try:
                # Проверяем, есть ли пользователь с таким email
                user = db.query(User).filter(User.email == email).first()

                if not user:
                    # Создаём нового локального пользователя
                    user = User(
                        username=username,
                        email=email,
                        hashed_password="***",
                        user_type=user_type,
                        subscription_end=subscription_end,
                        is_active=is_active
                    )
                    db.add(user)
                    db.commit()
                    db.refresh(user)
                    self.log(f"Создан локальный пользователь: {username}", "INFO")
                else:
                    # Обновляем статус, если он изменился
                    updated = False
                    if user.user_type != user_type:
                        user.user_type = user_type
                        updated = True
                    if user.subscription_end != subscription_end:
                        user.subscription_end = subscription_end
                        updated = True
                    if user.is_active != is_active:
                        user.is_active = is_active
                        updated = True
                    if updated:
                        db.commit()
                        self.log(f"Обновлён локальный пользователь: {username}", "INFO")

                # Проверяем наличие устройства
                device = db.query(Device).filter(Device.user_id == user.id).first()
                if not device:
                    device = Device(user_id=user.id, device_name="Local-Headset", device_id="LOCAL-001")
                    db.add(device)
                    db.commit()
                    db.refresh(device)
                    self.log(f"Создано устройство: {device.device_name}", "INFO")

                # Сохраняем ID
                self.user_id = user.id
                self.device_id = device.id
                self.log(f"Локальный ID: {user.id}, Device: {device.id}", "INFO")

            except Exception as e:
                db.rollback()
                self.log(f"БД ошибка: {e}", "ERROR")

        # db = next(get_db())
        # try:
        #     user = db.query(User).filter(User.username == username).first()
        #     if not user:
        #         user = User(username=username, email="local@temp", hashed_password="***", user_type=self.user_type)
        #         db.add(user); db.commit(); db.refresh(user)
        #     device = db.query(Device).filter(Device.user_id == user.id).first()
        #     if not device:
        #         device = Device(user_id=user.id, device_name="Local-Headset", device_id="LOCAL-001")
        #         db.add(device); db.commit(); db.refresh(device)
        #     self.user_id = user.id
        #     self.device_id = device.id
        #     self.log(f"Локальный ID: {user.id}, Device: {device.id}", "INFO")
        # except Exception as e:
        #     self.log(f"БД ошибка: {e}", "ERROR")
        # finally:
        #     db.close()

    def toggle_collection(self):
        if self.is_collecting:
            self.is_collecting = False
            self.start_stop_btn.setText("Старт сбора")
            self.log("Сбор остановлен", "WARN")
        else:
            if not all([self.jwt, self.user_id, self.device_id]):
                self.log("Сначала войдите", "WARN")
                return
            self.is_collecting = True
            self.start_stop_btn.setText("Стоп сбора")
            self.log("Сбор запущен (5 сек)", "SUCCESS")
            QTimer.singleShot(100, self.collect_data)

    def collect_data(self):
        if not self.is_collecting:
            return


        with get_db() as db:
            try:
                vector = SensorVector(
                    id=int(datetime.now().timestamp() * 1_000_000),
                    user_id=self.user_id,
                    device_id=self.device_id,
                    timestamp=datetime.utcnow(),
                    heart_rate=65 + randint(-15, 15),
                    hrv_rmssd=round(uniform(20, 80), 2),
                    spo2=randint(95, 100),
                    skin_temperature=round(uniform(36.0, 37.5), 2),
                    accel_x=round(uniform(-2, 2), 3),
                    accel_y=round(uniform(-2, 2), 3),
                    accel_z=round(uniform(-2, 2), 3),
                    steps_count=randint(0, 100),
                    noise_level_db=round(uniform(30, 80), 1),
                    stress_level=round(uniform(0.1, 0.9), 2),
                    model_version="v1.0",
                    confidence_score=round(uniform(0.7, 0.99), 2),
                    raw_features={"mock": True},
                    signal_quality=randint(70, 100)
                )
                db.add(vector)
                db.commit()
                count = db.query(SensorVector).count()
                self.local_count.setText(f"Локально: {count} записей")
                self.log(f"HR: {vector.heart_rate} | Stress: {vector.stress_level:.2f}", "INFO")
            except Exception as e:
                db.rollback()
                self.log(f"Ошибка записи: {e}", "ERROR")

        if self.is_collecting:
            QTimer.singleShot(5000, self.collect_data)

    # def collect_data(self):

        # if not self.is_collecting:
        #     return
        #
        # db = next(get_db())
        # try:
        #     vector = SensorVector(
        #         id=int(datetime.now().timestamp() * 1_000_000),
        #         user_id=self.user_id,
        #         device_id=self.device_id,
        #         timestamp=datetime.utcnow(),
        #         heart_rate=65 + randint(-15, 15),
        #         hrv_rmssd=round(uniform(20, 80), 2),
        #         spo2=randint(95, 100),
        #         skin_temperature=round(uniform(36.0, 37.5), 2),
        #         accel_x=round(uniform(-2, 2), 3),
        #         accel_y=round(uniform(-2, 2), 3),
        #         accel_z=round(uniform(-2, 2), 3),
        #         steps_count=randint(0, 100),
        #         noise_level_db=round(uniform(30, 80), 1),
        #         stress_level=round(uniform(0.1, 0.9), 2),
        #         model_version="v1.0",
        #         confidence_score=round(uniform(0.7, 0.99), 2),
        #         raw_features={"mock": True},
        #         signal_quality=randint(70, 100)
        #     )
        #     db.add(vector); db.commit()
        #     count = db.query(SensorVector).count()
        #     self.local_count.setText(f"Локально: {count} записей")
        #     self.log(f"HR: {vector.heart_rate} | Stress: {vector.stress_level:.2f}", "INFO")
        # except Exception as e:
        #     db.rollback()
        #     self.log(f"Ошибка записи: {e}", "ERROR")
        # finally:
        #     db.close()
        #     if self.is_collecting:
        #         QTimer.singleShot(5000, self.collect_data)

    # def sync(self):
    #     if not self.jwt:
    #         self.log("Войдите в аккаунт", "WARN")
    #         return
    #     if self.user_type != "premium":
    #         self.log("Только PREMIUM", "WARN")
    #         return
    #     self.log("Ручная синхронизация...", "SYNC")
    #     result = sync_to_server(self.jwt, f"{self.BASE_URL}/sync/")
    #     db = next(get_db())
    #     try:
    #         count = db.query(SensorVector).count()
    #         self.local_count.setText(f"Локально: {count} записей")
    #     finally:
    #         db.close()
    def sync(self):
        if not self.jwt:
            self.log("Войдите в аккаунт", "WARN")
            return
        if self.user_type != "premium":
            self.log("Только PREMIUM", "WARN")
            return

        self.log("Ручная синхронизация...", "SYNC")
        result = sync_to_server(self.jwt, f"{self.BASE_URL}/sync/")
        self.log(result, "INFO" if "Успешно" in result else "ERROR")

        from client.local_db import get_db
        with get_db() as db:
            count = db.query(SensorVector).count()
            self.local_count.setText(f"Локально: {count} записей")

    def logout(self):
        self.jwt = None; self.username = None; self.user_id = None; self.device_id = None
        self.user_type = "free"; self.is_collecting = False
        self.start_stop_btn.setText("Старт сбора"); self.start_stop_btn.setEnabled(False)
        self.sync_btn.setEnabled(False); self.logout_btn.setEnabled(False)
        self.status.setText("Ожидание входа..."); self.premium_label.setText("Тип: FREE")
        self.premium_label.setStyleSheet("color: orange;")
        self.log("Выход выполнен", "WARN")


if __name__ == "__main__":
    app = QApplication(sys.argv)
    win = HealthClient()
    win.show()
    sys.exit(app.exec())