@echo off
chcp 65001 >nul
echo ========================================
echo    Agent 自动化测试运行脚本
echo ========================================
echo.

cd /d "%~dp0backend"

echo [1/3] 清理编译...
call mvn clean compile -q

if %ERRORLEVEL% NEQ 0 (
    echo ❌ 编译失败
    pause
    exit /b 1
)

echo ✅ 编译成功
echo.

echo [2/3] 运行测试...
echo.
call mvn test -Dtest=AgentIntentRecognitionTest

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ❌ 测试失败，请查看上方错误信息
    pause
    exit /b 1
)

echo.
echo ========================================
echo ✅ 所有测试完成！
echo 📊 数据库内容已自动回滚，保持原样
echo ========================================
echo.
pause
