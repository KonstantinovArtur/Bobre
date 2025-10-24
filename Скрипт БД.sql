-- Роли пользователей
CREATE TABLE roles (
    role_id    SERIAL PRIMARY KEY,
    role_name  VARCHAR(50) UNIQUE NOT NULL
);

-- Категории товаров
CREATE TABLE categories (
    category_id SERIAL PRIMARY KEY,
    name        VARCHAR(100) UNIQUE NOT NULL
);

-- Операции (для аудита/логов)
CREATE TABLE operations (
    operation_id   SERIAL PRIMARY KEY,
    operation_name VARCHAR(100) NOT NULL,
    happened_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    role_id        INT NOT NULL,
    CONSTRAINT fk_operations_role
        FOREIGN KEY (role_id) REFERENCES roles(role_id) ON DELETE RESTRICT
);

-- ====== Пользователи и безопасность ======

CREATE TABLE users (
    user_id        SERIAL PRIMARY KEY,
    login          VARCHAR(100) NOT NULL UNIQUE,
    email          VARCHAR(255) NOT NULL UNIQUE,
    password_hash  TEXT NOT NULL,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    role_id        INT NOT NULL,
    name           VARCHAR(150),
    CONSTRAINT fk_users_role
        FOREIGN KEY (role_id) REFERENCES roles(role_id) ON DELETE RESTRICT,
    CONSTRAINT chk_email_has_at CHECK (position('@' in email) > 1)
);

-- Логи действий: кто-что-когда
CREATE TABLE logs (
    log_id       SERIAL PRIMARY KEY,
    operation_id INT NOT NULL,
    user_id      INT NOT NULL,
    details      TEXT,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_logs_operation
        FOREIGN KEY (operation_id) REFERENCES operations(operation_id) ON DELETE CASCADE,
    CONSTRAINT fk_logs_user
        FOREIGN KEY (user_id)      REFERENCES users(user_id) ON DELETE CASCADE
);

-- ====== Каталог товаров ======
CREATE TABLE products (
    product_id       SERIAL PRIMARY KEY,
    name             VARCHAR(200) NOT NULL,
    description      TEXT,
    price            NUMERIC(10,2) NOT NULL CHECK (price >= 0),
    stock_qty        INT NOT NULL DEFAULT 0 CHECK (stock_qty >= 0),
    category_id      INT NOT NULL REFERENCES categories(category_id) ON DELETE RESTRICT,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    discount_percent NUMERIC(5,2) NOT NULL DEFAULT 0 CHECK (discount_percent BETWEEN 0 AND 100),
    is_promotional   BOOLEAN NOT NULL DEFAULT FALSE
);

