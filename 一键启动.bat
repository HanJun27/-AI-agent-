@echo off
chcp 65001 >nul
title 紫金学院管理系统 - 一键启动
color 0A

echo.
echo ================================================
echo          紫金学院管理系统 - 一键启动
echo ================================================
echo.

REM ============================================
REM 环境检查
REM ============================================
echo [1/5] 检查运行环境...
echo.

REM 检查Java
java -version >nul 2>&1
if errorlevel 1 (
    echo   [错误] 未检测到Java，请先安装JDK 1.8或以上版本
    pause
    exit /b 1
)
echo   [√] Java环境正常

REM 检查Node.js
node -v >nul 2>&1
if errorlevel 1 (
    echo   [错误] 未检测到Node.js，请先安装Node.js 16或以上版本
    pause
    exit /b 1
)
echo   [√] Node.js环境正常

REM 检查Maven
where mvn >nul 2>&1
if errorlevel 1 (
    echo   [警告] Maven未安装，将尝试使用其他方式
    set MAVEN_AVAILABLE=0
) else (
    echo   [√] Maven环境正常
    set MAVEN_AVAILABLE=1
)
echo.

REM ============================================
REM MySQL服务检查
REM ============================================
echo [2/5] 检查MySQL服务...
sc query MySQL80 | find "RUNNING" >nul
if errorlevel 1 (
    echo   [!] MySQL服务未运行，正在启动...
    net start MySQL80 >nul 2>&1
    if errorlevel 1 (
        echo   [错误] 无法启动MySQL服务，请以管理员身份运行此脚本
        pause
        exit /b 1
    )
    echo   [√] MySQL服务已启动
) else (
    echo   [√] MySQL服务正在运行
)
echo.

REM ============================================
REM 数据库检查
REM ============================================
echo [3/5] 检查数据库...
mysql -u root -proot -e "USE zijin_college" >nul 2>&1
if errorlevel 1 (
    echo   [!] 数据库可能未初始化
    echo   请手动执行: Get-Content database\schema.sql ^| mysql -u root -p
) else (
    echo   [√] 数据库已就绪
)
echo.

REM ============================================
REM 检查后端依赖
REM ============================================
echo [4/5] 准备后端服务...
if not exist "backend\target\college-system-1.0.0.jar" (
    echo   [!] 检测到首次运行，需要下载Maven依赖
    echo   这可能需要几分钟时间，请耐心等待...
)
echo.

REM ============================================
REM 启动后端
REM ============================================
echo [5/5] 启动服务...
echo.
echo   正在启动后端服务（Spring Boot）...
start "后端服务-SpringBoot" cmd /k "cd backend && mvn spring-boot:run"
echo   [√] 后端服务窗口已打开
timeout /t 3 /nobreak >nul
echo.

REM 等待后端启动
echo   等待后端服务启动（约10-30秒）...
timeout /t 10 /nobreak >nul

REM ============================================
REM 启动前端
REM ============================================
echo   正在启动前端服务（Vue3 + Vite）...
if not exist "frontend\node_modules" (
    echo   [!] 检测到前端依赖未安装，正在安装...
    start "安装前端依赖" cmd /k "cd frontend && npm install"
    echo   请等待依赖安装完成后再手动启动前端
    echo   命令: cd frontend ^&^& npm run dev
) else (
    start "前端服务-Vue3" cmd /k "cd frontend && npm run dev"
    echo   [√] 前端服务窗口已打开
)
echo.

REM ============================================
REM 启动完成
REM ============================================
timeout /t 3 /nobreak >nul
echo.
echo ================================================
echo              启动完成！
echo ================================================
echo.
echo   后端服务: http://localhost:8080
echo   前端服务: http://localhost:3000
echo.
echo   默认登录账号:
echo     用户名: admin
echo     密码:   123456
echo.
echo   提示:
echo   1. 请等待两个服务都完全启动后再访问
echo   2. 后端首次启动需要下载依赖，可能需要几分钟
echo   3. 查看服务状态请留意打开的两个命令行窗口
echo   4. 按 Ctrl+C 可以停止服务
echo.
echo ================================================
echo.

REM 自动打开浏览器
timeout /t 5 /nobreak >nul
start http://localhost:3000

pause
