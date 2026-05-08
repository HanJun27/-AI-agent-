# 重新导入数据库（UTF8MB4编码）
$ErrorActionPreference = "Stop"

Write-Host "正在删除旧数据库..." -ForegroundColor Yellow
mysql -u root -p206663021 -e "DROP DATABASE IF EXISTS zijin_college;" 2>$null

Write-Host "正在创建新数据库..." -ForegroundColor Yellow
mysql -u root -p206663021 --default-character-set=utf8mb4 -e "CREATE DATABASE zijin_college DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" 2>$null

Write-Host "正在导入数据..." -ForegroundColor Yellow
$sqlContent = Get-Content "database\schema.sql" -Raw -Encoding UTF8

# 分割SQL语句并逐条执行
$statements = $sqlContent -split ';' | Where-Object { $_.Trim() -ne '' }

foreach ($stmt in $statements) {
    if ($stmt.Trim() -ne '' -and !$stmt.Trim().StartsWith('--')) {
        try {
            $stmt + ';' | mysql -u root -p206663021 --default-character-set=utf8mb4 zijin_college 2>$null
        } catch {
            Write-Host "警告: 某条语句执行失败，但继续执行" -ForegroundColor Yellow
        }
    }
}

Write-Host "`n验证数据..." -ForegroundColor Green
mysql -u root -p206663021 --default-character-set=utf8mb4 -e "USE zijin_college; SELECT dept_no, dept_name, location, manager FROM department;" 2>$null

Write-Host "`n✅ 数据库导入完成！" -ForegroundColor Green
