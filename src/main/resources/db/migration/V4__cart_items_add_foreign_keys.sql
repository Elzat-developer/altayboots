-- V4__cart_items_add_foreign_keys.sql

-- 1. Очистка: Удаляем невалидные записи, чтобы можно было установить NOT NULL
DELETE FROM cart_items
WHERE carts_id NOT IN (SELECT id FROM carts)
   OR products_id NOT IN (SELECT id FROM products);

-- 2. Обеспечение целостности: Устанавливаем NOT NULL
ALTER TABLE cart_items
    MODIFY COLUMN carts_id INT NOT NULL,
    MODIFY COLUMN products_id INT NOT NULL,
    MODIFY COLUMN quantity INT NOT NULL;

-- 3. Добавление внешнего ключа для связи с Cart
ALTER TABLE cart_items
    ADD CONSTRAINT fk_cart_items_carts
        FOREIGN KEY (carts_id)
            REFERENCES carts(id)
            ON DELETE CASCADE; -- Удаление корзины удалит элементы

-- 4. Добавление внешнего ключа для связи с Product
ALTER TABLE cart_items
    ADD CONSTRAINT fk_cart_items_products
        FOREIGN KEY (products_id)
            REFERENCES products(id)
            ON DELETE RESTRICT; -- Нельзя удалить продукт, пока он в чьей-то корзине