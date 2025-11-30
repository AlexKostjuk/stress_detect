# server/auth.py
from fastapi import APIRouter, Depends, HTTPException
from fastapi.security import HTTPBearer
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select, update
from pydantic import BaseModel
from datetime import datetime, timedelta
import jwt
import os
from dotenv import load_dotenv
from database import get_db
from models import User
from passlib.context import CryptContext

# === Загрузка переменных окружения ===
load_dotenv()

SECRET_KEY = os.getenv("SECRET_KEY")
ALGORITHM = os.getenv("ALGORITHM", "HS256")
ACCESS_TOKEN_EXPIRE_MINUTES = int(os.getenv("ACCESS_TOKEN_EXPIRE_MINUTES", 60 * 24))

# === Инициализация роутера ===
router = APIRouter()
security = HTTPBearer()

# === Ленивый pwd_context (argon2) ===
_pwd_context = None

def get_pwd_context():
    global _pwd_context
    if _pwd_context is None:
        _pwd_context = CryptContext(
            schemes=["argon2"],
            deprecated="auto",
            argon2__memory_cost=65536,
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

class UserLogin(BaseModel):
    username: str
    password: str

class UserUpdate(BaseModel):
    user_type: str

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
    # Проверка дубликата
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

    # НИЧЕГО НЕ ДЕЛАЕМ С user_retention — триггер сам создаст!

    # JWT
    access_token = create_access_token({"sub": new_user.username})
    return {"access_token": access_token, "token_type": "bearer"}


# === Логин ===
@router.post("/login", response_model=Token)
async def login(user_data: UserLogin, db: AsyncSession = Depends(get_db)):
    result = await db.execute(select(User).where(User.username == user_data.username))
    user = result.scalars().first()
    if not user:
        raise HTTPException(status_code=401, detail="Invalid credentials")

    pwd_context = get_pwd_context()
    if not pwd_context.verify(user_data.password, user.hashed_password):
        raise HTTPException(status_code=401, detail="Invalid credentials")

    access_token = create_access_token({"sub": user.username})
    return {"access_token": access_token, "token_type": "bearer"}


# === Получить текущего пользователя ===
@router.get("/me")
async def get_me(username: str = Depends(verify_token), db: AsyncSession = Depends(get_db)):
    result = await db.execute(select(User).where(User.username == username))
    user = result.scalars().first()
    if not user:
        raise HTTPException(status_code=404, detail="User not found")
    return {
        "id": user.id,
        "username": user.username,
        "email": user.email,
        "user_type": user.user_type
    }


# === Обновить себя (user_type) ===
@router.patch("/me")
async def update_me(
    update_data: UserUpdate,
    username: str = Depends(verify_token),
    db: AsyncSession = Depends(get_db)
):
    result = await db.execute(select(User).where(User.username == username))
    user = result.scalars().first()
    if not user:
        raise HTTPException(status_code=404, detail="User not found")

    if update_data.user_type not in ["free", "premium"]:
        raise HTTPException(status_code=400, detail="Invalid user_type")

    user.user_type = update_data.user_type
    db.add(user)
    await db.commit()

    # Триггер trigger_update_retention САМ обновит retention_days!

    return {
        "id": user.id,
        "username": user.username,
        "user_type": user.user_type
    }