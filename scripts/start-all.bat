@echo off
chcp 65001 >nul
title 固定资产管理系统
echo ========================================
echo   固定资产管理系统 - 一键启动
echo ========================================
echo.
echo 请确保:
echo  1. MySQL 8 已启动
echo  2. 数据库已初始化 (执行 init-db.bat)
echo  3. 前端依赖已安装 (执行 npm install)
echo.
echo 按任意键启动全部服务...
pause >nul

echo.
echo [1/2] 启动后端服务 (端口 8080)...
start "Backend" cmd /c "cd /d "%~dp0..\backend" && mvn spring-boot:run"
echo.
echo 等待后端启动 (约10秒)...
ping -n 10 127.0.0.1 >nul

echo [2/2] 启动前端服务 (端口 3000)...
start "Frontend" cmd /c "cd /d "%~dp0..\frontend" && npm run dev"

echo.
echo ========================================
echo   服务启动完成!
echo.
echo   后端地址: http://localhost:8080
echo   前端地址: http://localhost:3000
echo   默认账号: admin / 123456
echo.
echo   请在新打开的窗口中查看启动状态
echo   如遇端口占用请关闭其他服务后重试
echo ========================================
pause
