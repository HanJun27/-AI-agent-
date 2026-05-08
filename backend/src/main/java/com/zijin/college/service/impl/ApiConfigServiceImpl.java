package com.zijin.college.service.impl;

import com.zijin.college.entity.ApiConfig;
import com.zijin.college.mapper.ApiConfigMapper;
import com.zijin.college.service.ApiConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ApiConfigServiceImpl implements ApiConfigService {
    
    @Autowired
    private ApiConfigMapper apiConfigMapper;
    
    @Override
    public List<ApiConfig> getAllConfigs() {
        return apiConfigMapper.findAll();
    }
    
    @Override
    public ApiConfig getConfigByProvider(String provider) {
        return apiConfigMapper.findByProvider(provider);
    }
    
    @Override
    public void saveConfig(ApiConfig config) {
        ApiConfig existing = apiConfigMapper.findByProvider(config.getProvider());
        if (existing != null) {
            // 更新
            apiConfigMapper.update(config);
        } else {
            // 插入
            apiConfigMapper.insert(config);
        }
    }
    
    @Override
    public void deleteConfig(String provider) {
        apiConfigMapper.deleteByProvider(provider);
    }
}
