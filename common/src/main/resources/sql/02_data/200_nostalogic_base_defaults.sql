INSERT INTO service_config (id, name, setting) VALUES (1, 'schema.created', now()) ON CONFLICT DO NOTHING
