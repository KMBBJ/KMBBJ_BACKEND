--users table
CREATE TABLE users (
                       user_id SERIAL PRIMARY KEY,
                       email VARCHAR(50) NOT NULL,
                       nickname VARCHAR(15) ,
                       password VARCHAR(255) NULL,
                       create_date TIMESTAMP NULL,
                       modify_date TIMESTAMP NULL,
                       authority VARCHAR(5) NULL DEFAULT 'USER',
                       is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
                       suspension_end_date TIMESTAMP NULL DEFAULT NULL
);

--user_snstable
CREATE TABLE user_sns (
                          user_sns_id SERIAL PRIMARY KEY,
                          user_id BIGINT NOT NULL,
                          sns_id BIGINT NOT NULL,
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- Room 테이블 생성
CREATE TABLE rooms (
                       room_id BIGSERIAL PRIMARY KEY,
                       title VARCHAR(50) NOT NULL,
                       start_seed_money INTEGER NOT NULL,
                       end_round INTEGER NOT NULL,
                       create_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                       is_deleted BOOLEAN NOT NULL,
                       is_started BOOLEAN NOT NULL,
                       delay INTEGER,
                       user_count INTEGER DEFAULT 1
);

-- UserRoom 테이블 생성
CREATE TABLE user_rooms (
                            user_room_id BIGSERIAL PRIMARY KEY,
                            is_manager BOOLEAN NOT NULL,
                            is_played BOOLEAN NOT NULL,
                            user_id BIGINT NOT NULL,
                            room_id BIGINT NOT NULL,
                            CONSTRAINT fk_user
                                FOREIGN KEY(user_id)
                                    REFERENCES users(user_id)
                                    ON DELETE CASCADE,
                            CONSTRAINT fk_room
                                FOREIGN KEY(room_id)
                                    REFERENCES rooms(room_id)
                                    ON DELETE CASCADE
);
