#!/bin/bash

echo "========================================"
echo "   Agent 自动化测试运行脚本"
echo "========================================"
echo ""

cd "$(dirname "$0")/backend" || exit 1

echo "[1/3] 清理编译..."
mvn clean compile -q

if [ $? -ne 0 ]; then
    echo "❌ 编译失败"
    exit 1
fi

echo "✅ 编译成功"
echo ""

echo "[2/3] 运行测试..."
echo ""
mvn test -Dtest=AgentIntentRecognitionTest

if [ $? -ne 0 ]; then
    echo ""
    echo "❌ 测试失败，请查看上方错误信息"
    exit 1
fi

echo ""
echo "========================================"
echo "✅ 所有测试完成！"
echo "📊 数据库内容已自动回滚，保持原样"
echo "========================================"
echo ""
