CREATE TABLE order_items (

    id BIGSERIAL PRIMARY KEY,

    order_id BIGINT NOT NULL,

    sku VARCHAR(50) NOT NULL,

    product_name VARCHAR(255) NOT NULL,

    product_id BIGINT NOT NULL,

    unit_price NUMERIC(12,2) NOT NULL,

    quantity INTEGER NOT NULL,

    subtotal NUMERIC(12,2) NOT NULL,

    created_at TIMESTAMP NOT NULL,

    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_order_item_order
        FOREIGN KEY (order_id)
        REFERENCES orders(id)
        ON DELETE CASCADE
);