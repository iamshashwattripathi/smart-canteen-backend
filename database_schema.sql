-- ============================================================
--  SMART CANTEEN BILLING SYSTEM — DATABASE SCHEMA
--  Run this once in MySQL before starting the Spring Boot app.
--  (Spring Boot will auto-create tables via JPA, but this file
--   documents the full schema for your project report.)
-- ============================================================

CREATE DATABASE IF NOT EXISTS smart_canteen
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE smart_canteen;

-- ─── 1. USERS ────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS users (
  id              BIGINT AUTO_INCREMENT PRIMARY KEY,
  name            VARCHAR(100)  NOT NULL,
  email           VARCHAR(150)  NOT NULL UNIQUE,
  password        VARCHAR(255)  NOT NULL,
  role            ENUM('CUSTOMER','ADMIN','STAFF') DEFAULT 'CUSTOMER',
  phone           VARCHAR(15),
  wallet_balance  DECIMAL(10,2) DEFAULT 0.00,
  created_at      DATETIME      DEFAULT CURRENT_TIMESTAMP
);

-- ─── 2. STALLS ───────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS stalls (
  id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
  name                  VARCHAR(100) NOT NULL,
  location              VARCHAR(150),
  is_active             BOOLEAN      DEFAULT TRUE,
  current_queue_length  INT          DEFAULT 0
);

-- ─── 3. MENU ITEMS ───────────────────────────────────────────
CREATE TABLE IF NOT EXISTS menu_items (
  id           BIGINT AUTO_INCREMENT PRIMARY KEY,
  stall_id     BIGINT,
  name         VARCHAR(150)  NOT NULL,
  category     VARCHAR(100),
  price        DECIMAL(8,2)  NOT NULL,
  is_veg       BOOLEAN       DEFAULT TRUE,
  is_available BOOLEAN       DEFAULT TRUE,
  image_url    VARCHAR(300),
  description  TEXT,
  created_at   DATETIME      DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (stall_id) REFERENCES stalls(id) ON DELETE SET NULL
);

-- ─── 4. ORDERS ───────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS orders (
  id              BIGINT AUTO_INCREMENT PRIMARY KEY,
  order_code      VARCHAR(20)   NOT NULL UNIQUE,
  user_id         BIGINT,
  stall_id        BIGINT,
  status          ENUM('PENDING','PREPARING','READY','COMPLETED','CANCELLED') DEFAULT 'PENDING',
  payment_method  ENUM('CASH','UPI','CARD','WALLET') DEFAULT 'CASH',
  payment_status  ENUM('PAID','UNPAID','REFUNDED')   DEFAULT 'UNPAID',
  subtotal        DECIMAL(10,2) DEFAULT 0.00,
  gst_amount      DECIMAL(10,2) DEFAULT 0.00,
  total_amount    DECIMAL(10,2) DEFAULT 0.00,
  pickup_time     DATETIME,
  created_at      DATETIME      DEFAULT CURRENT_TIMESTAMP,
  updated_at      DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id)  REFERENCES users(id)  ON DELETE SET NULL,
  FOREIGN KEY (stall_id) REFERENCES stalls(id) ON DELETE SET NULL
);

-- ─── 5. ORDER ITEMS ──────────────────────────────────────────
CREATE TABLE IF NOT EXISTS order_items (
  id            BIGINT AUTO_INCREMENT PRIMARY KEY,
  order_id      BIGINT        NOT NULL,
  menu_item_id  BIGINT        NOT NULL,
  quantity      INT           NOT NULL DEFAULT 1,
  unit_price    DECIMAL(8,2)  NOT NULL,
  subtotal      DECIMAL(10,2) NOT NULL,
  FOREIGN KEY (order_id)     REFERENCES orders(id)     ON DELETE CASCADE,
  FOREIGN KEY (menu_item_id) REFERENCES menu_items(id) ON DELETE CASCADE
);

-- ─── 6. TOKENS ───────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS tokens (
  id            BIGINT AUTO_INCREMENT PRIMARY KEY,
  token_number  INT    NOT NULL,
  order_id      BIGINT,
  stall_id      BIGINT,
  status        ENUM('WAITING','SERVING','DONE') DEFAULT 'WAITING',
  issued_at     DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (order_id)  REFERENCES orders(id)  ON DELETE SET NULL,
  FOREIGN KEY (stall_id)  REFERENCES stalls(id)  ON DELETE SET NULL
);

-- ─── 7. INVENTORY ────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS inventory (
  id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
  material_name       VARCHAR(150) NOT NULL,
  stock_quantity      INT          NOT NULL DEFAULT 0,
  unit                VARCHAR(30)  DEFAULT 'units',
  low_stock_threshold INT          DEFAULT 5,
  last_updated        DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- ─── 8. WALLET TRANSACTIONS ──────────────────────────────────
CREATE TABLE IF NOT EXISTS wallet_transactions (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id     BIGINT NOT NULL,
  type        ENUM('CREDIT','DEBIT') NOT NULL,
  amount      DECIMAL(10,2) NOT NULL,
  description VARCHAR(255),
  created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ─── VIEWS ───────────────────────────────────────────────────
CREATE OR REPLACE VIEW daily_sales AS
  SELECT DATE(created_at) AS sale_date,
         COUNT(*)          AS total_orders,
         SUM(total_amount) AS total_revenue
  FROM orders
  WHERE payment_status = 'PAID'
  GROUP BY DATE(created_at);

CREATE OR REPLACE VIEW top_selling_items AS
  SELECT mi.name              AS item_name,
         SUM(oi.quantity)     AS total_sold,
         SUM(oi.subtotal)     AS total_revenue
  FROM order_items oi
  JOIN menu_items mi ON oi.menu_item_id = mi.id
  JOIN orders o       ON oi.order_id    = o.id
  WHERE o.payment_status = 'PAID'
  GROUP BY mi.id, mi.name
  ORDER BY total_sold DESC;
