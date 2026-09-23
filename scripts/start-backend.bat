@echo off
chcp 65001 >nul
title 固定资产管理系统 - 后端服务
cd /d "%~dp0..\backend"
echo ========================================
echo   正在启动后端服务...
echo   端口: 8080
echo ========================================
echo.
echo 方式一: Maven 直接启动
echo 请确保已安装 Maven 3.6+ 和 JDK 17+
echo.
echo 首次启动会自动编译，耗时可能较长
echo.
mvn spring-boot:run
pause
