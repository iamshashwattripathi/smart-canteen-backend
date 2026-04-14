-- ============================================================
--  SMART CANTEEN BILLING SYSTEM — DATABASE SCHEMA
--  Web Matrix Project | UCER Prayagraj | May 2025
--  Run this file in MySQL before starting the Spring Boot app
-- ============================================================

CREATE DATABASE IF NOT EXISTS smart_canteen;
USE smart_canteen;

-- ─── 1. USERS ────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS users (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    name           VARCHAR(100)   NOT NULL,
    email          VARCHAR(150)   NOT NULL UNIQUE,
    password       VARCHAR(255)   NOT NULL,
    role           ENUM('CUSTOMER','ADMIN','STAFF') DEFAULT 'CUSTOMER',
    phone          VARCHAR(15),
    wallet_balance DECIMAL(10,2)  DEFAULT 0.00,
    created_at     DATETIME       DEFAULT CURRENT_TIMESTAMP
);

-- ─── 2. STALLS ───────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS stalls (
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    name                 VARCHAR(100) NOT NULL,
    location             VARCHAR(150),
    is_active            BOOLEAN      DEFAULT TRUE,
    current_queue_length INT          DEFAULT 0
);

INSERT IGNORE INTO stalls (id, name, location) VALUES
    (1, 'Main Counter',   'Block A – Ground Floor'),
    (2, 'Snacks Counter', 'Block B – Ground Floor'),
    (3, 'Beverages',      'Block A – First Floor'),
    (4, 'South Indian',   'Block C – Ground Floor');

-- ─── 3. MENU ITEMS ───────────────────────────────────────────
CREATE TABLE IF NOT EXISTS menu_items (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    stall_id     BIGINT,
    name         VARCHAR(150)   NOT NULL,
    category     VARCHAR(100),
    price        DECIMAL(8,2)   NOT NULL,
    is_veg       BOOLEAN        DEFAULT TRUE,
    is_available BOOLEAN        DEFAULT TRUE,
    image_url    VARCHAR(300),
    description  TEXT,
    created_at   DATETIME       DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (stall_id) REFERENCES stalls(id) ON DELETE SET NULL
);

INSERT IGNORE INTO menu_items (stall_id, name, category, price, is_veg) VALUES
    (1,'Dal Tadka',           'Main Course (North Indian)', 63,  TRUE),
    (1,'Dal Makhani',         'Main Course (North Indian)', 151, TRUE),
    (1,'Rajma',               'Main Course (North Indian)', 115, TRUE),
    (1,'Chole',               'Main Course (North Indian)', 151, TRUE),
    (1,'Kadhi Pakora',        'Main Course (North Indian)', 226, TRUE),
    (1,'Shahi Paneer',        'Main Course (North Indian)', 171, TRUE),
    (1,'Palak Paneer',        'Main Course (North Indian)', 169, TRUE),
    (1,'Matar Paneer',        'Main Course (North Indian)', 242, TRUE),
    (1,'Kadai Paneer',        'Main Course (North Indian)', 112, TRUE),
    (1,'Malai Kofta',         'Main Course (North Indian)', 239, TRUE),
    (1,'Paneer Butter Masala','Main Course (North Indian)', 171, TRUE),
    (1,'Bhindi Masala',       'Main Course (North Indian)', 97,  TRUE),
    (1,'Aloo Gobi',           'Main Course (North Indian)', 87,  TRUE),
    (1,'Dum Aloo',            'Main Course (North Indian)', 69,  TRUE),
    (1,'Baingan Bharta',      'Main Course (North Indian)', 143, TRUE),
    (2,'Veg Burger',          'Snacks',                     60,  TRUE),
    (2,'Pizza',               'Snacks',                    120,  TRUE),
    (3,'Cold Drink',          'Beverages',                  40,  TRUE),
    (3,'Lassi',               'Beverages',                  50,  TRUE),
    (4,'Hyderabadi Biryani',  'Biryani',                   180, FALSE),
    (4,'Veg Biryani',         'Biryani',                   150,  TRUE),
    (4,'Chicken Biryani',     'Biryani',                   200, FALSE),
    (4,'Mutton Biryani',      'Biryani',                   250, FALSE);

-- ─── 4. ORDERS ───────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS orders (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_code     VARCHAR(20)    NOT NULL UNIQUE,
    user_id        BIGINT,
    stall_id       BIGINT,
    status         ENUM('PENDING','PREPARING','READY','COMPLETED','CANCELLED') DEFAULT 'PENDING',
    payment_method ENUM('CASH','UPI','CARD','WALLET') DEFAULT 'CASH',
    payment_status ENUM('PAID','UNPAID','REFUNDED')   DEFAULT 'UNPAID',
    subtotal       DECIMAL(10,2)  DEFAULT 0.00,
    gst_amount     DECIMAL(10,2)  DEFAULT 0.00,
    total_amount   DECIMAL(10,2)  DEFAULT 0.00,
    pickup_time    DATETIME,
    created_at     DATETIME       DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id)  REFERENCES users(id)  ON DELETE SET NULL,
    FOREIGN KEY (stall_id) REFERENCES stalls(id) ON DELETE SET NULL
);

