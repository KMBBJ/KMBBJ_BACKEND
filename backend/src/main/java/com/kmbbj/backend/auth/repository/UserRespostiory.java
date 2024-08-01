package com.kmbbj.backend.auth.repository;

import com.kmbbj.backend.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRespostiory extends JpaRepository<User, Integer> {
    User findByEmail(String email);
    Optional<User> findByNickname(String nickname);
}