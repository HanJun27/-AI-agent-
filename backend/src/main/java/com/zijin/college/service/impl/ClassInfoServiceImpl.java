package com.zijin.college.service.impl;

import com.zijin.college.entity.ClassInfo;
import com.zijin.college.mapper.ClassInfoMapper;
import com.zijin.college.service.ClassInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClassInfoServiceImpl implements ClassInfoService {
    
    @Autowired
    private ClassInfoMapper classInfoMapper;
    
    @Override
    public List<ClassInfo> getClasses(String keyword) {
        return classInfoMapper.findAll(keyword);
    }
    
    @Override
    public ClassInfo getClassById(Integer id) {
        return classInfoMapper.findById(id);
    }
    
    @Override
    public ClassInfo addClass(ClassInfo classInfo) {
        // 检查班级编号是否已存在
        ClassInfo existing = classInfoMapper.findByClassNo(classInfo.getClassNo());
        if (existing != null) {
            throw new RuntimeException("班级编号已存在");
        }
        
        classInfoMapper.insert(classInfo);
        return classInfo;
    }
    
    @Override
    public ClassInfo updateClass(Integer id, ClassInfo classInfo) {
        ClassInfo existing = classInfoMapper.findById(id);
        if (existing == null) {
            throw new RuntimeException("班级不存在");
        }
        
        // 如果修改了班级编号，检查新编号是否已被其他班级使用
        if (!existing.getClassNo().equals(classInfo.getClassNo())) {
            ClassInfo duplicate = classInfoMapper.findByClassNo(classInfo.getClassNo());
            if (duplicate != null && !duplicate.getId().equals(id)) {
                throw new RuntimeException("班级编号已存在");
            }
        }
        
        classInfo.setId(id);
        classInfoMapper.update(classInfo);
        return classInfo;
    }
    
    @Override
    public void deleteClass(Integer id) {
        ClassInfo existing = classInfoMapper.findById(id);
        if (existing == null) {
            throw new RuntimeException("班级不存在");
        }
        classInfoMapper.deleteById(id);
    }
    
    @Override
    public int getCount(String keyword) {
        return classInfoMapper.countAll(keyword);
    }
}
