CREATE TABLE products
(
    id BIGSERIAL PRIMARY KEY,

    sku VARCHAR(50) NOT NULL,

    name VARCHAR(255) NOT NULL,

    price NUMERIC(12,2) NOT NULL,

    active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMP NOT NULL,

    updated_at TIMESTAMP
);

ALTER TABLE products
    ADD CONSTRAINT uk_products_sku UNIQUE (sku);