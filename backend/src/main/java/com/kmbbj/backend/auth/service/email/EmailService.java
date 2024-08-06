package com.kmbbj.backend.auth.service.email;

import com.kmbbj.backend.auth.entity.User;

import java.util.Optional;

public interface EmailService {
    Optional<User> UserfindByEmail(String email);
    Optional<User> UserfindById(Long id);
}
