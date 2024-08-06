package com.kmbbj.backend.auth.service.register;

import com.kmbbj.backend.auth.controller.request.UserJoinRequest;

public interface RegisterService {
    void registerUser(UserJoinRequest userJoinRequest);
}
