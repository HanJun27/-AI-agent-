# LLM智能意图识别实现方案

## 概述

使用大语言模型（LLM）替代传统的规则匹配，实现更智能、更自然的意图识别和参数提取。

## 架构设计

### **双层识别机制**

```
用户输入
  ↓
┌─────────────────────────┐
│ 是否有API配置？          │
└──────┬──────────────────┘
       │
   Yes ├──────────────────┐
       ↓                  ↓
┌──────────────┐    ┌──────────────┐
│ LLM意图识别   │    │ 规则匹配      │
│ (优先)        │    │ (降级方案)    │
└──────┬───────┘    └──────┬───────┘
       │                   │
       ├─ 置信度 > 0.7     │
       ↓                   │
┌──────────────┐           │
│ 使用LLM结果   │           │
└──────┬───────┘           │
       │                   │
       ├─ 置信度 ≤ 0.7     │
       ↓                   │
┌──────────────┐           │
│ 降级到规则匹配 │◄──────────┘
└──────┬───────┘
       ↓
  执行操作
```

## 核心组件

### 1. IntentRecognitionService

**职责**：调用LLM API进行意图识别

**关键特性**：
- ✅ 详细的系统提示词（System Prompt）
- ✅ 结构化JSON输出
- ✅ 置信度评估
- ✅ 降级机制（LLM失败时使用规则匹配）

**工作流程**：
```java
1. 构建系统提示词（定义能力、格式、示例）
2. 调用LLM API（temperature=0.1确保稳定性）
3. 解析JSON响应
4. 返回结构化结果
```

### 2. AgentService改造

**修改前**：
```java
Intent intent = recognizeIntent(userMessage);  // 规则匹配
String result = executeIntent(intent, userMessage);
```

**修改后**：
```java
if (有API配置) {
    // 尝试LLM识别
    IntentRecognitionResult llmResult = 
        intentRecognitionService.recognizeIntent(...);
    
    if (llmResult.confidence > 0.7) {
        // 使用LLM结果
        intent = convertLLMResultToIntent(llmResult);
        params = llmResult.getParams();
    } else {
        // 降级到规则匹配
        intent = recognizeIntent(userMessage);
        params = extractParameters(userMessage, intent);
    }
} else {
    // 直接使用规则匹配
    intent = recognizeIntent(userMessage);
    params = extractParameters(userMessage, intent);
}

String result = executeIntentWithParams(intent, userMessage, params);
```

## System Prompt设计

### **核心要素**

```
1. 角色定义
   "你是紫金学院管理系统的智能助手"

2. 能力说明
   - 支持的意图类型（QUERY, ANALYSIS, CREATE, DELETE, UPDATE, CHAT）
   - 可操作的数据类型（employees, students, classes, departments）

3. 参数规则
   - id: 数据库主键ID
   - stuNo: 学号
   - empNo: 工号
   - name: 姓名
   - keyword: 搜索关键词

4. 输出格式（严格JSON）
   {
     "intent": "DELETE",
     "action": "deleteStudent",
     "targetType": "students",
     "params": {"stuNo": "1"},
     "confidence": 0.95
   }

5. Action映射表
   明确列出所有支持的操作名称

6. Few-shot示例
   提供多个输入输出示例，帮助LLM理解任务
```

### **完整Prompt结构**

见 `IntentRecognitionService.java` 中的 `SYSTEM_PROMPT` 常量。

## 使用示例

### **示例1：删除学生**

**用户输入**：
```
"删除学号为1的学生"
```

**LLM输出**：
```json
{
  "intent": "DELETE",
  "action": "deleteStudent",
  "targetType": "students",
  "params": {
    "stuNo": "1"
  },
  "confidence": 0.95
}
```

**执行流程**：
1. LLM识别为DELETE意图
2. 提取参数 stuNo="1"
3. CrudOperationTool根据学号查找学生
4. 执行删除操作

### **示例2：复杂查询**

**用户输入**：
```
"帮我看看有哪些大三的计算机专业学生"
```

**LLM输出**：
```json
{
  "intent": "QUERY",
  "action": "queryStudents",
  "targetType": "students",
  "params": {
    "keyword": "大三 计算机"
  },
  "confidence": 0.88
}
```

### **示例3：模糊表达**

**用户输入**：
```
"把那个叫张三的学生删掉"
```

**LLM输出**：
```json
{
  "intent": "DELETE",
  "action": "deleteStudent",
  "targetType": "students",
  "params": {
    "name": "张三"
  },
  "confidence": 0.92
}
```

### **示例4：通用对话**

**用户输入**：
```
"你好，你能做什么？"
```

**LLM输出**：
```json
{
  "intent": "CHAT",
  "action": "generalChat",
  "targetType": null,
  "params": {},
  "confidence": 1.0
}
```

## 优势对比

| 特性 | 规则匹配 | LLM识别 |
|------|---------|---------|
| **理解能力** | 只能匹配预设模式 | 理解自然语言语义 |
| **灵活性** | 需要手动添加规则 | 自动适应新表达 |
| **准确性** | 对固定格式准确 | 对多样表达准确 |
| **维护成本** | 高（需不断更新规则） | 低（只需优化Prompt） |
| **扩展性** | 差（每新增功能都要改代码） | 好（只需更新Prompt） |
| **容错性** | 低（拼写错误就失败） | 高（能纠正小错误） |
| **依赖** | 无 | 需要API配置 |
| **速度** | 快（毫秒级） | 较慢（秒级） |
| **成本** | 免费 | API调用费用 |

