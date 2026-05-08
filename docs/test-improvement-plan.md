# 自动化测试改进方案

**创建时间**: 2026-05-09  
**问题**: 现有测试未能覆盖参数混淆问题

---

## 🔍 问题分析

### 为什么现有测试没发现问题？

1. **缺少修改姓名的测试场景**
   - 所有update测试只修改非姓名字段（电话、位置、人数）
   - 没有测试"name + newName"参数组合

2. **只使用编号定位，未测试姓名定位**
   - 测试用例：`学号TEST001`、`部门编号TEST_DEPT`
   - 缺失用例：`名字为李小红`、`名为张三的学生`

3. **断言过于宽松**
   - 只检查是否包含"成功"或"修改"
   - 没有检查实际的成功标志"✅ 成功更新"
   - 没有验证数据库实际变更

4. **缺少边界情况测试**
   - 同名多人怎么办？
   - 找不到人时的错误处理
   - 参数缺失时的提示

---

## ✅ 需要补充的测试用例

### 测试1：通过姓名修改学生姓名 ⭐⭐⭐

```java
@Test
@Order(7)
@DisplayName("测试通过姓名修改学生姓名")
public void testUpdateStudentNameByName() {
    System.out.println("\n========== 测试7：通过姓名修改学生姓名 ==========");
    
    // 步骤1：先添加一个测试学生
    String addMessage = "添加一个学生，学号NAME_TEST_001，姓名原名字，性别男，年龄20";
    String addResult = llmAgentService.processMessage(addMessage, apiKey, model, baseUrl);
    assertTrue(addResult.contains("成功"), "添加学生应成功");
    
    // 步骤2：通过姓名修改姓名
    String updateMessage = "把名字为'原名字'的学生名字改为'新名字'";
    String updateResult = llmAgentService.processMessage(updateMessage, apiKey, model, baseUrl);
    
    System.out.println("用户输入: " + updateMessage);
    System.out.println("Agent响应: " + updateResult);
    
    // 严格断言：必须包含成功标志
    assertNotNull(updateResult);
    assertTrue(updateResult.contains("✅") || updateResult.contains("成功更新"), 
        "修改姓名应返回成功标志，实际: " + updateResult);
    
    // 验证数据库实际变更
    Student updatedStudent = studentMapper.findByStuNo("NAME_TEST_001");
    assertNotNull(updatedStudent, "学生应存在");
    assertEquals("新名字", updatedStudent.getStuName(), 
        "学生姓名应被修改为新名字");
    
    System.out.println("✅ 通过姓名修改学生姓名测试通过");
}
```

---

### 测试2：通过姓名修改员工姓名 ⭐⭐⭐

```java
@Test
@Order(8)
@DisplayName("测试通过姓名修改员工姓名")
public void testUpdateEmployeeNameByName() {
    System.out.println("\n========== 测试8：通过姓名修改员工姓名 ==========");
    
    // 步骤1：先添加一个测试员工
    String addMessage = "添加一个员工，工号EMP_NAME_TEST，姓名原名，职位教师";
    String addResult = llmAgentService.processMessage(addMessage, apiKey, model, baseUrl);
    assertTrue(addResult.contains("成功"), "添加员工应成功");
    
    // 步骤2：通过姓名修改姓名
    String updateMessage = "把名字为'原名'的员工的名字改为'新名'";
    String updateResult = llmAgentService.processMessage(updateMessage, apiKey, model, baseUrl);
    
    System.out.println("用户输入: " + updateMessage);
    System.out.println("Agent响应: " + updateResult);
    
    // 严格断言
    assertNotNull(updateResult);
    assertTrue(updateResult.contains("✅") || updateResult.contains("成功更新"), 
        "修改姓名应返回成功标志，实际: " + updateResult);
    
    // 验证数据库
    Employee updatedEmployee = employeeMapper.findByEmpNo("EMP_NAME_TEST");
    assertNotNull(updatedEmployee, "员工应存在");
    assertEquals("新名", updatedEmployee.getEmpName(), 
        "员工姓名应被修改为新名");
    
    System.out.println("✅ 通过姓名修改员工姓名测试通过");
}
```

---

### 测试3：通过班级名称修改班级名称 ⭐⭐

