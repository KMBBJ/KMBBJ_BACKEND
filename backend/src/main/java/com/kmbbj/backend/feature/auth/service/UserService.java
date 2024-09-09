package com.kmbbj.backend.feature.auth.service;

import com.kmbbj.backend.feature.auth.service.email.EmailService;
import com.kmbbj.backend.feature.auth.service.profile.ProfileService;
import com.kmbbj.backend.feature.auth.service.register.RegisterService;

public interface UserService extends
        EmailService,
        RegisterService,
        ProfileService
{ }