package com.zijin.college.service.impl;

import com.zijin.college.dto.DepartmentQueryDTO;
import com.zijin.college.entity.Department;
import com.zijin.college.mapper.DepartmentMapper;
import com.zijin.college.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DepartmentServiceImpl implements DepartmentService {
    
    @Autowired
    private DepartmentMapper departmentMapper;
    
    @Override
    public Map<String, Object> getDepartments(DepartmentQueryDTO query) {
        // 计算偏移量
        int offset = (query.getPageNum() - 1) * query.getPageSize();
        
        // 查询数据
        List<Department> departments = departmentMapper.findAll(
            query.getKeyword(), 
            offset, 
            query.getPageSize()
        );
        
        // 查询总数
        int total = departmentMapper.count(query.getKeyword());
        
        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("list", departments);
        result.put("total", total);
        result.put("pageNum", query.getPageNum());
        result.put("pageSize", query.getPageSize());
        
        return result;
    }
    
    @Override
    public Department getDepartmentById(Integer id) {
        Department department = departmentMapper.findById(id);
        if (department == null) {
            throw new RuntimeException("部门不存在");
        }
        return department;
    }
    
    @Override
    public void addDepartment(Department department) {
        // 检查部门编号是否已存在
        // 这里简化处理，实际应该先查询
        departmentMapper.insert(department);
    }
    
    @Override
    public Department updateDepartment(Integer id, Department department) {
        Department existing = departmentMapper.findById(id);
        if (existing == null) {
            throw new RuntimeException("部门不存在");
        }
        department.setId(id);  // 确保ID正确
        departmentMapper.update(department);
        return department;
    }
    
    @Override
    public void deleteDepartment(Integer id) {
        Department existing = departmentMapper.findById(id);
        if (existing == null) {
            throw new RuntimeException("部门不存在");
        }
        departmentMapper.delete(id);
    }
}
