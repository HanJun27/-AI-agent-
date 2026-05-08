package com.zijin.college.controller;

import com.zijin.college.dto.DepartmentQueryDTO;
import com.zijin.college.dto.Result;
import com.zijin.college.entity.Department;
import com.zijin.college.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/departments")
@CrossOrigin
public class DepartmentController {
    
    @Autowired
    private DepartmentService departmentService;
    
    @GetMapping
    public Result<?> getDepartments(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        try {
            DepartmentQueryDTO query = new DepartmentQueryDTO();
            query.setPageNum(pageNum);
            query.setPageSize(pageSize);
            query.setKeyword(keyword);
            
            return Result.success(departmentService.getDepartments(query));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @GetMapping("/{id}")
    public Result<Department> getDepartmentById(@PathVariable Integer id) {
        try {
            return Result.success(departmentService.getDepartmentById(id));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @PostMapping
    public Result<?> addDepartment(@RequestBody Department department) {
        try {
            departmentService.addDepartment(department);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @PutMapping("/{id}")
    public Result<?> updateDepartment(@PathVariable Integer id, @RequestBody Department department) {
        try {
            departmentService.updateDepartment(id, department);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    public Result<?> deleteDepartment(@PathVariable Integer id) {
        try {
            departmentService.deleteDepartment(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
