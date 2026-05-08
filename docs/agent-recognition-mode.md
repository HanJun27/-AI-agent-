# Agent识别模式配置功能

## 📋 功能概述

在对话界面添加了**意图识别模式选择器**，用户可以根据需求选择不同的识别策略。

---

## 🎯 **4种识别模式**

### **1. 纯LLM模式 (llm_only)**

**特点**：
- ✅ 完全依赖LLM进行意图识别
- ❌ 如果API未配置或调用失败，直接返回错误
- 💡 适合对识别准确率要求极高的场景

**工作流程**：
```
用户输入 → LLM识别 → 返回结果
                ↓ (失败)
           ❌ 报错提示
```

**适用场景**：
- API配置完善且稳定
- 需要最高识别准确率
- 复杂自然语言理解

---

### **2. 纯正则匹配模式 (rule_only)**

**特点**：
- ✅ 完全使用本地规则匹配（正则表达式）
- ✅ 速度快、稳定性高
- ✅ 不依赖网络和API
- ❌ 只能理解预设的模式

**工作流程**：
```
用户输入 → 规则匹配 → 参数提取 → 返回结果
```

**适用场景**：
- 快速测试和开发
- 离线环境
- 简单明确的指令
- CI/CD自动化测试

---

### **3. 优先LLM，降级正则 (llm_first) ⭐ 默认**

**特点**：
- ✅ 首先尝试LLM识别
- ✅ 如果LLM失败或置信度低，自动降级到规则匹配
- ✅ 兼顾准确性和稳定性
- ✅ 即使API不可用也能正常工作

**工作流程**：
```
用户输入 → LLM识别
              ↓ (成功且置信度>0.7)
         返回LLM结果
              ↓ (失败或置信度低)
         规则匹配 → 返回结果
```

**适用场景**：
- **日常使用（推荐）**
- API偶尔不稳定
- 平衡速度和准确性

---

### **4. 优先正则，降级LLM (rule_first)**

**特点**：
- ✅ 首先使用规则匹配
- ✅ 如果规则无法识别，尝试LLM补充
- ✅ 大部分情况快速响应
- ✅ 复杂语句有LLM兜底

**工作流程**：
```
用户输入 → 规则匹配
              ↓ (识别成功)
         返回结果
              ↓ (识别失败)
         LLM补充识别 → 返回结果
```

**适用场景**：
- 大部分是简单指令
- 偶尔有复杂表达
- 希望快速响应为主

---

## 🔧 **技术实现**

### **后端实现**

#### **1. RecognitionMode枚举**

文件：`AgentService.java`

```java
public enum RecognitionMode {
    LLM_ONLY("llm_only", "纯LLM模式"),
    RULE_ONLY("rule_only", "纯正则匹配模式"),
    LLM_FIRST("llm_first", "优先LLM，降级正则"),
    RULE_FIRST("rule_first", "优先正则，降级LLM");
    
    private final String code;
    private final String description;
    
    public static RecognitionMode fromCode(String code) {
        for (RecognitionMode mode : values()) {
            if (mode.code.equals(code)) {
                return mode;
            }
        }
        return LLM_FIRST; // 默认模式
    }
}
```

#### **2. processMessage方法重载**

```java
// 原有方法（向后兼容）
public String processMessage(String userMessage, String apiKey, 
                             String model, String baseUrl) {
    return processMessage(userMessage, apiKey, model, baseUrl, 
                         RecognitionMode.LLM_FIRST);
}

// 新方法（带识别模式）
public String processMessage(String userMessage, String apiKey, 
                             String model, String baseUrl, 
                             RecognitionMode mode) {
    switch (mode) {
        case LLM_ONLY:
            // 纯LLM逻辑
            break;
        case RULE_ONLY:
            // 纯正则逻辑
            break;
        case LLM_FIRST:
            // LLM优先，降级正则
            break;
        case RULE_FIRST:
            // 正则优先，降级LLM
            break;
    }
}
```

#### **3. ChatController接收参数**

```java
String recognitionMode = request.getOrDefault("recognitionMode", "llm_first");
AgentService.RecognitionMode mode = 
    AgentService.RecognitionMode.fromCode(recognitionMode);
String reply = agentService.processMessage(
    userMessage, apiKey, model, baseUrl, mode
);
```

---

### **前端实现**

#### **1. 状态定义**

文件：`Chat.vue`

```typescript
const recognitionMode = ref<string>('llm_first') // 默认LLM优先

const recognitionModes = [
  { value: 'llm_only', label: '纯LLM模式' },
  { value: 'rule_only', label: '纯正则匹配模式' },
  { value: 'llm_first', label: '优先LLM，降级正则' },
  { value: 'rule_first', label: '优先正则，降级LLM' }
]
```

#### **2. UI组件**

```vue
<!-- 识别模式选择器 -->
<div class="flex items-center gap-2">
  <label class="text-sm text-slate-600">识别模式：</label>
  <select v-model="recognitionMode" class="...">
    <option v-for="mode in recognitionModes" 
            :key="mode.value" 
            :value="mode.value">
      {{ mode.label }}
    </option>
  </select>
</div>
```

#### **3. 发送消息时传递参数**

```typescript
const response = await request({
  url: '/chat/send',
  method: 'post',
  data: {
    message: question,
    provider: selectedProvider.value,
    recognitionMode: recognitionMode.value, // ← 新增
    sessionId: activeSessionId.value
  }
})
```

---

## 📊 **模式对比**

