--users table
CREATE TABLE users (
                       user_id BIGINT NOT NULL,
                       email VARCHAR(50) NOT NULL,
                       nickname VARCHAR(15) NOT NULL,
                       password VARCHAR(255) NULL,
                       create_date TIMESTAMP NULL,
                       modify_date TIMESTAMP NULL,
                       authority VARCHAR(5) NULL DEFAULT 'user',
                       is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
                       suspension_end_date TIMESTAMP NULL DEFAULT NULL
);