## 降级策略

### **触发条件**

1. **没有API配置** - 用户未设置AI模型
2. **LLM调用失败** - 网络错误、API限流等
3. **置信度过低** - confidence < 0.7

### **降级流程**

```
LLM识别失败/置信度低
  ↓
调用 recognizeIntent() - 规则匹配
  ↓
调用 extractParameters() - 正则提取
  ↓
执行操作
```

### **保障机制**

- ✅ 始终有可用的降级方案
- ✅ 用户体验不受影响（只是可能不够智能）
- ✅ 日志记录降级原因，便于优化

## 配置要求

### **必需的API配置**

在设置界面配置以下信息：

```
提供商：智谱AI / DeepSeek / 通义千问 / OpenAI
API Key：sk-xxxxxxxxxxxxx
模型：glm-4-flash / deepseek-chat / qwen-turbo / gpt-3.5-turbo
Base URL：https://open.bigmodel.cn/api/paas/v4
```

### **推荐配置**

- **模型选择**：使用快速且便宜的模型（如glm-4-flash）
- **Temperature**：设置为0.1（确保输出稳定）
- **超时时间**：建议10秒

## 性能优化

### **1. 缓存机制（未来优化）**

```java
// 对常见问法进行缓存
Map<String, IntentRecognitionResult> cache = new ConcurrentHashMap<>();

String cacheKey = generateCacheKey(userMessage);
if (cache.containsKey(cacheKey)) {
    return cache.get(cacheKey);  // 直接返回缓存结果
}
```

### **2. 异步调用（未来优化）**

```java
// 使用CompletableFuture异步调用LLM
CompletableFuture<IntentRecognitionResult> future = 
    CompletableFuture.supplyAsync(() -> 
        intentRecognitionService.recognizeIntent(...)
    );

// 设置超时，超时后降级
return future.get(5, TimeUnit.SECONDS);
```

### **3. 批量处理（未来优化）**

如果同时有多个请求，可以批量调用LLM API。

## 监控与调试

### **日志输出**

```java
System.out.println("使用LLM进行意图识别...");
System.out.println("LLM识别结果: deleteStudent, 置信度: 0.95");
System.out.println("LLM识别置信度低，使用规则匹配...");
System.out.println("未配置API，使用规则匹配...");
```

### **关键指标**

- LLM调用成功率
- 平均置信度
- 降级触发频率
- 响应时间

### **调试技巧**

1. **查看后端控制台日志** - 确认使用的是LLM还是规则匹配
2. **检查置信度** - 如果经常降级，可能需要优化Prompt
3. **测试边界情况** - 确保降级机制正常工作

## 常见问题

### Q1: LLM识别很慢怎么办？

**A**: 
- 使用更快的模型（如glm-4-flash而非glm-4-plus）
- 考虑添加缓存机制
- 设置合理的超时时间

### Q2: LLM总是识别错误怎么办？

**A**:
- 优化System Prompt，添加更多示例
- 调整temperature参数（降低随机性）
- 检查API返回的原始内容

### Q3: 没有API配置时能用吗？

**A**: 
完全可以！会自动降级到规则匹配，功能完整只是不够智能。

### Q4: 会产生多少API费用？

**A**:
- 每次意图识别约消耗100-200 tokens
- 以智谱AI为例：glm-4-flash约0.001元/次
- 建议设置月度预算限制

## 未来扩展

### **1. 多轮对话支持**

```java
// 保存对话历史
List<Message> conversationHistory = new ArrayList<>();

// 在请求中包含历史
requestBody.put("messages", conversationHistory);
```

### **2. 意图确认机制**

```java
// 对于危险操作，让LLM生成确认消息
if (intent.getType() == DELETE && confidence < 0.9) {
    String confirmMsg = llmGenerateConfirmMessage(userMessage, extractedInfo);
    return confirmMsg;  // 等待用户确认
}
```

### **3. 学习机制**

```java
// 记录用户的纠正行为
if (userCorrectsAction(originalAction, correctedAction)) {
    saveToTrainingData(userMessage, correctedAction);
    // 定期微调模型或更新Prompt
}
```

### **4. 多语言支持**

LLM天然支持多语言，无需额外配置：
- "Delete student with ID 1" → 英文
- "删除学号为1的学生" → 中文
- "生徒ID 1を削除" → 日文

## 相关文件

- ✅ `IntentRecognitionService.java` - LLM意图识别服务
- ✅ `AgentService.java` - 集成LLM识别的Agent核心服务
- 📝 `docs/llm-intent-recognition.md` - 本文档

## 总结

通过引入LLM智能意图识别，Agent的自然语言理解能力得到质的飞跃：

**改进前**：
- ❌ 只能理解固定格式
- ❌ 需要精确的关键词
- ❌ 无法处理变体表达

**改进后**：
- ✅ 理解真正的语义
- ✅ 容忍表达差异
- ✅ 自动提取复杂参数
- ✅ 支持多种语言
- ✅ 持续学习能力

这是一个从"机械匹配"到"智能理解"的跨越！🚀
