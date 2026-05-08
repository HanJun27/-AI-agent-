package com.zijin.college.service.impl;

import com.zijin.college.entity.Student;
import com.zijin.college.mapper.StudentMapper;
import com.zijin.college.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentServiceImpl implements StudentService {
    
    @Autowired
    private StudentMapper studentMapper;
    
    @Override
    public List<Student> getStudents(String keyword) {
        return studentMapper.findAll(keyword);
    }
    
    @Override
    public Student getStudentById(Integer id) {
        return studentMapper.findById(id);
    }
    
    @Override
    public Student addStudent(Student student) {
        // 检查学号是否已存在
        Student existing = studentMapper.findByStuNo(student.getStuNo());
        if (existing != null) {
            throw new RuntimeException("学号已存在");
        }
        
        // 设置默认状态为在读
        if (student.getStatus() == null) {
            student.setStatus(1);
        }
        
        studentMapper.insert(student);
        return student;
    }
    
    @Override
    public Student updateStudent(Integer id, Student student) {
        Student existing = studentMapper.findById(id);
        if (existing == null) {
            throw new RuntimeException("学生不存在");
        }
        
        student.setId(id);
        studentMapper.update(student);
        return student;
    }
    
    @Override
    public void deleteStudent(Integer id) {
        Student existing = studentMapper.findById(id);
        if (existing == null) {
            throw new RuntimeException("学生不存在");
        }
        
        studentMapper.deleteById(id);
    }
    
    @Override
    public int getCount(String keyword) {
        return studentMapper.countAll(keyword);
    }
}
