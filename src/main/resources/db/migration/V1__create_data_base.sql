CREATE TABLE IF NOT EXISTS `users` (
                                       `id` int auto_increment primary key,
                                       `name` varchar(255) DEFAULT NULL,
                                       `surname` varchar(255) DEFAULT NULL,
                                       `lastname` varchar(255) DEFAULT NULL,
                                       `password` varchar(255) DEFAULT NULL,
                                       `phone` varchar(255) DEFAULT NULL,
                                       `authorities` enum('ADMIN','TEACHER','USER') DEFAULT NULL,
                                       `password_temporary` BOOLEAN DEFAULT FALSE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE IF NOT EXISTS products (
                                        id int auto_increment primary key,
                                        name varchar(255) DEFAULT NULL,
                                        description varchar(255) DEFAULT NULL,
                                        text varchar(255) DEFAULT NULL,
                                        price int DEFAULT NULL,
                                        old_price int DEFAULT NULL,
                                        photo_url varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
