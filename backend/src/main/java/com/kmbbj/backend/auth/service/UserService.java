package com.kmbbj.backend.auth.service;

import com.kmbbj.backend.auth.service.email.EmailService;
import com.kmbbj.backend.auth.service.profile.ProfileService;
import com.kmbbj.backend.auth.service.register.RegisterService;

public interface UserService extends
        EmailService,
        RegisterService,
        ProfileService
{ }