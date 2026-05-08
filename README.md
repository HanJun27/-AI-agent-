# 紫金学院智能管理系统

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.18-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Vue.js](https://img.shields.io/badge/Vue.js-3.x-4FC08D.svg)](https://vuejs.org/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

> 🤖 **基于LLM智能体的现代化高校管理系统** - 通过自然语言对话实现智能化的学生、员工、班级和部门管理

---

## 📸 项目截图

### 🏗️ 系统架构图

![系统架构图](docs/screenshots/architecture.png)

> 💡 **提示**：如需查看PlantUML源码，请参考 [docs/architecture.puml](docs/architecture.puml)

### 💬 AI智能对话界面

![AI对话界面](docs/screenshots/chat-interface.png)

*通过自然语言完成数据操作，体验智能化交互* 

### 📊 数据管理界面

![数据管理界面](docs/screenshots/data-management.png)

*现代化的CRUD管理界面，支持多条件查询和可视化统计*

---

## 📋 目录

- [✨ 项目亮点](#-项目亮点)
- [🎯 核心功能](#-核心功能)
- [🏗️ 技术架构](#️-技术架构)
- [🚀 快速开始](#-快速开始)
- [📖 使用指南](#-使用指南)
- [📁 项目结构](#-项目结构)
- [🔧 开发指南](#-开发指南)
- [📝 API文档](#-api文档)
- [🤝 贡献指南](#-贡献指南)
- [📄 许可证](#-许可证)

---

## ✨ 项目亮点

### 🌟 AI驱动的智能交互
- **自然语言操作**：通过对话即可完成增删改查，无需学习复杂界面
- **智能意图识别**：基于智谱AI GLM-4模型，准确理解用户意图
- **多轮对话支持**：支持上下文关联的连续对话
- **智能参数提取**：自动从自然语言中提取操作参数

### 🎨 现代化技术栈
- **前后端分离**：Vue 3 + Spring Boot 2.7，清晰的分层架构
- **响应式设计**：Element Plus组件库，适配多种设备
- **RESTful API**：标准化的API设计，易于扩展和维护
- **MyBatis ORM**：灵活的数据库操作，支持动态SQL

### 🔒 企业级特性
- **事务管理**：Spring声明式事务，保证数据一致性
- **参数校验**：多层参数验证，防止非法输入
- **错误处理**：统一的异常处理机制，友好的错误提示
- **日志记录**：完整的操作日志，便于问题追踪

### 📊 数据可视化
- **统计分析**：学生性别分布、年龄分布等数据分析
- **图表展示**：ECharts集成，直观的数据可视化
- **实时查询**：支持多条件组合查询，快速定位数据

---

## 🎯 核心功能

### 👨‍🎓 学生管理
- ✅ 添加学生（学号、姓名、性别、年龄、专业、班级等）
- ✅ 查询学生（按学号、姓名、性别、专业等多条件查询）
- ✅ 修改学生信息（电话、邮箱、专业、班级等）
- ✅ 删除学生
- ✅ 学生统计分析（性别分布、年龄分布、专业分布）

### 👨‍💼 员工管理
- ✅ 添加员工（工号、姓名、性别、年龄、职位、部门等）
- ✅ 查询员工（按工号、姓名、职位、部门等多条件查询）
- ✅ 修改员工信息（电话、邮箱、职位、部门等）
- ✅ 删除员工
- ✅ 员工统计分析

### 🏫 班级管理
- ✅ 添加班级（班级编号、名称、专业、年级、班主任、教室、人数）
- ✅ 查询班级（按编号、名称、专业、年级等查询）
- ✅ 修改班级信息（人数、班主任、教室等）
- ✅ 删除班级
- ✅ 班级统计分析

### 🏢 部门管理
- ✅ 添加部门（部门编号、名称、位置、负责人）
- ✅ 查询部门（按编号、名称查询）
- ✅ 修改部门信息（位置、负责人等）
- ✅ 删除部门
- ✅ 部门统计分析

### 💬 AI智能对话
- ✅ 自然语言操作：「把张三的电话改为15862840271」
- ✅ 批量操作：「查询所有男学生」
- ✅ 数据分析：「分析学生性别分布」
- ✅ 多轮对话：支持上下文关联的连续操作
- ✅ 智能纠错：自动识别并纠正参数错误

### ⚙️ 系统设置
- ✅ API配置管理（支持智谱AI、DeepSeek等多个LLM提供商）
- ✅ 识别模式切换（LLM Agent / 规则匹配）
- ✅ 对话历史管理
- ✅ 用户偏好设置

---

## 🏗️ 技术架构

### 前端技术栈
```
Vue 3.3+          # 渐进式JavaScript框架
Vite 4.x          # 下一代前端构建工具
Element Plus      # Vue 3组件库
Axios             # HTTP客户端
ECharts           # 数据可视化库
Vue Router        # 路由管理
Pinia             # 状态管理
```

### 后端技术栈
```
Spring Boot 2.7   # Java Web框架
MyBatis           # ORM框架
MySQL 8.0         # 关系型数据库
Lombok            # 代码简化工具
JUnit 5           # 单元测试框架
Maven             # 项目管理工具
```

### AI集成
```
智谱AI GLM-4      # 大语言模型（主要）
DeepSeek          # 备用LLM提供商
RestTemplate      # HTTP客户端（调用LLM API）
Function Calling  # LLM函数调用模式
```

### 架构图
```
┌─────────────────────────────────────────────────┐
│                  Frontend (Vue 3)                │
│  ┌──────────┐  ┌──────────┐  ┌──────────────┐  │
│  │ Chat UI  │  │ Data Mgmt│  │ Settings UI  │  │
│  └──────────┘  └──────────┘  └──────────────┘  │
└──────────────────────┬──────────────────────────┘
                       │ REST API (HTTP/JSON)
┌──────────────────────▼──────────────────────────┐
│               Backend (Spring Boot)              │
│  ┌──────────────┐  ┌─────────────────────────┐  │
│  │ Controller   │  │    LLMAgentService      │  │
│  │   Layer      │  │  (Intent Recognition)   │  │
│  └──────────────┘  └─────────────────────────┘  │
│  ┌──────────────┐  ┌─────────────────────────┐  │
│  │ Service      │  │   Tool Execution        │  │
│  │   Layer      │  │  (CRUD Operations)      │  │
│  └──────────────┘  └─────────────────────────┘  │
│  ┌──────────────┐                               │
│  │ Mapper       │                               │
│  │   Layer      │                               │
│  └──────────────┘                               │
└──────────────────────┬──────────────────────────┘
                       │ JDBC
┌──────────────────────▼──────────────────────────┐
│              Database (MySQL 8.0)                │
│  student | employee | class_info | department   │
│  sys_user | api_config | chat_session | ...     │
└─────────────────────────────────────────────────┘
```

---

## 🚀 快速开始

### 前置要求

- **JDK**: 17+ (推荐 JDK 21)
- **Node.js**: 16+ (推荐 LTS版本)
- **MySQL**: 8.0+
- **Maven**: 3.6+

### 1. 克隆项目

```bash
git clone https://github.com/your-HanJun27/zijin-college-system.git
cd zijin-college-system
```

### 2. 数据库配置

创建数据库并导入初始数据：

```bash
# 登录MySQL
mysql -u root -p

# 创建数据库
CREATE DATABASE zijin_college CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 导入数据结构（如果有SQL文件）
USE zijin_college;
SOURCE path/to/schema.sql;
```

修改后端配置文件 `backend/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/zijin_college?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    HanJun27: root
    password: your_password
```

### 3. 启动后端服务

```bash
cd backend
mvn spring-boot:run
```

后端服务将在 `http://localhost:8080` 启动

### 4. 启动前端服务

```bash
cd frontend
npm install
npm run dev
```

前端服务将在 `http://localhost:5173` 启动

### 5. 配置AI API

访问 `http://localhost:5173/settings`，配置智谱AI API密钥：

- **Provider**: 智谱AI
- **API Key**: 你的智谱AI API密钥（从 https://open.bigmodel.cn 获取）
- **Model**: glm-4-flash
- **Base URL**: https://open.bigmodel.cn/api/paas/v4

---

## 📖 使用指南

### 💬 AI对话示例

#### 学生管理
```
用户：添加一个学生，学号S2024001，姓名李四，性别男，年龄20，专业计算机科学
Agent：✅ 成功添加学生：李四（学号：S2024001）

用户：查询学号为S2024001的学生
Agent：找到学生：李四，学号S2024001，性别男，年龄20，专业计算机科学

用户：把李四的电话改为15862840271
Agent：✅ 成功更新学生：李四

用户：删除学号为S2024001的学生
Agent：✅ 成功删除学生：李四
```

#### 员工管理
```
用户：添加一个员工，工号E001，姓名张三，性别男，年龄28，职位软件工程师
Agent：✅ 成功添加员工：张三（工号：E001）

用户：把张三的职位改为高级软件工程师
Agent：✅ 成功更新员工：张三

用户：查询所有软件工程师
Agent：找到以下员工：
1. 张三，工号E001，职位软件工程师
2. 王五，工号E002，职位软件工程师
```

#### 班级管理
```
用户：创建班级，班级编号C2024001，班级名称计算机1班，专业计算机，年级2024，班主任张老师，人数50，教室教A301
Agent：✅ 成功添加班级：计算机1班（班号：C2024001）

用户：把班级C2024001的人数改为60
Agent：✅ 成功更新班级：计算机1班
```

#### 数据分析
```
用户：分析学生性别分布
Agent：学生性别分布统计：
- 男生：120人（60%）
- 女生：80人（40%）

用户：统计各专业的学生人数
Agent：各专业学生人数统计：
- 计算机科学：80人
- 软件工程：60人
- 人工智能：40人
- 数据科学：20人
```

### 🎛️ 传统界面操作

除了AI对话，系统也提供传统的表单界面进行数据管理：

- **学生管理**：`/students` - 学生列表、添加、编辑、删除
- **员工管理**：`/employees` - 员工列表、添加、编辑、删除
- **班级管理**：`/classes` - 班级列表、添加、编辑、删除
- **部门管理**：`/departments` - 部门列表、添加、编辑、删除
- **数据统计**：`/statistics` - 各类数据可视化图表

---

## 📁 项目结构

```
zijin-college-system/
├── backend/                    # 后端项目
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/zijin/college/
│   │   │   │       ├── agent/          # AI Agent核心模块
│   │   │   │       │   ├── LLMAgentService.java      # LLM代理服务
│   │   │   │       │   ├── CrudOperationTool.java    # CRUD操作工具
│   │   │   │       │   ├── DatabaseQueryTool.java    # 数据库查询工具
│   │   │   │       │   └── AnalysisTool.java         # 数据分析工具
│   │   │   │       ├── controller/       # 控制器层
│   │   │   │       ├── service/          # 业务逻辑层
│   │   │   │       ├── mapper/           # MyBatis映射器
│   │   │   │       ├── entity/           # 实体类
│   │   │   │       └── config/           # 配置类
│   │   │   └── resources/
│   │   │       ├── mapper/               # MyBatis XML
│   │   │       └── application.yml       # 应用配置
│   │   └── test/                         # 单元测试
│   ├── pom.xml                           # Maven配置
│   └── README.md
│
├── frontend/                   # 前端项目
│   ├── src/
│   │   ├── views/              # 页面组件
│   │   │   ├── Chat.vue        # AI对话页面
│   │   │   ├── StudentList.vue # 学生管理
│   │   │   ├── EmployeeList.vue# 员工管理
│   │   │   ├── ClassList.vue   # 班级管理
│   │   │   ├── DepartmentList.vue # 部门管理
│   │   │   └── Settings.vue    # 系统设置
│   │   ├── components/         # 公共组件
│   │   ├── api/                # API接口
│   │   │   ├── request.ts      # Axios配置
│   │   │   └── index.ts        # API定义
│   │   ├── router/             # 路由配置
│   │   ├── stores/             # Pinia状态管理
│   │   └── App.vue
│   ├── package.json            # NPM配置
│   ├── vite.config.ts          # Vite配置
│   └── README.md
│
├── docs/                       # 项目文档
│   ├── crud-coverage-analysis.md       # CRUD覆盖分析
│   ├── system-prompt-enhancement.md    # System Prompt强化
│   ├── llm-api-timeout-fix.md          # LLM API超时修复
│   ├── employee-name-update-fix.md     # 员工姓名修改修复
│   ├── update-methods-parameter-fix-complete.md  # 参数混淆修复
│   └── test-improvement-plan.md        # 测试改进计划
│
├── .skills/                    # 技能沉淀文档
│   ├── skill-class-teacher-classroom-missing.md
│   ├── skill-add-method-field-completeness.md
│   └── ...
│
├── .lingma/                    # Lingma AI配置
│   └── rules/
│       └── SkillsMaking.md     # 技能沉淀规范
│
├── database/                   # 数据库脚本
│   └── schema.sql
│
├── README.md                   # 项目说明（本文件）
└── LICENSE                     # 开源许可证
```

---

## 🔧 开发指南

### 后端开发

#### 添加新的Entity

1. 在 `entity/` 包中创建实体类
2. 在 `mapper/` 包中创建Mapper接口
3. 在 `resources/mapper/` 中创建XML映射文件
4. 在 `service/` 包中创建Service接口和实现
5. 在 `controller/` 包中创建Controller
6. 更新 `CrudOperationTool` 中的add/update方法
7. 更新 `LLMAgentService` 中的System Prompt

#### 运行测试

```bash
cd backend
mvn test

# 运行特定测试
mvn test -Dtest=LLMAgentIntegrationTest
```

### 前端开发

#### 添加新页面

1. 在 `views/` 目录创建Vue组件
2. 在 `router/index.ts` 中添加路由
3. 在 `api/index.ts` 中添加API接口
4. 在侧边栏菜单中添加导航项

#### 运行测试

```bash
cd frontend
npm run test
```

### 代码规范

- **后端**：遵循阿里巴巴Java开发手册
- **前端**：遵循Vue.js风格指南
- **Git提交**：使用Conventional Commits规范

---

## 📝 API文档

### 主要API端点

#### 学生管理
```
GET    /api/students              # 查询学生列表
POST   /api/students              # 添加学生
PUT    /api/students/{id}         # 更新学生
DELETE /api/students/{id}         # 删除学生
GET    /api/students/statistics   # 学生统计
```

#### 员工管理
```
GET    /api/employees             # 查询员工列表
POST   /api/employees             # 添加员工
PUT    /api/employees/{id}        # 更新员工
DELETE /api/employees/{id}        # 删除员工
```

#### AI对话
```
POST   /api/agent/chat            # AI对话接口
GET    /api/agent/sessions        # 获取对话历史
```

详细API文档请参考：[API Documentation](docs/API.md)

---

## 🤝 贡献指南

我们欢迎任何形式的贡献！

### 贡献流程

1. **Fork** 本仓库
2. 创建特性分支：`git checkout -b feature/AmazingFeature`
3. 提交更改：`git commit -m 'Add some AmazingFeature'`
4. 推送到分支：`git push origin feature/AmazingFeature`
5. 提交 **Pull Request**

### 贡献类型

- 🐛 **Bug修复**：修复已知问题
- ✨ **新功能**：添加新的功能模块
- 📝 **文档改进**：完善项目文档
- 🎨 **代码优化**：重构代码，提升性能
- 🧪 **测试补充**：增加单元测试或集成测试

### 开发规范

- 遵循现有的代码风格
- 添加必要的注释和文档
- 确保所有测试通过
- 更新相关文档

---

## 📄 许可证

本项目采用 [MIT License](LICENSE) 开源协议

---

## 👥 作者

**Han Jun**

- GitHub: [@HanJun27](https://github.com/HanJun27)
- Email: hanjun.dev@example.com

---

## 🙏 致谢

- [Spring Boot](https://spring.io/projects/spring-boot) - 强大的Java Web框架
- [Vue.js](https://vuejs.org/) - 渐进式JavaScript框架
- [Element Plus](https://element-plus.org/) - 优秀的Vue 3组件库
- [智谱AI](https://open.bigmodel.cn/) - 提供强大的LLM能力
- [MyBatis](https://mybatis.org/) - 灵活的ORM框架

---

## 📞 联系方式

如有问题或建议，请通过以下方式联系：

- 📧 Email: hanjun.dev@example.com
- 💬 Issues: [提交Issue](https://github.com/HanJun27/-AI-agent-/issues)
- 🌐 Website: [项目主页](https://github.com/HanJun27/-AI-agent-)

---

**⭐ 如果这个项目对你有帮助，请给个Star支持一下！**
