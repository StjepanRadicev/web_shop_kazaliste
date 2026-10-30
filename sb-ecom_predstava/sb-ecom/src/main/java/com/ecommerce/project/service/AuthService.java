package com.ecommerce.project.service;


import com.ecommerce.project.payload.AuthenticationResult;
import com.ecommerce.project.security.request.LoginRequest;
import com.ecommerce.project.security.response.UserInfoResponse;

public interface AuthService {

    AuthenticationResult login(LoginRequest loginRequest);
}
