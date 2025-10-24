-- =========================
-- ЧИСТЫЙ СТАРТ (по желанию)
-- =========================
DROP VIEW  IF EXISTS vw_products_catalog;
DROP VIEW  IF EXISTS vw_orders_summary;

DROP FUNCTION IF EXISTS add_to_cart(INT,INT,INT);
DROP FUNCTION IF EXISTS create_order_from_cart(INT, TEXT, NUMERIC);
DROP FUNCTION IF EXISTS add_product_image(INT, VARCHAR, VARCHAR, BYTEA, BOOLEAN);
DROP FUNCTION IF EXISTS set_order_status_by_code(INT, VARCHAR);

DROP TABLE IF EXISTS order_items       CASCADE;
DROP TABLE IF EXISTS order_addresses   CASCADE;
DROP TABLE IF EXISTS orders            CASCADE;
DROP TABLE IF EXISTS order_statuses    CASCADE;
DROP TABLE IF EXISTS product_images    CASCADE;
DROP TABLE IF EXISTS products          CASCADE;
DROP TABLE IF EXISTS categories        CASCADE;
DROP TABLE IF EXISTS favorites         CASCADE;
DROP TABLE IF EXISTS cart_items        CASCADE;
DROP TABLE IF EXISTS reviews           CASCADE;
DROP TABLE IF EXISTS logs              CASCADE;
DROP TABLE IF EXISTS operations        CASCADE;
DROP TABLE IF EXISTS users             CASCADE;
DROP TABLE IF EXISTS roles             CASCADE;

-- =====================
-- СПРАВОЧНИКИ / БАЗА
-- =====================

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

-- Пользователи
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

-- Логи действий
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

-- Каталог товаров
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

-- Изображения товара (храним байты в БД)
CREATE TABLE product_images (
    image_id   SERIAL PRIMARY KEY,
    product_id INT NOT NULL REFERENCES products(product_id) ON DELETE CASCADE,
    filename   VARCHAR(255) NOT NULL,
    mime_type  VARCHAR(100) NOT NULL,      -- image/png, image/jpeg
    bytes      BYTEA NOT NULL,
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
/* Опционально: единственная «главная» картинка на товар
CREATE UNIQUE INDEX uq_product_primary_img
ON product_images(product_id) WHERE is_primary;
*/

-- Социальные сущности
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

-- Справочник статусов заказа
CREATE TABLE order_statuses (
  status_id SERIAL PRIMARY KEY,
  code      VARCHAR(30)  UNIQUE NOT NULL,   -- 'created' / 'in_transit' / 'received'
  title     VARCHAR(50)  UNIQUE NOT NULL    -- 'Оформлен' / 'В пути' / 'Получен'
);

-- Адрес доставки (1:1 с заказом)
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
    user_id    INT NOT NULL,
    address_id INT,
    status_id  INT NOT NULL,  -- ссылка на order_statuses
    CONSTRAINT fk_orders_user
        FOREIGN KEY (user_id)    REFERENCES users(user_id) ON DELETE RESTRICT,
    CONSTRAINT fk_orders_address
        FOREIGN KEY (address_id) REFERENCES order_addresses(address_id) ON DELETE SET NULL,
    CONSTRAINT fk_orders_status
        FOREIGN KEY (status_id)  REFERENCES order_statuses(status_id) ON DELETE RESTRICT,
    CONSTRAINT chk_total_nonneg CHECK (total_sum >= 0)
);

-- Связка адрес ↔ заказ (после создания orders)
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

-- =======================
-- ТЕСТОВЫЕ ДАННЫЕ
-- =======================

-- роли
INSERT INTO roles (role_name) VALUES ('Admin'), ('Manager'), ('User');

-- операции
INSERT INTO operations (operation_name, role_id) VALUES
('LOGIN', 3), ('CREATE_ORDER', 3), ('ADD_PRODUCT', 2);

-- пользователи
INSERT INTO users (login, email, password_hash, role_id, name) VALUES
('admin',   'admin@example.com',   'hash_admin',   1, 'Администратор'),
('manager', 'manager@example.com', 'hash_manager', 2, 'Менеджер'),
('ivan',    'ivan@example.com',    'hash_ivan',    3, 'Иван Петров'),
('olga',    'olga@example.com',    'hash_olga',    3, 'Ольга Сидорова');

