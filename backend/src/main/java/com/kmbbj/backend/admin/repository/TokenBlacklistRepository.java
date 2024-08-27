package com.kmbbj.backend.admin.repository;


import com.kmbbj.backend.admin.entity.TokenBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;




public interface TokenBlacklistRepository extends JpaRepository<TokenBlacklist, Long> {
    boolean existsByToken(String token);
    void deleteById(Long id);
}