package com.zijin.college.controller;

import com.zijin.college.dto.Result;
import com.zijin.college.entity.User;
import com.zijin.college.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/test")
public class TestController {
    
    @Autowired
    private UserMapper userMapper;
    
    @GetMapping("/db")
    public Result<?> testDatabase() {
        Map<String, Object> result = new HashMap<>();
        try {
            // 测试数据库连接
            User user = userMapper.findByUsername("admin");
            if (user != null) {
                result.put("status", "success");
                result.put("message", "数据库连接正常");
                result.put("user", user.getUsername());
                result.put("passwordHash", user.getPassword());
            } else {
                result.put("status", "error");
                result.put("message", "未找到admin用户");
            }
        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", e.getMessage());
            e.printStackTrace();
        }
        return Result.success(result);
    }
}
