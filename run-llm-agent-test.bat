@echo off
chcp 65001 >nul
echo ========================================
echo   LLM Agent 自动化测试
echo ========================================
echo.

cd backend

echo [步骤1] 编译测试代码...
call mvn test-compile -q
if errorlevel 1 (
    echo ❌ 编译失败
    pause
    exit /b 1
)
echo ✅ 编译成功
echo.

echo [步骤2] 运行LLM Agent测试...
echo.
call mvn test -Dtest=LLMAgentIntegrationTest

echo.
echo ========================================
echo   测试完成
echo ========================================
pause
