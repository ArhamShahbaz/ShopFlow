-- Order Service Database Initialization
-- This script runs inside shopflow_order database

CREATE TABLE IF NOT EXISTS orders (
    id              BIGSERIAL PRIMARY KEY,
    order_number    VARCHAR(255) NOT NULL UNIQUE,
    customer_id     BIGINT NOT NULL,
    status          VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    total_amount    DECIMAL(19, 2) NOT NULL DEFAULT 0.00,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS order_items (
    id              BIGSERIAL PRIMARY KEY,
    order_id        BIGINT NOT NULL REFERENCES orders(id),
    sku_code        VARCHAR(255) NOT NULL,
    quantity        INTEGER NOT NULL,
    price           DECIMAL(19, 2) NOT NULL
);
