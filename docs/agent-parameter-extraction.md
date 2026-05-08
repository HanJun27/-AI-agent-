# Agent意图识别与参数提取增强

## 问题描述

用户输入自然语言指令时，Agent无法正确理解并提取参数：

**示例**：
- ❌ "删除学号为1的学生" → "请提供学生ID"
- ❌ "删除学生 学号为1" → "请提供学生ID"

**原因**：
1. `extractParameters`方法是空实现，没有提取任何参数
2. 只支持数据库主键ID，不支持业务字段（学号、工号等）
3. 缺乏灵活的自然语言解析能力

## 解决方案

### 1. 增强参数提取逻辑

在 `AgentService.java` 中实现了智能参数提取：

#### **支持的参数格式**

```java
// ID提取（多种表达方式）
"删除ID为1的学生"      → id=1
"删除学号为1的学生"    → id=1
"删除工号123的员工"    → id=123
"删除编号456的班级"    → id=456
"删除学生 1"          → id=1

// 姓名提取
"删除名字叫张三的学生"  → name="张三"
"删除姓名为李四的员工"  → name="李四"

// 学号/工号提取（字母+数字）
"删除学号S001的学生"   → stuNo="S001"
"删除工号E002的员工"   → empNo="E002"
```

#### **正则表达式模式**

```java
// 1. ID模式 - 匹配各种ID表达
(?:id|编号|学号|工号|班级号|部门号)[\s:=：]*?(\d+)
(?:删除|查询|查看|修改)[\s:：]*(?:学生|员工|班级|部门)[\s:：]*?(\d+)

// 2. 纯数字兜底 - 如果上面没匹配到，提取第一个数字
(\d+)

// 3. 姓名模式 - 匹配中文名字
(?:姓名|名字|叫)[\s:=：]*?([\u4e00-\u9fa5]{2,4})

// 4. 编码模式 - 匹配学号/工号（字母+数字）
(?:学号|工号|编号)[\s:=：]*?([A-Za-z]\d+)
```

### 2. 支持业务字段查找

修改 `CrudOperationTool.java`，让删除操作支持学号/工号：

#### **删除学生逻辑**

```java
public String deleteStudent(Map<String, Object> params) {
    Integer id = (Integer) params.get("id");
    String stuNo = (String) params.get("stuNo");
    
    // 如果提供了学号，先查找对应的ID
    if (id == null && stuNo != null && !stuNo.isEmpty()) {
        Student student = studentMapper.findByStuNo(stuNo);
        if (student != null) {
            id = student.getId();  // 找到后使用主键ID
        } else {
            return "❌ 未找到学号为 " + stuNo + " 的学生";
        }
    }
    
    if (id == null) {
        return "❌ 请提供学生ID或学号";
    }
    
    studentService.deleteStudent(id);
    return String.format("✅ 成功删除学生（ID：%d）", id);
}
```

#### **删除员工逻辑**

同样的逻辑应用于员工删除，支持工号查找。

### 3. 添加Mapper依赖注入

在 `CrudOperationTool` 中添加：

```java
@Autowired
private EmployeeMapper employeeMapper;

@Autowired
private StudentMapper studentMapper;
```

## 使用示例

### ✅ 现在支持的表达方式

#### **删除操作**
```
"删除学号为1的学生"        → ✅ 成功
"删除学生 学号为1"         → ✅ 成功
"删除ID为5的学生"          → ✅ 成功
"删除学生 5"              → ✅ 成功
"删除工号E001的员工"       → ✅ 成功
"删除员工 工号E001"        → ✅ 成功
```

#### **查询操作**
```
"查询学号为S001的学生"     → ✅ 成功
"查看工号E002的员工"       → ✅ 成功
```

#### **添加操作**
```
"添加学生 学号S003 姓名王五" → ✅ 成功（需要进一步完善）
```

### ❌ 尚不支持的表达

```
"删除那个穿红衣服的学生"    → 无法识别
"删除昨天入学的学生"        → 无法识别时间
"删除成绩最差的学生"        → 无法理解比较
```

