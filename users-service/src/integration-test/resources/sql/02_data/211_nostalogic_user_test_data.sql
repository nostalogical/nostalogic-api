
-- Test site owner
INSERT INTO "user" (id, name, email, status)
VALUES ('c1e99394-08e6-4a65-9a66-b33f64b3ba74', 'Test Site Owner', 'test.owner@nostalogic.net', 'ACTIVE') ON CONFLICT DO NOTHING;

INSERT INTO authentication (id, user_id, "hash", salt, iterations, encoder)
VALUES ('a71e1f36-2c07-4fd6-b3f5-b8663dc390f2', 'c1e99394-08e6-4a65-9a66-b33f64b3ba74',
        'ca45e23f28a4ef12f0b7d316a0c415844add08bf4dd016b601d2d1505e13e0834c4cfadf165ecf91e4f9db01e9368ccac29d8591babb28216c5da0c8f786577b',
        '7f152d81e8651627999724b3fd217738', 1319, 'PBKDF2') ON CONFLICT DO NOTHING;

INSERT INTO membership (user_id, group_id, status) VALUES
('c1e99394-08e6-4a65-9a66-b33f64b3ba74', '309e6617-2e5a-4d77-b51d-18097383233f', 'ACTIVE') ON CONFLICT DO NOTHING;

-- Test regular user
INSERT INTO "user" (id, name, email, status)
VALUES ('a9b0dea5-0515-4f77-b358-45e3fd6fc340', 'Generic User', 'generic.user@nostalogic.net', 'ACTIVE') ON CONFLICT DO NOTHING;

INSERT INTO authentication (id, user_id, "hash", salt, iterations, encoder)
VALUES ('03ea0cfa-5b6c-4983-9de9-bf5cae8e111d', 'a9b0dea5-0515-4f77-b358-45e3fd6fc340',
        '05662ef426e5ef45eca554e0cfaba779aaa46808e4c708fe8903f2bb27cc961b47c6d5563acc850342cabeed09177bab3f52f35897c21cb80f3ca2b11d16a6db',
        '32bf48422543622d2adc6e0f35f8a594', 1124, 'PBKDF2') ON CONFLICT DO NOTHING;
