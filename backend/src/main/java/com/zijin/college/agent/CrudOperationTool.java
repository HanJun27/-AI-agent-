package com.zijin.college.agent;

import com.zijin.college.entity.*;
import com.zijin.college.mapper.*;
import com.zijin.college.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;  // 新增
import java.util.Map;

/**
 * CRUD操作工具
 * 提供通过自然语言执行增删改查的能力
 */
@Component
public class CrudOperationTool {
    
    @Autowired
    private EmployeeService employeeService;
    
    @Autowired
    private StudentService studentService;
    
    @Autowired
    private ClassInfoService classInfoService;
    
    @Autowired
    private DepartmentService departmentService;
    
    @Autowired
    private EmployeeMapper employeeMapper;
    
    @Autowired
    private StudentMapper studentMapper;
    
    @Autowired
    private ClassInfoMapper classInfoMapper;
    
    @Autowired
    private DepartmentMapper departmentMapper;
    
    /**
     * 添加员工
     */
    public String addEmployee(Map<String, Object> params) {
        try {
            Employee employee = new Employee();
            employee.setEmpNo((String) params.get("empNo"));
            
            // 支持两种参数名：empName（标准）或 name（LLM提取）
            String empName = (String) params.get("empName");
            if (empName == null || empName.isEmpty()) {
                empName = (String) params.get("name");
            }
            employee.setEmpName(empName);
            
            employee.setPosition((String) params.get("position"));
            
            // 设置性别
            if (params.containsKey("gender")) {
                employee.setGender((String) params.get("gender"));
            }
            
            // 设置部门ID（如果有）
            if (params.containsKey("deptId")) {
                employee.setDeptId((Integer) params.get("deptId"));
            }
            
            if (params.containsKey("age")) {
                employee.setAge((Integer) params.get("age"));
            }
            if (params.containsKey("phone")) {
                employee.setPhone((String) params.get("phone"));
            }
            if (params.containsKey("email")) {
                employee.setEmail((String) params.get("email"));
            }
            
            System.out.println("========== 添加员工参数 ==========");
            System.out.println("工号: " + employee.getEmpNo());
            System.out.println("姓名: " + employee.getEmpName());
            System.out.println("年龄: " + employee.getAge());
            System.out.println("职位: " + employee.getPosition());
            
            employeeService.addEmployee(employee);
            return String.format("✅ 成功添加员工：%s（工号：%s）", employee.getEmpName(), employee.getEmpNo());
        } catch (Exception e) {
            System.err.println("添加员工异常: " + e.getMessage());
            e.printStackTrace();
            return "❌ 添加员工失败：" + e.getMessage();
        }
    }
    
    /**
     * 删除员工
     */
    public String deleteEmployee(Map<String, Object> params) {
        try {
            Integer id = (Integer) params.get("id");
            String empNo = (String) params.get("empNo");
            
            System.out.println("========== 删除员工参数 ==========");
            System.out.println("params: " + params);
            System.out.println("id: " + id);
            System.out.println("empNo: " + empNo);
            
            // 如果提供了工号，先查找对应的ID
            if (id == null && empNo != null && !empNo.isEmpty()) {
                System.out.println("通过工号查找员工: " + empNo);
                Employee employee = employeeMapper.findByEmpNo(empNo);
                if (employee != null) {
                    System.out.println("找到员工: ID=" + employee.getId() + ", 姓名=" + employee.getEmpName());
                    id = employee.getId();
                } else {
                    return "❌ 未找到工号为 " + empNo + " 的员工";
                }
            }
            
            if (id == null) {
                return "❌ 请提供员工ID或工号";
            }
            
            System.out.println("即将删除员工ID: " + id);
            employeeService.deleteEmployee(id);
            return String.format("✅ 成功删除员工（ID：%d）", id);
        } catch (Exception e) {
            System.err.println("删除员工异常: " + e.getMessage());
            e.printStackTrace();
            return "❌ 删除员工失败：" + e.getMessage();
        }
    }
    
