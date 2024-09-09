package com.kmbbj.backend.feature.auth.service.email;

import com.kmbbj.backend.feature.auth.entity.User;

import java.util.Optional;

public interface EmailService {
    Optional<User> UserfindByEmail(String email);
    Optional<User> UserfindById(Long id);
}
