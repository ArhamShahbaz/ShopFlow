-- Product Service Database Initialization
-- This script runs inside shopflow_product database

CREATE TABLE IF NOT EXISTS product (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(255) NOT NULL,
    description     TEXT,
    sku_code        VARCHAR(255) NOT NULL UNIQUE,
    price           DECIMAL(19, 2) NOT NULL,
    category        VARCHAR(100),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Seed sample data
INSERT INTO product (name, description, sku_code, price, category) VALUES
    ('Wireless Mouse', 'Ergonomic wireless mouse with USB receiver', 'SKU-001', 29.99, 'Electronics'),
    ('Mechanical Keyboard', 'RGB mechanical keyboard with Cherry MX switches', 'SKU-002', 89.99, 'Electronics'),
    ('USB-C Hub', '7-in-1 USB-C hub with HDMI and ethernet', 'SKU-003', 45.99, 'Accessories')
ON CONFLICT (sku_code) DO NOTHING;
