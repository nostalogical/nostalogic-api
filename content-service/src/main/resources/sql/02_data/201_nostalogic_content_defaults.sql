
INSERT INTO navigation (id, urn, full_urn, text, icon, ordinal, status)
VALUES ('0ed05608-701b-456c-ae12-50009e3fe9a9', '', '', 'Home', 'test', 0, 'ACTIVE') ON CONFLICT DO NOTHING;

INSERT INTO container(id, navigation_id, "type", resource_id, locale)
VALUES ('46c827e6-0d01-4f74-842f-d2c373a0ad9a', '0ed05608-701b-456c-ae12-50009e3fe9a9', 'ARTICLE', '0e36d9f1-3060-4def-925c-18bf985db25b', 'en_GB') ON CONFLICT DO NOTHING;

INSERT INTO article(id, name, contents, status)
VALUES ('0e36d9f1-3060-4def-925c-18bf985db25b', 'Main page', '### Welcome!
This is the main page of Nostalogic.', 'ACTIVE') ON CONFLICT DO NOTHING;

INSERT INTO navigation (id, urn, full_urn, text, icon, ordinal, status)
VALUES ('d7318720-e807-409a-99c5-2ae2e7817289', 'test', 'test', 'Test Page 1', 'test', 0, 'ACTIVE') ON CONFLICT DO NOTHING;

INSERT INTO container(id, navigation_id, "type", resource_id, locale)
VALUES ('90683fdd-1650-4ef5-9f40-eda0bb16e134', 'd7318720-e807-409a-99c5-2ae2e7817289', 'ARTICLE', '54176cc9-c348-4240-ba5e-0d23affc6bd6', 'en_GB') ON CONFLICT DO NOTHING;

INSERT INTO article(id, name, contents, status)
VALUES ('54176cc9-c348-4240-ba5e-0d23affc6bd6', 'Test Page', '### Test Page
This is a test content page. There should be three additional test pages linked to this one.

#### Side Pages
Pages **Test Page 2** and **Test Page 3** should be linked in the sidebar.
#### Top pages
Page **Test Page 4** should be linked in the upper navigation bar.', 'ACTIVE') ON CONFLICT DO NOTHING;

INSERT INTO navigation (id, urn, full_urn, text, icon, ordinal, status, parent_id, type)
VALUES ('6726cc14-2c6c-48e2-8b04-f1ecbc229885', 'test', 'test/test', 'Test Page 2', 'test', 0, 'ACTIVE', 'd7318720-e807-409a-99c5-2ae2e7817289', 'SIDE') ON CONFLICT DO NOTHING;

INSERT INTO container(id, navigation_id, "type", resource_id, locale)
VALUES ('0cdc76df-886e-4122-8795-4507719cc875', '6726cc14-2c6c-48e2-8b04-f1ecbc229885', 'ARTICLE', '072b892d-5f3a-4537-bd76-7611e85f3d6b', 'en_GB') ON CONFLICT DO NOTHING;

INSERT INTO article(id, name, contents, status)
VALUES ('072b892d-5f3a-4537-bd76-7611e85f3d6b', 'Test Page 2', '### Test Page 2
This should be linked to **Test Page 1** via the side bar.', 'ACTIVE') ON CONFLICT DO NOTHING;

INSERT INTO navigation (id, urn, full_urn, text, icon, ordinal, status, parent_id, type)
VALUES ('a67748f7-538c-452c-b0dc-ac4ffdfa2130', 'other', 'test/other', 'Test Page 3', 'test', 1, 'ACTIVE', 'd7318720-e807-409a-99c5-2ae2e7817289', 'SIDE') ON CONFLICT DO NOTHING;

INSERT INTO container(id, navigation_id, "type", resource_id, locale)
VALUES ('3ba10052-7d91-4ace-bf28-d5a94f8c009c', 'a67748f7-538c-452c-b0dc-ac4ffdfa2130', 'ARTICLE', '7b5938e1-7db7-4d39-bde3-628c1f071cbe', 'en_GB') ON CONFLICT DO NOTHING;

INSERT INTO article(id, name, contents, status)
VALUES ('7b5938e1-7db7-4d39-bde3-628c1f071cbe', 'Test Page 3', '### Test Page 3
This should be linked to **Test Page 1** via the side bar.', 'ACTIVE') ON CONFLICT DO NOTHING;

INSERT INTO navigation (id, urn, full_urn, text, icon, ordinal, status, parent_id, type)
VALUES ('8206d105-6320-472f-b41b-c084bce076de', 'different', 'test/different', 'Test Page 4', 'test', 0, 'ACTIVE', 'd7318720-e807-409a-99c5-2ae2e7817289', 'TOP') ON CONFLICT DO NOTHING;

INSERT INTO container(id, navigation_id, "type", resource_id, locale)
VALUES ('31b4658f-23c2-4a41-a961-c531790e4074', '8206d105-6320-472f-b41b-c084bce076de', 'ARTICLE', '48483f08-1f0a-4f71-8b00-15fce3782de4', 'en_GB') ON CONFLICT DO NOTHING;

INSERT INTO article(id, name, contents, status)
VALUES ('48483f08-1f0a-4f71-8b00-15fce3782de4', 'Test Page 4', '### Test Page 4
This should be linked to **Test Page 1** via the navigation bar.', 'ACTIVE') ON CONFLICT DO NOTHING;
