# LLM Agent 使用指南

## 📋 概述

LLM Agent 是一个智能对话系统，能够理解用户的自然语言指令并自动执行相应的数据库操作。

**核心特性**：
- ✅ 自动从系统设置获取API配置（无需硬编码）
- ✅ 支持多种操作：查询、添加、修改、删除、分析
- ✅ 智能工具选择：LLM自动决定调用哪个工具
- ✅ 自然语言回复：生成友好的对话式回复
- ✅ 降级机制：API不可用时自动切换到传统模式

---

## 🚀 快速开始

### 步骤1：配置API密钥

1. 启动后端服务
2. 打开前端设置界面
3. 配置智谱AI（或其他提供商）的API密钥
4. 勾选"启用"复选框
5. 点击"保存配置"

**或者直接使用SQL插入配置**：
```sql
UPDATE api_config 
SET api_key = 'your-api-key-here', enabled = 1 
WHERE provider = 'zhipu';
```

### 步骤2：重启后端服务

```bash
cd backend
mvn spring-boot:run
```

### 步骤3：测试Agent功能

**方式1：使用测试脚本**
```bash
test-llm-agent.bat
```

**方式2：使用Postman/curl**
```bash
curl -X POST http://localhost:8080/chat/agent \
  -H "Content-Type: application/json" \
  -d '{
    "message": "把学生李四的电话改为15862840271",
    "provider": "zhipu"
  }'
```

**方式3：前端集成**（见下方"前端集成"章节）

---

## 📊 工作流程

```
用户输入："把学生李四的电话改为15862840271"
         ↓
┌─────────────────────────────────┐
│  LLMAgentService                │
│                                 │
│  1. 从数据库获取API配置          │
│     → api_config表              │
│     → provider: zhipu           │
│     → apiKey, model, baseUrl    │
│                                 │
│  2. 构建System Prompt           │
│     - 定义可用工具列表           │
│     - 说明每个工具的参数         │
│                                 │
│  3. 调用LLM进行决策             │
│     → LLM返回JSON:              │
│       {                         │
│         "tool": "updateStudent",│
│         "params": {             │
│           "name": "李四",       │
│           "phone": "158..."     │
│         }                       │
│       }                         │
│                                 │
│  4. 执行工具                    │
│     → crudTool.updateStudent()  │
│     → SQL更新数据库             │
│     → 返回结果字符串             │
│                                 │
│  5. LLM生成最终回复             │
│     → "已成功将学生李四的        │
│        电话号码更新为..."        │
└─────────────────────────────────┘
         ↓
返回给用户
```

---

## 🔧 API接口

### 端点：`POST /chat/agent`

**请求格式**：
```json
{
  "message": "用户消息",
  "provider": "zhipu",
  "sessionId": "可选的会话ID"
}
```

