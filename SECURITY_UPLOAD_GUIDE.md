# 安全上传指南 - 敏感信息清理

## ⚠️ 重要提醒

在上传到GitHub之前，**必须**检查并清理以下敏感信息！

---

## 🔴 绝对不要上传的文件/信息

### 1. API密钥和密码
- ❌ `application.yml` 中的数据库密码
- ❌ `application.yml` 中的API密钥
- ❌ `.env` 文件（如果存在）
- ❌ 任何包含真实密码的配置文件

### 2. 个人敏感信息
- ❌ 真实的邮箱地址（建议使用示例邮箱）
- ❌ 真实的手机号
- ❌ 身份证号等个人信息

### 3. 编译产物和依赖
- ❌ `target/` 目录（Maven编译输出）
- ❌ `node_modules/` 目录（NPM依赖）
- ❌ `dist/` 目录（前端构建输出）
- ❌ `.class` 文件
- ❌ `.jar` 文件

### 4. IDE配置
- ❌ `.idea/` 目录（IntelliJ IDEA）
- ❌ `.vscode/` 目录（VS Code个人配置）
- ❌ `.classpath`, `.project`（Eclipse）

### 5. 日志文件
- ❌ `*.log` 文件
- ❌ `logs/` 目录

---

## ✅ 应该上传的文件

### 核心代码
- ✅ `backend/src/` - 后端源代码
- ✅ `frontend/src/` - 前端源代码
- ✅ `pom.xml` - Maven配置
- ✅ `package.json` - NPM配置

### 文档
- ✅ `README.md` - 项目说明
- ✅ `QUICKSTART.md` - 快速开始
- ✅ `LICENSE` - 开源许可证
- ✅ `docs/` - 技术文档

### 配置模板
- ✅ `application.yml.example` - 配置示例（不含真实密码）
- ✅ `.gitignore` - Git忽略规则

### 技能沉淀
- ✅ `.skills/` - 技能文档
- ✅ `.lingma/rules/` - AI规则

---

## 🛡️ 敏感信息清理步骤

### 步骤1：检查后端配置文件

**文件**: `backend/src/main/resources/application.yml`

**需要清理的内容**：
```yaml
spring:
  datasource:
    username: root          # ✅ 可以保留（默认用户名）
    password: your_real_password  # ❌ 必须替换为占位符
```

**修改为**：
```yaml
spring:
  datasource:
    username: root
    password: ${DB_PASSWORD:your_password}  # 使用环境变量或占位符
```

或者创建配置示例文件：
```bash
# 复制配置文件
cp backend/src/main/resources/application.yml backend/src/main/resources/application.yml.example

# 编辑示例文件，替换真实密码
# password: your_password_here
```

### 步骤2：检查前端配置文件

**文件**: `frontend/.env` 或 `frontend/.env.local`

**操作**：
```bash
# 删除包含真实API密钥的环境文件
rm frontend/.env.local

# 创建示例文件
cat > frontend/.env.example << EOF
# API Configuration Example
VITE_API_BASE_URL=http://localhost:8080/api
VITE_APP_TITLE=Zijin College System
EOF
```

### 步骤3：检查Git历史

如果之前已经提交过敏感信息，需要清理Git历史：

```bash
# 检查是否有敏感文件被提交
git log --all --full-history -- "**/application.yml"
git log --all --full-history -- "**/.env"

# 如果有，使用 BFG Repo-Cleaner 或 git filter-branch 清理
# 警告：这会重写Git历史，谨慎操作！
```

### 步骤4：验证.gitignore

确保 `.gitignore` 文件包含以下内容：

```gitignore
# 已确认包含在 .gitignore 中
target/
node_modules/
dist/
*.log
.env
.env.local
.idea/
.vscode/
```

---

## 📋 上传前检查清单

### 配置文件检查
- [ ] `application.yml` 中无真实密码
- [ ] `.env` 文件未包含敏感信息
- [ ] API密钥已替换为占位符
- [ ] 创建了 `.example` 配置文件

### 代码检查
- [ ] 无硬编码的密码或密钥
- [ ] 无个人隐私信息（电话、身份证等）
- [ ] 注释中无敏感信息

### 文件检查
- [ ] `target/` 目录不存在
- [ ] `node_modules/` 目录不存在
- [ ] `dist/` 目录不存在
- [ ] `.log` 文件已删除
- [ ] IDE配置目录已删除

### Git检查
- [ ] `.gitignore` 生效
- [ ] 无大文件（>100MB）
- [ ] 无敏感文件在Git历史中

---

## 🔧 推荐的配置文件处理方式

### 方案1：使用环境变量（推荐）

**application.yml**:
```yaml
spring:
  datasource:
    url: ${DB_URL:jdbc:mysql://localhost:3306/zijin_college}
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:changeme}
```

