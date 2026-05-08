package com.zijin.college.service;

import com.zijin.college.dto.DepartmentQueryDTO;
import com.zijin.college.entity.Department;
import java.util.List;
import java.util.Map;

public interface DepartmentService {
    Map<String, Object> getDepartments(DepartmentQueryDTO query);
    
    Department getDepartmentById(Integer id);
    
    void addDepartment(Department department);
    
    Department updateDepartment(Integer id, Department department);
    
    void deleteDepartment(Integer id);
}
