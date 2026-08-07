CREATE TABLE return_items (
    id BIGSERIAL PRIMARY KEY,
    return_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    sku VARCHAR(50),
    product_name VARCHAR(255) NOT NULL,
    quantity INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    
    CONSTRAINT fk_return_item_return 
        FOREIGN KEY(return_id) REFERENCES returns(id) ON DELETE CASCADE
);
