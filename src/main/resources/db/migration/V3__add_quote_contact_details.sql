ALTER TABLE quotes
    ADD COLUMN selected_boiler VARCHAR(255),
    ADD COLUMN client_email VARCHAR(255),
    ADD COLUMN client_phone VARCHAR(50),
    ADD COLUMN contact_requested_at TIMESTAMP WITH TIME ZONE;
