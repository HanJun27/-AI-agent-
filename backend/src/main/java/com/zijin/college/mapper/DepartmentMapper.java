package com.zijin.college.mapper;

import com.zijin.college.entity.Department;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface DepartmentMapper {
    List<Department> findAll(@Param("keyword") String keyword, @Param("offset") Integer offset, @Param("limit") Integer limit);
    
    Department findById(@Param("id") Integer id);
    
    Department findByDeptNo(@Param("deptNo") String deptNo);
    
    int insert(Department department);
    
    int update(Department department);
    
    int delete(@Param("id") Integer id);
    
    int count(@Param("keyword") String keyword);
}
