-- Inventory Service Database Initialization
-- This script runs inside shopflow_inventory database

CREATE TABLE IF NOT EXISTS inventory (
    id          BIGSERIAL PRIMARY KEY,
    sku_code    VARCHAR(255) NOT NULL UNIQUE,
    quantity    INTEGER NOT NULL DEFAULT 0,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Seed sample data
INSERT INTO inventory (sku_code, quantity) VALUES
    ('SKU-001', 100),
    ('SKU-002', 200),
    ('SKU-003', 50)
ON CONFLICT (sku_code) DO NOTHING;