```java
@Test
@Order(9)
@DisplayName("测试通过班级名称修改班级名称")
public void testUpdateClassNameByName() {
    System.out.println("\n========== 测试9：通过班级名称修改班级名称 ==========");
    
    // 步骤1：先添加一个测试班级
    String addMessage = "添加一个班级，编号CLASS_NAME_TEST，名称原班级名，人数50";
    String addResult = llmAgentService.processMessage(addMessage, apiKey, model, baseUrl);
    assertTrue(addResult.contains("成功"), "添加班级应成功");
    
    // 步骤2：通过班级名称修改班级名称
    String updateMessage = "把班级'原班级名'的名称改为'新班级名'";
    String updateResult = llmAgentService.processMessage(updateMessage, apiKey, model, baseUrl);
    
    System.out.println("用户输入: " + updateMessage);
    System.out.println("Agent响应: " + updateResult);
    
    // 严格断言
    assertNotNull(updateResult);
    assertTrue(updateResult.contains("✅") || updateResult.contains("成功更新"), 
        "修改班级名应返回成功标志，实际: " + updateResult);
    
    // 验证数据库
    ClassInfo updatedClass = classInfoMapper.findByClassNo("CLASS_NAME_TEST");
    assertNotNull(updatedClass, "班级应存在");
    assertEquals("新班级名", updatedClass.getClassName(), 
        "班级名称应被修改为新班级名");
    
    System.out.println("✅ 通过班级名称修改班级名称测试通过");
}
```

---

### 测试4：通过部门名称修改部门名称 ⭐⭐

```java
@Test
@Order(10)
@DisplayName("测试通过部门名称修改部门名称")
public void testUpdateDepartmentNameByName() {
    System.out.println("\n========== 测试10：通过部门名称修改部门名称 ==========");
    
    // 步骤1：先添加一个测试部门
    String addMessage = "添加一个部门，编号DEPT_NAME_TEST，名称原部门名，位置行政楼";
    String addResult = llmAgentService.processMessage(addMessage, apiKey, model, baseUrl);
    assertTrue(addResult.contains("成功"), "添加部门应成功");
    
    // 步骤2：通过部门名称修改部门名称
    String updateMessage = "把部门'原部门名'的名称改为'新部门名'";
    String updateResult = llmAgentService.processMessage(updateMessage, apiKey, model, baseUrl);
    
    System.out.println("用户输入: " + updateMessage);
    System.out.println("Agent响应: " + updateResult);
    
    // 严格断言
    assertNotNull(updateResult);
    assertTrue(updateResult.contains("✅") || updateResult.contains("成功更新"), 
        "修改部门名应返回成功标志，实际: " + updateResult);
    
    // 验证数据库
    Department updatedDept = departmentMapper.findByDeptNo("DEPT_NAME_TEST");
    assertNotNull(updatedDept, "部门应存在");
    assertEquals("新部门名", updatedDept.getDeptName(), 
        "部门名称应被修改为新部门名");
    
    System.out.println("✅ 通过部门名称修改部门名称测试通过");
}
```

---

### 测试5：找不到人时的错误处理 ⭐

```java
@Test
@Order(11)
@DisplayName("测试找不到人时的错误处理")
public void testUpdateNonExistentPerson() {
    System.out.println("\n========== 测试11：找不到人时的错误处理 ==========");
    
    // 尝试修改不存在的学生
    String updateMessage = "把名字为'不存在的名字'的学生电话改为13800000000";
    String updateResult = llmAgentService.processMessage(updateMessage, apiKey, model, baseUrl);
    
    System.out.println("用户输入: " + updateMessage);
    System.out.println("Agent响应: " + updateResult);
    
    // 应该返回友好的错误提示
    assertNotNull(updateResult);
    assertTrue(updateResult.contains("未找到") || updateResult.contains("不存在") || 
               updateResult.contains("❌"), 
        "找不到人时应返回错误提示，实际: " + updateResult);
    
    System.out.println("✅ 找不到人时的错误处理测试通过");
}
```

---

### 测试6：同时修改多个字段 ⭐

