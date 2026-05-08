# LLM Agent 架构说明

## 🎯 什么是LLM Agent？

传统的意图识别方式是让LLM输出JSON格式，然后Java代码解析并执行。这种方式的问题：
- ❌ Prompt复杂冗长（100+行）
- ❌ LLM需要记忆所有Action名称和参数规则
- ❌ JSON格式容易出错
- ❌ 不支持动态字段（如phone、age等）

**LLM Agent方式**采用Function Calling模式：
- ✅ LLM直接"思考"并选择工具
- ✅ 自然对话风格，更符合人类思维
- ✅ 支持任意参数字段
- ✅ 更灵活、更智能

---

## 📊 架构对比

### 传统架构
```
用户输入 → IntentRecognitionService → JSON解析 → AgentService → CrudOperationTool
         ↓
    System Prompt (103行)
    - 定义6种意图
    - 定义20+个Action
    - 定义5个参数字段
    - 提供7个示例
    
问题：LLM需要记住太多规则！
```

### Agent架构（新）
```
用户输入 → LLMAgentService → 选择工具 → 执行工具 → 生成回复
         ↓
    System Prompt (简化版)
    - 定义可用工具列表
    - 每个工具的参数说明
    - 工作流程说明
    
优势：LLM只需要选择合适的工具！
```

---

## 🔧 核心组件

### 1. LLMAgentService.java
**位置**: `backend/src/main/java/com/zijin/college/agent/LLMAgentService.java`

**主要方法**:
```java
public String processMessage(String userMessage, String apiKey, String model, String baseUrl)
```

**工作流程**:
1. **识别与决策**: LLM分析用户意图，选择最合适的工具
2. **执行工具**: 调用对应的CrudOperationTool或AnalysisTool
3. **生成回复**: LLM根据工具结果生成自然语言回复

### 2. 工具定义
在System Prompt中定义可用工具：

```
### 查询工具
- queryStudents: 查询学生信息
  参数: stuNo(学号), name(姓名), keyword(关键词)
  
### 添加工具
- addStudent: 添加学生
  参数: stuNo(必填), name(必填), age, gender, major
  
### 修改工具
- updateStudent: 修改学生信息
  参数: stuNo或name(用于定位), 以及其他要修改的字段
  
### 删除工具
- deleteStudent: 删除学生
  参数: stuNo或name
  
### 分析工具
- analyzeStudentGender: 分析学生性别比例
```

### 3. ChatController新增端点
**路径**: `/chat/agent`

**使用方式**:
```bash
POST http://localhost:8080/chat/agent
Content-Type: application/json

{
  "message": "把学生李四的电话改为15862840271",
  "provider": "zhipu",
  "sessionId": "123456"
}
```

---

## 💡 使用示例

### 示例1：修改学生信息

**用户输入**: "把学生李四的电话改为15862840271"

**LLM决策**:
```json
{
  "tool": "updateStudent",
  "params": {
    "name": "李四",
    "phone": "15862840271"
  },
  "thought": "用户想修改学生李四的电话号码"
}
```

**执行过程**:
1. LLMAgentService接收到决策
2. 调用 `crudTool.updateStudent(params)`
3. 工具返回: "✅ 成功更新学生李四的信息"
4. LLM生成最终回复: "已成功将学生李四的电话号码更新为15862840271"

---

### 示例2：添加学生

**用户输入**: "添加学生张三，学号S2024001，年龄20岁"

**LLM决策**:
```json
{
  "tool": "addStudent",
  "params": {
    "name": "张三",
    "stuNo": "S2024001",
    "age": 20
  },
  "thought": "用户想添加一个新学生"
}
```

**执行过程**:
1. 调用 `crudTool.addStudent(params)`
2. 工具返回: "✅ 成功添加学生：张三（学号：S2024001）"
3. LLM生成回复: "已成功添加学生张三，学号为S2024001，年龄20岁"

---

