package com.kmbbj.backend.global.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@ToString
@Getter
//entitiy가 변경되는것을 받는 이벤트
@EntityListeners(AuditingEntityListener.class)
//데이터 베이스 테이블에 매핑되지 안도록 하며 이를 상속받는 서브 클래스들이 해당 속성, 메서드 상속 받도록함
@MappedSuperclass
public abstract class AuditingFields {
    /**
     * create_date 엔티티 생성 시 자동 생성, 수정 불가
     */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @CreatedDate
    @Column(nullable = false, updatable = false)
    protected LocalDateTime createDate;

    /**
     * modify_date 엔티티 수정 시 자동 업데이트
     */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @LastModifiedDate
    @Column(nullable = false)
    protected LocalDateTime modifyDate;
}
