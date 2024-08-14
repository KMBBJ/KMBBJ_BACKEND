package com.kmbbj.backend.admin.entity;

import com.kmbbj.backend.auth.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.sql.Timestamp;

@Entity
@Table(name = "admin_alarms")
@Getter
@Setter
public class AdminAlarm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "admin_alarms_id")
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "create_date_posted")
    private Timestamp createDatePosted;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
