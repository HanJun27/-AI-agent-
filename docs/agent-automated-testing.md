# Agent自动化测试指南

## 📋 概述

本测试套件用于自动化测试Agent的意图识别、参数提取和CRUD操作功能。

### ✨ 核心特性

1. **数据隔离** - 使用`@Transactional`确保测试后数据自动回滚
2. **全面覆盖** - 测试查询、添加、删除、更新等所有功能
3. **自动验证** - 使用断言检查返回结果和数据状态
4. **详细日志** - 每个测试都输出执行结果

---

## 🚀 运行测试

### 方法1：使用Maven命令

```bash
cd backend
mvn test -Dtest=AgentIntentRecognitionTest
```

### 方法2：使用IDEA

1. 打开 `AgentIntentRecognitionTest.java`
2. 右键点击类名或测试方法
3. 选择 "Run 'AgentIntentRecognitionTest'"

### 方法3：运行所有测试

```bash
cd backend
mvn test
```

---

## 📊 测试用例清单

### ✅ 查询功能测试

| 编号 | 测试名称 | 输入 | 预期输出 |
|------|---------|------|---------|
| 1 | 查询学生列表 | "查看所有学生" | 返回学生列表 |
| 2 | 查询员工列表 | "显示所有员工" | 返回员工列表 |
| 3 | 统计学生数量 | "统计学生数量" | 返回数字 |

### ✅ 添加功能测试（会回滚）

| 编号 | 测试名称 | 输入 | 预期输出 |
|------|---------|------|---------|
| 4 | 添加学生 | "添加学生 学号TEST001 姓名测试学生 年龄20" | 成功添加，数据可查询 |
| 5 | 添加员工 | "添加员工 工号TEST001 姓名测试员工 年龄30 职位教师" | 成功添加，数据可查询 |
| 6 | 添加部门 | "添加部门 编号TEST001 名称测试部门" | 成功添加，数据可查询 |

### ✅ 删除功能测试（会回滚）

| 编号 | 测试名称 | 输入 | 预期输出 |
|------|---------|------|---------|
| 7 | 删除学生-按学号 | "删除学号为DELETE_TEST_001的学生" | 学生被删除 |
| 8 | 删除员工-按工号 | "删除工号为DELETE_TEST_001的员工" | 员工被删除 |

### ✅ 更新功能测试（会回滚）

| 编号 | 测试名称 | 输入 | 预期输出 |
|------|---------|------|---------|
| 9 | 更新学生姓名 | "修改学号UPDATE_TEST_001的学生姓名为新名字" | 姓名更新为"新名字" |
| 10 | 更新员工信息 | "修改工号UPDATE_TEST_001的员工姓名为新姓名" | 姓名更新为"新姓名" |
| 11 | 参数提取准确性 | "修改工号PARAM_TEST_001的员工姓名为王五" | 姓名为"王五"（不含"为"字） |

### ✅ 其他功能测试

| 编号 | 测试名称 | 输入 | 预期输出 |
|------|---------|------|---------|
| 12 | 分析功能 | "分析学生性别比例" | 返回分析结果 |
| 13 | 通用对话 | "你好" | 返回友好回复 |
| 14 | 错误处理 | "删除学号为NOT_EXIST_999的学生" | 返回错误提示 |
| 15 | 复杂语句理解 | 多种表达方式 | 正确理解并执行 |

---

## 🔍 测试原理

### 1. 事务回滚机制

```java
@SpringBootTest
@Transactional  // ← 关键注解
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AgentIntentRecognitionTest {
    // 所有测试方法执行后，Spring会自动回滚事务
    // 数据库保持原样，不受测试影响
}
```

**工作流程**：
```
测试开始
  ↓
开启事务
  ↓
执行测试操作（添加/删除/更新）
  ↓
断言验证
  ↓
测试结束 → 自动回滚事务 ← 数据恢复原状
```

### 2. 测试数据隔离

每个测试方法：
- ✅ 可以创建临时测试数据
- ✅ 在事务内可见和验证
- ✅ 测试结束后自动清理
- ✅ 不影响其他测试和数据库

### 3. 断言验证

```java
// 验证返回值
assertNotNull(result);
assertTrue(result.contains("成功"));

// 验证数据库状态
Student student = studentMapper.findByStuNo("TEST001");
assertEquals("测试学生", student.getStuName());
```

---

## 📝 测试输出示例

```
========================================
🧪 开始运行 Agent 自动化测试
========================================

✅ 测试1通过: 学生列表共 10 条记录...
✅ 测试2通过: 员工列表共 5 条记录...
✅ 测试3通过: 当前共有 10 名学生
✅ 测试4通过: 学生已添加（测试后将回滚）
✅ 测试5通过: 员工已添加（测试后将回滚）
✅ 测试6通过: 部门已添加（测试后将回滚）
✅ 测试7通过: 学生已删除（测试后将回滚）
✅ 测试8通过: 员工已删除（测试后将回滚）
✅ 测试9通过: 学生姓名已更新为'新名字'（测试后将回滚）
✅ 测试10通过: 员工姓名已更新为'新姓名'（测试后将回滚）
✅ 测试11通过: 姓名正确提取为'王五'（不包含'为'字）
✅ 测试12通过: 学生性别比例分析...
✅ 测试13通过: 您好！我是紫金学院智能管理顾问...
✅ 测试14通过: ❌ 未找到学号为 NOT_EXIST_999 的学生
  输入: 把学号COMPLEX_TEST_001的学生年龄改为20
  输出: ✅ 成功更新学生：张三
  输入: 更新学生张三的年龄为21
  输出: ✅ 成功更新学生：张三
  输入: 更改学号COMPLEX_TEST_001的学生姓名为李四
  输出: ✅ 成功更新学生：李四
✅ 测试15通过: 复杂语句理解正常

========================================
🎉 所有测试完成！
📊 由于使用了 @Transactional，所有测试数据已自动回滚
💾 数据库内容保持原样，未受影响
========================================

Tests run: 15, Failures: 0, Errors: 0, Skipped: 0
```

