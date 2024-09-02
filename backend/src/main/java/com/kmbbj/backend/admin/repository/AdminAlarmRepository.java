package com.kmbbj.backend.admin.repository;

import com.kmbbj.backend.admin.entity.AdminAlarm;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdminAlarmRepository extends JpaRepository<AdminAlarm, Long> {
}
