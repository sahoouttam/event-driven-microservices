CREATE TABLE inventory_transactions
(
    id BIGSERIAL PRIMARY KEY,

    inventory_id BIGINT NOT NULL,

    transaction_type VARCHAR(30) NOT NULL,

    quantity INTEGER NOT NULL,

    created_at TIMESTAMP NOT NULL,

    updated_at TIMESTAMP
);

ALTER TABLE inventory_transactions
    ADD CONSTRAINT fk_inventory_transaction_product
        FOREIGN KEY(product_id)
            REFERENCES products(id);