    /**
     * 添加学生
     */
    public String addStudent(Map<String, Object> params) {
        try {
            Student student = new Student();
            student.setStuNo((String) params.get("stuNo"));
            
            // 支持两种参数名：stuName（标准）或 name（LLM提取）
            String stuName = (String) params.get("stuName");
            if (stuName == null || stuName.isEmpty()) {
                stuName = (String) params.get("name");
            }
            student.setStuName(stuName);
            
            student.setGender((String) params.get("gender"));
            student.setMajor((String) params.get("major"));
            
            if (params.containsKey("age")) {
                student.setAge((Integer) params.get("age"));
            }
            if (params.containsKey("phone")) {
                student.setPhone((String) params.get("phone"));
            }
            if (params.containsKey("email")) {
                student.setEmail((String) params.get("email"));
            }
            if (params.containsKey("classId")) {
                student.setClassId((Integer) params.get("classId"));
            }
            
            // 默认入学日期为今天
            student.setEnrollmentDate(LocalDate.now());
            
            System.out.println("========== 添加学生参数 ==========");
            System.out.println("学号: " + student.getStuNo());
            System.out.println("姓名: " + student.getStuName());
            System.out.println("年龄: " + student.getAge());
            System.out.println("性别: " + student.getGender());
            
            studentService.addStudent(student);
            return String.format("✅ 成功添加学生：%s（学号：%s）", student.getStuName(), student.getStuNo());
        } catch (Exception e) {
            System.err.println("添加学生异常: " + e.getMessage());
            e.printStackTrace();
            return "❌ 添加学生失败：" + e.getMessage();
        }
    }
    
    /**
     * 删除学生
     */
    public String deleteStudent(Map<String, Object> params) {
        try {
            Integer id = (Integer) params.get("id");
            String stuNo = (String) params.get("stuNo");
            
            System.out.println("========== 删除学生参数 ==========");
            System.out.println("params: " + params);
            System.out.println("id: " + id);
            System.out.println("stuNo: " + stuNo);
            
            // 如果提供了学号，先查找对应的ID
            if (id == null && stuNo != null && !stuNo.isEmpty()) {
                System.out.println("通过学号查找学生: " + stuNo);
                Student student = studentMapper.findByStuNo(stuNo);
                if (student != null) {
                    System.out.println("找到学生: ID=" + student.getId() + ", 姓名=" + student.getStuName());
                    id = student.getId();
                } else {
                    return "❌ 未找到学号为 " + stuNo + " 的学生";
                }
            }
            
            if (id == null) {
                return "❌ 请提供学生ID或学号";
            }
            
            System.out.println("即将删除学生ID: " + id);
            studentService.deleteStudent(id);
            return String.format("✅ 成功删除学生（ID：%d）", id);
        } catch (Exception e) {
            System.err.println("删除学生异常: " + e.getMessage());
            e.printStackTrace();
            return "❌ 删除学生失败：" + e.getMessage();
        }
    }
    
    /**
     * 添加班级
     */
    public String addClass(Map<String, Object> params) {
        try {
            ClassInfo classInfo = new ClassInfo();
            classInfo.setClassNo((String) params.get("classNo"));
            classInfo.setClassName((String) params.get("className"));
            classInfo.setMajor((String) params.get("major"));
            classInfo.setGrade((String) params.get("grade"));
            
            if (params.containsKey("studentCount")) {
                classInfo.setStudentCount((Integer) params.get("studentCount"));
            }
            
            // 支持多种参数名：teacher（标准）或 headTeacher（LLM提取）
            String teacher = (String) params.get("teacher");
            if (teacher == null || teacher.isEmpty()) {
                teacher = (String) params.get("headTeacher");
            }
            if (teacher != null && !teacher.isEmpty()) {
                classInfo.setTeacher(teacher);
            }
            
            // 处理教室参数
            if (params.containsKey("classroom")) {
                classInfo.setClassroom((String) params.get("classroom"));
            }
            
            classInfoService.addClass(classInfo);
            return String.format("✅ 成功添加班级：%s（班号：%s）", classInfo.getClassName(), classInfo.getClassNo());
        } catch (Exception e) {
            return "❌ 添加班级失败：" + e.getMessage();
        }
    }
    
