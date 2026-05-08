# 识别模式灵活切换功能

## 📋 概述

实现了更灵活的识别模式配置，将原来的单一"识别模式"选项拆分为**优先选项**和**降级选项**两个独立选择器，支持多种组合方式。

---

## 🎯 核心改进

### **改进前**（单一选择器）
```
识别模式：[下拉选择]
- 纯LLM模式
- 纯正则匹配模式  
- 优先LLM，降级正则
- 优先正则，降级LLM
```

### **改进后**（双选择器）
```
优先：[LLM Agent] [LLM解析JSON] [规则匹配]
降级：[无] [LLM解析JSON] [规则匹配]
```

---

## 🔧 技术实现

### **1. 前端状态管理**

```typescript
// 优先选项（3种）
const priorityMode = ref<string>('llm_agent')  // 默认：LLM Agent
const priorityOptions = [
  { value: 'llm_agent', label: 'LLM Agent（智能工具选择）' },
  { value: 'llm_parse', label: 'LLM解析JSON' },
  { value: 'rule', label: '规则匹配' }
]

// 降级选项（3种）
const fallbackMode = ref<string>('none')  // 默认：无
const fallbackOptions = [
  { value: 'none', label: '无（不降级）' },
  { value: 'llm_parse', label: 'LLM解析JSON' },
  { value: 'rule', label: '规则匹配' }
]
```

### **2. 模式组合逻辑**

根据用户选择的组合，自动计算实际使用的接口和参数：

```typescript
if (priorityMode.value === 'llm_agent') {
  // 使用LLM Agent模式
  apiUrl = '/chat/agent'
  modeParam = 'agent'
} else {
  // 使用传统模式，计算recognitionMode
  if (priorityMode.value === 'llm_parse' && fallbackMode.value === 'none') {
    modeParam = 'llm_only'      // 纯LLM解析
  } else if (priorityMode.value === 'rule' && fallbackMode.value === 'none') {
    modeParam = 'rule_only'     // 纯规则匹配
  } else if (priorityMode.value === 'llm_parse' && fallbackMode.value === 'rule') {
    modeParam = 'llm_first'     // LLM优先，降级规则
  } else if (priorityMode.value === 'rule' && fallbackMode.value === 'llm_parse') {
    modeParam = 'rule_first'    // 规则优先，降级LLM
  }
}
```

### **3. 持久化存储**

使用 `localStorage` 保存用户的设置，切换页面后保持不变：

```typescript
// 保存设置
const saveRecognitionSettings = () => {
  localStorage.setItem('priorityMode', priorityMode.value)
  localStorage.setItem('fallbackMode', fallbackMode.value)
}

// 加载设置
const loadRecognitionSettings = () => {
  const savedPriority = localStorage.getItem('priorityMode')
  const savedFallback = localStorage.getItem('fallbackMode')
  
  if (savedPriority) priorityMode.value = savedPriority
  if (savedFallback) fallbackMode.value = savedFallback
}

// 在组件挂载时加载
onMounted(() => {
  loadRecognitionSettings()
})
```

---

## 📊 支持的组合模式

| 优先选项 | 降级选项 | 实际效果 | 使用接口 |
|---------|---------|---------|---------|
| **LLM Agent** | 无 | 纯LLM Agent模式 | `/chat/agent` |
| **LLM Agent** | LLM解析JSON | LLM Agent（降级忽略） | `/chat/agent` |
| **LLM Agent** | 规则匹配 | LLM Agent（降级忽略） | `/chat/agent` |
| **LLM解析JSON** | 无 | 纯LLM解析模式 | `/chat/send` (llm_only) |
| **LLM解析JSON** | 规则匹配 | LLM优先，降级规则 | `/chat/send` (llm_first) |
| **规则匹配** | 无 | 纯规则匹配模式 | `/chat/send` (rule_only) |
| **规则匹配** | LLM解析JSON | 规则优先，降级LLM | `/chat/send` (rule_first) |

---

## 💡 使用场景

