package com.kmbbj.backend.feature.auth.repository;

import com.kmbbj.backend.feature.auth.entity.UserSns;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * JpaRepository를 통해서 UesrSns객체를 가져오도록 설계
 */
@Repository
public interface UserSnsRepository extends JpaRepository<UserSns, Long> {
}