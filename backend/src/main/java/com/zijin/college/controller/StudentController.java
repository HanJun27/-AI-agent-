package com.zijin.college.controller;

import com.zijin.college.dto.Result;
import com.zijin.college.entity.Student;
import com.zijin.college.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/students")
@CrossOrigin
public class StudentController {
    
    @Autowired
    private StudentService studentService;
    
    @GetMapping
    public Result<?> getStudents(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        
        List<Student> students = studentService.getStudents(keyword);
        int total = studentService.getCount(keyword);
        
        // 分页处理
        int fromIndex = (pageNum - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, students.size());
        
        List<Student> pageData = students.subList(fromIndex, toIndex);
        
        Map<String, Object> data = new HashMap<>();
        data.put("list", pageData);
        data.put("total", total);
        data.put("pageNum", pageNum);
        data.put("pageSize", pageSize);
        
        return Result.success(data);
    }
    
    @GetMapping("/{id}")
    public Result<Student> getStudentById(@PathVariable Integer id) {
        Student student = studentService.getStudentById(id);
        if (student == null) {
            return Result.error("学生不存在");
        }
        return Result.success(student);
    }
    
    @PostMapping
    public Result<Student> addStudent(@RequestBody Student student) {
        try {
            Student newStudent = studentService.addStudent(student);
            return Result.success(newStudent);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
    
    @PutMapping("/{id}")
    public Result<Student> updateStudent(@PathVariable Integer id, @RequestBody Student student) {
        try {
            Student updatedStudent = studentService.updateStudent(id, student);
            return Result.success(updatedStudent);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    public Result<Void> deleteStudent(@PathVariable Integer id) {
        try {
            studentService.deleteStudent(id);
            return Result.success(null);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
}
