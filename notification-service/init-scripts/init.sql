-- Notification Service Database Initialization
-- This script runs inside shopflow_notification database

CREATE TABLE IF NOT EXISTS notification (
    id              BIGSERIAL PRIMARY KEY,
    recipient       VARCHAR(255) NOT NULL,
    channel         VARCHAR(50) NOT NULL DEFAULT 'EMAIL',
    subject         VARCHAR(500),
    message         TEXT NOT NULL,
    status          VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    sent_at         TIMESTAMP
);
