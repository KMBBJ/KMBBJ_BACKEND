--사용자 users
CREATE TABLE users (
                       user_id BIGSERIAL PRIMARY KEY,
                       email VARCHAR(50) NOT NULL,
                       nickname VARCHAR(15) ,
                       password VARCHAR(255) NULL,
                       create_date TIMESTAMP NULL,
                       modify_date TIMESTAMP NULL,
                       authority VARCHAR(5) NULL DEFAULT 'USER',
                       is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
                       suspension_end_date TIMESTAMP NULL DEFAULT NULL
);

--유저sns user_sns
CREATE TABLE user_sns (
                          user_sns_id BIGSERIAL PRIMARY KEY,
                          user_id BIGINT NOT NULL,
                          sns_id BIGINT NOT NULL,
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

--자산 total_balances
CREATE TABLE total_balances (
                          total_balance_id BIGSERIAL PRIMARY KEY,
                          assets BIGINT NOT NULL DEFAULT 0,
                          user_id BIGINT NOT NULL,
                          CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

--자산 변동 내역 asset_transactions
CREATE TABLE asset_transactions (
                                transaction_id BIGSERIAL PRIMARY KEY,
                                change_type VARCHAR(10) NOT NULL,
                                change_amount BIGINT NOT NULL,
                                create_time TIMESTAMP,
                                total_balance_id BIGINT NOT NULL,
                                CONSTRAINT fk_total_balances FOREIGN KEY (total_balance_id) REFERENCES total_balances(total_balance_id ) ON DELETE CASCADE
);

--친구 friends
CREATE TABLE friends (
                        friends_id BIGSERIAL PRIMARY KEY,
                        user_id BIGINT NOT NULL,
                        send_user_id BIGINT NOT NULL,
                        CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

--친구 요청 friends_requests
CREATE TABLE friends_requests (
                                request_id BIGSERIAL PRIMARY KEY,
                                user_id BIGINT NOT NULL,
                                send_user_id BIGINT NOT NULL,
                                Field VARCHAR(10) NOT NULL DEFAULT 'HOLD',
                                CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
                                CONSTRAINT fk_send_user FOREIGN KEY (send_user_id) REFERENCES users(user_id) ON DELETE CASCADE
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
                       average_asset BIGINT NOT NULL,
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
