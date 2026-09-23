@echo off
chcp 65001 >nul
echo ========================================
echo   固定资产管理系统 - 数据库初始化
echo ========================================
echo.
echo 请在执行前确认:
echo  1. MySQL 服务已启动
echo  2. MySQL root 密码为 123456
echo  3. 如果密码不同，请手动执行:
echo     mysql -uroot -p < backend\src\main\resources\sql\init.sql
echo.
pause
echo.
echo 正在初始化数据库...
mysql -uroot -p123456 < "%~dp0..\backend\src\main\resources\sql\init.sql"
if %errorlevel% equ 0 (
    echo.
    echo [成功] 数据库初始化完成
) else (
    echo.
    echo [失败] 数据库初始化失败，请检查 MySQL 连接配置
)
echo.
pause