    /**
     * 删除班级
     */
    public String deleteClass(Map<String, Object> params) {
        try {
            Integer id = (Integer) params.get("id");
            String classNo = (String) params.get("classNo");
            
            System.out.println("========== 删除班级参数 ==========");
            System.out.println("params: " + params);
            System.out.println("id: " + id);
            System.out.println("classNo: " + classNo);
            
            // 如果提供了班级号，先查找对应的ID
            if (id == null && classNo != null && !classNo.isEmpty()) {
                System.out.println("通过班级号查找班级: " + classNo);
                ClassInfo classInfo = classInfoMapper.findByClassNo(classNo);
                if (classInfo != null) {
                    System.out.println("找到班级: ID=" + classInfo.getId() + ", 名称=" + classInfo.getClassName());
                    id = classInfo.getId();
                } else {
                    return "❌ 未找到班级号为 " + classNo + " 的班级";
                }
            }
            
            if (id == null) {
                return "❌ 请提供班级ID或班级号";
            }
            
            System.out.println("即将删除班级ID: " + id);
            classInfoService.deleteClass(id);
            return String.format("✅ 成功删除班级（ID：%d）", id);
        } catch (Exception e) {
            System.err.println("删除班级异常: " + e.getMessage());
            e.printStackTrace();
            return "❌ 删除班级失败：" + e.getMessage();
        }
    }
    
    /**
     * 删除部门
     */
    public String deleteDepartment(Map<String, Object> params) {
        try {
            Integer id = (Integer) params.get("id");
            String deptNo = (String) params.get("deptNo");
            
            System.out.println("========== 删除部门参数 ==========");
            System.out.println("params: " + params);
            System.out.println("id: " + id);
            System.out.println("deptNo: " + deptNo);
            
            // 如果提供了部门号，先查找对应的ID
            if (id == null && deptNo != null && !deptNo.isEmpty()) {
                System.out.println("通过部门号查找部门: " + deptNo);
                Department department = departmentMapper.findByDeptNo(deptNo);
                if (department != null) {
                    System.out.println("找到部门: ID=" + department.getId() + ", 名称=" + department.getDeptName());
                    id = department.getId();
                } else {
                    return "❌ 未找到部门号为 " + deptNo + " 的部门";
                }
            }
            
            if (id == null) {
                return "❌ 请提供部门ID或部门号";
            }
            
            System.out.println("即将删除部门ID: " + id);
            departmentService.deleteDepartment(id);
            return String.format("✅ 成功删除部门（ID：%d）", id);
        } catch (Exception e) {
            System.err.println("删除部门异常: " + e.getMessage());
            e.printStackTrace();
            return "❌ 删除部门失败：" + e.getMessage();
        }
    }
    
    /**
     * 添加部门
     */
    public String addDepartment(Map<String, Object> params) {
        try {
            Department dept = new Department();
            dept.setDeptNo((String) params.get("deptNo"));
            
            // 支持两种参数名：deptName（标准）或 name（LLM提取）
            String deptName = (String) params.get("deptName");
            if (deptName == null || deptName.isEmpty()) {
                deptName = (String) params.get("name");
            }
            dept.setDeptName(deptName);
            
            if (params.containsKey("location")) {
                dept.setLocation((String) params.get("location"));
            }
            if (params.containsKey("manager")) {
                dept.setManager((String) params.get("manager"));
            }
            
            System.out.println("========== 添加部门参数 ==========");
            System.out.println("部门号: " + dept.getDeptNo());
            System.out.println("部门名称: " + dept.getDeptName());
            System.out.println("位置: " + dept.getLocation());
            System.out.println("负责人: " + dept.getManager());
            
            departmentService.addDepartment(dept);
            return String.format("✅ 成功添加部门：%s（编号：%s）", 
                                 dept.getDeptName(), dept.getDeptNo());
        } catch (Exception e) {
            System.err.println("添加部门异常: " + e.getMessage());
            e.printStackTrace();
            return "❌ 添加部门失败：" + e.getMessage();
        }
    }
    
