CREATE TABLE orders (

    id BIGSERIAL PRIMARY KEY,

    order_number VARCHAR(50) NOT NULL UNIQUE,

    customer_id BIGINT NOT NULL,

    order_status VARCHAR(30) NOT NULL,

    total_amount NUMERIC(12,2) NOT NULL,

    created_at TIMESTAMP NOT NULL,

    updated_at TIMESTAMP NOT NULL
);