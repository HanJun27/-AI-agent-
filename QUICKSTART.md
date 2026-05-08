# 快速开始指南

## 🚀 5分钟快速体验

### 第一步：准备环境

确保已安装：
- ✅ JDK 17+
- ✅ Node.js 16+
- ✅ MySQL 8.0+
- ✅ Maven 3.6+

### 第二步：克隆项目

```bash
git clone https://github.com/HanJun27/-AI-agent-.git
cd zijin-college-system
```

### 第三步：配置数据库

```bash
# 登录MySQL
mysql -u root -p

# 执行以下SQL
CREATE DATABASE zijin_college CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE zijin_college;

# 如果有schema.sql文件，导入它
SOURCE database/schema.sql;
```

### 第四步：启动后端

```bash
cd backend

# 修改配置文件（可选）
# 编辑 src/main/resources/application.yml
# 设置正确的MySQL用户名和密码

# 启动服务
mvn spring-boot:run
```

看到以下输出表示成功：
```
Started ZijinCollegeApplication in X.XXX seconds
```

### 第五步：启动前端

打开新终端：

```bash
cd frontend

# 安装依赖（首次运行）
npm install

# 启动开发服务器
npm run dev
```

看到以下输出表示成功：
```
VITE v4.x.x  ready in xxx ms

➜  Local:   http://localhost:5173/
```

### 第六步：配置AI API

1. 打开浏览器访问：http://localhost:5173
2. 点击左侧菜单的"系统设置"
3. 在"API配置"标签页中：
   - Provider: 选择"智谱AI"
   - API Key: 输入你的智谱AI密钥（从 https://open.bigmodel.cn 获取）
   - Model: 选择"glm-4-flash"
   - Base URL: `https://open.bigmodel.cn/api/paas/v4`
4. 点击"保存配置"
5. 确保"启用"开关已打开

### 第七步：开始使用

#### 方式1：AI对话（推荐）⭐

点击左侧菜单的"AI对话"，尝试以下对话：

```
👤 用户：添加一个学生，学号S2024001，姓名李四，性别男，年龄20，专业计算机科学
🤖 Agent：✅ 成功添加学生：李四（学号：S2024001）

👤 用户：查询学号为S2024001的学生
🤖 Agent：找到学生：李四，学号S2024001，性别男，年龄20，专业计算机科学

👤 用户：分析学生性别分布
🤖 Agent：学生性别分布统计：
         - 男生：X人（XX%）
         - 女生：Y人（YY%）
```

#### 方式2：传统界面

- **学生管理**：点击"学生管理" → 查看学生列表、添加、编辑、删除
- **员工管理**：点击"员工管理" → 管理员工信息
- **班级管理**：点击"班级管理" → 管理班级信息
- **部门管理**：点击"部门管理" → 管理部门信息
- **数据统计**：点击"数据统计" → 查看可视化图表

---

## ❓ 常见问题

### Q1: 后端启动失败，提示端口被占用

**解决方案**：
```bash
# Windows
netstat -ano | findstr :8080
taskkill /PID <进程ID> /F

# 或者修改端口
# 编辑 backend/src/main/resources/application.yml
server:
  port: 8081  # 改为其他端口
```

### Q2: 前端无法连接后端

**解决方案**：
1. 确认后端已启动（访问 http://localhost:8080/api/test）
2. 检查前端代理配置：`frontend/vite.config.ts`
3. 清除浏览器缓存，刷新页面

### Q3: AI对话返回错误

**解决方案**：
1. 检查API密钥是否正确
2. 确认网络连接正常
3. 查看后端日志，确认错误信息
4. 尝试切换到规则匹配模式（在系统设置中）

### Q4: 数据库连接失败

**解决方案**：
```yaml
# 检查 application.yml 配置
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/zijin_college?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: root      # 确认用户名
    password: your_password  # 确认密码
```

### Q5: npm install 失败

**解决方案**：
```bash
# 清理缓存
npm cache clean --force

# 删除node_modules和package-lock.json
rm -rf node_modules package-lock.json

# 重新安装
npm install

# 如果仍然失败，尝试使用淘宝镜像
npm config set registry https://registry.npmmirror.com
npm install
```

---

## 🎯 下一步

- 📖 阅读完整文档：[README.md](README.md)
- 🔧 查看API文档：[docs/API.md](docs/API.md)
- 💡 学习更多示例：[docs/examples.md](docs/examples.md)
- 🤝 参与贡献：[CONTRIBUTING.md](CONTRIBUTING.md)

---

## 📞 需要帮助？

- 📧 Email: hanjun.dev@example.com
- 💬 Issues: [提交Issue](https://github.com/HanJun27/-AI-agent-/issues)
- 📚 Wiki: [项目Wiki](https://github.com/HanJun27/-AI-agent-/wiki)

---

**祝您使用愉快！** 🎉
