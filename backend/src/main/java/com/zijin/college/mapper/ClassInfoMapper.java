package com.zijin.college.mapper;

import com.zijin.college.entity.ClassInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ClassInfoMapper {
    
    List<ClassInfo> findAll(@Param("keyword") String keyword);
    
    ClassInfo findById(Integer id);
    
    ClassInfo findByClassNo(String classNo);
    
    int insert(ClassInfo classInfo);
    
    int update(ClassInfo classInfo);
    
    int deleteById(Integer id);
    
    int countAll(@Param("keyword") String keyword);
}