    /**
     * 更新学生
     */
    public String updateStudent(Map<String, Object> params) {
        try {
            Integer id = (Integer) params.get("id");
            String stuNo = (String) params.get("stuNo");
            
            System.out.println("========== 更新学生参数 ==========");
            System.out.println("params: " + params);
            
            // 查找学生
            Student student = null;
            String name = (String) params.get("name");  // 新增：支持通过姓名查找
            
            if (stuNo != null && !stuNo.isEmpty()) {
                System.out.println("通过学号查找学生: " + stuNo);
                student = studentMapper.findByStuNo(stuNo);
            } else if (name != null && !name.isEmpty()) {
                // 新增：通过姓名查找（可能返回多个，取第一个）
                System.out.println("通过姓名查找学生: " + name);
                List<Student> students = studentMapper.findAll(name);
                if (students != null && !students.isEmpty()) {
                    student = students.get(0);
                    System.out.println("找到匹配的学生: " + student.getStuName());
                }
            } else if (id != null) {
                System.out.println("通过ID查找学生: " + id);
                student = studentMapper.findById(id);
            }
            
            if (student == null) {
                return "❌ 未找到要更新的学生";
            }
            
            System.out.println("找到学生: ID=" + student.getId() + ", 姓名=" + student.getStuName());
            
            // 更新字段
            boolean updated = false;
            
            // 特殊处理：newName表示要修改为的新姓名
            if (params.containsKey("newName")) {
                student.setStuName((String) params.get("newName"));
                updated = true;
                System.out.println("将学生姓名修改为: " + params.get("newName"));
            } else if (params.containsKey("name") && stuNo == null && id == null) {
                // 如果通过姓名查找，且没有newName，则不更新姓名（name只是定位参数）
                System.out.println("注意：name参数用于定位，未提供newName，不修改姓名");
            } else if (params.containsKey("name") && (stuNo != null || id != null)) {
                // 如果通过学号或ID查找，name可以是要更新的字段
                student.setStuName((String) params.get("name"));
                updated = true;
            }
            
            if (params.containsKey("age")) {
                student.setAge((Integer) params.get("age"));
                updated = true;
            }
            if (params.containsKey("gender")) {
                student.setGender((String) params.get("gender"));
                updated = true;
            }
            if (params.containsKey("phone")) {
                student.setPhone((String) params.get("phone"));
                updated = true;
            }
            if (params.containsKey("email")) {
                student.setEmail((String) params.get("email"));
                updated = true;
            }
            if (params.containsKey("major")) {
                student.setMajor((String) params.get("major"));
                updated = true;
            }
            
            if (!updated) {
                return "⚠️ 没有提供要更新的字段";
            }
            
            studentService.updateStudent(student.getId(), student);
            return String.format("✅ 成功更新学生：%s", student.getStuName());
        } catch (Exception e) {
            System.err.println("更新学生异常: " + e.getMessage());
            e.printStackTrace();
            return "❌ 更新学生失败：" + e.getMessage();
        }
    }
    
