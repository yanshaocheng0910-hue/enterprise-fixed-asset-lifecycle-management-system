@echo off
chcp 65001 >nul
title 固定资产管理系统 - 前端服务
cd /d "%~dp0..\frontend"
echo ========================================
echo   正在启动前端服务...
echo   端口: 3000
echo ========================================
echo.
echo 确保已执行过 npm install
echo 首次启动请先执行: npm install
echo.
npm run dev
pause