### 示例3：普通对话

**用户输入**: "你好"

**LLM决策**:
```
不需要调用工具，直接回复
```

**执行过程**:
1. LLM直接生成回复: "你好！我是紫金学院管理系统的智能助手，有什么可以帮助您的吗？"

---

## 🚀 如何启用LLM Agent

### 步骤1：配置API密钥
在设置界面配置智谱AI或其他提供商的API密钥，并启用。

### 步骤2：前端调用新接口
修改前端Chat.vue，将请求发送到 `/chat/agent` 而不是 `/chat/send`：

```typescript
// 修改前
const response = await request({
  url: '/chat/send',
  method: 'post',
  data: { message, provider, sessionId }
})

// 修改后（使用Agent模式）
const response = await request({
  url: '/chat/agent',  // ← 新接口
  method: 'post',
  data: { message, provider, sessionId }
})
```

### 步骤3：重启后端服务
```bash
cd backend
mvn spring-boot:run
```

---

## ⚖️ 两种模式对比

| 特性 | 传统模式 (/chat/send) | Agent模式 (/chat/agent) |
|------|---------------------|------------------------|
| **Prompt复杂度** | 高（103行） | 低（约50行） |
| **灵活性** | 低（固定Action列表） | 高（动态工具选择） |
| **参数支持** | 有限（预定义字段） | 无限（任意字段） |
| **容错性** | 一般（JSON格式严格） | 好（自然语言理解） |
| **适用场景** | 简单查询、固定操作 | 复杂对话、多步操作 |
| **API依赖** | 可降级到规则匹配 | 需要API密钥 |

---

## 🎓 技术原理

### Function Calling / Tool Use

现代LLM（GPT-3.5+、GLM-4、Claude等）支持**函数调用**功能：

1. **定义工具Schema**: 告诉LLM有哪些可用函数
2. **LLM选择工具**: 根据用户输入决定调用哪个函数
3. **执行函数**: 后端执行选定的函数
4. **返回结果**: LLM看到结果后生成最终回复

**智谱GLM-4的Function Calling格式**:
```json
{
  "messages": [
    {"role": "system", "content": "..."},
    {"role": "user", "content": "把学生李四的电话改为15862840271"}
  ],
  "tools": [
    {
      "type": "function",
      "function": {
        "name": "updateStudent",
        "description": "修改学生信息",
        "parameters": {
          "type": "object",
          "properties": {
            "name": {"type": "string"},
            "phone": {"type": "string"}
          }
        }
      }
    }
  ]
}
```

---

## 📝 后续优化方向

1. **真正的Function Calling**: 当前实现是简化版，可以升级为智谱API的原生Function Calling
2. **多轮对话**: 支持上下文记忆，处理连续对话
3. **工具组合**: LLM可以连续调用多个工具完成复杂任务
4. **错误恢复**: 工具执行失败时，LLM自动尝试其他方案
5. **流式输出**: 实时显示LLM的思考过程和工具调用

---

## 🔗 相关文件

- `LLMAgentService.java` - Agent核心服务
- `ChatController.java` - 新增 `/chat/agent` 端点
- `IntentRecognitionService.java` - 传统意图识别（保留作为降级方案）
- `CrudOperationTool.java` - CRUD操作工具
- `AnalysisTool.java` - 数据分析工具

---

## ❓ FAQ

**Q: Agent模式和传统模式有什么区别？**
A: Agent模式让LLM更像"智能体"，主动选择工具并执行；传统模式让LLM只负责解析JSON。

**Q: 如果API不可用怎么办？**
A: Agent模式会自动降级到传统Agent，再降级到规则匹配。

**Q: 需要修改前端吗？**
A: 是的，需要将请求从 `/chat/send` 改为 `/chat/agent`。

**Q: 性能会受影响吗？**
A: Agent模式可能需要2次LLM调用（决策+生成回复），速度稍慢，但更智能。
