
-- Test site owner
INSERT INTO "user" (id, username, email, status)
VALUES ('c1e99394-08e6-4a65-9a66-b33f64b3ba74', 'Test Site Owner', 'test.owner@nostalogic.net', 'ACTIVE') ON CONFLICT DO NOTHING;

INSERT INTO authentication (id, user_id, "hash", salt, iterations, encoder)
VALUES ('a71e1f36-2c07-4fd6-b3f5-b8663dc390f2', 'c1e99394-08e6-4a65-9a66-b33f64b3ba74',
        'ca45e23f28a4ef12f0b7d316a0c415844add08bf4dd016b601d2d1505e13e0834c4cfadf165ecf91e4f9db01e9368ccac29d8591babb28216c5da0c8f786577b',
        '7f152d81e8651627999724b3fd217738', 1319, 'PBKDF2') ON CONFLICT DO NOTHING;

INSERT INTO membership (user_id, group_id, status) VALUES
    ('c1e99394-08e6-4a65-9a66-b33f64b3ba74', '309e6617-2e5a-4d77-b51d-18097383233f', 'ACTIVE') ON CONFLICT DO NOTHING;

-- Test regular user
INSERT INTO "user" (id, username, email, status)
VALUES ('a9b0dea5-0515-4f77-b358-45e3fd6fc340', 'Generic User', 'generic.user@nostalogic.net', 'ACTIVE') ON CONFLICT DO NOTHING;

INSERT INTO authentication (id, user_id, "hash", salt, iterations, encoder)
VALUES ('03ea0cfa-5b6c-4983-9de9-bf5cae8e111d', 'a9b0dea5-0515-4f77-b358-45e3fd6fc340',
        '05662ef426e5ef45eca554e0cfaba779aaa46808e4c708fe8903f2bb27cc961b47c6d5563acc850342cabeed09177bab3f52f35897c21cb80f3ca2b11d16a6db',
        '32bf48422543622d2adc6e0f35f8a594', 1124, 'PBKDF2') ON CONFLICT DO NOTHING;


-- Test users
INSERT INTO "user" (id, username, email, status)
VALUES ('f8680c40-2280-4125-812e-25dce05b4d01', 'User 01', 'user1@nostalogic.net', 'ACTIVE') ON CONFLICT DO NOTHING;
INSERT INTO "user" (id, username, email, status)
VALUES ('f8680c40-2280-4125-812e-25dce05b4d02', 'User 02', 'user2@nostalogic.net', 'ACTIVE') ON CONFLICT DO NOTHING;
INSERT INTO "user" (id, username, email, status)
VALUES ('f8680c40-2280-4125-812e-25dce05b4d03', 'User 03', 'user3@nostalogic.net', 'ACTIVE') ON CONFLICT DO NOTHING;
INSERT INTO "user" (id, username, email, status)
VALUES ('f8680c40-2280-4125-812e-25dce05b4d04', 'User 04', 'user4@nostalogic.net', 'ACTIVE') ON CONFLICT DO NOTHING;
INSERT INTO "user" (id, username, email, status)
VALUES ('f8680c40-2280-4125-812e-25dce05b4d05', 'User 05', 'user5@nostalogic.net', 'ACTIVE') ON CONFLICT DO NOTHING;
INSERT INTO "user" (id, username, email, status)
VALUES ('f8680c40-2280-4125-812e-25dce05b4d06', 'User 06', 'user6@nostalogic.net', 'ACTIVE') ON CONFLICT DO NOTHING;
INSERT INTO "user" (id, username, email, status)
VALUES ('f8680c40-2280-4125-812e-25dce05b4d07', 'User 07', 'user7@nostalogic.net', 'ACTIVE') ON CONFLICT DO NOTHING;
INSERT INTO "user" (id, username, email, status)
VALUES ('f8680c40-2280-4125-812e-25dce05b4d08', 'User 08', 'user8@nostalogic.net', 'ACTIVE') ON CONFLICT DO NOTHING;
INSERT INTO "user" (id, username, email, status)
VALUES ('f8680c40-2280-4125-812e-25dce05b4d09', 'User 09', 'user9@nostalogic.net', 'ACTIVE') ON CONFLICT DO NOTHING;
INSERT INTO "user" (id, username, email, status)
VALUES ('f8680c40-2280-4125-812e-25dce05b4d10', 'User 10', 'user10@nostalogic.net', 'ACTIVE') ON CONFLICT DO NOTHING;

