-- =============================================================
-- ShopFlow: Master Database Initialization Script
-- Creates a dedicated database and user for each microservice.
-- =============================================================

-- Inventory Service
CREATE USER inventory_user WITH PASSWORD 'inventory_pass';
CREATE DATABASE shopflow_inventory OWNER inventory_user;
GRANT ALL PRIVILEGES ON DATABASE shopflow_inventory TO inventory_user;
\c shopflow_inventory
GRANT ALL ON SCHEMA public TO inventory_user;

-- Order Service
\c shopflow
CREATE USER order_user WITH PASSWORD 'order_pass';
CREATE DATABASE shopflow_order OWNER order_user;
GRANT ALL PRIVILEGES ON DATABASE shopflow_order TO order_user;
\c shopflow_order
GRANT ALL ON SCHEMA public TO order_user;

-- Payment Service
\c shopflow
CREATE USER payment_user WITH PASSWORD 'payment_pass';
CREATE DATABASE shopflow_payment OWNER payment_user;
GRANT ALL PRIVILEGES ON DATABASE shopflow_payment TO payment_user;
\c shopflow_payment
GRANT ALL ON SCHEMA public TO payment_user;

-- Product Service
\c shopflow
CREATE USER product_user WITH PASSWORD 'product_pass';
CREATE DATABASE shopflow_product OWNER product_user;
GRANT ALL PRIVILEGES ON DATABASE shopflow_product TO product_user;
\c shopflow_product
GRANT ALL ON SCHEMA public TO product_user;

-- Notification Service
\c shopflow
CREATE USER notification_user WITH PASSWORD 'notification_pass';
CREATE DATABASE shopflow_notification OWNER notification_user;
GRANT ALL PRIVILEGES ON DATABASE shopflow_notification TO notification_user;
\c shopflow_notification
GRANT ALL ON SCHEMA public TO notification_user;
