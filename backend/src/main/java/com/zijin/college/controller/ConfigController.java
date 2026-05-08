package com.zijin.college.controller;

import com.zijin.college.dto.Result;
import com.zijin.college.entity.ApiConfig;
import com.zijin.college.entity.UserPreference;
import com.zijin.college.service.ApiConfigService;
import com.zijin.college.service.UserPreferenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/config")
@CrossOrigin
public class ConfigController {
    
    @Autowired
    private ApiConfigService apiConfigService;
    
    @Autowired
    private UserPreferenceService userPreferenceService;
    
    // ==================== API配置接口 ====================
    
    @GetMapping("/api-configs")
    public Result<?> getAllApiConfigs() {
        List<ApiConfig> configs = apiConfigService.getAllConfigs();
        
        // 转换为前端需要的格式
        Map<String, Object> result = new HashMap<>();
        for (ApiConfig config : configs) {
            Map<String, Object> configMap = new HashMap<>();
            configMap.put("name", config.getProviderName());
            configMap.put("key", config.getApiKey());
            configMap.put("model", config.getModel());
            configMap.put("enabled", config.getEnabled() == 1);
            configMap.put("baseUrl", config.getBaseUrl());
            result.put(config.getProvider(), configMap);
        }
        
        return Result.success(result);
    }
    
    @GetMapping("/api-config/{provider}")
    public Result<?> getApiConfig(@PathVariable String provider) {
        ApiConfig config = apiConfigService.getConfigByProvider(provider);
        if (config == null) {
            return Result.error("配置不存在");
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("name", config.getProviderName());
        result.put("key", config.getApiKey());
        result.put("model", config.getModel());
        result.put("enabled", config.getEnabled() == 1);
        result.put("baseUrl", config.getBaseUrl());
        
        return Result.success(result);
    }
    
    @PutMapping("/api-config/{provider}")
    public Result<?> saveApiConfig(@PathVariable String provider, @RequestBody Map<String, Object> configData) {
        System.out.println("========== 收到保存API配置请求 ==========");
        System.out.println("Provider: " + provider);
        System.out.println("ConfigData: " + configData);
        
        ApiConfig config = new ApiConfig();
        config.setProvider(provider);
        config.setProviderName((String) configData.get("name"));
        config.setApiKey((String) configData.get("key"));
        config.setModel((String) configData.get("model"));
        config.setBaseUrl((String) configData.get("baseUrl"));
        config.setEnabled(Boolean.TRUE.equals(configData.get("enabled")) ? 1 : 0);
        
        apiConfigService.saveConfig(config);
        System.out.println("配置保存成功");
        return Result.success("配置保存成功");
    }
    
    @DeleteMapping("/api-config/{provider}")
    public Result<?> deleteApiConfig(@PathVariable String provider) {
        apiConfigService.deleteConfig(provider);
        return Result.success("配置删除成功");
    }
    
    // ==================== 用户偏好设置接口 ====================
    
    @GetMapping("/preferences/{userId}")
    public Result<?> getUserPreferences(@PathVariable Integer userId) {
        UserPreference preference = userPreferenceService.getPreferences(userId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("language", preference.getLanguage());
        result.put("theme", preference.getTheme());
        result.put("notifications", preference.getNotifications() == 1);
        result.put("autoSave", preference.getAutoSave() == 1);
        
        return Result.success(result);
    }
    
    @PutMapping("/preferences/{userId}")
    public Result<?> saveUserPreferences(@PathVariable Integer userId, @RequestBody Map<String, Object> prefData) {
        UserPreference preference = new UserPreference();
        preference.setUserId(userId);
        preference.setLanguage((String) prefData.get("language"));
        preference.setTheme((String) prefData.get("theme"));
        preference.setNotifications(Boolean.TRUE.equals(prefData.get("notifications")) ? 1 : 0);
        preference.setAutoSave(Boolean.TRUE.equals(prefData.get("autoSave")) ? 1 : 0);
        
        userPreferenceService.savePreferences(preference);
        return Result.success("设置保存成功");
    }
}
