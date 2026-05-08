package com.zijin.college.agent;

import com.zijin.college.entity.*;
import com.zijin.college.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据库查询工具
 * 提供自然语言到数据库查询的转换能力
 */
@Component
public class DatabaseQueryTool {
    
    @Autowired
    private EmployeeMapper employeeMapper;
    
    @Autowired
    private ClassInfoMapper classInfoMapper;
    
    @Autowired
    private StudentMapper studentMapper;
    
    @Autowired
    private DepartmentMapper departmentMapper;
    
    /**
     * 查询员工信息
     */
    public String queryEmployees(Map<String, Object> params) {
        List<Employee> employees = employeeMapper.findAll("");
        
        if (employees.isEmpty()) {
            return "当前没有员工数据。";
        }
        
        StringBuilder result = new StringBuilder();
        result.append("找到 ").append(employees.size()).append(" 名员工：\n\n");
        
        for (Employee emp : employees) {
            result.append("• ").append(emp.getEmpName())
                  .append(" (").append(emp.getEmpNo()).append(")\n");
            result.append("  部门ID: ").append(emp.getDeptId() != null ? emp.getDeptId() : "未分配")
                  .append(" | 职位: ").append(emp.getPosition() != null ? emp.getPosition() : "未知")
                  .append("\n");
        }
        
        return result.toString();
    }
    
    /**
     * 查询班级信息
     */
    public String queryClasses(Map<String, Object> params) {
        List<ClassInfo> classes = classInfoMapper.findAll("");
        
        if (classes.isEmpty()) {
            return "当前没有班级数据。";
        }
        
        StringBuilder result = new StringBuilder();
        result.append("找到 ").append(classes.size()).append(" 个班级：\n\n");
        
        for (ClassInfo cls : classes) {
            result.append("• ").append(cls.getClassName())
                  .append(" (").append(cls.getClassNo()).append(")\n");
            result.append("  专业: ").append(cls.getMajor())
                  .append(" | 年级: ").append(cls.getGrade())
                  .append(" | 人数: ").append(cls.getStudentCount())
                  .append("\n");
        }
        
        return result.toString();
    }
    
    /**
     * 查询学生信息
     */
    public String queryStudents(Map<String, Object> params) {
        List<Student> students = studentMapper.findAll("");
        
        if (students.isEmpty()) {
            return "当前没有学生数据。";
        }
        
        StringBuilder result = new StringBuilder();
        result.append("找到 ").append(students.size()).append(" 名学生：\n\n");
        
        // 只显示前10条，避免输出过长
        int limit = Math.min(students.size(), 10);
        for (int i = 0; i < limit; i++) {
            Student stu = students.get(i);
            result.append("• ").append(stu.getStuName())
                  .append(" (").append(stu.getStuNo()).append(")\n");
            result.append("  性别: ").append(stu.getGender())
                  .append(" | 年龄: ").append(stu.getAge())
                  .append(" | 专业: ").append(stu.getMajor())
                  .append("\n");
        }
        
        if (students.size() > 10) {
            result.append("\n... 还有 ").append(students.size() - 10).append(" 名学生\n");
        }
        
        return result.toString();
    }
    
    /**
     * 查询部门信息
     */
    public String queryDepartments(Map<String, Object> params) {
        List<Department> departments = departmentMapper.findAll("", 0, 100);
        
        if (departments.isEmpty()) {
            return "当前没有部门数据。";
        }
        
        StringBuilder result = new StringBuilder();
        result.append("找到 ").append(departments.size()).append(" 个部门：\n\n");
        
        for (Department dept : departments) {
            result.append("• ").append(dept.getDeptName())
                  .append(" (").append(dept.getDeptNo()).append(")\n");
            result.append("  负责人: ").append(dept.getManager() != null ? dept.getManager() : "未指定")
                  .append(" | 位置: ").append(dept.getLocation() != null ? dept.getLocation() : "未知")
                  .append("\n");
        }
        
        return result.toString();
    }
    
    /**
     * 统计员工数量
     */
    public String countEmployees() {
        List<Employee> employees = employeeMapper.findAll("");
        return "当前共有 " + employees.size() + " 名员工。";
    }
    
    /**
     * 统计学生数量
     */
    public String countStudents() {
        List<Student> students = studentMapper.findAll("");
        return "当前共有 " + students.size() + " 名学生。";
    }
    
    /**
     * 统计班级数量
     */
    public String countClasses() {
        List<ClassInfo> classes = classInfoMapper.findAll("");
        return "当前共有 " + classes.size() + " 个班级。";
    }
    
    /**
     * 获取数据库概览
     */
    public String getDatabaseOverview() {
        StringBuilder overview = new StringBuilder();
        overview.append("📊 **紫金学院数据库概览**\n\n");
        overview.append("• 员工总数: ").append(employeeMapper.findAll("").size()).append(" 人\n");
        overview.append("• 学生总数: ").append(studentMapper.findAll("").size()).append(" 人\n");
        overview.append("• 班级总数: ").append(classInfoMapper.findAll("").size()).append(" 个\n");
        overview.append("• 部门总数: ").append(departmentMapper.findAll("", 0, 100).size()).append(" 个\n");
        
        return overview.toString();
    }
}
