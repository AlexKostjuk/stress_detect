# server/auth.py
from fastapi import APIRouter, Depends, HTTPException
from fastapi.security import HTTPBearer
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select
from pydantic import BaseModel
from datetime import datetime, timedelta
import jwt
import os
from dotenv import load_dotenv
from database import get_db
from models import User, UserRetention
from passlib.context import CryptContext

# === Загрузка переменных окружения ===
load_dotenv()

SECRET_KEY = os.getenv("SECRET_KEY")
ALGORITHM = os.getenv("ALGORITHM", "HS256")
ACCESS_TOKEN_EXPIRE_MINUTES = int(os.getenv("ACCESS_TOKEN_EXPIRE_MINUTES", 60 * 24))

# === Инициализация роутера ===
router = APIRouter()
security = HTTPBearer()

# === ОТЛОЖЕННАЯ ИНИЦИАЛИЗАЦИЯ pwd_context (argon2) ===
_pwd_context = None

def get_pwd_context():
    """Ленивая инициализация CryptContext с argon2"""
    global _pwd_context
    if _pwd_context is None:
        from passlib.context import CryptContext
        _pwd_context = CryptContext(
            schemes=["argon2"],
            deprecated="auto",
            argon2__memory_cost=65536,      # 64 MB
            argon2__time_cost=3,
            argon2__parallelism=4
        )
    return _pwd_context


# === Pydantic схемы ===
class UserCreate(BaseModel):
    username: str
    email: str
    password: str
    user_type: str = "free"

class Token(BaseModel):
    access_token: str
    token_type: str


# === JWT ===
def create_access_token(data: dict):
    to_encode = data.copy()
    expire = datetime.utcnow() + timedelta(minutes=ACCESS_TOKEN_EXPIRE_MINUTES)
    to_encode.update({"exp": expire})
    return jwt.encode(to_encode, SECRET_KEY, algorithm=ALGORITHM)


async def verify_token(credentials: HTTPBearer = Depends(security)):
    try:
        payload = jwt.decode(credentials.credentials, SECRET_KEY, algorithms=[ALGORITHM])
        username: str = payload.get("sub")
        if not username:
            raise HTTPException(status_code=401, detail="Invalid token")
        return username
    except jwt.PyJWTError:
        raise HTTPException(status_code=401, detail="Invalid token")
# === Регистрация ===
@router.post("/register", response_model=Token)
async def register(user_data: UserCreate, db: AsyncSession = Depends(get_db)):
    # Проверка на дубликат
    result = await db.execute(select(User).where(User.username == user_data.username))
    if result.scalars().first():
        raise HTTPException(status_code=400, detail="Username already exists")

    # Хешируем пароль
    pwd_context = get_pwd_context()
    hashed_password = pwd_context.hash(user_data.password)

    # Создаём пользователя
    new_user = User(
        username=user_data.username,
        email=user_data.email,
        hashed_password=hashed_password,
        user_type=user_data.user_type
    )
    db.add(new_user)
    await db.commit()
    await db.refresh(new_user)

    # ← ДОБАВЛЯЕМ user_retention ПОСЛЕ commit()!
    retention = UserRetention(
        user_id=new_user.id,
        retention_days=365 if user_data.user_type == "premium" else 60
    )
    db.add(retention)  # ← ЭТО БЫЛО ПРОПУЩЕНО!
    await db.commit()  # ← Второй commit

    # JWT
    access_token = create_access_token({"sub": new_user.username})
    return {"access_token": access_token, "token_type": "bearer"}
#

# === Логин ===
@router.post("/login", response_model=Token)
async def login(username: str, password: str, db: AsyncSession = Depends(get_db)):
    result = await db.execute(select(User).where(User.username == username))
    user = result.scalars().first()
    if not user:
        raise HTTPException(status_code=401, detail="Invalid credentials")

    pwd_context = get_pwd_context()
    if not pwd_context.verify(password, user.hashed_password):
        raise HTTPException(status_code=401, detail="Invalid credentials")

    access_token = create_access_token({"sub": user.username})
    return {"access_token": access_token, "token_type": "bearer"}