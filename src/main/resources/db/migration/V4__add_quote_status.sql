ALTER TABLE quotes
    ADD COLUMN status VARCHAR(30) NOT NULL DEFAULT 'DRAFT';

UPDATE quotes
SET status = CASE
    WHEN contact_requested_at IS NOT NULL
        OR client_email IS NOT NULL
        OR client_phone IS NOT NULL
        OR selected_boiler IS NOT NULL
        THEN 'NEW_LEAD'
    ELSE 'DRAFT'
END;

CREATE INDEX idx_quotes_status ON quotes(status);
