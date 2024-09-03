package com.kmbbj.backend.auth.service.register;


import com.kmbbj.backend.auth.controller.request.UserJoinRequest;

public interface AdminRegisterService {
    void registerAdmin(UserJoinRequest userJoinRequest);
}
