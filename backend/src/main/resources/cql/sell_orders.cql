CREATE TABLE sell_orders (
                             coin_id BIGINT,
                             price DECIMAL,
                             timestamp TIMESTAMP,
                             order_id BIGINT,
                             quantity DECIMAL,
                             PRIMARY KEY (coin_id, price, timestamp)
) WITH CLUSTERING ORDER BY (price ASC, timestamp ASC);