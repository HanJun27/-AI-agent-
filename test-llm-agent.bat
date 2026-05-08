@echo off
chcp 65001 >nul
echo ========================================
echo   LLM Agent 功能测试
echo ========================================
echo.

set BASE_URL=http://localhost:8080/chat/agent
set PROVIDER=zhipu

echo [测试1] 修改学生电话
curl -X POST %BASE_URL% ^
  -H "Content-Type: application/json" ^
  -d "{\"message\":\"把学生李四的电话改为15862840271\",\"provider\":\"%PROVIDER%\"}"
echo.
echo.

timeout /t 3 /nobreak >nul

echo [测试2] 添加学生
curl -X POST %BASE_URL% ^
  -H "Content-Type: application/json" ^
  -d "{\"message\":\"添加学生张三，学号S2024001，年龄20岁\",\"provider\":\"%PROVIDER%\"}"
echo.
echo.

timeout /t 3 /nobreak >nul

echo [测试3] 查询学生
curl -X POST %BASE_URL% ^
  -H "Content-Type: application/json" ^
  -d "{\"message\":\"查询所有学生\",\"provider\":\"%PROVIDER%\"}"
echo.
echo.

timeout /t 3 /nobreak >nul

echo [测试4] 普通对话
curl -X POST %BASE_URL% ^
  -H "Content-Type: application/json" ^
  -d "{\"message\":\"你好\",\"provider\":\"%PROVIDER%\"}"
echo.
echo.

echo ========================================
echo   测试完成
echo ========================================
pause