**响应格式**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "reply": "AI的回复内容",
    "timestamp": 1234567890,
    "mode": "agent"
  }
}
```

**参数说明**：
- `message`（必填）：用户的自然语言消息
- `provider`（必填）：API提供商标识（如 "zhipu", "deepseek"）
- `sessionId`（可选）：会话ID，用于保存历史记录

---

## 💡 使用示例

### 示例1：修改学生信息

**用户输入**：
```
把学生李四的电话改为15862840271
```

**Agent行为**：
1. LLM识别意图：UPDATE操作
2. 选择工具：`updateStudent`
3. 提取参数：`{name: "李四", phone: "15862840271"}`
4. 执行SQL：`UPDATE student SET phone='15862840271' WHERE stu_name='李四'`
5. 生成回复："已成功将学生李四的电话号码更新为15862840271"

---

### 示例2：添加学生

**用户输入**：
```
添加学生张三，学号S2024001，年龄20岁
```

**Agent行为**：
1. LLM识别意图：CREATE操作
2. 选择工具：`addStudent`
3. 提取参数：`{name: "张三", stuNo: "S2024001", age: 20}`
4. 执行SQL：`INSERT INTO student (...) VALUES (...)`
5. 生成回复："已成功添加学生张三，学号为S2024001，年龄20岁"

---

### 示例3：查询数据

**用户输入**：
```
查询所有学生
```

**Agent行为**：
1. LLM识别意图：QUERY操作
2. 选择工具：`queryStudents`
3. 执行SQL：`SELECT * FROM student`
4. 生成回复："当前共有X名学生，分别是：..."

---

### 示例4：普通对话

**用户输入**：
```
你好
```

**Agent行为**：
1. LLM识别为普通对话
2. 不调用任何工具
3. 直接生成回复："你好！我是紫金学院管理系统的智能助手，有什么可以帮助您的吗？"

---

## 🎯 支持的命令

### 查询类
- "查看所有学生"
- "查询员工张三的信息"
- "显示部门列表"
- "获取数据库概览"

### 添加类
- "添加学生李四，学号S2024001"
- "新增员工王五，工号E001"
- "创建部门教务处"

### 修改类
- "把学生李四的电话改为15862840271"
- "更新员工张三的职位为教授"
- "修改部门位置到行政楼"

### 删除类
- "删除学号为S2023001的学生"
- "移除员工王五"

### 分析类
- "分析学生性别比例"
- "统计员工分布情况"
- "生成综合报告"

---

## 🔑 API配置复用机制

### 传统方式（已废弃）
```java
// ❌ 硬编码API密钥
private static final String API_KEY = "ed4bac5b...";
private static final String MODEL = "glm-4-flash";
```

### 新方式（当前实现）
```java
// ✅ 从数据库动态获取
ApiConfig config = apiConfigService.getConfigByProvider(provider);
String apiKey = config.getApiKey();
String model = config.getModel();
String baseUrl = config.getBaseUrl();
```

**优势**：
- ✅ 统一管理：在设置界面配置一次，全局生效
- ✅ 灵活切换：可以轻松切换不同的API提供商
- ✅ 安全性：API密钥存储在数据库中，不在代码中暴露
- ✅ 可维护性：修改配置无需重新编译代码

---

## 🛠️ 前端集成

### 修改Chat.vue

找到发送消息的代码，将URL从 `/chat/send` 改为 `/chat/agent`：

```typescript
// 原来的代码
const response = await request({
  url: '/chat/send',  // ← 旧接口
  method: 'post',
  data: { 
    message: inputMessage.value,
    provider: selectedProvider.value,
    sessionId: currentSessionId.value
  }
})

// 修改后的代码
const response = await request({
  url: '/chat/agent',  // ← 新接口（LLM Agent）
  method: 'post',
  data: { 
    message: inputMessage.value,
    provider: selectedProvider.value,
    sessionId: currentSessionId.value
  }
})
```

**或者保留两种模式，让用户选择**：
```typescript
// 根据用户选择的模式调用不同接口
const apiUrl = useAgentMode.value ? '/chat/agent' : '/chat/send'

const response = await request({
  url: apiUrl,
  method: 'post',
  data: { ... }
})
```

---

## ⚙️ 架构设计

### 核心组件

1. **LLMAgentService** - Agent核心服务
   - 负责协调整个工作流程
   - 从数据库获取API配置
   - 调用LLM进行决策
   - 执行工具并生成回复

2. **CrudOperationTool** - CRUD操作工具集
   - addStudent, updateStudent, deleteStudent
   - addEmployee, updateEmployee, deleteEmployee
   - queryStudents, queryEmployees, etc.

3. **AnalysisTool** - 数据分析工具集
   - analyzeStudentGender
   - analyzeStudentAge
   - generateComprehensiveReport

4. **ApiConfigService** - API配置服务
   - 从 `api_config` 表读取配置
   - 提供统一的配置访问接口

---

## 📝 技术细节

### System Prompt设计

```
你是紫金学院管理系统的智能助手。你可以调用以下工具来帮助用户：

## 可用工具列表

