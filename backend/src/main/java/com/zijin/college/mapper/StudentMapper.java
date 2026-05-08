package com.zijin.college.mapper;

import com.zijin.college.entity.Student;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StudentMapper {
    
    List<Student> findAll(@Param("keyword") String keyword);
    
    Student findById(Integer id);
    
    Student findByStuNo(String stuNo);
    
    int insert(Student student);
    
    int update(Student student);
    
    int deleteById(Integer id);
    
    int countAll(@Param("keyword") String keyword);
}
