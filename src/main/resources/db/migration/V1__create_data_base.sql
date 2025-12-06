CREATE TABLE IF NOT EXISTS `users` (
                                       `id` int auto_increment primary key,
                                       `name` varchar(255) DEFAULT NULL,
                                       `surname` varchar(255) DEFAULT NULL,
                                       `lastname` varchar(255) DEFAULT NULL,
                                       `password` varchar(255) DEFAULT NULL,
                                       `phone` varchar(255) DEFAULT NULL,
                                       `authorities` enum('ADMIN','USER') DEFAULT 'USER',
                                       region varchar(255) DEFAULT NULL,
                                       city_or_district varchar(255) DEFAULT NULL,
                                       street varchar(255) DEFAULT NULL,
                                       house_or_apartment varchar(255) DEFAULT NULL,
                                       index_post varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE IF NOT EXISTS products (
                                        id int auto_increment primary key,
                                        name varchar(255) DEFAULT NULL,
                                        description varchar(255) DEFAULT NULL,
                                        text varchar(255) DEFAULT NULL,
                                        price int DEFAULT NULL,
                                        old_price int DEFAULT NULL,
                                        catalogs_id int DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE IF NOT EXISTS catalogs (
                                        id int auto_increment primary key,
                                        name varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE IF NOT EXISTS companies (
                                        id int auto_increment primary key,
                                        name varchar(255) DEFAULT NULL,
                                        text varchar(255) DEFAULT NULL,
                                        photo_url varchar(255) DEFAULT NULL,
                                        base varchar(255) DEFAULT NULL,
                                        city varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE IF NOT EXISTS product_photos (
                                         id int auto_increment primary key,
                                         photo_url varchar(255) DEFAULT NULL,
                                         products_id int DEFAULT NULL,
                                         promotions_id int DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE IF NOT EXISTS promotions (
                                         id int auto_increment primary key,
                                         name varchar(255) DEFAULT NULL,
                                         description varchar(255) DEFAULT NULL,
                                         percentage_discounted int DEFAULT NULL,
                                         global boolean default false,
                                         active boolean default false,
                                         start_date varchar(255) DEFAULT NULL,
                                         end_date varchar(255) DEFAULT NULL,
                                         catalogs_id int DEFAULT NULL,
                                         products_id int DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE IF NOT EXISTS orders (
                                          id int auto_increment primary key,
                                          name varchar(255) DEFAULT NULL,
                                          order_start_date datetime DEFAULT NULL,
                                          paid_status enum('PAID','NOTPAY') DEFAULT 'NOTPAY',
                                          users_id int DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE IF NOT EXISTS order_products (
                                          orders_id int auto_increment primary key,
                                          products_id int DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE IF NOT EXISTS order_items (
                                      id int auto_increment primary key,
                                      quantity int DEFAULT NULL,
                                      products_id int DEFAULT NULL,
                                      orders_id int DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE IF NOT EXISTS carts (
                                      id int auto_increment primary key,
                                      users_id int DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE IF NOT EXISTS cart_items (
                                      id int auto_increment primary key,
                                      quantity int DEFAULT NULL,
                                      carts_id int DEFAULT NULL,
                                      products_id int DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
