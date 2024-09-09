package com.kmbbj.backend.global.notifications.repository;


import com.kmbbj.backend.global.notifications.entity.EmailAlarm;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailAlarmRepository extends JpaRepository<EmailAlarm, Long> {
}
