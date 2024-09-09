package com.kmbbj.backend.feature.auth.service.register;

import com.kmbbj.backend.feature.auth.controller.request.UserJoinRequest;

public interface RegisterService {
    void registerUser(UserJoinRequest userJoinRequest);
}