```java
@Test
@Order(12)
@DisplayName("测试同时修改多个字段")
public void testUpdateMultipleFields() {
    System.out.println("\n========== 测试12：同时修改多个字段 ==========");
    
    // 步骤1：先添加一个测试学生
    String addMessage = "添加一个学生，学号MULTI_UPDATE_TEST，姓名测试生，性别男，年龄20";
    String addResult = llmAgentService.processMessage(addMessage, apiKey, model, baseUrl);
    assertTrue(addResult.contains("成功"), "添加学生应成功");
    
    // 步骤2：同时修改年龄和电话
    String updateMessage = "把学号为MULTI_UPDATE_TEST的学生年龄改为22，电话改为13900000000";
    String updateResult = llmAgentService.processMessage(updateMessage, apiKey, model, baseUrl);
    
    System.out.println("用户输入: " + updateMessage);
    System.out.println("Agent响应: " + updateResult);
    
    // 严格断言
    assertNotNull(updateResult);
    assertTrue(updateResult.contains("✅") || updateResult.contains("成功更新"), 
        "多字段修改应返回成功标志，实际: " + updateResult);
    
    // 验证数据库
    Student updatedStudent = studentMapper.findByStuNo("MULTI_UPDATE_TEST");
    assertNotNull(updatedStudent, "学生应存在");
    assertEquals(22, updatedStudent.getAge(), "年龄应被修改为22");
    assertEquals("13900000000", updatedStudent.getPhone(), "电话应被修改为13900000000");
    
    System.out.println("✅ 同时修改多个字段测试通过");
}
```

---

## 📋 测试覆盖矩阵

| 测试场景 | 定位方式 | 修改字段 | 当前测试 | 需要补充 |
|---------|---------|---------|---------|---------|
| 修改学生电话 | 学号 | phone | ✅ | - |
| 修改学生姓名 | 姓名 | name→newName | ❌ | ⭐⭐⭐ |
| 修改员工姓名 | 姓名 | name→newName | ❌ | ⭐⭐⭐ |
| 修改班级名称 | 班级名 | className→newName | ❌ | ⭐⭐ |
| 修改部门名称 | 部门名 | deptName→newName | ❌ | ⭐⭐ |
| 修改班级人数 | 班级号 | studentCount | ✅ | - |
| 修改部门位置 | 部门号 | location | ✅ | - |
| 找不到人 | 姓名 | - | ❌ | ⭐ |
| 多字段修改 | 学号 | age+phone | ❌ | ⭐ |

---

## 🎯 改进原则

### 1. 断言要严格

```java
// ❌ 宽松断言（可能误判）
assertTrue(updateResult.contains("成功") || updateResult.contains("修改"));

// ✅ 严格断言（准确判断）
assertTrue(updateResult.contains("✅") || updateResult.contains("成功更新"));
```

### 2. 验证数据库实际变更

```java
// 不仅检查响应文本，还要验证数据库
Student updatedStudent = studentMapper.findByStuNo("TEST001");
assertEquals("新值", updatedStudent.get某个字段());
```

### 3. 覆盖多种定位方式

- 编号定位（精确）：stuNo, empNo, classNo, deptNo
- 名称定位（模糊）：name, className, deptName
- ID定位（内部）：id

### 4. 覆盖边界情况

- 找不到记录
- 同名多人
- 参数缺失
- 参数类型错误

---

## 🚀 实施计划

### 阶段1：补充核心测试（P0）
- [ ] 测试通过姓名修改学生姓名
- [ ] 测试通过姓名修改员工姓名
- [ ] 测试通过班级名称修改班级名称
- [ ] 测试通过部门名称修改部门名称

### 阶段2：补充边界测试（P1）
- [ ] 测试找不到人时的错误处理
- [ ] 测试同名多人的处理
- [ ] 测试参数缺失的提示

### 阶段3：补充复杂场景（P2）
- [ ] 测试同时修改多个字段
- [ ] 测试批量更新
- [ ] 测试事务回滚

---

## 📊 预期效果

补充测试后：
- ✅ 能够发现"name vs newName"参数混淆问题
- ✅ 能够发现缺少通过姓名查找的支持
- ✅ 能够验证数据库实际变更
- ✅ 提高测试覆盖率和可靠性
- ✅ 防止类似问题再次出现

---

**创建时间**: 2026-05-09  
**状态**: 待实施
