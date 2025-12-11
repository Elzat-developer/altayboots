-- 1. Устанавливаем NOT NULL для обязательных колонок в cart_items
-- Это обеспечит, что в таблице не будет записей без ссылки на Cart или Product.
ALTER TABLE cart_items
    MODIFY COLUMN carts_id INT NOT NULL,
    MODIFY COLUMN products_id INT NOT NULL,
    MODIFY COLUMN quantity INT NOT NULL;

-- 2. Добавляем внешний ключ для связи с carts
-- ON DELETE CASCADE: Если корзина (Cart) будет удалена, все связанные с ней CartItem будут удалены автоматически.
ALTER TABLE cart_items
    ADD CONSTRAINT fk_cart_items_carts
        FOREIGN KEY (carts_id)
            REFERENCES carts(id)
            ON DELETE CASCADE;

-- 3. Добавляем внешний ключ для связи с products
-- ON DELETE NO ACTION (или RESTRICT): Запрещает удаление Product, пока на него ссылается CartItem.
-- Это предотвратит появление "осиротевших" CartItem (товаров без продукта), что было причиной вашего NullPointerException.
ALTER TABLE cart_items
    ADD CONSTRAINT fk_cart_items_products
        FOREIGN KEY (products_id)
            REFERENCES products(id)
            ON DELETE NO ACTION;

-- Дополнительная, но полезная миграция:
-- Убедимся, что product.catalogs_id тоже имеет внешний ключ, если его нет в V1
ALTER TABLE products
    ADD CONSTRAINT fk_products_catalogs
        FOREIGN KEY (catalogs_id)
            REFERENCES catalogs(id)
            ON DELETE SET NULL;