--users table
CREATE TABLE users (
                       user_id SERIAL PRIMARY KEY,
                       email VARCHAR(50) NOT NULL,
                       nickname VARCHAR(15) ,
                       password VARCHAR(255) NULL,
                       create_date TIMESTAMP NULL,
                       modify_date TIMESTAMP NULL,
                       authority VARCHAR(5) NULL DEFAULT 'user',
                       is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
                       suspension_end_date TIMESTAMP NULL DEFAULT NULL
);

--user_snstable
CREATE TABLE user_sns (
                          user_sns_id BIGINT PRIMARY KEY,
                          user_id BIGINT NOT NULL,
                          sns_id BIGINT NOT NULL,
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          FOREIGN KEY (user_id) REFERENCES users(user_id)
);