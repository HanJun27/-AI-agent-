# Agent删除功能调试指南

## 问题描述

用户输入"删除学号为1的学生"，Agent显示"✅ 成功删除学生（ID：1）"，但数据库中学号为1的学生仍然存在。

## 可能原因

### 1. **参数提取错误**
LLM可能将"1"提取为`id`而不是`stuNo`，导致删除了ID=1的学生（可能不是学号=1的学生）。

### 2. **业务字段转换失败**
即使提取了`stuNo`，查找对应ID的逻辑可能有问题。

### 3. **事务未提交**
删除操作可能在事务中，但未正确提交。

## 调试步骤

### **Step 1: 重启后端并查看日志**

```bash
cd backend
mvn clean compile
mvn spring-boot:run
```

### **Step 2: 测试删除操作**

在AI对话界面输入：
```
删除学号为1的学生
```

### **Step 3: 检查后端控制台输出**

应该看到类似以下日志：

#### **正常情况（通过学号查找）**
```
使用LLM进行意图识别...
LLM原始响应: {"intent":"DELETE","action":"deleteStudent","params":{"stuNo":"1"},"confidence":0.95}
清理后JSON: {"intent":"DELETE","action":"deleteStudent","params":{"stuNo":"1"},"confidence":0.95}
LLM提取的参数节点: {"stuNo":"1"}
  - stuNo: 1
LLM识别结果: deleteStudent, 置信度: 0.95
========== 删除学生参数 ==========
params: {stuNo=1}
id: null
stuNo: 1
通过学号查找学生: 1
找到学生: ID=5, 姓名=张三
即将删除学生ID: 5
```

**关键点**：
- ✅ `stuNo: 1` - LLM正确提取了学号
- ✅ `id: null` - 没有直接提供ID
- ✅ `通过学号查找学生` - 触发了学号查找逻辑
- ✅ `找到学生: ID=5` - 学号1对应的数据库ID是5
- ✅ `即将删除学生ID: 5` - 实际删除的是ID=5

#### **异常情况（直接使用ID）**
```
LLM提取的参数节点: {"id":1}
  - id: 1
========== 删除学生参数 ==========
params: {id=1}
id: 1
stuNo: null
即将删除学生ID: 1
```

**问题**：
- ❌ `id: 1` - LLM错误地将"1"当作ID
- ❌ `stuNo: null` - 没有提取学号
- ❌ 直接删除ID=1，可能不是学号=1的学生

### **Step 4: 验证数据库**

```sql
-- 查看所有学生
SELECT id, stu_no, stu_name FROM student;

-- 确认学号为1的学生是否存在
SELECT * FROM student WHERE stu_no = '1';

-- 确认ID为1的学生是谁
SELECT * FROM student WHERE id = 1;
```

## 常见问题及解决方案

### **问题1: LLM总是提取id而不是stuNo**

**原因**：System Prompt不够清晰，LLM不理解"学号"和"ID"的区别。

**解决**：优化System Prompt，添加更多示例：

```java
// 在 IntentRecognitionService.java 的 SYSTEM_PROMPT 中添加

"## 重要区分：\n" +
"- id: 数据库主键ID（用户通常不会直接说'ID'）\n" +
"- stuNo: 学号（用户说'学号为X'、'学号X'时使用）\n" +
"- empNo: 工号（用户说'工号为X'、'工号X'时使用）\n\n" +

"## 示例：\n" +
"用户：\"删除学号为1的学生\"\n" +
"正确：{\"params\":{\"stuNo\":\"1\"}}\n" +
"错误：{\"params\":{\"id\":1}}\n\n" +

"用户：\"删除ID为5的学生\"\n" +
"正确：{\"params\":{\"id\":5}}\n" +
```

### **问题2: 学号查找返回null**

**原因**：
1. 数据库中不存在该学号
2. 学号格式不匹配（如"1" vs "S001"）

**解决**：
```sql
-- 检查学号格式
SELECT stu_no FROM student LIMIT 10;

-- 可能的格式：
-- "1", "001", "S001", "2021001"
```

如果学号格式是"S001"，用户说"学号为1"时，需要模糊匹配或提示用户完整学号。

### **问题3: 删除成功但数据仍在**

**原因**：
1. 事务未提交
2. 删除了错误的记录
3. 前端缓存未刷新