-- ─── 5. ORDER ITEMS ──────────────────────────────────────────
CREATE TABLE IF NOT EXISTS order_items (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id     BIGINT         NOT NULL,
    menu_item_id BIGINT         NOT NULL,
    quantity     INT            NOT NULL DEFAULT 1,
    unit_price   DECIMAL(8,2)   NOT NULL,
    subtotal     DECIMAL(10,2)  NOT NULL,
    FOREIGN KEY (order_id)     REFERENCES orders(id)     ON DELETE CASCADE,
    FOREIGN KEY (menu_item_id) REFERENCES menu_items(id) ON DELETE CASCADE
);

-- ─── 6. TOKENS ───────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS tokens (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    token_number INT            NOT NULL,
    order_id     BIGINT,
    stall_id     BIGINT,
    status       ENUM('WAITING','SERVING','DONE') DEFAULT 'WAITING',
    issued_at    DATETIME       DEFAULT CURRENT_TIMESTAMP,
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

INSERT IGNORE INTO inventory (material_name, stock_quantity, unit, low_stock_threshold) VALUES
    ('Bread',           20, 'pieces', 5),
    ('Cheese',           8, 'slices', 5),
    ('Tomato',           4, 'kg',     5),
    ('Cold Drink Syrup',15, 'litres', 5),
    ('Rice',            50, 'kg',    10),
    ('Paneer',          10, 'kg',     5),
    ('Onion',           30, 'kg',     8),
    ('Oil',             20, 'litres', 5);

-- ─── 8. WALLET TRANSACTIONS ──────────────────────────────────
CREATE TABLE IF NOT EXISTS wallet_transactions (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT        NOT NULL,
    type        ENUM('CREDIT','DEBIT') NOT NULL,
    amount      DECIMAL(10,2) NOT NULL,
    description VARCHAR(255),
    created_at  DATETIME      DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ─── 9. NOTIFICATIONS ────────────────────────────────────────
CREATE TABLE IF NOT EXISTS notifications (
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id  BIGINT,
    order_id BIGINT,
    type     ENUM('SMS','EMAIL','PUSH') DEFAULT 'SMS',
    message  TEXT,
    phone    VARCHAR(15),
    status   ENUM('SENT','FAILED','PENDING') DEFAULT 'PENDING',
    sent_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id)  REFERENCES users(id)  ON DELETE SET NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE SET NULL
);

-- ─── 10. VIEWS ───────────────────────────────────────────────
CREATE OR REPLACE VIEW daily_sales AS
    SELECT
        DATE(created_at)    AS sale_date,
        COUNT(*)            AS total_orders,
        SUM(total_amount)   AS total_revenue
    FROM orders
    WHERE payment_status = 'PAID'
    GROUP BY DATE(created_at);

CREATE OR REPLACE VIEW top_selling_items_view AS
    SELECT
        mi.name              AS item_name,
        SUM(oi.quantity)     AS total_sold,
        SUM(oi.subtotal)     AS total_revenue
    FROM order_items oi
    JOIN menu_items mi ON oi.menu_item_id = mi.id
    JOIN orders     o  ON oi.order_id     = o.id
    WHERE o.payment_status = 'PAID'
    GROUP BY mi.id, mi.name
    ORDER BY total_sold DESC;

-- ─── 11. FEEDBACK ────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS feedback (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id    BIGINT,
    order_id   BIGINT,
    rating     INT           NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment    TEXT,
    category   ENUM('FOOD_QUALITY','SERVICE','APP_EXPERIENCE','CLEANLINESS','GENERAL') DEFAULT 'GENERAL',
    created_at DATETIME      DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id)  REFERENCES users(id)  ON DELETE SET NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE SET NULL
);

-- ─── 12. CHAT MESSAGES (AI Chatbot history) ──────────────────
CREATE TABLE IF NOT EXISTS chat_messages (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id   VARCHAR(64)  NOT NULL,
    role         ENUM('USER','ASSISTANT') NOT NULL,
    content      TEXT         NOT NULL,
    page_context VARCHAR(50),
    created_at   DATETIME     DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_session (session_id)
);

-- ─── Add is_read column to notifications (if not present) ────
ALTER TABLE notifications
    ADD COLUMN IF NOT EXISTS is_read BOOLEAN DEFAULT FALSE;

-- ─── Seed default admin user (password: admin123) ────────────
-- NOTE: replace the hash below by running BCryptPasswordEncoder.encode("admin123")
INSERT IGNORE INTO users (id, name, email, password, role) VALUES
    (1, 'Admin', 'admin@canteen.com',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lh7y',
     'ADMIN');
