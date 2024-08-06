package com.kmbbj.backend.notifications.repository;


import com.kmbbj.backend.notifications.entity.EmailAlarm;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailAlarmRepository extends JpaRepository<EmailAlarm, Long> {
}
