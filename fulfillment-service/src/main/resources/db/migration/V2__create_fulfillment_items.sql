CREATE TABLE fulfillment_items (
    id BIGSERIAL PRIMARY KEY,
    fulfillment_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    quantity INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    
    CONSTRAINT fk_fulfillment_item_fulfillment 
        FOREIGN KEY(fulfillment_id) REFERENCES fulfillments(id) ON DELETE CASCADE
);
