CREATE TABLE buy_orders (
                            coin_id BIGINT,
                            price DECIMAL,
                            timestamp TIMESTAMP,
                            transaction_id BIGINT,
                            quantity DECIMAL,
                            PRIMARY KEY (coin_id, price, timestamp)
) WITH CLUSTERING ORDER BY (price DESC, timestamp ASC);