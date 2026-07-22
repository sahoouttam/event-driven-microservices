CREATE TABLE inventories
(
    id BIGSERIAL PRIMARY KEY,

    product_id BIGINT NOT NULL,

    total_quantity INTEGER NOT NULL DEFAULT 0,

    available_quantity INTEGER NOT NULL DEFAULT 0,

    version BIGINT NOT NULL DEFAULT 0,

    created_at TIMESTAMP NOT NULL,

    updated_at TIMESTAMP
);

ALTER TABLE inventory
    ADD CONSTRAINT fk_inventory_product
        FOREIGN KEY (product_id)
            REFERENCES products(id);

ALTER TABLE inventory
    ADD CONSTRAINT uk_inventory_product
        UNIQUE(product_id);