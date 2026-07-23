CREATE TABLE payment_transactions (
    id BIGSERIAL PRIMARY KEY,
    payment_id BIGINT NOT NULL,
    action VARCHAR(30) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    transaction_reference VARCHAR(100),
    psp_reference VARCHAR(100),
    updated_at TIMESTAMP,
    
    CONSTRAINT fk_transaction_payment 
        FOREIGN KEY(payment_id) REFERENCES payments(id)
);