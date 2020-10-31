
INSERT INTO "group" (id, name)
VALUES ('309e6617-2e5a-4d77-b51d-18097383233f', 'Site Owner') ON CONFLICT DO NOTHING;

INSERT INTO "group" (id, name)
VALUES ('06cd7155-576e-465d-8722-3eb8373351b7', 'Admin') ON CONFLICT DO NOTHING;

INSERT INTO "group" (id, name)
VALUES ('77c52c87-f46e-4c12-8eb0-b6a701f0ba36', 'Content Admin') ON CONFLICT DO NOTHING;

INSERT INTO "group" (id, name)
VALUES ('817ab8e6-c1ce-435a-86e1-69a4c07bafb3', 'Content Creator') ON CONFLICT DO NOTHING;

INSERT INTO "group" (id, name)
VALUES ('a6a5c68e-13d0-4dfa-a688-b41c4255b3c2', 'User Admin') ON CONFLICT DO NOTHING;

INSERT INTO "group" (id, name)
VALUES ('c9253a33-4936-4c59-ab85-e358d4460bc6', 'Group Admin') ON CONFLICT DO NOTHING;

INSERT INTO "user" (id, name, email, status)
VALUES ('09acf630-1a15-49a0-bddf-cc1c0794c2f9', 'Site Owner', 'admin@nostalogic.net', 'ACTIVE') ON CONFLICT DO NOTHING;

INSERT INTO authentication (id, user_id, "hash", salt, iterations, encoder)
VALUES ('ec4c72a3-33b5-49cf-9e2f-ce84b2ccaafb', '09acf630-1a15-49a0-bddf-cc1c0794c2f9',
        'c73c55b23c3545be0925eeb4698c2acc8edcee2a3652cf47113a141bbe2deb6767e81448f9b02b83f53766c05b8243da3655b5156d138df91b568e042756141c',
        '44aeb2ca614661e7507ba8cf4424f13d', 1379, 'PBKDF2') ON CONFLICT DO NOTHING;

INSERT INTO membership (user_id, group_id, status) VALUES
('09acf630-1a15-49a0-bddf-cc1c0794c2f9', '309e6617-2e5a-4d77-b51d-18097383233f', 'ACTIVE') ON CONFLICT DO NOTHING;