### **场景1：追求最佳体验**
```
优先：LLM Agent
降级：无
```
- ✅ 最智能的意图识别
- ✅ 自然语言回复
- ✅ 自动工具选择
- ⚠️ 需要API密钥

### **场景2：平衡性能和成本**
```
优先：LLM解析JSON
降级：规则匹配
```
- ✅ LLM理解意图
- ✅ API失败时自动降级
- ✅ 保证可用性

### **场景3：完全离线使用**
```
优先：规则匹配
降级：无
```
- ✅ 不需要API密钥
- ✅ 响应速度快
- ❌ 只能识别预设的规则

### **场景4：开发调试**
```
优先：规则匹配
降级：LLM解析JSON
```
- ✅ 先用规则快速测试
- ✅ 规则失败时用LLM补充
- ✅ 适合开发阶段

---

## 🎨 UI设计

### **布局**
```
┌─────────────────────────────────────────┐
│  优先：[LLM Agent ▼]  降级：[无 ▼]      │
└─────────────────────────────────────────┘
```

### **样式特点**
- 两个独立的选择器，水平排列
- 清晰的标签："优先"和"降级"
- 统一的紫色主题色
- 选中时有焦点环效果

---

## 🔄 工作流程

```
用户选择模式
    ↓
触发 @change 事件
    ↓
saveRecognitionSettings()
    ↓
保存到 localStorage
    ↓
发送消息时读取设置
    ↓
计算实际使用的接口和参数
    ↓
调用后端API
    ↓
返回结果
```

---

## 📝 代码位置

### **前端文件**
- `frontend/src/views/Chat.vue`
  - 第24-39行：状态定义
  - 第70-105行：模式组合逻辑
  - 第318-339行：保存/加载函数
  - 第437-461行：UI组件

### **后端文件**
- `backend/src/main/java/com/zijin/college/controller/ChatController.java`
  - `/chat/send` - 传统Agent接口
  - `/chat/agent` - LLM Agent接口

---

## ✨ 关键特性

### **1. 灵活性**
- 9种可能的组合（3×3）
- 满足不同场景需求
- 用户可以自由切换

### **2. 持久化**
- 使用localStorage保存
- 刷新页面后保持设置
- 无需重新配置

### **3. 智能路由**
- 自动选择最佳接口
- LLM Agent优先
- 无缝降级机制

### **4. 用户体验**
- 直观的界面设计
- 实时反馈
- 清晰的选项说明

---

## 🐛 常见问题

### **Q1: 为什么选择LLM Agent后，降级选项无效？**
A: LLM Agent有自己的内置降级机制（→传统Agent→规则匹配），不需要额外的降级选项。

### **Q2: 设置会在不同浏览器间同步吗？**
A: 不会。localStorage是浏览器级别的存储，每个浏览器独立。

### **Q3: 如何清除设置？**
A: 打开浏览器开发者工具 → Application → Local Storage → 删除 `priorityMode` 和 `fallbackMode`。

### **Q4: 默认值是什么？**
A: 
- 优先：LLM Agent
- 降级：无

---

## 🚀 后续优化建议

1. **云端同步**：将设置保存到用户账户，多设备同步
2. **预设方案**：提供"推荐配置"、"性能优先"等预设
3. **使用说明**：在界面上添加帮助提示
4. **统计分析**：记录用户常用的组合，优化默认值

---

## 📖 相关文档

- [LLM Agent架构设计](./llm-agent-architecture.md)
- [LLM Agent使用指南](./llm-agent-usage-guide.md)
- [Agent识别模式配置](../README.md#agent识别模式配置)

---

## 🎉 总结

通过引入**优先选项**和**降级选项**的双选择器设计，实现了：

✅ **更灵活的配置** - 9种组合满足不同需求  
✅ **更好的用户体验** - 直观易懂的界面  
✅ **持久化存储** - 设置不会丢失  
✅ **智能路由** - 自动选择最佳接口  
✅ **向后兼容** - 保留原有所有功能  

这使得用户可以根据自己的需求和环境，灵活选择最适合的识别模式！🚀
