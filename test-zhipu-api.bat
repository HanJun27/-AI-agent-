@echo off
chcp 65001 >nul
echo ========================================
echo   测试智谱AI API密钥
echo ========================================
echo.

set API_KEY=da169e0c2659415fb8a2aa931599e0cb.Ni3tTHoouDGos8AS
set MODEL=glm-4-flash
set BASE_URL=https://open.bigmodel.cn/api/paas/v4

echo 正在测试API密钥...
echo.

curl -X POST %BASE_URL%/chat/completions ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer %API_KEY%" ^
  -d "{\"model\":\"%MODEL%\",\"messages\":[{\"role\":\"user\",\"content\":\"你好\"}],\"temperature\":0.1}"

echo.
echo.
echo ========================================
echo   如果返回200且包含choices，则API密钥有效
echo   如果返回401，则API密钥无效或已过期
echo ========================================
pause