    /**
     * 更新员工
     */
    public String updateEmployee(Map<String, Object> params) {
        try {
            Integer id = (Integer) params.get("id");
            String empNo = (String) params.get("empNo");
            
            System.out.println("========== 更新员工参数 ==========");
            System.out.println("params: " + params);
            
            // 查找员工
            Employee employee = null;
            String empName = (String) params.get("name");  // 新增：支持通过姓名查找
            
            if (empNo != null && !empNo.isEmpty()) {
                System.out.println("通过工号查找员工: " + empNo);
                employee = employeeMapper.findByEmpNo(empNo);
            } else if (empName != null && !empName.isEmpty()) {
                // 新增：通过姓名查找（可能返回多个，取第一个）
                System.out.println("通过姓名查找员工: " + empName);
                List<Employee> employees = employeeMapper.findAll(empName);
                if (employees != null && !employees.isEmpty()) {
                    employee = employees.get(0);
                    System.out.println("找到匹配的员工: " + employee.getEmpName());
                }
            } else if (id != null) {
                System.out.println("通过ID查找员工: " + id);
                employee = employeeMapper.findById(id);
            }
            
            if (employee == null) {
                return "❌ 未找到要更新的员工";
            }
            
            System.out.println("找到员工: ID=" + employee.getId() + ", 姓名=" + employee.getEmpName());
            
            // 更新字段
            boolean updated = false;
            
            // 特殊处理：newName表示要修改为的新姓名
            if (params.containsKey("newName")) {
                employee.setEmpName((String) params.get("newName"));
                updated = true;
                System.out.println("将员工姓名修改为: " + params.get("newName"));
            } else if (params.containsKey("name") && empNo == null && id == null) {
                // 如果通过姓名查找，且没有newName，则不更新姓名（name只是定位参数）
                System.out.println("注意：name参数用于定位，未提供newName，不修改姓名");
            } else if (params.containsKey("name") && (empNo != null || id != null)) {
                // 如果通过工号或ID查找，name可以是要更新的字段
                employee.setEmpName((String) params.get("name"));
                updated = true;
            }
            
            if (params.containsKey("age")) {
                employee.setAge((Integer) params.get("age"));
                updated = true;
            }
            if (params.containsKey("gender")) {
                employee.setGender((String) params.get("gender"));
                updated = true;
            }
            if (params.containsKey("phone")) {
                employee.setPhone((String) params.get("phone"));
                updated = true;
            }
            if (params.containsKey("email")) {
                employee.setEmail((String) params.get("email"));
                updated = true;
            }
            if (params.containsKey("position")) {
                employee.setPosition((String) params.get("position"));
                updated = true;
            }
            
            if (!updated) {
                return "⚠️ 没有提供要更新的字段";
            }
            
            employeeService.updateEmployee(employee.getId(), employee);
            return String.format("✅ 成功更新员工：%s", employee.getEmpName());
        } catch (Exception e) {
            System.err.println("更新员工异常: " + e.getMessage());
            e.printStackTrace();
            return "❌ 更新员工失败：" + e.getMessage();
        }
    }
    
    /**
     * 更新班级
     */
    public String updateClass(Map<String, Object> params) {
        try {
            Integer id = (Integer) params.get("id");
            String classNo = (String) params.get("classNo");
            
            System.out.println("========== 更新班级参数 ==========");
            System.out.println("params: " + params);
            
            // 查找班级
            ClassInfo classInfo = null;
            String className = (String) params.get("className");  // 新增：支持通过班级名称查找
            
            if (classNo != null && !classNo.isEmpty()) {
                System.out.println("通过班级号查找班级: " + classNo);
                classInfo = classInfoMapper.findByClassNo(classNo);
            } else if (className != null && !className.isEmpty()) {
                // 新增：通过班级名称查找
                System.out.println("通过班级名称查找班级: " + className);
                List<ClassInfo> classes = classInfoMapper.findAll(className);
                if (classes != null && !classes.isEmpty()) {
                    classInfo = classes.get(0);
                    System.out.println("找到匹配的班级: " + classInfo.getClassName());
                }
            } else if (id != null) {
                System.out.println("通过ID查找班级: " + id);
                classInfo = classInfoMapper.findById(id);
            }
            
            if (classInfo == null) {
                return "❌ 未找到要更新的班级";
            }
            
            System.out.println("找到班级: ID=" + classInfo.getId() + ", 名称=" + classInfo.getClassName());
            
            // 更新字段
            boolean updated = false;
            
            // 特殊处理：newName表示要修改为的新班级名
            if (params.containsKey("newName")) {
                classInfo.setClassName((String) params.get("newName"));
                updated = true;
                System.out.println("将班级名称修改为: " + params.get("newName"));
            } else if (params.containsKey("name") && classNo == null && id == null && className == null) {
                // 如果通过名称查找，且没有newName，则不更新名称（name只是定位参数）
                System.out.println("注意：name参数用于定位，未提供newName，不修改班级名称");
            } else if (params.containsKey("name")) {
                // 如果通过编号/ID查找，name可以是要更新的字段
                classInfo.setClassName((String) params.get("name"));
                updated = true;
            }
            
            if (params.containsKey("major")) {
                classInfo.setMajor((String) params.get("major"));
                updated = true;
            }
            if (params.containsKey("grade")) {
                classInfo.setGrade((String) params.get("grade"));
                updated = true;
            }
            if (params.containsKey("studentCount")) {
                classInfo.setStudentCount((Integer) params.get("studentCount"));
                updated = true;
            }
            // 兼容classSize参数（LLM可能返回classSize而非studentCount）
            if (params.containsKey("classSize")) {
                Object classSizeObj = params.get("classSize");
                if (classSizeObj instanceof Integer) {
                    classInfo.setStudentCount((Integer) classSizeObj);
                } else if (classSizeObj instanceof String) {
                    classInfo.setStudentCount(Integer.parseInt((String) classSizeObj));
                }
                updated = true;
            }
            
            // 支持多种参数名：teacher（标准）或 headTeacher（LLM提取）
            if (params.containsKey("teacher") || params.containsKey("headTeacher")) {
                String teacher = (String) params.get("teacher");
                if (teacher == null || teacher.isEmpty()) {
                    teacher = (String) params.get("headTeacher");
                }
                if (teacher != null && !teacher.isEmpty()) {
                    classInfo.setTeacher(teacher);
                    updated = true;
                    System.out.println("将班主任修改为: " + teacher);
                }
            }
            
            // 处理教室参数
            if (params.containsKey("classroom")) {
                classInfo.setClassroom((String) params.get("classroom"));
                updated = true;
                System.out.println("将教室修改为: " + params.get("classroom"));
            }
            
            if (!updated) {
                return "⚠️ 没有提供要更新的字段";
            }
            
            classInfoService.updateClass(classInfo.getId(), classInfo);
            return String.format("✅ 成功更新班级：%s", classInfo.getClassName());
        } catch (Exception e) {
            System.err.println("更新班级异常: " + e.getMessage());
            e.printStackTrace();
            return "❌ 更新班级失败：" + e.getMessage();
        }
    }
    
