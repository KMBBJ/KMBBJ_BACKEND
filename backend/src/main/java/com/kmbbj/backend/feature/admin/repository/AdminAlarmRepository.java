package com.kmbbj.backend.feature.admin.repository;

import com.kmbbj.backend.feature.admin.entity.AdminAlarm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminAlarmRepository extends JpaRepository<AdminAlarm, Long> {
}
