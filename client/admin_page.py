from PyQt6.QtWidgets import QWidget, QVBoxLayout, QLabel, QLineEdit, QPushButton
import requests

class UserAdminPage(QWidget):
    def __init__(self, jwt: str, base_url: str):
        super().__init__()
        self.jwt = jwt
        self.base_url = base_url

        layout = QVBoxLayout()

        self.label = QLabel("Введите username для изменения статуса:")
        layout.addWidget(self.label)

        self.username_input = QLineEdit()
        layout.addWidget(self.username_input)

        self.btn_premium = QPushButton("Сделать PREMIUM")
        self.btn_premium.clicked.connect(self.make_premium)
        layout.addWidget(self.btn_premium)

        self.result_label = QLabel("")
        layout.addWidget(self.result_label)

        self.setLayout(layout)

    def make_premium(self):
        username = self.username_input.text().strip()
        if not username:
            self.result_label.setText("Введите имя пользователя")
            return

        headers = {"Authorization": f"Bearer {self.jwt}", "Content-Type": "application/json"}
        data = {"user_type": "premium"}

        try:
            r = requests.patch(f"{self.base_url}/auth/me", json=data, headers=headers, timeout=10)
            if r.status_code == 200:
                self.result_label.setText(f"Пользователь {username} теперь PREMIUM")
            else:
                self.result_label.setText(f"Ошибка: {r.status_code} {r.text}")
        except Exception as e:
            self.result_label.setText(f"Ошибка запроса: {e}")