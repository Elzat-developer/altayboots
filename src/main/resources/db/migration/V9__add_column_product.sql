ALTER TABLE products ADD COLUMN youtube_url varchar(255) DEFAULT NULL;
-- 2. Убеждаемся, что старые записи тоже получили true
UPDATE products SET youtube_url = NULL WHERE youtube_url IS NULL;