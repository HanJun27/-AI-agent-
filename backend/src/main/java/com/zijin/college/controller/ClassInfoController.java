package com.zijin.college.controller;

import com.zijin.college.dto.Result;
import com.zijin.college.entity.ClassInfo;
import com.zijin.college.service.ClassInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/classes")
@CrossOrigin
public class ClassInfoController {
    
    @Autowired
    private ClassInfoService classInfoService;
    
    @GetMapping
    public Result<?> getClasses(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        
        List<ClassInfo> classes = classInfoService.getClasses(keyword);
        int total = classInfoService.getCount(keyword);
        
        // 分页处理
        int fromIndex = (pageNum - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, classes.size());
        
        if (fromIndex >= classes.size()) {
            classes = List.of();
        } else {
            classes = classes.subList(fromIndex, toIndex);
        }
        
        Map<String, Object> data = new HashMap<>();
        data.put("list", classes);
        data.put("total", total);
        data.put("pageNum", pageNum);
        data.put("pageSize", pageSize);
        
        return Result.success(data);
    }
    
    @GetMapping("/{id}")
    public Result<?> getClassById(@PathVariable Integer id) {
        ClassInfo classInfo = classInfoService.getClassById(id);
        if (classInfo == null) {
            return Result.error("班级不存在");
        }
        return Result.success(classInfo);
    }
    
    @PostMapping
    public Result<?> addClass(@RequestBody ClassInfo classInfo) {
        try {
            ClassInfo newClass = classInfoService.addClass(classInfo);
            return Result.success(newClass);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
    
    @PutMapping("/{id}")
    public Result<?> updateClass(@PathVariable Integer id, @RequestBody ClassInfo classInfo) {
        try {
            ClassInfo updatedClass = classInfoService.updateClass(id, classInfo);
            return Result.success(updatedClass);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    public Result<?> deleteClass(@PathVariable Integer id) {
        try {
            classInfoService.deleteClass(id);
            return Result.success("删除成功");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
}
