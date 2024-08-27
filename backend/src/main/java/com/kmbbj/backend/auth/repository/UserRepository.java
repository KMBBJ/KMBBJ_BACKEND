package com.kmbbj.backend.auth.repository;

import com.kmbbj.backend.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JpaRepository를 통해서 Uesr객체를 가져오도록 설계
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    //Email을 통해서 User를 가져오도록 함
    Optional<User> findByEmail(String email);

    // 유저 이메일 글자로 검색
    List<User> findByEmailContainingIgnoreCase(String email);

    Optional<User> findById(Long id);
}