-- логи
INSERT INTO logs (operation_id, user_id, details) VALUES
(1, 3, 'Пользователь вошёл'),
(1, 4, 'Пользователь вошёл');

-- категории под кузню/HEMA
INSERT INTO categories (name) VALUES
('Мечи'),
('Кинжалы'),
('Доспехи (HEMA)'),
('Аксессуары')
ON CONFLICT (name) DO NOTHING;

-- товары
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

-- заглушки-картинки (1×1 PNG)
WITH ph AS (
  SELECT decode(
    'iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR4nGNgYAAAAAMAAWgmWQ0AAAAASUVORK5CYII=',
    'base64'
  ) AS bytes
)
INSERT INTO product_images (product_id, filename, mime_type, bytes, is_primary)
SELECT p.product_id, fn, 'image/png', (SELECT bytes FROM ph), TRUE
FROM (
  SELECT 'Полуторный меч (Longsword) HEMA' AS nm, 'placeholder_longsword.png' AS fn UNION ALL
  SELECT 'Федер тренировочный HEMA',       'placeholder_feder.png'          UNION ALL
  SELECT 'Кинжал тренировочный',           'placeholder_dagger.png'         UNION ALL
  SELECT 'Набор доспехов HEMA (базовый)',  'placeholder_hema_set.png'       UNION ALL
  SELECT 'Баклер стальной 30 см',          'placeholder_buckler.png'
) x
JOIN products p ON p.name = x.nm;

-- избранное / корзина / отзывы
INSERT INTO favorites (user_id, product_id) VALUES (3, 1), (3, 3), (4, 2);

INSERT INTO cart_items (user_id, product_id, quantity) VALUES
(3, 1, 1),
(3, 3, 2),
(4, 2, 1);

INSERT INTO reviews (user_id, product_id, rating, comment) VALUES
(3, 1, 5, 'Отличный меч!'),
(4, 2, 4, 'Добротный федер, доволен.');

-- статусы заказов
INSERT INTO order_statuses (code, title) VALUES
('created',   'Оформлен'),
('in_transit','В пути'),
('received',  'Получен');

-- заказы (шапка)
INSERT INTO orders (user_id, status_id)
VALUES
(3, (SELECT status_id FROM order_statuses WHERE code='created')),     -- заказ 1
(4, (SELECT status_id FROM order_statuses WHERE code='in_transit'));  -- заказ 2

-- позиции
INSERT INTO order_items (order_id, product_id, quantity, price_per_unit) VALUES
(1, 1, 1, 39.90),
(1, 3, 2, 24.50),
(2, 2, 1, 49.90);

-- адреса
INSERT INTO order_addresses (order_id, address_line, shipping_cost) VALUES
(1, 'г. Москва, ул. Примерная, д. 1, кв. 10', 4.99),
(2, 'г. Санкт-Петербург, Невский пр., 100, кв. 5', 3.50);

-- финализация сумм и привязка адресов
UPDATE orders o SET
  address_id = (SELECT address_id FROM order_addresses oa WHERE oa.order_id = o.order_id),
  total_sum  = (
    SELECT COALESCE(SUM(oi.quantity * oi.price_per_unit),0)
    FROM order_items oi WHERE oi.order_id = o.order_id
  ) + COALESCE((SELECT shipping_cost FROM order_addresses oa WHERE oa.order_id = o.order_id),0);

-- логи
INSERT INTO logs (operation_id, user_id, details) VALUES
(2, 3, 'Создан заказ №1'),
(2, 4, 'Создан заказ №2');

-- =======================
-- ПРЕДСТАВЛЕНИЯ (VIEW)
-- =======================

-- Каталог товаров (с категорией, финальной ценой, рейтингом и главной картинкой)
CREATE VIEW vw_products_catalog AS
SELECT
  p.product_id,
  p.name,
  p.description,
  c.name                                  AS category_name,
  p.price,
  p.discount_percent,
  ROUND(p.price * (1 - p.discount_percent/100.0), 2) AS price_final,
  p.stock_qty,
  p.is_promotional,
  COALESCE(r.avg_rating, 0)::numeric(3,2) AS avg_rating,
  COALESCE(r.reviews_count, 0)            AS reviews_count,
  img.image_id                            AS primary_image_id,
  img.filename                            AS primary_image_filename,
  img.mime_type                           AS primary_image_mime
