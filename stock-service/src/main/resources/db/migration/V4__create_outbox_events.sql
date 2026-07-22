CREATE TABLE outbox_events
(
    id BIGSERIAL PRIMARY KEY,

    event_id VARCHAR(36) NOT NULL,

    aggregate_type VARCHAR(100) NOT NULL,

    aggregate_id VARCHAR(100) NOT NULL,

    event_type VARCHAR(50) NOT NULL,

    payload TEXT NOT NULL,

    status VARCHAR(20) NOT NULL,

    retry_count INTEGER NOT NULL DEFAULT 0,

    published_at TIMESTAMP,

    error_message VARCHAR(1000),

    created_at TIMESTAMP NOT NULL,

    updated_at TIMESTAMP
);

ALTER TABLE outbox_events
    ADD CONSTRAINT uk_outbox_event_id
        UNIQUE(event_id);