-- Test groups
INSERT INTO "group" (id, name)
VALUES ('53941203-f159-4a2a-8cc8-5af32c015301', 'Group 01') ON CONFLICT DO NOTHING;
INSERT INTO "group" (id, name)
VALUES ('53941203-f159-4a2a-8cc8-5af32c015302', 'Group 02') ON CONFLICT DO NOTHING;
INSERT INTO "group" (id, name)
VALUES ('53941203-f159-4a2a-8cc8-5af32c015303', 'Group 03') ON CONFLICT DO NOTHING;
INSERT INTO "group" (id, name)
VALUES ('53941203-f159-4a2a-8cc8-5af32c015304', 'Group 04') ON CONFLICT DO NOTHING;
INSERT INTO "group" (id, name)
VALUES ('53941203-f159-4a2a-8cc8-5af32c015305', 'Group 05') ON CONFLICT DO NOTHING;
INSERT INTO "group" (id, name, type)
VALUES ('53941203-f159-4a2a-8cc8-5af32c015306', 'Group 06', 'RIGHTS') ON CONFLICT DO NOTHING;
INSERT INTO "group" (id, name, type)
VALUES ('53941203-f159-4a2a-8cc8-5af32c015307', 'Group 07', 'RIGHTS') ON CONFLICT DO NOTHING;
INSERT INTO "group" (id, name, type)
VALUES ('53941203-f159-4a2a-8cc8-5af32c015308', 'Group 08', 'RIGHTS') ON CONFLICT DO NOTHING;
INSERT INTO "group" (id, name, type)
VALUES ('53941203-f159-4a2a-8cc8-5af32c015309', 'Group 09', 'RIGHTS') ON CONFLICT DO NOTHING;
INSERT INTO "group" (id, name, type)
VALUES ('53941203-f159-4a2a-8cc8-5af32c015310', 'Group 10', 'RIGHTS') ON CONFLICT DO NOTHING;

-- Test memberships
INSERT INTO membership (user_id, group_id, status, role) VALUES
    ('f8680c40-2280-4125-812e-25dce05b4d01', '53941203-f159-4a2a-8cc8-5af32c015301', 'ACTIVE', 'CHIEF') ON CONFLICT DO NOTHING;
INSERT INTO membership (user_id, group_id, status, role) VALUES
    ('f8680c40-2280-4125-812e-25dce05b4d02', '53941203-f159-4a2a-8cc8-5af32c015302', 'ACTIVE', 'CHIEF') ON CONFLICT DO NOTHING;
INSERT INTO membership (user_id, group_id, status, role) VALUES
    ('f8680c40-2280-4125-812e-25dce05b4d03', '53941203-f159-4a2a-8cc8-5af32c015303', 'ACTIVE', 'MANAGER') ON CONFLICT DO NOTHING;
INSERT INTO membership (user_id, group_id, status, role) VALUES
    ('f8680c40-2280-4125-812e-25dce05b4d04', '53941203-f159-4a2a-8cc8-5af32c015304', 'ACTIVE', 'MANAGER') ON CONFLICT DO NOTHING;
INSERT INTO membership (user_id, group_id, status, role) VALUES
    ('f8680c40-2280-4125-812e-25dce05b4d05', '53941203-f159-4a2a-8cc8-5af32c015305', 'ACTIVE', 'MODERATOR') ON CONFLICT DO NOTHING;
INSERT INTO membership (user_id, group_id, status, role) VALUES
    ('f8680c40-2280-4125-812e-25dce05b4d06', '53941203-f159-4a2a-8cc8-5af32c015306', 'ACTIVE', 'MODERATOR') ON CONFLICT DO NOTHING;
INSERT INTO membership (user_id, group_id, status, role) VALUES
    ('f8680c40-2280-4125-812e-25dce05b4d07', '53941203-f159-4a2a-8cc8-5af32c015307', 'ACTIVE', 'REGULAR') ON CONFLICT DO NOTHING;
INSERT INTO membership (user_id, group_id, status, role) VALUES
    ('f8680c40-2280-4125-812e-25dce05b4d08', '53941203-f159-4a2a-8cc8-5af32c015308', 'INVITED', 'REGULAR') ON CONFLICT DO NOTHING;
INSERT INTO membership (user_id, group_id, status, role) VALUES
    ('f8680c40-2280-4125-812e-25dce05b4d09', '53941203-f159-4a2a-8cc8-5af32c015309', 'APPLIED', 'REGULAR') ON CONFLICT DO NOTHING;
INSERT INTO membership (user_id, group_id, status, role) VALUES
    ('f8680c40-2280-4125-812e-25dce05b4d10', '53941203-f159-4a2a-8cc8-5af32c015310', 'SUSPENDED', 'REGULAR') ON CONFLICT DO NOTHING;
