CREATE TABLE quotes (
    id BIGSERIAL PRIMARY KEY,
    postcode VARCHAR(10) NOT NULL,
    fuel VARCHAR(20),
    ownership VARCHAR(20),
    property_type VARCHAR(50),
    service_type VARCHAR(50),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_quotes_postcode ON quotes(postcode);