---

## ⚙️ 配置说明

### API配置

测试默认不使用LLM（API_KEY为空），会自动降级到规则匹配：

```java
private static final String API_KEY = "";
private static final String MODEL = "";
private static final String BASE_URL = "";
```

**如果要测试LLM功能**：
1. 在设置界面配置API
2. 从数据库获取配置信息
3. 填入测试类的常量中

### 测试顺序

使用`@Order`注解控制测试执行顺序：
- 先测试查询（不修改数据）
- 再测试添加（创建测试数据）
- 然后测试删除和更新（使用测试数据）
- 最后测试错误处理和复杂场景

---

## 🔧 自定义测试

### 添加新的测试用例

```java
@Test
@Order(16)
@DisplayName("测试新功能")
public void testNewFeature() {
    String message = "你的测试输入";
    String result = agentService.processMessage(message, API_KEY, MODEL, BASE_URL);
    
    // 添加断言
    assertNotNull(result);
    assertTrue(result.contains("预期内容"));
    
    System.out.println("✅ 测试16通过: " + result);
}
```

### 测试特定的参数提取

```java
@Test
@DisplayName("测试特定参数提取")
public void testSpecificParameterExtraction() {
    // 准备测试数据
    Student student = new Student();
    student.setStuNo("SPECIFIC_TEST");
    student.setStuName("原始名");
    studentMapper.insert(student);
    
    // 执行操作
    String message = "修改学号SPECIFIC_TEST的学生年龄为25";
    agentService.processMessage(message, API_KEY, MODEL, BASE_URL);
    
    // 验证结果
    Student updated = studentMapper.findByStuNo("SPECIFIC_TEST");
    assertEquals(25, updated.getAge(), "年龄应该更新为25");
}
```

---

## 🐛 常见问题

### Q1: 测试失败，提示数据库连接错误

**解决方案**：
1. 确保MySQL服务正在运行
2. 检查`application.yml`中的数据库配置
3. 确认数据库`zijin_college`存在

### Q2: 测试数据没有回滚

**可能原因**：
1. 忘记添加`@Transactional`注解
2. 使用了原生SQL绕过事务管理

**解决方案**：
确保测试类上有`@Transactional`注解

### Q3: 某些测试依赖前面的测试

**问题**：测试之间不应该有依赖关系

**解决方案**：
每个测试都应该独立准备自己的测试数据：
```java
@Test
public void testSomething() {
    // 自己创建需要的测试数据
    Student student = new Student();
    student.setStuNo("MY_TEST");
    studentMapper.insert(student);
    
    // 执行测试...
}
```

### Q4: 如何查看详细的测试日志？

**解决方案**：
在`application.yml`中添加：
```yaml
logging:
  level:
    com.zijin.college.agent: DEBUG
```

---

## 📈 测试覆盖率

### 当前覆盖的功能

- ✅ 查询操作（4个action）
- ✅ 添加操作（4个类型）
- ✅ 删除操作（4个类型，支持业务字段）
- ✅ 更新操作（4个类型，支持业务字段）
- ✅ 分析操作（5个action）
- ✅ 参数提取（姓名、年龄、电话、邮箱等）
- ✅ 错误处理
- ✅ 复杂语句理解

### 覆盖率目标

- **代码覆盖率**: > 80%
- **功能覆盖率**: 100%
- **边界情况**: 主要场景已覆盖

---

## 🎯 最佳实践

### 1. 测试命名规范

```java
// ✅ 好的命名
testAddStudent()
testDeleteEmployeeByEmpNo()
testUpdateStudentName()

// ❌ 不好的命名
test1()
testAdd()
myTest()
```

### 2. 断言清晰

```java
// ✅ 清晰的断言
assertEquals("王五", updated.getEmpName(), "员工姓名应该已更新");

// ❌ 不清晰的断言
assertTrue(updated != null);
```

### 3. 测试独立性

```java
// ✅ 每个测试独立准备数据
@Test
public void testA() {
    Student s = createTestStudent("TEST_A");
    // 测试逻辑...
}

@Test
public void testB() {
    Student s = createTestStudent("TEST_B");
    // 测试逻辑...
}
```

### 4. 有意义的输出

```java
// ✅ 提供有用的调试信息
System.out.println("✅ 测试通过: 学生姓名已更新为'新名字'");

// ❌ 无意义的输出
System.out.println("OK");
```

---

## 📚 相关文档

- [agent-completeness-check.md](./agent-completeness-check.md) - 功能完备性检查
- [agent-parameter-fix.md](./agent-parameter-fix.md) - 参数提取修复
- [llm-intent-recognition.md](./llm-intent-recognition.md) - LLM意图识别

---

## ✅ 总结

这个自动化测试套件提供了：

1. ✅ **15个测试用例** - 覆盖所有核心功能
2. ✅ **数据隔离** - 测试后自动回滚，不影响数据库
3. ✅ **自动验证** - 使用断言检查功能和数据状态
4. ✅ **详细日志** - 便于调试和问题定位
5. ✅ **易于扩展** - 可以轻松添加新的测试用例

**运行测试，确保Agent功能正常工作！** 🚀