| 特性 | 纯LLM | 纯正则 | LLM优先 | 正则优先 |
|------|-------|--------|---------|----------|
| **速度** | 🐌 慢 | ⚡ 快 | ⚡🐌 中等 | ⚡ 快 |
| **准确性** | ✅ 高 | ⚠️ 中 | ✅ 高 | ⚠️ 中 |
| **稳定性** | ⚠️ 依赖API | ✅ 稳定 | ✅ 稳定 | ✅ 稳定 |
| **离线可用** | ❌ 否 | ✅ 是 | ✅ 是 | ✅ 是 |
| **成本** | 💸 高 | 💰 免费 | 💸 中 | 💸 低 |
| **复杂度支持** | ✅ 强 | ❌ 弱 | ✅ 强 | ⚠️ 中 |

---

## 🎨 **界面效果**

对话界面顶部工具栏显示：

```
┌─────────────────────────────────────────────────┐
│ AI引擎：[智谱AI ▼]  识别模式：[优先LLM，降级正则 ▼] │
└─────────────────────────────────────────────────┘
```

用户可以随时切换识别模式，无需刷新页面。

---

## 🔍 **日志输出示例**

### **LLM优先模式**

```
========== 意图识别模式: 优先LLM，降级正则 ==========
使用LLM进行意图识别...
LLM识别成功: deleteStudent, 置信度: 0.95
```

### **纯正则模式**

```
========== 意图识别模式: 纯正则匹配模式 ==========
使用纯正则模式进行意图识别...
========== 规则匹配参数提取 ==========
消息: 删除学号S001的学生
提取到编码: S001
设置 stuNo: S001
```

### **纯LLM模式（API未配置）**

```
========== 意图识别模式: 纯LLM模式 ==========
❌ API配置未设置，无法使用纯LLM模式。请在设置中配置API或切换识别模式。
```

---

## 💡 **使用建议**

### **推荐配置**

| 场景 | 推荐模式 | 原因 |
|------|---------|------|
| **日常使用** | LLM优先（默认） | 平衡准确性和稳定性 |
| **开发调试** | 纯正则 | 快速、可预测 |
| **生产环境** | LLM优先 | 最佳用户体验 |
| **离线环境** | 纯正则 | 不依赖网络 |
| **高精度需求** | 纯LLM | 最高识别准确率 |
| **性能优先** | 正则优先 | 大部分情况快速响应 |

---

## 🧪 **测试方法**

### **1. 切换模式测试**

1. 打开对话界面
2. 在顶部选择不同识别模式
3. 输入相同的问题
4. 观察后端日志和响应时间

### **2. 验证降级机制**

**测试LLM优先模式**：
```
1. 禁用API配置
2. 输入："删除学号S001的学生"
3. 查看日志是否显示"降级到规则匹配"
4. 验证操作是否成功执行
```

**测试正则优先模式**：
```
1. 启用API配置
2. 输入复杂语句："帮我把那个学号为S001的同学删掉呗"
3. 查看日志是否显示"规则匹配未识别，尝试LLM"
4. 验证LLM是否正确识别
```

---

## 📝 **注意事项**

### **1. 默认模式**

- 默认选择：**优先LLM，降级正则**
- 原因：兼顾准确性和稳定性，适合大多数场景

### **2. API配置要求**

- **纯LLM模式**：必须配置有效的API
- **其他模式**：API可选，无API时自动使用规则匹配

### **3. 模式持久化**

当前实现中，识别模式**不会持久化**，刷新页面后会恢复默认值。

如需持久化，可以：
```typescript
// 保存到localStorage
watch(recognitionMode, (newMode) => {
  localStorage.setItem('recognitionMode', newMode)
})

// 加载时恢复
onMounted(() => {
  const savedMode = localStorage.getItem('recognitionMode')
  if (savedMode) {
    recognitionMode.value = savedMode
  }
})
```

---

## 🚀 **后续优化建议**

### **1. 智能模式切换**

根据用户输入自动选择最佳模式：
```java
if (isSimpleCommand(message)) {
    mode = RULE_ONLY;  // 简单命令用正则
} else {
    mode = LLM_FIRST;  // 复杂语句用LLM
}
```

### **2. 性能监控**

记录每种模式的：
- 平均响应时间
- 识别成功率
- API调用次数

### **3. 用户偏好学习**

根据用户历史行为，推荐最适合的模式。

---

## 📚 **相关文档**

- [agent-test-llm-vs-rules.md](./agent-test-llm-vs-rules.md) - LLM与规则匹配对比
- [agent-completeness-check.md](./agent-completeness-check.md) - 功能完备性检查
- [llm-intent-recognition.md](./llm-intent-recognition.md) - LLM意图识别说明

---

## ✅ **总结**

### **已实现功能**

1. ✅ **4种识别模式** - 满足不同场景需求
2. ✅ **前端UI选择器** - 直观易用的切换界面
3. ✅ **后端逻辑支持** - 完整的模式切换逻辑
4. ✅ **默认模式** - LLM优先，降级正则
5. ✅ **降级机制** - 确保系统稳定性

### **核心价值**

- 🎯 **灵活性** - 用户可根据需求选择最佳模式
- ⚡ **性能** - 提供快速和准确的不同选项
- 🛡️ **稳定性** - 降级机制保证系统可用性
- 💡 **智能化** - 结合LLM和规则的优势

**现在用户可以自由选择合适的识别模式，获得最佳的对话体验！** 🎉
