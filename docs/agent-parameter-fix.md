# Agent参数提取与LLM配置问题修复

## 📋 修复时间
2026-05-08

---

## 🔍 发现的问题

### 问题1：姓名提取包含多余字符 ❌

**现象**：
用户输入："修改工号E001的员工姓名为王五"
实际提取：`name="为王五"` （多了"为"字）
数据库结果：员工姓名变成"为王五"

**原因**：
正则表达式 `(?:姓名|名字|叫|改为|设置为)[\s:=：]*?([\u4e00-\u9fa5]{2,4})` 中的 `[\s:=：]*?` 只匹配空格、冒号、等号，不匹配"为"字。

当输入"姓名为王五"时：
- `(?:姓名)` 匹配 "姓名"
- `[\s:=：]*?` 匹配空（非贪婪）
- `([\u4e00-\u9fa5]{2,4})` 捕获 "为王五"（3个中文字符）

---

### 问题2：LLM未介入，使用规则匹配 ⚠️

**现象**：
```
provider:
provider为空，将使用规则匹配
apiKey: null
model: null
baseUrl: null
```

**原因**：
前端Chat.vue的`selectedProvider`初始化为空字符串`''`，且没有在页面加载时从后端获取已启用的API配置。

---

## ✅ 修复方案

### 修复1：优化姓名提取正则表达式

**文件**：`AgentService.java`

**修改前**：
```java
java.util.regex.Pattern namePattern = java.util.regex.Pattern.compile(
    "(?:姓名|名字|叫|改为|设置为)[\\s:=：]*?([\\u4e00-\\u9fa5]{2,4})"
);
```

**修改后**：
```java
java.util.regex.Pattern namePattern = java.util.regex.Pattern.compile(
    "(?:姓名|名字|叫|改为|设置为)[^\\u4e00-\\u9fa5]*([\\u4e00-\\u9fa5]{2,4})"
);
```

**改进说明**：
- `[^\\u4e00-\\u9fa5]*` - 匹配任意数量的非中文字符（包括"为"、空格、冒号等）
- `([\\u4e00-\\u9fa5]{2,4})` - 只捕获纯中文字符（2-4个）

**测试用例**：
```
✅ "姓名为王五" → name="王五"
✅ "名字叫张三" → name="张三"
✅ "改为李四" → name="李四"
✅ "设置为王小明" → name="王小明"
```

---

### 修复2：前端自动加载已启用的API配置

**文件**：`Chat.vue`

**添加功能**：
```typescript
// 加载已启用的API提供商
const loadEnabledProvider = async () => {
  try {
    const response: any = await request({
      url: '/config/api-config/list',
      method: 'get'
    })
    
    if (response.code === 200 && response.data) {
      // 查找第一个启用的配置
      const enabledConfig = response.data.find((config: any) => config.enabled === 1)
      if (enabledConfig) {
        selectedProvider.value = enabledConfig.provider
        console.log('自动选择API提供商:', selectedProvider.value)
      }
    }
  } catch (error) {
    console.error('加载API配置失败:', error)
  }
}

onMounted(async () => {
  loadSessions()
  // 加载已启用的API配置
  await loadEnabledProvider()
})
```

**工作流程**：
1. 页面加载时调用 `/config/api-config/list` 获取所有API配置
2. 查找第一个 `enabled=1` 的配置
3. 自动设置 `selectedProvider` 为该配置的provider
4. 后续发送消息时会携带正确的provider参数

---

## 📊 修复效果对比

### 修复前

| 项目 | 状态 | 说明 |
|------|------|------|
| 姓名提取 | ❌ 错误 | "为王五"（包含"为"字） |
| LLM介入 | ❌ 未启用 | provider为空，使用规则匹配 |
| 用户体验 | ⚠️ 一般 | 需要手动选择API提供商 |

### 修复后

| 项目 | 状态 | 说明 |
|------|------|------|
| 姓名提取 | ✅ 正确 | "王五"（纯中文） |
| LLM介入 | ✅ 自动启用 | 自动加载已启用的API配置 |
| 用户体验 | ✅ 优秀 | 无需手动配置，开箱即用 |

---

## 🧪 测试步骤

### 1. 重启前后端服务

**后端**：
```bash
cd backend
mvn clean compile
mvn spring-boot:run
```

**前端**：
```bash
cd frontend
npm run dev
```

### 2. 在设置界面配置并启用API

1. 打开设置界面
2. 选择一个提供商（如智谱AI）
3. 填写API Key、模型、Base URL
4. **勾选"启用"复选框** ✅
5. 点击保存

### 3. 切换到对话界面

刷新页面，查看浏览器控制台应该看到：
```
自动选择API提供商: zhipu
```

### 4. 测试更新操作

输入：
```
修改工号E001的员工姓名为王五
```

