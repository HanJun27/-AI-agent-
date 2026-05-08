@echo off
chcp 65001 >nul
echo ========================================
echo    LLM语义识别专项测试
echo ========================================
echo.

cd /d "%~dp0backend"

echo [提示] 此测试需要配置真实的API密钥
echo [说明] 请在 AgentLLMRecognitionTest.java 中填入API_KEY
echo.

echo [1/3] 清理编译...
call mvn clean compile -q

if %ERRORLEVEL% NEQ 0 (
    echo ❌ 编译失败
    pause
    exit /b 1
)

echo ✅ 编译成功
echo.

echo [2/3] 运行LLM测试...
echo.
call mvn test -Dtest=AgentLLMRecognitionTest

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ⚠️  部分测试失败（可能是API未配置或网络问题）
    echo 💡 请检查：
    echo    1. API_KEY是否正确配置
    echo    2. 网络连接是否正常
    echo    3. API配额是否充足
    pause
    exit /b 0
)

echo.
echo ========================================
echo ✅ LLM测试完成！
echo 📊 数据库内容已自动回滚，保持原样
echo ========================================
echo.
pause