-- Храним сами байты: не зависим от файловой системы
CREATE TABLE product_images (
    image_id   SERIAL PRIMARY KEY,
    product_id INT NOT NULL REFERENCES products(product_id) ON DELETE CASCADE,
    filename   VARCHAR(255) NOT NULL,      -- исходное имя файла (для удобства)
    mime_type  VARCHAR(100) NOT NULL,      -- например: image/png, image/jpeg
    bytes      BYTEA NOT NULL,             -- содержимое файла
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Опционально (если нужно гарантировать ровно одну «главную» картинку на товар):
-- CREATE UNIQUE INDEX uq_product_primary_img ON product_images(product_id) WHERE is_primary;


-- ====== Социальные сущности ======

-- Избранное
CREATE TABLE favorites (
    favorite_id SERIAL PRIMARY KEY,
    user_id     INT NOT NULL,
    product_id  INT NOT NULL,
    added_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_favorites_user
        FOREIGN KEY (user_id)    REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_favorites_prod
        FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE,
    CONSTRAINT uq_favorites_user_product UNIQUE (user_id, product_id)
);

-- Корзина
CREATE TABLE cart_items (
    cart_item_id SERIAL PRIMARY KEY,
    user_id      INT NOT NULL,
    product_id   INT NOT NULL,
    quantity     INT NOT NULL,
    added_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_cart_user
        FOREIGN KEY (user_id)    REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_cart_product
        FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE RESTRICT,
    CONSTRAINT chk_cart_qty_pos CHECK (quantity > 0),
    CONSTRAINT uq_cart_user_product UNIQUE (user_id, product_id)
);

-- Отзывы
CREATE TABLE reviews (
    review_id  SERIAL PRIMARY KEY,
    user_id    INT NOT NULL,
    product_id INT NOT NULL,
    rating     INT NOT NULL,
    comment    TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_reviews_user
        FOREIGN KEY (user_id)    REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_reviews_product
        FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE,
    CONSTRAINT chk_rating_1_5 CHECK (rating BETWEEN 1 AND 5)
    -- ,CONSTRAINT uq_review_once UNIQUE (user_id, product_id)
);

-- ====== Заказы ======

-- Адрес доставки заказа (1:1 с заказом)
CREATE TABLE order_addresses (
    address_id    SERIAL PRIMARY KEY,
    order_id      INT UNIQUE,
    address_line  TEXT NOT NULL,
    shipping_cost NUMERIC(10,2) NOT NULL DEFAULT 0,
    CONSTRAINT chk_ship_cost_nonneg CHECK (shipping_cost >= 0)
);

-- Заказ (шапка)
CREATE TABLE orders (
    order_id   SERIAL PRIMARY KEY,
    order_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    total_sum  NUMERIC(12,2) NOT NULL DEFAULT 0,
    status     VARCHAR(20)  NOT NULL DEFAULT 'new',
    user_id    INT NOT NULL,
    address_id INT,
    CONSTRAINT fk_orders_user
        FOREIGN KEY (user_id)    REFERENCES users(user_id) ON DELETE RESTRICT,
    CONSTRAINT fk_orders_address
        FOREIGN KEY (address_id) REFERENCES order_addresses(address_id) ON DELETE SET NULL,
    CONSTRAINT chk_total_nonneg CHECK (total_sum >= 0),
    CONSTRAINT chk_status_enum CHECK (status IN ('new','paid','shipped','delivered','cancelled'))
);

-- Связка адрес ↔ заказ (добавляем FK после создания orders)
ALTER TABLE order_addresses
    ADD CONSTRAINT fk_addr_order
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE;

-- Позиции заказа
CREATE TABLE order_items (
    order_item_id  SERIAL PRIMARY KEY,
    order_id       INT NOT NULL,
    product_id     INT NOT NULL,
    quantity       INT NOT NULL,
    price_per_unit NUMERIC(10,2) NOT NULL,
    CONSTRAINT fk_order_items_order
        FOREIGN KEY (order_id)   REFERENCES orders(order_id) ON DELETE CASCADE,
    CONSTRAINT fk_order_items_product
        FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE RESTRICT,
    CONSTRAINT chk_item_qty_pos   CHECK (quantity > 0),
    CONSTRAINT chk_item_price_ge0 CHECK (price_per_unit >= 0)
);

-- ================= ТЕСТОВЫЕ ДАННЫЕ =================

INSERT INTO roles (role_name) VALUES ('Admin'), ('Manager'), ('User');

INSERT INTO operations (operation_name, role_id) VALUES
('LOGIN', 3), ('CREATE_ORDER', 3), ('ADD_PRODUCT', 2);

INSERT INTO users (login, email, password_hash, role_id, name) VALUES
('admin',   'admin@example.com',   'hash_admin',   1, 'Администратор'),
('manager', 'manager@example.com', 'hash_manager', 2, 'Менеджер'),
('ivan',    'ivan@example.com',    'hash_ivan',    3, 'Иван Петров'),
('olga',    'olga@example.com',    'hash_olga',    3, 'Ольга Сидорова');

INSERT INTO logs (operation_id, user_id, details) VALUES
(1, 3, 'Пользователь вошёл'), (1, 4, 'Пользователь вошёл');


-- ====== Категории под кузню/HEMA ======
-- если категории уже есть — просто добавятся недостающие
INSERT INTO categories (name) VALUES
('Мечи'),
('Кинжалы'),
('Доспехи (HEMA)'),
('Аксессуары')
ON CONFLICT (name) DO NOTHING;

-- ====== Товары (без image_url; картинки в product_images) ======
INSERT INTO products
(name, description, price, stock_qty, category_id, discount_percent, is_promotional)
VALUES
('Полуторный меч (Longsword) HEMA',
 'Клинок из пружинной стали 50CrV4, сбалансирован для спарринга.',
 289.90, 10, (SELECT category_id FROM categories WHERE name = 'Мечи'), 5, TRUE),

('Федер тренировочный HEMA',
 'Безопасные кромки, гибкая верхушка, для интенсивных тренировок.',
 219.00, 15, (SELECT category_id FROM categories WHERE name = 'Мечи'), 0, FALSE),

('Кинжал тренировочный',
 'Сталь 40Х13, тупые кромки, парирование и ближний бой.',
 59.90,  30, (SELECT category_id FROM categories WHERE name = 'Кинжалы'), 0, FALSE),

('Набор доспехов HEMA (базовый)',
 'Маска 350N, горжет, перчатки — стартовый комплект для спарринга.',
 399.00, 5,  (SELECT category_id FROM categories WHERE name = 'Доспехи (HEMA)'), 10, TRUE),

('Баклер стальной 30 см',
 'Толстая сталь, удобная рукоять, классический круглый баклер.',
 44.90,  25, (SELECT category_id FROM categories WHERE name = 'Аксессуары'), 0, FALSE);

-- ====== Заглушки-картинки (1×1 PNG), складываем в БД ======
WITH ph AS (
  SELECT decode(
    'iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR4nGNgYAAAAAMAAWgmWQ0AAAAASUVORK5CYII=',
    'base64'
  ) AS bytes
)
INSERT INTO product_images (product_id, filename, mime_type, bytes, is_primary)
VALUES
((SELECT product_id FROM products WHERE name = 'Полуторный меч (Longsword) HEMA'),
 'placeholder_longsword.png', 'image/png', (SELECT bytes FROM ph), TRUE),

((SELECT product_id FROM products WHERE name = 'Федер тренировочный HEMA'),
 'placeholder_feder.png', 'image/png', (SELECT bytes FROM ph), TRUE),

((SELECT product_id FROM products WHERE name = 'Кинжал тренировочный'),
 'placeholder_dagger.png', 'image/png', (SELECT bytes FROM ph), TRUE),

((SELECT product_id FROM products WHERE name = 'Набор доспехов HEMA (базовый)'),
 'placeholder_hema_set.png', 'image/png', (SELECT bytes FROM ph), TRUE),

((SELECT product_id FROM products WHERE name = 'Баклер стальной 30 см'),
 'placeholder_buckler.png', 'image/png', (SELECT bytes FROM ph), TRUE);

INSERT INTO favorites (user_id, product_id) VALUES
(3, 1), (3, 3), (4, 2);

INSERT INTO cart_items (user_id, product_id, quantity) VALUES
(3, 1, 1),
(3, 3, 2),
(4, 2, 1);

INSERT INTO reviews (user_id, product_id, rating, comment) VALUES
(3, 1, 5, 'Отличный набор!'),
(4, 2, 4, 'Полезная книга');

-- Заказ Ивана
INSERT INTO orders (user_id, status) VALUES (3, 'new');              -- order_id = 1
INSERT INTO order_items (order_id, product_id, quantity, price_per_unit) VALUES
(1, 1, 1, 39.90),
(1, 3, 2, 24.50);
INSERT INTO order_addresses (order_id, address_line, shipping_cost) VALUES
(1, 'г. Москва, ул. Примерная, д. 1, кв. 10', 4.99);
UPDATE orders
   SET address_id = (SELECT address_id FROM order_addresses WHERE order_id = 1),
       total_sum  = (
           SELECT COALESCE(SUM(quantity * price_per_unit),0) FROM order_items WHERE order_id = 1
       ) + (SELECT shipping_cost FROM order_addresses WHERE order_id = 1)
 WHERE order_id = 1;

-- Заказ Ольги
INSERT INTO orders (user_id, status) VALUES (4, 'paid');            -- order_id = 2
INSERT INTO order_items (order_id, product_id, quantity, price_per_unit) VALUES
(2, 2, 1, 49.90);
INSERT INTO order_addresses (order_id, address_line, shipping_cost) VALUES
(2, 'г. Санкт-Петербург, Невский пр., 100, кв. 5', 3.50);
UPDATE orders
   SET address_id = (SELECT address_id FROM order_addresses WHERE order_id = 2),
       total_sum  = (
           SELECT COALESCE(SUM(quantity * price_per_unit),0) FROM order_items WHERE order_id = 2
       ) + (SELECT shipping_cost FROM order_addresses WHERE order_id = 2)
 WHERE order_id = 2;

INSERT INTO logs (operation_id, user_id, details) VALUES
(2, 3, 'Создан заказ №1'),
(2, 4, 'Создан заказ №2');
select * from products

