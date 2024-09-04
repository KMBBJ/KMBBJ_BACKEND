CREATE TABLE sell_orders (
                             coin_id BIGINT,
                             price BIGINT,
                             transaction_id BIGINT,
                             timestamp TIMESTAMP,
                             PRIMARY KEY (coin_id, price, transaction_id, timestamp)
) WITH CLUSTERING ORDER BY (price ASC, transaction_id ASC, timestamp ASC);