**解决**：
```java
// 检查 StudentServiceImpl 中的 deleteStudent 方法
@Override
public void deleteStudent(Integer id) {
    Student existing = studentMapper.findById(id);
    if (existing == null) {
        throw new RuntimeException("学生不存在");
    }
    studentMapper.deleteById(id);
    // 确保没有 @Transactional 注解导致的事务问题
}
```

## 修复方案

### **方案1: 优化LLM Prompt（推荐）**

在 `IntentRecognitionService.java` 中增强System Prompt：

```java
private static final String SYSTEM_PROMPT = 
    // ... 现有内容 ...
    
    "## 参数提取规则（非常重要）：\n" +
    "1. 当用户说'学号为X'、'学号X'时，必须使用 stuNo 字段，不要用 id\n" +
    "2. 当用户说'工号为X'、'工号X'时，必须使用 empNo 字段，不要用 id\n" +
    "3. 只有当用户明确说'ID为X'时，才使用 id 字段\n" +
    "4. 数字本身（如'删除学生 1'）优先作为 stuNo/empNo，除非上下文明确是ID\n\n" +
    
    "## 错误示例：\n" +
    "❌ 用户：\"删除学号为1的学生\" → {\"id\":1}  // 错误！\n" +
    "✅ 用户：\"删除学号为1的学生\" → {\"stuNo\":\"1\"}  // 正确！\n\n" +
    
    "## 正确示例：\n" +
    "用户：\"删除ID为5的学生\" → {\"id\":5}\n" +
    "用户：\"删除学号为S001的学生\" → {\"stuNo\":\"S001\"}\n" +
    "用户：\"删除工号E002的员工\" → {\"empNo\":\"E002\"}\n";
```

### **方案2: 增强参数提取逻辑**

在 `AgentService.java` 的 `extractParameters` 方法中添加后处理：

```java
private Map<String, Object> extractParameters(String message, Intent intent) {
    Map<String, Object> params = new HashMap<>();
    
    // ... 现有的正则提取逻辑 ...
    
    // 后处理：如果同时有id和stuNo/empNo，优先使用业务字段
    if (message.contains("学号") && params.containsKey("id")) {
        // 用户说了"学号"，但LLM提取成了id，转换为stuNo
        params.put("stuNo", String.valueOf(params.remove("id")));
    }
    
    if (message.contains("工号") && params.containsKey("id")) {
        // 用户说了"工号"，但LLM提取成了id，转换为empNo
        params.put("empNo", String.valueOf(params.remove("id")));
    }
    
    return params;
}
```

### **方案3: 添加二次确认**

对于删除操作，添加确认机制：

```java
public String deleteStudent(Map<String, Object> params) {
    Integer id = (Integer) params.get("id");
    String stuNo = (String) params.get("stuNo");
    
    // 先查询要删除的学生信息
    Student targetStudent = null;
    if (stuNo != null) {
        targetStudent = studentMapper.findByStuNo(stuNo);
    } else if (id != null) {
        targetStudent = studentMapper.findById(id);
    }
    
    if (targetStudent == null) {
        return "❌ 未找到要删除的学生";
    }
    
    // 显示将要删除的学生信息
    String confirmMsg = String.format(
        "即将删除学生：%s（学号：%s，ID：%d），是否确认？",
        targetStudent.getStuName(),
        targetStudent.getStuNo(),
        targetStudent.getId()
    );
    
    // TODO: 等待用户确认（需要实现多轮对话）
    // 暂时直接执行
    studentService.deleteStudent(targetStudent.getId());
    return String.format("✅ 成功删除学生：%s（ID：%d）", 
                         targetStudent.getStuName(), 
                         targetStudent.getId());
}
```

## 验证清单

完成修复后，验证以下场景：

| 用户输入 | 期望行为 | 验证方法 |
|---------|---------|---------|
| "删除学号为1的学生" | 删除学号=1的学生 | 检查日志中是否有"通过学号查找" |
| "删除ID为5的学生" | 删除ID=5的学生 | 检查日志中id=5 |
| "删除工号E001的员工" | 删除工号=E001的员工 | 检查日志中empNo=E001 |
| "删除学生 1" | 删除学号=1的学生 | 检查是否优先作为stuNo |

## 相关文件

- ✅ `IntentRecognitionService.java` - 添加调试日志
- ✅ `CrudOperationTool.java` - 添加调试日志
- 📝 `docs/agent-delete-debug.md` - 本文档

## 下一步

1. **重启后端服务**
2. **测试删除操作**
3. **查看控制台日志**
4. **根据日志判断问题所在**
5. **应用相应的修复方案**
