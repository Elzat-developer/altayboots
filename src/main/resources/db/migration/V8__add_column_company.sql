ALTER TABLE companies ADD COLUMN user_main_url varchar(255) DEFAULT NULL;
-- 2. Убеждаемся, что старые записи тоже получили true
UPDATE companies SET user_main_url = NULL WHERE user_main_url IS NULL;