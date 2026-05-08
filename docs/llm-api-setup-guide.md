# LLM测试API配置快速指南

## 🚀 **3步完成配置**

### **步骤1：选择API提供商**

推荐以下3个提供商（都有免费额度）：

#### **选项A：智谱AI（推荐）** ⭐

- **官网**: https://open.bigmodel.cn/
- **免费额度**: 新用户赠送100万token
- **模型**: glm-4-flash（快速）、glm-4-plus（强大）
- **优点**: 中文理解能力强，速度快

**注册步骤**：
1. 访问 https://open.bigmodel.cn/
2. 点击"注册"，使用手机号或邮箱注册
3. 登录后进入"控制台" → "API Keys"
4. 点击"创建API Key"
5. 复制生成的Key

---

#### **选项B：DeepSeek**

- **官网**: https://platform.deepseek.com/
- **免费额度**: 新用户赠送50万token
- **模型**: deepseek-chat
- **优点**: 性价比高，推理能力强

**注册步骤**：
1. 访问 https://platform.deepseek.com/
2. 注册账号
3. 进入"API Keys"页面
4. 创建新的API Key
5. 复制Key

---

#### **选项C：通义千问**

- **官网**: https://dashscope.aliyun.com/
- **免费额度**: 每月赠送100万token
- **模型**: qwen-turbo、qwen-plus
- **优点**: 阿里出品，稳定可靠

**注册步骤**：
1. 访问 https://dashscope.aliyun.com/
2. 使用阿里云账号登录
3. 进入"API-KEY管理"
4. 创建新的Key
5. 复制Key

---

### **步骤2：填入测试类**

编辑文件：`backend/src/test/java/com/zijin/college/agent/AgentLLMRecognitionTest.java`

找到第45-47行：

```java
private static final String API_KEY = "";  // TODO: 填入你的API Key
private static final String MODEL = "glm-4-flash";
private static final String BASE_URL = "https://open.bigmodel.cn/api/paas/v4";
```

**如果使用智谱AI**：
```java
private static final String API_KEY = "sk-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx";  // 你的Key
private static final String MODEL = "glm-4-flash";
private static final String BASE_URL = "https://open.bigmodel.cn/api/paas/v4";
```

**如果使用DeepSeek**：
```java
private static final String API_KEY = "sk-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx";  // 你的Key
private static final String MODEL = "deepseek-chat";
private static final String BASE_URL = "https://api.deepseek.com/v1";
```

**如果使用通义千问**：
```java
private static final String API_KEY = "sk-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx";  // 你的Key
private static final String MODEL = "qwen-turbo";
private static final String BASE_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1";
```

---

### **步骤3：运行测试**

**方法1：使用脚本（推荐）**
```bash
双击运行 run-llm-tests.bat
```

**方法2：命令行**
```bash
cd backend
mvn test -Dtest=AgentLLMRecognitionTest
```

**方法3：IDEA**
1. 打开 `AgentLLMRecognitionTest.java`
2. 右键点击类名
3. 选择 "Run 'AgentLLMRecognitionTest'"

---

## 🔍 **验证配置是否成功**

运行测试后，看到以下输出表示配置成功：

```
========================================
🧪 LLM语义识别专项测试
========================================
✅ API配置已设置
📡 模型: glm-4-flash
🌐 Base URL: https://open.bigmodel.cn/api/paas/v4
========================================

========== 测试1：标准表达 vs 口语化表达 ==========
标准表达: 删除学号为S001的学生
LLM回复: ✅ 成功删除学生（ID：1）
...
```

如果看到以下提示，说明API未配置：

```
⚠️  警告：API_KEY未配置！
💡 请在测试类中填入真实的API密钥以启用LLM测试
```

---

## 💰 **费用说明**

### **智谱AI定价**

| 模型 | 价格 | 免费额度 |
|------|------|---------|
| glm-4-flash | 0.1元/百万token | 100万token |
| glm-4-plus | 0.5元/百万token | 100万token |

**测试消耗估算**：
- 每次测试约消耗 1000-2000 token
- 12个测试用例总计约 20,000 token
- 费用：约 0.002 元（几乎免费）

### **DeepSeek定价**

| 模型 | 价格 | 免费额度 |
|------|------|---------|
| deepseek-chat | 0.14元/百万token | 50万token |

### **通义千问定价**

| 模型 | 价格 | 免费额度 |
|------|------|---------|
| qwen-turbo | 0.08元/百万token | 100万token/月 |

---

## ⚠️ **注意事项**

### **1. API Key安全**

❌ **不要**将API Key提交到Git仓库
✅ **应该**使用环境变量或配置文件

如果需要更安全的方式：

```java
// 从环境变量读取
private static final String API_KEY = System.getenv("LLM_API_KEY");
```

然后在系统中设置环境变量：
```bash
# Windows
set LLM_API_KEY=sk-your-key-here

# Linux/Mac
export LLM_API_KEY=sk-your-key-here
```

---

### **2. 配额管理**

**查看使用情况**：
- 智谱AI: 控制台 → 用量统计
- DeepSeek: 控制台 → 账单
- 通义千问: 控制台 → 用量查询

**设置提醒**：
在API提供商控制台设置用量提醒，避免超额。

---

### **3. 网络要求**

确保能够访问API服务：
- 智谱AI: open.bigmodel.cn
- DeepSeek: api.deepseek.com
- 通义千问: dashscope.aliyuncs.com

如果遇到连接超时：
1. 检查网络连接
2. 检查防火墙设置
3. 尝试更换网络环境

---

## 🆘 **常见问题**

### **Q1: 如何知道我的API Key是否有效？**

运行一个简单的测试：
```java
@Test
public void testApiKey() {
    String result = executeLLMTest("你好");
    System.out.println(result);
    assertNotNull(result);
}
```

如果返回正常回复，说明Key有效。

---

### **Q2: 测试失败，提示"Invalid API Key"**

**原因**：API Key错误或已过期

**解决**：
1. 重新复制API Key（注意不要复制空格）
2. 检查Key是否在有效期内
3. 确认账户余额充足

---

### **Q3: 测试很慢，每次都要等几秒**

**原因**：LLM需要网络请求

**优化**：
1. 使用更快的模型（如glm-4-flash而非glm-4-plus）
2. 检查网络延迟
3. 考虑使用本地部署的LLM（高级用法）

---

### **Q4: 我想先不花钱，能测试吗？**

**可以！** 所有推荐的提供商都有免费额度：
- 智谱AI: 100万token（足够测试几百次）
- DeepSeek: 50万token
- 通义千问: 每月100万token

测试12个用例只消耗约2万token，完全在免费额度内。

---

### **Q5: 测试完成后，数据库会受影响吗？**

**不会！** 测试使用了 `@Transactional` 注解：
- 所有测试数据在测试结束后自动回滚
- 数据库保持原样
- 不会影响生产数据

---

## 📞 **获取帮助**

如果遇到问题：

1. **查看日志** - 详细的错误信息会在控制台输出
2. **检查文档** - [llm-recognition-testing.md](./llm-recognition-testing.md)
3. **联系API提供商** - 查看官方文档和支持

---

## ✅ **配置检查清单**

在运行测试前，确认：

- [ ] 已注册API提供商账号
- [ ] 已创建API Key
- [ ] API Key已填入测试类
- [ ] MODEL和BASE_URL配置正确
- [ ] 网络连接正常
- [ ] 账户有足够配额

**全部勾选后，就可以开始测试了！** 🎉