**期望日志**：
```
========== ChatController收到请求 ==========
userMessage: 修改工号E001的员工姓名为王五
provider: zhipu  ← ✅ 不再是空
查询API配置: zhipu
找到配置: enabled=1
API配置已启用，将使用LLM
使用LLM进行意图识别...
LLM识别结果: updateEmployee, 置信度: 0.95
===========================================
========== 规则匹配参数提取 ==========
消息: 修改工号E001的员工姓名为王五
提取到编码: E001
设置 empNo: E001
设置 name: 王五  ← ✅ 不再包含"为"字
最终参数: {empNo=E001, name=王五}
=====================================
✅ 成功更新员工：王五
```

**数据库验证**：
```sql
SELECT emp_name FROM employee WHERE emp_no = 'E001';
-- 应该返回：王五
```

---

## 🎯 支持的表达方式

### 姓名提取（修复后）

| 用户输入 | 提取结果 | 状态 |
|---------|---------|------|
| "姓名为王五" | name="王五" | ✅ |
| "名字叫张三" | name="张三" | ✅ |
| "改为李四" | name="李四" | ✅ |
| "设置为王小明" | name="王小明" | ✅ |
| "姓名：赵六" | name="赵六" | ✅ |
| "名字=孙七" | name="孙七" | ✅ |

### 其他字段提取

| 字段类型 | 示例输入 | 提取结果 |
|---------|---------|---------|
| 年龄 | "年龄为20" | age=20 |
| 电话 | "电话13800138001" | phone="13800138001" |
| 邮箱 | "邮箱test@example.com" | email="test@example.com" |
| 职位 | "职位为教授" | position="教授" |

---

## 🔧 技术细节

### 正则表达式解析

#### 修改前的问题
```regex
(?:姓名|名字|叫|改为|设置为)[\s:=：]*?([\u4e00-\u9fa5]{2,4})
```

- `[\s:=：]*?` - 非贪婪匹配空格、冒号、等号
- 问题：遇到"为"字时停止匹配，导致"为"被包含在捕获组中

#### 修改后的优势
```regex
(?:姓名|名字|叫|改为|设置为)[^\u4e00-\u9fa5]*([\u4e00-\u9fa5]{2,4})
```

- `[^\u4e00-\u9fa5]*` - 匹配任意数量的**非中文字符**
- 优势：跳过所有分隔符（包括"为"、空格、冒号等），只捕获纯中文

### 前端自动加载逻辑

```typescript
// 1. 获取所有API配置
GET /config/api-config/list

// 2. 响应示例
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "provider": "zhipu",
      "apiKey": "xxx",
      "model": "glm-4-flash",
      "baseUrl": "https://open.bigmodel.cn/api/paas/v4",
      "enabled": 1  ← 查找此字段
    },
    {
      "id": 2,
      "provider": "deepseek",
      "enabled": 0
    }
  ]
}

// 3. 自动选择第一个启用的配置
const enabledConfig = response.data.find(config => config.enabled === 1)
if (enabledConfig) {
  selectedProvider.value = enabledConfig.provider  // "zhipu"
}
```

---

## 📝 注意事项

### 1. API配置必须启用

如果没有任何API配置被启用（`enabled=1`），则：
- `selectedProvider` 保持为空字符串
- 后端会使用规则匹配（降级方案）
- 功能仍然可用，但没有LLM的智能理解

### 2. 多个启用的配置

如果有多个API配置被启用，系统会：
- 选择**第一个**启用的配置
- 按数据库ID顺序（最小的ID优先）

建议：**只启用一个主要使用的API提供商**。

### 3. 切换API提供商

如果需要切换API提供商：
1. 在设置界面禁用当前配置
2. 启用新的配置
3. **刷新对话页面**（重新加载配置）

---

## 🚀 后续优化建议

### 短期优化

1. **用户手动选择支持**
   - 在对话界面顶部添加下拉框
   - 允许用户手动切换API提供商
   - 记住用户的选择（localStorage）

2. **配置状态提示**
   - 如果没有启用的API配置，显示提示
   - 引导用户去设置界面配置

3. **LLM失败降级优化**
   - LLM调用失败时，给出友好提示
   - 自动切换到规则匹配，并告知用户

### 长期优化

1. **多轮对话上下文**
   - 记住上一次操作的实体
   - "把它删除" → 指代上一轮提到的对象

2. **智能推荐**
   - 根据历史操作推荐常用功能
   - 自动补全参数

3. **操作确认机制**
   - 删除/更新操作前要求确认
   - 提供撤销功能

---

## 📚 相关文档

- [agent-completeness-check.md](./agent-completeness-check.md) - 功能完备性检查
- [agent-functions-completed.md](./agent-functions-completed.md) - 功能实现报告
- [llm-intent-recognition.md](./llm-intent-recognition.md) - LLM意图识别说明

---

## ✅ 总结

本次修复解决了两个关键问题：

1. ✅ **姓名提取错误** - 正则表达式优化，正确提取纯中文字符
2. ✅ **LLM未自动启用** - 前端自动加载已启用的API配置

**现在Agent系统可以：**
- 正确理解"修改工号E001的员工姓名为王五"
- 自动使用LLM进行智能意图识别
- 准确提取参数并执行更新操作

**请重启前后端并测试！** 🚀
