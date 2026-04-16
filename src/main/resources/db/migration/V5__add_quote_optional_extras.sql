ALTER TABLE quotes
    ADD COLUMN selected_optional_extras_json JSONB NOT NULL DEFAULT '[]'::jsonb,
    ADD COLUMN optional_extras_price_gbp INTEGER NOT NULL DEFAULT 0;