FROM products p
JOIN categories c ON c.category_id = p.category_id
LEFT JOIN LATERAL (
  SELECT image_id, filename, mime_type
  FROM product_images
  WHERE product_id = p.product_id
  ORDER BY is_primary DESC, image_id ASC
  LIMIT 1
) img ON TRUE
LEFT JOIN LATERAL (
  SELECT AVG(rating) AS avg_rating, COUNT(*) AS reviews_count
  FROM reviews r
  WHERE r.product_id = p.product_id
) r ON TRUE;

-- Сводка заказов (без строк), с вычисленной суммой и статусом
CREATE VIEW vw_orders_summary AS
SELECT
  o.order_id,
  o.order_date,
  u.login                                 AS user_login,
  oa.address_line,
  oa.shipping_cost,
  s.title                                  AS status_title,   -- 'Оформлен' / 'В пути' / 'Получен'
  s.code                                   AS status_code,    -- 'created' / 'in_transit' / 'received'
  COALESCE(items.items_count, 0)           AS items_count,
  COALESCE(items.items_sum, 0)::numeric(12,2) AS items_sum,
  (COALESCE(items.items_sum, 0) + COALESCE(oa.shipping_cost, 0))::numeric(12,2) AS computed_total_sum,
  o.total_sum                              AS stored_total_sum
FROM orders o
JOIN users u              ON u.user_id = o.user_id
LEFT JOIN order_addresses oa ON oa.address_id = o.address_id
JOIN order_statuses s     ON s.status_id = o.status_id
LEFT JOIN (
  SELECT oi.order_id,
         SUM(oi.quantity)                                    AS items_count,
         SUM(oi.quantity * oi.price_per_unit)::numeric(12,2) AS items_sum
  FROM order_items oi
  GROUP BY oi.order_id
) items ON items.order_id = o.order_id;

-- =======================
-- ФУНКЦИИ (PL/pgSQL)
-- =======================

-- Добавить товар в корзину (upsert)
CREATE OR REPLACE FUNCTION add_to_cart(
  p_user_id    INT,
  p_product_id INT,
  p_qty        INT
)
RETURNS TABLE(cart_item_id INT, quantity INT) LANGUAGE plpgsql AS $$
BEGIN
  IF p_qty <= 0 THEN
    RAISE EXCEPTION 'Количество должно быть > 0';
  END IF;

  RETURN QUERY
  INSERT INTO cart_items (user_id, product_id, quantity)
  VALUES (p_user_id, p_product_id, p_qty)
  ON CONFLICT (user_id, product_id)
  DO UPDATE SET quantity = cart_items.quantity + EXCLUDED.quantity
  RETURNING cart_item_id, quantity;
END;
$$;

-- Оформить заказ из корзины (со списанием склада)
CREATE OR REPLACE FUNCTION create_order_from_cart(
  p_user_id       INT,
  p_address_line  TEXT,
  p_shipping_cost NUMERIC(10,2) DEFAULT 0
)
RETURNS TABLE(order_id INT, total_sum NUMERIC(12,2)) LANGUAGE plpgsql AS $$
DECLARE
  v_order_id   INT;
  v_address_id INT;
  v_items_sum  NUMERIC(12,2) := 0;
  v_unit_price NUMERIC(10,2);
  rec RECORD;
