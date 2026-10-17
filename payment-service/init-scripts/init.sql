-- Payment Service Database Initialization
-- This script runs inside shopflow_payment database

CREATE TABLE IF NOT EXISTS payment (
    id                  BIGSERIAL PRIMARY KEY,
    order_id            BIGINT NOT NULL,
    amount              DECIMAL(19, 2) NOT NULL,
    currency            VARCHAR(10) NOT NULL DEFAULT 'USD',
    status              VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    payment_method      VARCHAR(50),
    transaction_id      VARCHAR(255) UNIQUE,
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
