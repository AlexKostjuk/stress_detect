@echo off
echo.
echo ====================================
echo   СОЗДАНИЕ EXE — HealthMonitor.exe
echo ====================================
echo.

pip install -r requirements.txt

cd client

pyinstaller --onefile ^
    --windowed ^
    --name HealthMonitor ^
    main.py

echo.
echo ГОТОВО! Файл: dist\HealthMonitor.exe
echo.
pause