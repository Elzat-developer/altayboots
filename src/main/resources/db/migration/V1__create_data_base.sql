-- 1. Справочники и независимые таблицы
CREATE TABLE IF NOT EXISTS `catalogs` (
                                          `id` INT AUTO_INCREMENT PRIMARY KEY,
                                          `name` VARCHAR(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS `users` (
                                       `id` INT AUTO_INCREMENT PRIMARY KEY,
                                       `name` VARCHAR(255) DEFAULT NULL,
                                       `surname` VARCHAR(255) DEFAULT NULL,
                                       `lastname` VARCHAR(255) DEFAULT NULL,
                                       `password` VARCHAR(255) DEFAULT NULL,
                                       `phone` VARCHAR(255) DEFAULT NULL,
                                       `authorities` ENUM('ADMIN', 'USER') DEFAULT 'USER',
                                       `region` VARCHAR(255) DEFAULT NULL,
                                       `city_or_district` VARCHAR(255) DEFAULT NULL,
                                       `street` VARCHAR(255) DEFAULT NULL,
                                       `house_or_apartment` VARCHAR(255) DEFAULT NULL,
                                       `index_post` VARCHAR(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS `companies` (
                                           `id` INT AUTO_INCREMENT PRIMARY KEY,
                                           `name` VARCHAR(255) DEFAULT NULL,
                                           `text` VARCHAR(255) DEFAULT NULL,
                                           `photo_url` VARCHAR(255) DEFAULT NULL,
                                           `base` VARCHAR(255) DEFAULT NULL,
                                           `city` VARCHAR(255) DEFAULT NULL,
                                           `street` VARCHAR(255) DEFAULT NULL,
                                           `email` VARCHAR(255) DEFAULT NULL,
                                           `phone` VARCHAR(255) DEFAULT NULL,
                                           `job_start` VARCHAR(255) DEFAULT NULL,
                                           `job_end` VARCHAR(255) DEFAULT NULL,
                                           `free_start` VARCHAR(255) DEFAULT NULL,
                                           `free_end` VARCHAR(255) DEFAULT NULL,
                                           `user_main_url` VARCHAR(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 2. Товары и связанные с ними таблицы
CREATE TABLE IF NOT EXISTS `products` (
                                          `id` INT AUTO_INCREMENT PRIMARY KEY,
                                          `name` VARCHAR(255) DEFAULT NULL,
                                          `description` VARCHAR(255) DEFAULT NULL,
                                          `text` VARCHAR(255) DEFAULT NULL,
                                          `price` INT DEFAULT NULL,
                                          `old_price` INT DEFAULT NULL,
                                          `catalogs_id` INT DEFAULT NULL,
                                          `size` VARCHAR(255) DEFAULT NULL,
                                          `active` BOOLEAN NOT NULL DEFAULT TRUE,
                                          `youtube_url` VARCHAR(255) DEFAULT NULL,
                                          CONSTRAINT `fk_products_catalogs` FOREIGN KEY (`catalogs_id`) REFERENCES `catalogs` (`id`) ON DELETE SET NULL,
                                          INDEX `idx_products_active` (`active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS `product_sizes` (
                                               `products_id` INT NOT NULL,
                                               `size_value` VARCHAR(50) NOT NULL,
                                               CONSTRAINT `fk_product_sizes_product`
                                                   FOREIGN KEY (`products_id`)
                                                       REFERENCES `products` (`id`) ON DELETE CASCADE,
                                               UNIQUE (`products_id`, `size_value`),
                                               INDEX `idx_product_sizes_value` (`size_value`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 3. Акции и фото (зависят от продуктов и каталогов)
CREATE TABLE IF NOT EXISTS `promotions` (
                                            `id` INT AUTO_INCREMENT PRIMARY KEY,
                                            `name` VARCHAR(255) DEFAULT NULL,
                                            `description` VARCHAR(255) DEFAULT NULL,
                                            `percentage_discounted` INT DEFAULT NULL,
                                            `global` BOOLEAN DEFAULT FALSE,
                                            `active` BOOLEAN DEFAULT FALSE,
                                            `start_date` VARCHAR(255) DEFAULT NULL,
                                            `end_date` VARCHAR(255) DEFAULT NULL,
                                            `catalogs_id` INT DEFAULT NULL,
                                            `products_id` INT DEFAULT NULL,
                                            CONSTRAINT `fk_promotions_catalogs`
                                                FOREIGN KEY (`catalogs_id`)
                                                    REFERENCES `catalogs` (`id`) ON DELETE SET NULL,
                                            CONSTRAINT `fk_promotions_products`
                                                FOREIGN KEY (`products_id`)
                                                    REFERENCES `products` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS `product_photos` (
                                                `id` INT AUTO_INCREMENT PRIMARY KEY,
                                                `photo_url` VARCHAR(255) DEFAULT NULL,
                                                `products_id` INT DEFAULT NULL,
                                                `promotions_id` INT DEFAULT NULL,
                                                `photo_order_index` INT NULL DEFAULT 0,
                                                CONSTRAINT `fk_photos_products`
                                                    FOREIGN KEY (`products_id`)
                                                        REFERENCES `products` (`id`) ON DELETE CASCADE,
                                                CONSTRAINT `fk_photos_promotions`
                                                    FOREIGN KEY (`promotions_id`)
                                                        REFERENCES `promotions` (`id`) ON DELETE CASCADE,
                                                INDEX `idx_product_photos_order` (`promotions_id`, `photo_order_index`),
                                                INDEX `idx_product_photos_product_order` (`products_id`, `photo_order_index`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 4. Заказы и корзины
CREATE TABLE IF NOT EXISTS `orders` (
                                        `id` INT AUTO_INCREMENT PRIMARY KEY,
                                        `name` VARCHAR(255) DEFAULT NULL,
                                        `order_start_date` DATETIME DEFAULT NULL,
                                        `paid_status` ENUM('PAID', 'NOTPAY') DEFAULT 'NOTPAY',
                                        `users_id` INT DEFAULT NULL,
                                        CONSTRAINT `fk_orders_users`
                                            FOREIGN KEY (`users_id`)
                                                REFERENCES `users` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS `order_products` (
                                                `orders_id` INT AUTO_INCREMENT PRIMARY KEY,
                                                `products_id` INT DEFAULT NULL,
                                                CONSTRAINT `fk_order_products_orders`
                                                    FOREIGN KEY (`orders_id`)
                                                        REFERENCES `orders` (`id`) ON DELETE CASCADE,
                                                CONSTRAINT `fk_order_products_products`
                                                    FOREIGN KEY (`products_id`)
                                                        REFERENCES `products` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS `order_items` (
                                             `id` INT AUTO_INCREMENT PRIMARY KEY,
                                             `quantity` INT DEFAULT NULL,
                                             `products_id` INT DEFAULT NULL,
                                             `orders_id` INT DEFAULT NULL,
                                             CONSTRAINT `fk_order_items_products`
                                                 FOREIGN KEY (`products_id`)
                                                     REFERENCES `products` (`id`) ON DELETE SET NULL,
                                             CONSTRAINT `fk_order_items_orders`
                                                 FOREIGN KEY (`orders_id`)
                                                     REFERENCES `orders` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS `carts` (
                                       `id` INT AUTO_INCREMENT PRIMARY KEY,
                                       `users_id` INT DEFAULT NULL,
                                       CONSTRAINT `fk_carts_users`
                                           FOREIGN KEY (`users_id`)
                                               REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS `cart_items` (
                                            `id` INT AUTO_INCREMENT PRIMARY KEY,
                                            `quantity` INT NOT NULL,
                                            `carts_id` INT NOT NULL,
                                            `products_id` INT NOT NULL,
                                            CONSTRAINT `fk_cart_items_carts`
                                                FOREIGN KEY (`carts_id`)
                                                    REFERENCES `carts` (`id`) ON DELETE CASCADE,
                                            CONSTRAINT `fk_cart_items_products`
                                                FOREIGN KEY (`products_id`)
                                                    REFERENCES `products` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;