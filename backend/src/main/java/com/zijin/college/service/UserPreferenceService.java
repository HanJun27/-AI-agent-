package com.zijin.college.service;

import com.zijin.college.entity.UserPreference;

public interface UserPreferenceService {
    
    UserPreference getPreferences(Integer userId);
    
    void savePreferences(UserPreference preference);
}
