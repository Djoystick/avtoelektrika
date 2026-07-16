@echo off
echo =======================================
echo Starting Drive2 Spider Parser
echo =======================================
cd /d "%~dp0"
call venv\Scripts\activate.bat
python main.py
pause
