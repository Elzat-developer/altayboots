DELETE FROM cart_items
WHERE carts_id NOT IN (SELECT id FROM carts)
   OR products_id NOT IN (SELECT id FROM products);

-- 2. Устанавливаем NOT NULL для обязательных колонок в cart_items
ALTER TABLE cart_items
    MODIFY COLUMN carts_id INT NOT NULL,
    MODIFY COLUMN products_id INT NOT NULL,
    MODIFY COLUMN quantity INT NOT NULL;

-- 3. Добавляем внешний ключ для связи с carts
-- ON DELETE CASCADE: Если корзина (Cart) будет удалена, все связанные с ней CartItem удалятся.
ALTER TABLE cart_items
    ADD CONSTRAINT fk_cart_items_carts
        FOREIGN KEY (carts_id)
            REFERENCES carts(id)
            ON DELETE CASCADE;

-- 4. Добавляем внешний ключ для связи с products
-- ON DELETE RESTRICT (или NO ACTION): Запрещает удаление Product, пока на него ссылается CartItem.
-- Это гарантирует, что i.getProduct() в вашем Java-коде никогда не будет null.
ALTER TABLE cart_items
    ADD CONSTRAINT fk_cart_items_products
        FOREIGN KEY (products_id)
            REFERENCES products(id)
            ON DELETE RESTRICT;