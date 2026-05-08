# Agent删除功能完整实现

## 概述

已完成所有数据类型的删除功能，支持通过业务字段（学号、工号、班级号、部门号）或数据库ID进行删除。

## 已实现的功能

### ✅ 1. 删除学生 (deleteStudent)

**支持的参数**：
- `id` - 数据库主键ID
- `stuNo` - 学号（如 S2023001）

**示例**：
```
用户："删除学号为S2023001的学生"
→ 提取 stuNo="S2023001"
→ 查找学号对应的ID
→ 删除学生
```

### ✅ 2. 删除员工 (deleteEmployee)

**支持的参数**：
- `id` - 数据库主键ID
- `empNo` - 工号（如 E001）

**示例**：
```
用户："删除工号E001的员工"
→ 提取 empNo="E001"
→ 查找工号对应的ID
→ 删除员工
```

### ✅ 3. 删除班级 (deleteClass)

**支持的参数**：
- `id` - 数据库主键ID
- `classNo` - 班级号（如 C001）

**示例**：
```
用户："删除班级号为C001的班级"
→ 提取 classNo="C001"
→ 查找班级号对应的ID
→ 删除班级
```

### ✅ 4. 删除部门 (deleteDepartment) - 新增

**支持的参数**：
- `id` - 数据库主键ID
- `deptNo` - 部门号（如 D001）

**示例**：
```
用户："删除部门号为D001的部门"
→ 提取 deptNo="D001"
→ 查找部门号对应的ID
→ 删除部门
```

## 技术实现

### 1. Mapper层

#### DepartmentMapper.java
```java
Department findByDeptNo(@Param("deptNo") String deptNo);
```

#### DepartmentMapper.xml
```xml
<select id="findByDeptNo" resultMap="DepartmentResultMap">
    SELECT * FROM department WHERE dept_no = #{deptNo}
</select>
```

### 2. Service层

使用现有的 `DepartmentService.deleteDepartment(id)` 方法。

### 3. Agent层

#### CrudOperationTool.java
```java
public String deleteDepartment(Map<String, Object> params) {
    Integer id = (Integer) params.get("id");
    String deptNo = (String) params.get("deptNo");
    
    // 如果提供了部门号，先查找对应的ID
    if (id == null && deptNo != null && !deptNo.isEmpty()) {
        Department department = departmentMapper.findByDeptNo(deptNo);
        if (department != null) {
            id = department.getId();
        } else {
            return "❌ 未找到部门号为 " + deptNo + " 的部门";
        }
    }
    
    if (id == null) {
        return "❌ 请提供部门ID或部门号";
    }
    
    departmentService.deleteDepartment(id);
    return String.format("✅ 成功删除部门（ID：%d）", id);
}
```

#### AgentService.java
```java
case "deleteDepartment":
    return crudTool.deleteDepartment(params);
```

#### IntentRecognitionService.java
在System Prompt中添加：
```
- deleteEmployee, deleteStudent, deleteClass, deleteDepartment
```

### 4. 参数提取

在 `AgentService.extractParameters()` 中添加部门号提取：
```java
else if (intent.getAction().contains("Department")) {
    params.put("deptNo", code);
    return params;
}
```

## 测试用例

### 学生删除
```bash
# 通过学号
"删除学号为S2023001的学生"
"删除学生 S2023001"

# 通过ID
"删除ID为6的学生"
"删除学生 6"
```

### 员工删除
```bash
# 通过工号
"删除工号E001的员工"
"删除员工 E001"

# 通过ID
"删除ID为2的员工"
"删除员工 2"
```

### 班级删除
```bash
# 通过班级号
"删除班级号为C001的班级"
"删除班级 C001"

# 通过ID
"删除ID为1的班级"
"删除班级 1"
```

### 部门删除
```bash
# 通过部门号
"删除部门号为D001的部门"
"删除部门 D001"

# 通过ID
"删除ID为1的部门"
"删除部门 1"
```

## 日志输出示例

### 删除部门
```
========== ChatController收到请求 ==========
userMessage: 删除部门号为D001的部门
provider: zhipu
API配置已启用，将使用LLM
===========================================

使用LLM进行意图识别...
LLM原始响应: {"intent":"DELETE","action":"deleteDepartment","params":{"deptNo":"D001"},"confidence":0.95}
LLM提取的参数节点: {"deptNo":"D001"}
  - deptNo: D001
LLM识别结果: deleteDepartment, 置信度: 0.95

========== 删除部门参数 ==========
params: {deptNo=D001}
id: null
deptNo: D001
通过部门号查找部门: D001
找到部门: ID=1, 名称=计算机系
即将删除部门ID: 1

✅ 成功删除部门（ID：1）
```

## 相关文件清单

### 后端代码
- ✅ `DepartmentMapper.java` - 添加findByDeptNo方法
- ✅ `DepartmentMapper.xml` - 添加SQL查询
- ✅ `CrudOperationTool.java` - 添加deleteDepartment方法及依赖注入
- ✅ `AgentService.java` - 添加deleteDepartment调用和参数提取
- ✅ `IntentRecognitionService.java` - 更新System Prompt

### 前端代码
- 无需修改（已支持）

## 注意事项

### 1. 外键约束
删除部门前需要确保：
- 该部门下没有员工
- 或者先删除/转移员工

否则会因为外键约束导致删除失败。

### 2. 级联删除
如果需要级联删除，需要在Service层实现：
```java
public void deleteDepartmentWithEmployees(Integer id) {
    // 1. 删除该部门下的所有员工
    employeeMapper.deleteByDeptId(id);
    // 2. 删除部门
    departmentMapper.delete(id);
}
```

### 3. 软删除
建议实现软删除（标记删除而非物理删除）：
```sql
UPDATE department SET status = 0 WHERE id = #{id}
```

## 总结

现在Agent支持完整的CRUD操作：

| 数据类型 | 查询 | 添加 | 删除 | 更新 |
|---------|------|------|------|------|
| 学生 | ✅ | ✅ | ✅ | ⏳ |
| 员工 | ✅ | ✅ | ✅ | ⏳ |
| 班级 | ✅ | ✅ | ✅ | ⏳ |
| 部门 | ✅ | ⏳ | ✅ | ⏳ |

下一步可以实现更新（Update）功能。
