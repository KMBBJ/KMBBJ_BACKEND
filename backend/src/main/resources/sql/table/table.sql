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

-- Games 테이블 생성
CREATE TABLE games (
                       game_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       enum VARCHAR(255) NOT NULL,
                       room_id BIGINT,
                       CONSTRAINT fk_room
                           FOREIGN KEY (room_id)
                               REFERENCES rooms(room_id)
                               ON DELETE CASCADE
);

-- Rounds 테이블 생성
CREATE TABLE rounds (
                        round_id BIGSERIAL PRIMARY KEY,
                        round_number INTEGER,
                        duration_minutes INTEGER,
                        game_id UUID,
                        CONSTRAINT fk_game
                            FOREIGN KEY (game_id)
                                REFERENCES games(game_id)
                                ON DELETE CASCADE
);

-- Round_Results 테이블 생성
CREATE TABLE round_results (
                               round_result_id BIGSERIAL PRIMARY KEY,
                               round_id BIGINT NOT NULL,
                               top_buy_coin VARCHAR(255),
                               top_buy_percent VARCHAR(255),
                               top_profit_coin VARCHAR(255),
                               top_profit_percent VARCHAR(255),
                               top_loss_coin VARCHAR(255),
                               top_loss_percent VARCHAR(255),
                               CONSTRAINT fk_round
                                   FOREIGN KEY (round_id)
                                       REFERENCES rounds(round_id)
                                       ON DELETE CASCADE
);

-- Game_Results 테이블 생성
CREATE TABLE game_results (
                              result_id BIGSERIAL PRIMARY KEY,
                              game_id UUID,
                              user_id BIGINT,
                              total_profit INTEGER,
                              total_loss INTEGER,
                              user_rank INTEGER,
                              CONSTRAINT fk_game_result
                                  FOREIGN KEY (game_id)
                                      REFERENCES games(game_id)
                                      ON DELETE CASCADE
);

-- Game_Balances 테이블 생성
CREATE TABLE game_balances (
                               game_balances_id BIGSERIAL PRIMARY KEY,
                               room_id BIGINT NOT NULL,
                               user_id BIGINT NOT NULL UNIQUE,
                               CONSTRAINT fk_room
                                   FOREIGN KEY (room_id)
                                       REFERENCES rooms(room_id)
                                       ON DELETE CASCADE,
                               CONSTRAINT fk_user
                                   FOREIGN KEY (user_id)
                                       REFERENCES users(user_id)
                                       ON DELETE CASCADE
);


-- 코인 coins
CREATE TABLE coins (
                       coin_id BIGSERIAL PRIMARY KEY,
                       coin_name VARCHAR(50) NOT NULL,
                       symbol VARCHAR(25) NOT NULL,
                       status VARCHAR(50),
                       order_types VARCHAR(25)
);

-- 코인 24시간 기준 정보 coin_details
CREATE TABLE coin24h_details (
                              coin_detail_id BIGSERIAL PRIMARY KEY,          -- 기본 키로 자동 증가하는 ID
                              price DOUBLE PRECISION NOT NULL,               -- 최종 거래 가격 (lastPrice)
                              bid_price DOUBLE PRECISION NOT NULL,           -- 현재 매수 호가 (bidPrice)
                              bid_qty DOUBLE PRECISION NOT NULL,             -- 현재 매수 잔량 (bidQty)
                              ask_price DOUBLE PRECISION NOT NULL,           -- 현재 매도 호가 (askPrice)
                              ask_qty DOUBLE PRECISION NOT NULL,             -- 현재 매도 잔량 (askQty)
                              price_change DOUBLE PRECISION NOT NULL,        -- 가격 변동 (priceChange)
                              price_change_percent DOUBLE PRECISION NOT NULL,-- 가격 변동률 (priceChangePercent)
                              weighted_avg_price DOUBLE PRECISION NOT NULL,  -- 가중 평균 가격 (weightedAvgPrice)
                              prev_close_price DOUBLE PRECISION NOT NULL,    -- 이전 마감 가격 (prevClosePrice)
                              open_price DOUBLE PRECISION NOT NULL,          -- 개장 가격 (openPrice)
                              high_price DOUBLE PRECISION NOT NULL,          -- 최고가 (highPrice)
                              low_price DOUBLE PRECISION NOT NULL,           -- 최저가 (lowPrice)
                              volume DOUBLE PRECISION NOT NULL,              -- 거래량 (volume)
                              quote_volume DOUBLE PRECISION NOT NULL,        -- 견적 거래량 (quoteVolume)
                              trade_count INT NOT NULL,                      -- 거래 횟수 (count)
                              open_time TIMESTAMP NOT NULL,                  -- 개장 시간 (openTime)
                              close_time TIMESTAMP NOT NULL,                 -- 마감 시간 (closeTime)
                              voting_amount DOUBLE PRECISION,                -- 투표량 (기존 필드)
                              timezone TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 데이터가 저장된 시간대 (기본값: 현재 시간)
                              coin_id BIGINT NOT NULL,                       -- 연관된 코인 엔티티 (외래 키)
                              FOREIGN KEY (coin_id) REFERENCES coins(coin_id) -- 코인 테이블과의 관계 설정
);

-- kline 그래프 정보 kline
CREATE TABLE kline (
                       kline_id BIGSERIAL PRIMARY KEY,
                       interval VARCHAR(25) NOT NULL,  -- 1m, 3m, 5m, 30m, 1d, 1w 등
                       open_price DOUBLE PRECISION NOT NULL,
                       close_price DOUBLE PRECISION NOT NULL,
                       high_price DOUBLE PRECISION NOT NULL,
                       low_price DOUBLE PRECISION NOT NULL,
                       timezone BIGINT NOT NULL,
                       volume DOUBLE PRECISION NOT NULL,
                       ma10 DOUBLE PRECISION,
                       ma20 DOUBLE PRECISION,
                       ma30 DOUBLE PRECISION,
                       bbu DOUBLE PRECISION,
                       bbd DOUBLE PRECISION,
                       coin_id BIGINT NOT NULL,
                       CONSTRAINT fk_coin
                           FOREIGN KEY(coin_id)
                               REFERENCES coins(coin_id)
                               ON DELETE CASCADE
);