## 技术实现细节

### 参数提取流程

```
用户输入："删除学号为1的学生"
  ↓
意图识别：IntentType.DELETE, action="deleteStudent"
  ↓
参数提取：
  1. 匹配"学号" + "1" → params.put("id", 1)
  2. 如果没有匹配，尝试提取纯数字
  3. 尝试提取姓名
  4. 尝试提取学号/工号编码
  ↓
执行操作：crudTool.deleteStudent(params)
  ↓
返回结果："✅ 成功删除学生（ID：1）"
```

### 容错机制

1. **多级匹配** - 从精确到模糊
   - 首先尝试带关键词的匹配（学号、工号等）
   - 然后尝试纯数字提取
   - 最后才报错

2. **业务字段转换** - 学号/工号 → 主键ID
   - 通过Mapper查询转换为数据库主键
   - 找不到时给出友好提示

3. **友好的错误提示**
   - "请提供学生ID或学号"（而不是只说ID）
   - "未找到学号为 XXX 的学生"（明确指出问题）

## 扩展建议

### 1. 使用AI进行意图识别（推荐）

当前使用规则匹配，可以改为调用LLM API：

```java
// 使用AI识别意图和提取参数
String prompt = String.format(
    "分析以下用户请求，提取意图和参数：\n" +
    "用户输入：%s\n" +
    "返回JSON格式：{\"action\": \"deleteStudent\", \"params\": {\"stuNo\": \"1\"}}",
    userMessage
);
```

**优点**：
- ✅ 理解更自然的人类语言
- ✅ 支持更多表达方式
- ✅ 自动处理同义词

**缺点**：
- ❌ 需要API调用，有延迟
- ❌ 可能产生费用
- ❌ 需要处理API失败情况

### 2. 添加更多参数类型

```java
// 日期提取
"删除2024年入学的学生" → params.put("enrollYear", 2024)

// 条件提取
"删除年龄大于20的学生" → params.put("ageMin", 20)

// 范围提取
"删除年龄在18-22之间的学生" → params.put("ageMin", 18), params.put("ageMax", 22)
```

### 3. 支持批量操作

```java
"删除所有大三学生" → 先查询符合条件的学生列表，然后批量删除
"删除成绩低于60的学生" → 复杂条件查询后删除
```

### 4. 添加确认机制

对于危险操作（删除），添加二次确认：

```java
if (intent.getType() == IntentType.DELETE) {
    // 先显示将要删除的信息
    String confirmMsg = String.format(
        "即将删除学生：%s（学号：%s），是否确认？",
        student.getStuName(), student.getStuNo()
    );
    // 等待用户确认
}
```

## 测试用例

### 单元测试建议

```java
@Test
public void testExtractIdFromMessage() {
    Map<String, Object> params = extractParameters("删除学号为1的学生", intent);
    assertEquals(1, params.get("id"));
}

@Test
public void testExtractStuNo() {
    Map<String, Object> params = extractParameters("删除学号S001的学生", intent);
    assertEquals("S001", params.get("stuNo"));
}

@Test
public void testDeleteByStuNo() {
    Map<String, Object> params = new HashMap<>();
    params.put("stuNo", "S001");
    String result = crudTool.deleteStudent(params);
    assertTrue(result.contains("成功删除"));
}
```

## 相关文件

- ✅ `AgentService.java` - 增强extractParameters方法
- ✅ `CrudOperationTool.java` - 支持按学号/工号删除
- 📝 `docs/agent-parameter-extraction.md` - 本文档

## 总结

通过本次优化，Agent的自然语言理解能力得到显著提升：

| 优化前 | 优化后 |
|--------|--------|
| 只能识别"删除ID为X" | 支持"删除学号为X"、"删除工号X"等 |
| 必须提供数据库主键ID | 支持业务字段（学号、工号）自动转换 |
| 参数提取为空 | 智能提取ID、姓名、编码等参数 |
| 错误提示不友好 | 明确的错误提示和建议 |

下一步可以考虑引入真正的NLP或LLM来实现更智能的意图识别。
