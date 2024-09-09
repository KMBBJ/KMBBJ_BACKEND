CREATE TABLE sell_orders (
                             coin_id BIGINT,
                             price BIGINT,
                             transaction_id BIGINT,
                             PRIMARY KEY (coin_id, price, transaction_id)
) WITH CLUSTERING ORDER BY (price ASC);