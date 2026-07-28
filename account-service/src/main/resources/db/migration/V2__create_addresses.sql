CREATE TABLE addresses (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    street VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    country VARCHAR(100) NOT NULL DEFAULT 'US',
    zip_code VARCHAR(20) NOT NULL,
    address_type VARCHAR(30) NOT NULL,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    
    CONSTRAINT fk_address_customer 
        FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE
);