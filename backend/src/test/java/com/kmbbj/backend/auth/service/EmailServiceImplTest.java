package com.kmbbj.backend.auth.service;

import com.kmbbj.backend.feature.auth.service.email.EmailServiceImpl;
import com.kmbbj.backend.feature.auth.entity.User;
import com.kmbbj.backend.feature.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class EmailServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private EmailServiceImpl emailService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void userfindByEmailTest() {
        String email = "test@example.com";
        User user = User.builder()
                .id(1L)
                .email(email)
                .nickname("nickname")
                .password("password")
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        Optional<User> result = emailService.UserfindByEmail(email);

        assertTrue(result.isPresent());
        assertEquals(user, result.get());
    }

    @Test
    void userfindByIdTest() {
        Long userId = 1L;
        User user = User.builder()
                .id(userId)
                .email("test@example.com")
                .nickname("nickname")
                .password("password")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        Optional<User> result = emailService.UserfindById(userId);

        assertTrue(result.isPresent());
        assertEquals(user, result.get());
    }
}