ALTER TABLE products
    ADD COLUMN size VARCHAR(255) DEFAULT NULL;
ALTER TABLE companies
    ADD COLUMN street VARCHAR(255) DEFAULT NULL,
    ADD COLUMN email VARCHAR(255) DEFAULT NULL,
    ADD COLUMN phone VARCHAR(255) DEFAULT NULL,
    ADD COLUMN job_start VARCHAR(255) DEFAULT NULL,
    ADD COLUMN job_end VARCHAR(255) DEFAULT NULL,
    ADD COLUMN free_start VARCHAR(255) DEFAULT NULL,
    ADD COLUMN free_end VARCHAR(255) DEFAULT NULL;
CREATE TABLE product_sizes (
                               products_id INT NOT NULL,
                               size_value VARCHAR(50) NOT NULL,

                               CONSTRAINT fk_product_sizes_product
                                   FOREIGN KEY (products_id)
                                       REFERENCES products (id)
                                       ON DELETE CASCADE,

                            UNIQUE (products_id, size_value)
);
-- Индекс ускорит поиск товаров по размеру
CREATE INDEX idx_product_sizes_value ON product_sizes(size_value);