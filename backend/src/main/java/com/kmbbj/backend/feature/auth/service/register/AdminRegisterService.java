package com.kmbbj.backend.feature.auth.service.register;


import com.kmbbj.backend.feature.auth.controller.request.UserJoinRequest;

public interface AdminRegisterService {
    void registerAdmin(UserJoinRequest userJoinRequest);
}
