CREATE TABLE returns (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    customer_id BIGINT NOT NULL,
    fulfillment_id BIGINT NOT NULL,
    return_status VARCHAR(30) NOT NULL DEFAULT 'INITIATED',
    received_at TIMESTAMP,
    completed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    
    CONSTRAINT fk_return_fulfillment 
        FOREIGN KEY(fulfillment_id) REFERENCES fulfillments(id)
);
