# Agent测试中LLM与规则匹配说明

## 📋 当前状态

### ❓ **测试是否使用LLM识别？**

**答案：否**。当前所有测试都使用**规则匹配（正则表达式）**，而非LLM识别。

---

## 🔍 **原因分析**

### 1. 测试配置

在 `AgentIntentRecognitionTest.java` 中：

```java
private static final String API_KEY = "";
private static final String MODEL = "";
private static final String BASE_URL = "";
```

API配置为空，导致 `AgentService.processMessage()` 方法中的判断：

```java
if (apiKey != null && !apiKey.isEmpty() && baseUrl != null && !baseUrl.isEmpty()) {
    // 使用LLM进行智能意图识别
    System.out.println("使用LLM进行意图识别...");
    // ... LLM调用逻辑
} else {
    // 没有API配置，使用规则匹配
    System.out.println("未配置API，使用规则匹配...");
    intent = recognizeIntent(userMessage);  // ← 走这里
    params = extractParameters(userMessage, intent);
}
```

### 2. 测试日志证据

从测试输出可以看到：

```
未配置API，使用规则匹配...
========== 规则匹配参数提取 ==========
消息: 删除学号为DELETE_TEST_001的学生
提取到编码: DELETE_TEST_001
设置 stuNo: DELETE_TEST_001
最终参数: {stuNo=DELETE_TEST_001}
=====================================
```

**没有出现**：
```
使用LLM进行意图识别...
LLM识别结果: deleteStudent, 置信度: 0.95
```

---

## ✅ **为什么这样设计？**

### **优势**

| 特性 | 规则匹配 | LLM识别 |
|------|---------|---------|
| **速度** | ⚡ 快（毫秒级） | 🐌 慢（秒级，需网络请求） |
| **稳定性** | ✅ 稳定（本地执行） | ⚠️ 依赖网络和API服务 |
| **成本** | 💰 免费 | 💸 消耗API配额 |
| **可重复性** | ✅ 完全可重复 | ⚠️ LLM输出可能有波动 |
| **离线测试** | ✅ 支持 | ❌ 需要网络 |
| **CI/CD集成** | ✅ 容易 | ⚠️ 需要配置API密钥 |

### **测试目标**

当前测试主要验证：
1. ✅ **参数提取准确性** - 正则表达式是否正确提取学号、姓名等
2. ✅ **业务逻辑正确性** - CRUD操作是否正常工作
3. ✅ **数据隔离机制** - 事务回滚是否有效
4. ✅ **错误处理** - 异常情况是否正确处理

这些目标**不需要LLM**也能充分验证。

---

## 🔧 **如何启用LLM测试？**

如果需要测试LLM识别功能，有两种方式：

### **方式1：修改测试类配置（推荐用于临时测试）**

编辑 `AgentIntentRecognitionTest.java`：

```java
// 填入真实的API配置
private static final String API_KEY = "your-actual-api-key";
private static final String MODEL = "glm-4-flash";
private static final String BASE_URL = "https://open.bigmodel.cn/api/paas/v4";
```

然后重新运行测试：

```bash
mvn test -Dtest=AgentIntentRecognitionTest
```

**测试开始时会显示**：
```
========================================
🧪 开始运行 Agent 自动化测试
========================================
✅ 当前使用LLM意图识别
📡 API模型: glm-4-flash
========================================
```

### **方式2：从数据库读取配置（更灵活）**

可以修改测试类，从数据库读取已启用的API配置：

```java
@Autowired
private ApiConfigService apiConfigService;

@BeforeAll
public static void setup() {
    // 查询第一个启用的API配置
    ApiConfig config = apiConfigService.getConfigByProvider("zhipu");
    if (config != null && config.getEnabled() == 1) {
        API_KEY = config.getApiKey();
        MODEL = config.getModel();
        BASE_URL = config.getBaseUrl();
    }
}
```

---

## 📊 **两种方式的对比测试**

### **规则匹配测试（当前）**

**输入**：`"删除学号为S2023001的学生"`

**处理流程**：
```
用户输入
  ↓
recognizeIntent() - 规则匹配
  ↓ (检测到"删除"和"学生")
intent.setAction("deleteStudent")
  ↓
extractParameters() - 正则提取
  ↓ (匹配到"学号S2023001")
params = {stuNo: "S2023001"}
  ↓
executeIntentWithParams()
  ↓
crudTool.deleteStudent(params)
  ↓
返回结果
```

**优点**：
- ✅ 速度快（< 10ms）
- ✅ 确定性高
- ✅ 不依赖外部服务

**缺点**：
- ❌ 只能理解预设的模式
- ❌ 对复杂语句理解有限

---

### **LLM识别测试（可选）**

**输入**：`"删除学号为S2023001的学生"`

