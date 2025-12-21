-- Добавляем колонку
ALTER TABLE products ADD COLUMN active BOOLEAN NOT NULL DEFAULT TRUE;
-- 2. Убеждаемся, что старые записи тоже получили true
UPDATE products SET active = TRUE WHERE active IS NULL;
-- (Опционально) Индекс для быстрого поиска активных товаров
CREATE INDEX idx_products_active ON products(active);