### 查询工具
- queryStudents: 查询学生信息
  参数: stuNo(学号), name(姓名), keyword(关键词)
  
### 添加工具
- addStudent: 添加学生
  参数: stuNo(必填), name(必填), age, gender, major
  
...（更多工具定义）

## 工作流程
1. 理解用户意图
2. 如果需要操作数据，选择最合适的工具并提取参数
3. 如果只是聊天，直接回复，不需要调用工具

## 输出格式
如果需要调用工具，返回JSON：
{"tool":"工具名","params":{参数对象},"thought":"你的思考过程"}

如果不需要调用工具，直接返回自然语言回复。
```

### 工具执行流程

```java
// 1. LLM返回决策
{
  "tool": "updateStudent",
  "params": {"name": "李四", "phone": "15862840271"}
}

// 2. Agent解析决策
AgentDecision decision = parseAgentDecision(llmResponse);

// 3. 执行对应工具
String result = executeTool(decision.getToolName(), decision.getParams());
// → crudTool.updateStudent(params)

// 4. LLM生成友好回复
String finalReply = generateFinalResponse(userMessage, result, ...);
// → "已成功将学生李四的电话号码更新为15862840271"
```

---

## 🐛 常见问题

### Q1: 提示"API配置未启用或无效"
**原因**：数据库中该provider的配置未启用或API密钥为空

**解决**：
1. 检查 `api_config` 表
2. 确保 `enabled = 1`
3. 确保 `api_key` 不为空
4. 或者在设置界面重新配置

### Q2: LLM返回的不是JSON格式
**原因**：LLM可能直接回复了自然语言

**解决**：这是正常现象，Agent会将其视为普通对话处理

### Q3: 工具执行失败
**原因**：参数不完整或数据库约束冲突

**解决**：
1. 检查日志中的错误信息
2. 确保提供了必要的参数（如学号、姓名）
3. 检查数据库是否有唯一约束冲突

### Q4: 响应速度慢
**原因**：需要两次LLM调用（决策 + 生成回复）

**解决**：
1. 使用更快的模型（如 glm-4-flash）
2. 优化网络延迟
3. 考虑缓存常用回复

---

## 📈 性能优化建议

1. **缓存API配置**：避免每次都查询数据库
2. **异步处理**：对于耗时操作，可以异步执行
3. **流式输出**：实时显示LLM的思考过程
4. **工具预筛选**：根据关键词预先缩小工具范围

---

## 🔗 相关文件

- `LLMAgentService.java` - Agent核心服务
- `ChatController.java` - API端点（`/chat/agent`）
- `CrudOperationTool.java` - CRUD工具集
- `AnalysisTool.java` - 分析工具集
- `ApiConfigService.java` - API配置服务
- `api_config` 表 - 存储API配置

---

## 🎓 扩展开发

### 添加新工具

1. 在 `CrudOperationTool` 或 `AnalysisTool` 中添加新方法
2. 在 `LLMAgentService.buildAgentSystemPrompt()` 中添加工具定义
3. 在 `executeTool()` 的switch语句中添加case分支

**示例**：
```java
// 1. 添加工具方法
public String exportStudentReport(Map<String, Object> params) {
    // 实现导出逻辑
    return "✅ 导出成功";
}

// 2. 在Prompt中添加定义
"- exportStudentReport: 导出学生报告\n" +
"  参数: format(格式，如pdf/excel)\n"

// 3. 在executeTool中添加case
case "exportStudentReport":
    return crudTool.exportStudentReport(params);
```

---

## ✨ 总结

LLM Agent通过以下方式提升了系统的智能化水平：

1. **自动配置管理**：复用系统设置界面的API配置
2. **智能工具选择**：LLM自动决定调用哪个工具
3. **自然对话体验**：生成友好的回复而非机械的技术信息
4. **灵活的扩展性**：轻松添加新工具和新功能

这使得用户可以像与真人对话一样与系统交互，大大提升了用户体验！🎉
