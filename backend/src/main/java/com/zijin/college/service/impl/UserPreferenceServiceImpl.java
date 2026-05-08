package com.zijin.college.service.impl;

import com.zijin.college.entity.UserPreference;
import com.zijin.college.mapper.UserPreferenceMapper;
import com.zijin.college.service.UserPreferenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserPreferenceServiceImpl implements UserPreferenceService {
    
    @Autowired
    private UserPreferenceMapper userPreferenceMapper;
    
    @Override
    public UserPreference getPreferences(Integer userId) {
        UserPreference preference = userPreferenceMapper.findByUserId(userId);
        if (preference == null) {
            // 如果没有偏好设置，创建默认值
            preference = new UserPreference();
            preference.setUserId(userId);
            preference.setLanguage("zh-CN");
            preference.setTheme("light");
            preference.setNotifications(1);
            preference.setAutoSave(1);
            userPreferenceMapper.insert(preference);
        }
        return preference;
    }
    
    @Override
    public void savePreferences(UserPreference preference) {
        UserPreference existing = userPreferenceMapper.findByUserId(preference.getUserId());
        if (existing != null) {
            userPreferenceMapper.update(preference);
        } else {
            userPreferenceMapper.insert(preference);
        }
    }
}
