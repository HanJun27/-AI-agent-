package com.zijin.college.service.impl;

import com.zijin.college.entity.Employee;
import com.zijin.college.mapper.EmployeeMapper;
import com.zijin.college.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {
    
    @Autowired
    private EmployeeMapper employeeMapper;
    
    @Override
    public List<Employee> getEmployees(String keyword) {
        return employeeMapper.findAll(keyword);
    }
    
    @Override
    public Employee getEmployeeById(Integer id) {
        return employeeMapper.findById(id);
    }
    
    @Override
    public Employee addEmployee(Employee employee) {
        // 检查员工编号是否已存在
        Employee existing = employeeMapper.findByEmpNo(employee.getEmpNo());
        if (existing != null) {
            throw new RuntimeException("员工编号已存在");
        }
        
        employee.setStatus(1); // 默认在职
        employeeMapper.insert(employee);
        return employee;
    }
    
    @Override
    public Employee updateEmployee(Integer id, Employee employee) {
        Employee existing = employeeMapper.findById(id);
        if (existing == null) {
            throw new RuntimeException("员工不存在");
        }
        
        // 如果修改了员工编号，检查新编号是否已被其他员工使用
        if (!existing.getEmpNo().equals(employee.getEmpNo())) {
            Employee duplicate = employeeMapper.findByEmpNo(employee.getEmpNo());
            if (duplicate != null && !duplicate.getId().equals(id)) {
                throw new RuntimeException("员工编号已存在");
            }
        }
        
        employee.setId(id);
        employeeMapper.update(employee);
        return employee;
    }
    
    @Override
    public void deleteEmployee(Integer id) {
        Employee existing = employeeMapper.findById(id);
        if (existing == null) {
            throw new RuntimeException("员工不存在");
        }
        employeeMapper.deleteById(id);
    }
    
    @Override
    public int getCount(String keyword) {
        return employeeMapper.countAll(keyword);
    }
}
