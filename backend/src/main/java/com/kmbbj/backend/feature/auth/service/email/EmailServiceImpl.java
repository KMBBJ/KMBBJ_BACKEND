package com.kmbbj.backend.feature.auth.service.email;

import com.kmbbj.backend.feature.auth.entity.User;
import com.kmbbj.backend.feature.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    private final UserRepository userRespostiory;

    @Transactional(readOnly = true)
    @Override
    public Optional<User> UserfindByEmail(String email) {
        return userRespostiory.findByEmail(email);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<User> UserfindById(Long id) {
        return userRespostiory.findById(id);
    }
}