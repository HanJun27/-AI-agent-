package com.zijin.college.service;

import com.zijin.college.dto.LoginRequest;
import com.zijin.college.dto.LoginResponse;

public interface UserService {
    LoginResponse login(LoginRequest request);
}