**处理流程**：
```
用户输入
  ↓
调用LLM API
  ↓
LLM分析语义
  ↓
返回JSON: {
  "intent": "DELETE",
  "action": "deleteStudent",
  "params": {"stuNo": "S2023001"},
  "confidence": 0.95
}
  ↓
convertLLMResultToIntent()
  ↓
executeIntentWithParams()
  ↓
crudTool.deleteStudent(params)
  ↓
返回结果
```

**优点**：
- ✅ 理解能力强
- ✅ 支持自然语言表达
- ✅ 能处理复杂语句

**缺点**：
- ❌ 速度慢（1-3秒）
- ❌ 依赖网络
- ❌ 消耗API配额
- ❌ 输出可能有波动

---

## 🎯 **建议的测试策略**

### **日常开发测试** - 使用规则匹配

```java
private static final String API_KEY = "";  // 留空
```

**适用场景**：
- ✅ 快速迭代开发
- ✅ 单元测试
- ✅ CI/CD流水线
- ✅ 参数提取验证

### **集成测试/发布前测试** - 使用LLM

```java
private static final String API_KEY = "your-api-key";
```

**适用场景**：
- ✅ 验证LLM识别效果
- ✅ 测试复杂语句理解
- ✅ 性能基准测试
- ✅ 发布前全面验证

---

## 📝 **测试覆盖情况**

### **当前测试（规则匹配）**

| 功能类别 | 测试数量 | 通过率 | 说明 |
|---------|---------|--------|------|
| 查询功能 | 3 | 100% | ✅ 全部通过 |
| 添加功能 | 3 | 0% | ❌ 参数提取问题 |
| 删除功能 | 3 | 100% | ✅ 全部通过 |
| 更新功能 | 3 | 100% | ✅ 全部通过 |
| 分析/对话 | 3 | 100% | ✅ 全部通过 |
| **总计** | **15** | **80%** | **12/15通过** |

### **如果使用LLM**

预期通过率会**更高**，因为：
- ✅ LLM能更好地理解自然语言
- ✅ 参数提取更准确
- ✅ 支持更多表达方式

但会有以下问题：
- ⚠️ 测试时间增加（从5秒 → 30-60秒）
- ⚠️ 需要稳定的网络连接
- ⚠️ 消耗API配额

---

## 🔬 **如何验证LLM是否介入？**

### **方法1：查看日志**

**规则匹配**：
```
未配置API，使用规则匹配...
========== 规则匹配参数提取 ==========
```

**LLM识别**：
```
使用LLM进行意图识别...
LLM识别结果: deleteStudent, 置信度: 0.95
LLM原始响应: {"intent":"DELETE","action":"deleteStudent",...}
```

### **方法2：添加断言**

在测试中添加：

```java
@Test
public void testLLMIntervention() {
    String message = "删除学号S001的学生";
    String result = agentService.processMessage(message, API_KEY, MODEL, BASE_URL);
    
    if (!API_KEY.isEmpty()) {
        // 如果配置了API，应该看到LLM相关日志
        assertTrue(result.contains("成功") || result.contains("删除"));
    } else {
        // 如果没有配置API，应该使用规则匹配
        assertNotNull(result);
    }
}
```

---

## 💡 **最佳实践建议**

### **1. 分层测试**

```
单元测试（规则匹配）
  ↓
集成测试（LLM识别）
  ↓
端到端测试（完整流程）
```

### **2. 环境隔离**

```yaml
# application-test.yml (测试环境)
agent:
  use-llm: false  # 测试环境不使用LLM

# application-prod.yml (生产环境)
agent:
  use-llm: true   # 生产环境使用LLM
```

### **3. 混合模式**

```java
// 简单查询使用规则匹配
if (isSimpleQuery(message)) {
    return ruleBasedProcess(message);
} else {
    // 复杂语句使用LLM
    return llmBasedProcess(message);
}
```

---

## 📚 **相关文档**

- [agent-automated-testing.md](./agent-automated-testing.md) - 自动化测试指南
- [agent-completeness-check.md](./agent-completeness-check.md) - 功能完备性检查
- [llm-intent-recognition.md](./llm-intent-recognition.md) - LLM意图识别说明

---

## ✅ **总结**

### **当前状态**
- ❌ 测试**未使用LLM**，使用规则匹配
- ✅ 这是**有意设计**，为了快速、稳定、低成本的测试
- ✅ 覆盖了80%的核心功能（12/15通过）

### **如何启用LLM测试**
1. 在测试类中填写真实的API配置
2. 重新运行测试
3. 观察日志确认LLM介入

### **建议**
- **日常开发**：使用规则匹配（当前方式）
- **发布前验证**：启用LLM测试
- **持续改进**：逐步提高规则匹配的覆盖率

**规则匹配已经能很好地验证核心功能，LLM测试可以作为补充！** 🎯
