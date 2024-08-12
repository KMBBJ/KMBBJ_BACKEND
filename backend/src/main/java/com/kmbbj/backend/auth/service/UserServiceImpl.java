package com.kmbbj.backend.auth.service;

import com.kmbbj.backend.auth.controller.request.UserJoinRequest;
import com.kmbbj.backend.auth.controller.response.UserProfileReponse;
import com.kmbbj.backend.auth.service.email.EmailService;
import com.kmbbj.backend.auth.service.profile.ProfileService;
import com.kmbbj.backend.auth.service.register.RegisterService;
import com.kmbbj.backend.auth.entity.User;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    private final EmailService emailService;
    private final RegisterService registerService;
    private final ProfileService profileService;

    public UserServiceImpl(@Qualifier("emailServiceImpl") EmailService emailService,
                           @Qualifier("registerServiceImpl") RegisterService registerService,
                           @Qualifier("profileServiceImpl") ProfileService profileService) {
        this.emailService = emailService;
        this.registerService = registerService;
        this.profileService = profileService;
    }

    @Override
    public Optional<User> UserfindByEmail(String email) {
        return emailService.UserfindByEmail(email);
    }

    @Override
    public Optional<User> UserfindById(Long id) {
        return emailService.UserfindById(id);
    }

    @Override
    public void registerUser(UserJoinRequest userJoinRequest) {
        registerService.registerUser(userJoinRequest);
    }

    @Override
    public UserProfileReponse UserProfilefindByUserId(Long userId) {
        return profileService.UserProfilefindByUserId(userId);
    }
}