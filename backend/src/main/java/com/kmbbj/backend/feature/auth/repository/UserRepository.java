package com.kmbbj.backend.feature.auth.repository;

import com.kmbbj.backend.feature.auth.entity.Authority;
import com.kmbbj.backend.feature.auth.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    //id를 이용해서 User를 가져오도록 함
    Optional<User> findById(Long id);

    //관리자만 가져와서 페이징을 하기 위해 추가
    Page<User> findByAuthority(Authority authority, Pageable pageable);
}