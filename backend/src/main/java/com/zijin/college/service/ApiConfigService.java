package com.zijin.college.service;

import com.zijin.college.entity.ApiConfig;

import java.util.List;

public interface ApiConfigService {
    
    List<ApiConfig> getAllConfigs();
    
    ApiConfig getConfigByProvider(String provider);
    
    void saveConfig(ApiConfig config);
    
    void deleteConfig(String provider);
}
