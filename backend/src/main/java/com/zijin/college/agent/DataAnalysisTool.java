package com.zijin.college.agent;

import com.zijin.college.entity.*;
import com.zijin.college.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据分析工具
 * 提供数据统计和分析能力
 */
@Component
public class DataAnalysisTool {
    
    @Autowired
    private EmployeeMapper employeeMapper;
    
    @Autowired
    private ClassInfoMapper classInfoMapper;
    
    @Autowired
    private StudentMapper studentMapper;
    
    @Autowired
    private DepartmentMapper departmentMapper;
    
    /**
     * 分析员工分布情况
     */
    public String analyzeEmployeeDistribution() {
        List<Employee> employees = employeeMapper.findAll("");
        
        if (employees.isEmpty()) {
            return "暂无员工数据可供分析。";
        }
        
        // 按部门ID统计
        Map<Integer, Long> deptCount = employees.stream()
            .filter(e -> e.getDeptId() != null)
            .collect(Collectors.groupingBy(Employee::getDeptId, Collectors.counting()));
        
        StringBuilder result = new StringBuilder();
        result.append("📊 **员工部门分布分析**\n\n");
        result.append("总员工数: ").append(employees.size()).append(" 人\n\n");
        
        deptCount.entrySet().stream()
            .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
            .forEach(entry -> {
                double percentage = (entry.getValue() * 100.0) / employees.size();
                result.append(String.format("• 部门ID %d: %d 人 (%.1f%%)\n", 
                    entry.getKey(), entry.getValue(), percentage));
            });
        
        return result.toString();
    }
    
    /**
     * 分析学生性别比例
     */
    public String analyzeStudentGender() {
        List<Student> students = studentMapper.findAll("");
        
        if (students.isEmpty()) {
            return "暂无学生数据可供分析。";
        }
        
        long maleCount = students.stream()
            .filter(s -> "男".equals(s.getGender()))
            .count();
        long femaleCount = students.stream()
            .filter(s -> "女".equals(s.getGender()))
            .count();
        
        double malePercent = (maleCount * 100.0) / students.size();
        double femalePercent = (femaleCount * 100.0) / students.size();
        
        StringBuilder result = new StringBuilder();
        result.append("👥 **学生性别比例分析**\n\n");
        result.append("总学生数: ").append(students.size()).append(" 人\n\n");
        result.append(String.format("• 男生: %d 人 (%.1f%%)\n", maleCount, malePercent));
        result.append(String.format("• 女生: %d 人 (%.1f%%)\n", femaleCount, femalePercent));
        
        return result.toString();
    }
    
    /**
     * 分析班级规模
     */
    public String analyzeClassSize() {
        List<ClassInfo> classes = classInfoMapper.findAll("");
        
        if (classes.isEmpty()) {
            return "暂无班级数据可供分析。";
        }
        
        int totalStudents = classes.stream()
            .mapToInt(c -> c.getStudentCount() != null ? c.getStudentCount() : 0)
            .sum();
        double avgSize = (double) totalStudents / classes.size();
        
        Optional<ClassInfo> largestClass = classes.stream()
            .max(Comparator.comparingInt(c -> c.getStudentCount() != null ? c.getStudentCount() : 0));
        
        Optional<ClassInfo> smallestClass = classes.stream()
            .min(Comparator.comparingInt(c -> c.getStudentCount() != null ? c.getStudentCount() : 0));
        
        StringBuilder result = new StringBuilder();
        result.append("🏫 **班级规模分析**\n\n");
        result.append("总班级数: ").append(classes.size()).append(" 个\n");
        result.append("总学生数: ").append(totalStudents).append(" 人\n");
        result.append(String.format("平均班级规模: %.1f 人/班\n\n", avgSize));
        
        if (largestClass.isPresent()) {
            result.append(String.format("• 最大班级: %s (%d 人)\n", 
                largestClass.get().getClassName(), largestClass.get().getStudentCount()));
        }
        if (smallestClass.isPresent()) {
            result.append(String.format("• 最小班级: %s (%d 人)\n", 
                smallestClass.get().getClassName(), smallestClass.get().getStudentCount()));
        }
        
        return result.toString();
    }
    
    /**
     * 分析学生年龄分布
     */
    public String analyzeStudentAge() {
        List<Student> students = studentMapper.findAll("");
        
        if (students.isEmpty()) {
            return "暂无学生数据可供分析。";
        }
        
        Map<Integer, Long> ageCount = students.stream()
            .filter(s -> s.getAge() != null)
            .collect(Collectors.groupingBy(Student::getAge, Collectors.counting()));
        
        OptionalInt maxAge = students.stream()
            .filter(s -> s.getAge() != null)
            .mapToInt(Student::getAge)
            .max();
        
        OptionalInt minAge = students.stream()
            .filter(s -> s.getAge() != null)
            .mapToInt(Student::getAge)
            .min();
        
        double avgAge = students.stream()
            .filter(s -> s.getAge() != null)
            .mapToInt(Student::getAge)
            .average()
            .orElse(0);
        
        StringBuilder result = new StringBuilder();
        result.append("🎂 **学生年龄分布分析**\n\n");
        result.append(String.format("平均年龄: %.1f 岁\n", avgAge));
        if (maxAge.isPresent()) result.append("最大年龄: ").append(maxAge.getAsInt()).append(" 岁\n");
        if (minAge.isPresent()) result.append("最小年龄: ").append(minAge.getAsInt()).append(" 岁\n\n");
        
        result.append("各年龄段人数:\n");
        ageCount.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(entry -> {
                result.append(String.format("• %d 岁: %d 人\n", entry.getKey(), entry.getValue()));
            });
        
        return result.toString();
    }
    
    /**
     * 综合分析报告
     */
    public String generateComprehensiveReport() {
        StringBuilder report = new StringBuilder();
        report.append("📈 **紫金学院综合分析报告**\n\n");
        report.append("生成时间: ").append(new Date()).append("\n\n");
        
        report.append(analyzeEmployeeDistribution());
        report.append("\n---\n\n");
        report.append(analyzeStudentGender());
        report.append("\n---\n\n");
        report.append(analyzeClassSize());
        report.append("\n---\n\n");
        report.append(analyzeStudentAge());
        
        return report.toString();
    }
}