BEGIN
  IF p_shipping_cost < 0 THEN
    RAISE EXCEPTION 'Стоимость доставки не может быть отрицательной';
  END IF;

  IF NOT EXISTS (SELECT 1 FROM cart_items WHERE user_id = p_user_id) THEN
    RAISE EXCEPTION 'Корзина пользователя % пуста', p_user_id;
  END IF;

  -- Лочим товары и проверяем остатки
  FOR rec IN
    SELECT p.product_id, p.name, p.stock_qty, p.price, p.discount_percent, ci.quantity
    FROM products p
    JOIN cart_items ci ON ci.product_id = p.product_id
    WHERE ci.user_id = p_user_id
    FOR UPDATE
  LOOP
    IF rec.stock_qty < rec.quantity THEN
      RAISE EXCEPTION 'Недостаточно на складе: % (в наличии %, запрошено %)',
        rec.name, rec.stock_qty, rec.quantity;
    END IF;
  END LOOP;

  -- Создаём заказ со статусом "Оформлен"
  INSERT INTO orders (user_id, status_id)
  VALUES (p_user_id, (SELECT status_id FROM order_statuses WHERE code='created'))
  RETURNING order_id INTO v_order_id;

  -- Позиции, сумма и списание склада
  FOR rec IN
    SELECT p.product_id, p.price, p.discount_percent, ci.quantity
    FROM products p
    JOIN cart_items ci ON ci.product_id = p.product_id
    WHERE ci.user_id = p_user_id
  LOOP
    v_unit_price := ROUND(rec.price * (1 - rec.discount_percent/100.0), 2);

    INSERT INTO order_items (order_id, product_id, quantity, price_per_unit)
    VALUES (v_order_id, rec.product_id, rec.quantity, v_unit_price);

    v_items_sum := v_items_sum + rec.quantity * v_unit_price;

    UPDATE products
       SET stock_qty = stock_qty - rec.quantity
     WHERE product_id = rec.product_id;
  END LOOP;

  -- Адрес и итог
  INSERT INTO order_addresses (order_id, address_line, shipping_cost)
  VALUES (v_order_id, p_address_line, COALESCE(p_shipping_cost,0))
  RETURNING address_id INTO v_address_id;

  UPDATE orders
     SET address_id = v_address_id,
         total_sum  = v_items_sum + COALESCE(p_shipping_cost,0)
   WHERE order_id = v_order_id;

  -- Очистка корзины
  DELETE FROM cart_items WHERE user_id = p_user_id;

  RETURN QUERY SELECT v_order_id, (v_items_sum + COALESCE(p_shipping_cost,0))::NUMERIC(12,2);
END;
$$;

-- Добавить картинку товара (BYTEA) и по желанию сделать её «главной»
CREATE OR REPLACE FUNCTION add_product_image(
  p_product_id INT,
  p_filename   VARCHAR(255),
  p_mime_type  VARCHAR(100),
  p_bytes      BYTEA,
  p_is_primary BOOLEAN DEFAULT FALSE
)
RETURNS INT LANGUAGE plpgsql AS $$
DECLARE
  v_image_id INT;
BEGIN
  IF p_is_primary THEN
    UPDATE product_images
       SET is_primary = FALSE
     WHERE product_id = p_product_id
       AND is_primary = TRUE;
  END IF;

  INSERT INTO product_images (product_id, filename, mime_type, bytes, is_primary)
  VALUES (p_product_id, p_filename, p_mime_type, p_bytes, COALESCE(p_is_primary, FALSE))
  RETURNING image_id INTO v_image_id;

  RETURN v_image_id;
END;
$$;

-- Сменить статус заказа по коду ('created'/'in_transit'/'received')
CREATE OR REPLACE FUNCTION set_order_status_by_code(
  p_order_id INT,
  p_status_code VARCHAR
)
RETURNS TABLE(order_id INT, old_status VARCHAR, new_status VARCHAR) LANGUAGE plpgsql AS $$
DECLARE
  v_old VARCHAR;
  v_new_id INT;
  v_new_title VARCHAR;
BEGIN
  SELECT status_id, title INTO v_new_id, v_new_title
  FROM order_statuses WHERE code = p_status_code;
  IF v_new_id IS NULL THEN
    RAISE EXCEPTION 'Неизвестный статус: %', p_status_code;
  END IF;

  SELECT s.title INTO v_old
  FROM orders o JOIN order_statuses s ON s.status_id = o.status_id
  WHERE o.order_id = p_order_id
  FOR UPDATE;
  IF NOT FOUND THEN
    RAISE EXCEPTION 'Заказ % не найден', p_order_id;
  END IF;

  UPDATE orders SET status_id = v_new_id WHERE order_id = p_order_id;

  RETURN QUERY SELECT p_order_id, v_old, v_new_title;
END;
$$;

-- =======================
-- ПРОВЕРКИ (по желанию)
-- =======================
-- SELECT * FROM vw_products_catalog ORDER BY product_id;
-- SELECT * FROM vw_orders_summary  ORDER BY order_id;
SELECT * FROM Users;

UPDATE Users
SET role_id = 1
WHERE user_id = 5;



