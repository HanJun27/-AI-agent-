package com.zijin.college.mapper;

import com.zijin.college.entity.Employee;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface EmployeeMapper {
    
    List<Employee> findAll(@Param("keyword") String keyword);
    
    Employee findById(Integer id);
    
    Employee findByEmpNo(String empNo);
    
    int insert(Employee employee);
    
    int update(Employee employee);
    
    int deleteById(Integer id);
    
    int countAll(@Param("keyword") String keyword);
}
