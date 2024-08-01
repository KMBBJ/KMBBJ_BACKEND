--users table
CREATE TABLE users (
                       user_id BIGINT NOT NULL COMMENT 'seq값',
                       email VARCHAR(50) NOT NULL COMMENT '사용자 email',
                       nickname VARCHAR(15) NOT NULL COMMENT '사용자 이름',
                       password VARCHAR(255) NULL COMMENT '비밀번호',
                       create_date TIMESTAMP NULL COMMENT '생성일',
                       modify_date TIMESTAMP NULL COMMENT '수정일',
                       authority VARCHAR(5) NULL DEFAULT 'user' COMMENT '권한',
                       is_deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '탈퇴 여부',
                       suspension_end_date TIMESTAMP NULL DEFAULT NULL COMMENT '정지시 정지시간'
);