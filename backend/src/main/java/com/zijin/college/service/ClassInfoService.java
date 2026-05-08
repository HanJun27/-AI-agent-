package com.zijin.college.service;

import com.zijin.college.entity.ClassInfo;

import java.util.List;

public interface ClassInfoService {
    
    List<ClassInfo> getClasses(String keyword);
    
    ClassInfo getClassById(Integer id);
    
    ClassInfo addClass(ClassInfo classInfo);
    
    ClassInfo updateClass(Integer id, ClassInfo classInfo);
    
    void deleteClass(Integer id);
    
    int getCount(String keyword);
}
