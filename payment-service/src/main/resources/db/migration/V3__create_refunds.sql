CREATE TABLE refunds (
    id BIGSERIAL PRIMARY KEY,
    payment_id BIGINT NOT NULL,
    amount NUMERIC(12,2) NOT NULL,
    refund_status VARCHAR(30) NOT NULL,
    transaction_id VARCHAR(100),
    processed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    
    CONSTRAINT fk_refund_payment 
        FOREIGN KEY(payment_id) REFERENCES payments(id)
);