    /**
     * 更新部门
     */
    public String updateDepartment(Map<String, Object> params) {
        try {
            Integer id = (Integer) params.get("id");
            String deptNo = (String) params.get("deptNo");
            
            System.out.println("========== 更新部门参数 ==========");
            System.out.println("params: " + params);
            
            // 查找部门
            Department department = null;
            String deptName = (String) params.get("deptName");  // 新增：支持通过部门名称查找
            
            if (deptNo != null && !deptNo.isEmpty()) {
                System.out.println("通过部门号查找部门: " + deptNo);
                department = departmentMapper.findByDeptNo(deptNo);
            } else if (deptName != null && !deptName.isEmpty()) {
                // 新增：通过部门名称查找
                System.out.println("通过部门名称查找部门: " + deptName);
                List<Department> departments = departmentMapper.findAll(deptName, 0, 1);
                if (departments != null && !departments.isEmpty()) {
                    department = departments.get(0);
                    System.out.println("找到匹配的部门: " + department.getDeptName());
                }
            } else if (id != null) {
                System.out.println("通过ID查找部门: " + id);
                department = departmentMapper.findById(id);
            }
            
            if (department == null) {
                return "❌ 未找到要更新的部门";
            }
            
            System.out.println("找到部门: ID=" + department.getId() + ", 名称=" + department.getDeptName());
            
            // 更新字段
            boolean updated = false;
            
            // 特殊处理：newName表示要修改为的新部门名
            if (params.containsKey("newName")) {
                department.setDeptName((String) params.get("newName"));
                updated = true;
                System.out.println("将部门名称修改为: " + params.get("newName"));
            } else if (params.containsKey("name") && deptNo == null && id == null && deptName == null) {
                // 如果通过名称查找，且没有newName，则不更新名称（name只是定位参数）
                System.out.println("注意：name参数用于定位，未提供newName，不修改部门名称");
            } else if (params.containsKey("name")) {
                // 如果通过编号/ID查找，name可以是要更新的字段
                department.setDeptName((String) params.get("name"));
                updated = true;
            }
            
            if (params.containsKey("location")) {
                department.setLocation((String) params.get("location"));
                updated = true;
            }
            if (params.containsKey("manager")) {
                department.setManager((String) params.get("manager"));
                updated = true;
            }
            
            if (!updated) {
                return "⚠️ 没有提供要更新的字段";
            }
            
            departmentService.updateDepartment(department.getId(), department);
            return String.format("✅ 成功更新部门：%s", department.getDeptName());
        } catch (Exception e) {
            System.err.println("更新部门异常: " + e.getMessage());
            e.printStackTrace();
            return "❌ 更新部门失败：" + e.getMessage();
        }
    }
}
