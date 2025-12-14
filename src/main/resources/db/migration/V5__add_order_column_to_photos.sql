ALTER TABLE product_photos
    ADD COLUMN photo_order_index INT;

-- Создаем индекс для более быстрого доступа к порядку, если необходимо (опционально)
CREATE INDEX idx_product_photos_order ON product_photos (promotions_id, photo_order);
-- Добавление нового индекса для оптимизации выборки по Product
CREATE INDEX idx_product_photos_product_order ON product_photos (products_id, photo_order);