**.env.example**:
```bash
DB_URL=jdbc:mysql://localhost:3306/zijin_college
DB_USERNAME=root
DB_PASSWORD=your_password_here
```

**README中添加说明**：
```markdown
### 配置环境变量

复制 `.env.example` 为 `.env` 并修改配置：

```bash
cp .env.example .env
# 编辑 .env 文件，设置正确的数据库密码
```
```

### 方案2：使用配置模板

1. 创建 `application.yml.template`
2. 在模板中使用占位符：`{{DB_PASSWORD}}`
3. 在README中说明如何替换
4. **不上传**真实的 `application.yml`

### 方案3：使用Spring Profiles

```yaml
# application.yml (公共配置)
spring:
  profiles:
    active: dev

# application-dev.yml (开发环境，不上传)
spring:
  datasource:
    password: dev_password

# application-prod.yml (生产环境，不上传)
spring:
  datasource:
    password: ${PROD_DB_PASSWORD}
```

在 `.gitignore` 中添加：
```gitignore
application-dev.yml
application-prod.yml
application-local.yml
```

---

## 🚀 安全上传命令

```bash
# 1. 进入项目目录
cd e:\工作台B\2026年春课设\java课设\zijin-college-system

# 2. 检查状态
git status

# 3. 确认没有敏感文件
git ls-files | grep -E "(application\.yml|\.env|\.log)$"

# 4. 添加文件
git add .

# 5. 再次检查将要提交的文件
git diff --cached --name-only

# 6. 提交
git commit -m "feat: Initial release with security cleanup

- Removed sensitive information
- Added configuration templates
- Updated documentation
- Cleaned up build artifacts"

# 7. 推送到GitHub
git push -u origin main
```

---

## 🔍 上传后验证

### 1. 检查GitHub仓库
访问：https://github.com/HanJun27/-AI-agent-/

确认：
- [ ] README正确显示
- [ ] 无敏感文件
- [ ] 文件大小合理（<50MB为佳）

### 2. 克隆测试
```bash
# 在新目录克隆
cd /tmp
git clone https://github.com/HanJun27/-AI-agent-.git test-repo
cd test-repo

# 检查是否有敏感文件
find . -name "*.yml" -o -name ".env" -o -name "*.log"
```

### 3. 检查文件大小
```bash
# 检查最大文件
git rev-list --objects --all | \
  git cat-file --batch-check='%(objecttype) %(objectname) %(objectsize) %(rest)' | \
  sed -n 's/^blob //p' | \
  sort --numeric-sort --key=2 | \
  tail -n 10
```

---

## 📝 更新README中的配置说明

在README.md的"快速开始"部分添加：

```markdown
### 配置数据库密码

**重要**：为了安全起见，项目中不包含真实的数据库密码。

方式1：使用环境变量
```bash
export DB_PASSWORD=your_mysql_password
mvn spring-boot:run
```

方式2：修改配置文件
```yaml
# 编辑 backend/src/main/resources/application.yml
spring:
  datasource:
    password: your_mysql_password  # 修改这里
```

⚠️ 注意：请勿将包含真实密码的配置文件提交到Git！
```

---

## 🎯 最终建议

### 对于当前项目

1. **立即执行**：
   ```bash
   # 删除可能的敏感文件
   rm -f backend/src/main/resources/application.yml
   rm -f frontend/.env.local
   
   # 创建示例配置
   cp backend/src/main/resources/application.yml.example backend/src/main/resources/application.yml.example
   ```

2. **更新README**：
   - 添加配置说明
   - 说明如何使用环境变量
   - 提供配置示例

3. **首次提交**：
   - 仔细检查 `git status`
   - 确认无敏感文件
   - 使用清晰的commit message

### 长期维护

- 定期审查提交的代码
- 使用pre-commit hooks检测敏感信息
- 考虑使用Git secrets工具
- 教育团队成员安全意识

---

## 🆘 如果不小心上传了敏感信息

### 立即行动

1. **撤销推送**（如果刚推送）：
   ```bash
   git reset --hard HEAD~1
   git push --force
   ```

2. **轮换凭证**：
   - 立即更改数据库密码
   - 重新生成API密钥
   - 更新所有使用该凭证的地方

3. **清理Git历史**：
   ```bash
   # 使用 BFG Repo-Cleaner
   java -jar bfg.jar --delete-files application.yml my-repo.git
   
   # 或使用 git filter-branch
   git filter-branch --force --index-filter \
     'git rm --cached --ignore-unmatch path/to/sensitive/file' \
     --prune-empty --tag-name-filter cat -- --all
   ```

4. **通知相关人员**：
   - 告知团队成员
   - 如果涉及生产环境，立即采取措施

---

**安全第一！** 🔒

在开源项目中保护敏感信息是每个开发者的责任。遵循以上指南，确保您的项目安全可靠。
