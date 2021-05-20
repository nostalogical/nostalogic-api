
-- System pages have no content, they only exist to support links for custom client pages
-- These pages are updated from the preset_pages folder on each launch, so content is managed from that folder's .md files
INSERT INTO navigation(id, urn, full_urn, system, text, icon, status)
VALUES ('0ed05608-701b-456c-ae12-50009e3fe9a9', '', '', true, 'Home', 'home', 'ACTIVE') ON CONFLICT DO NOTHING;

INSERT INTO navigation(id, parent_id, urn, full_urn, text, icon, status)
VALUES ('d7318720-e807-409a-99c5-2ae2e7817289', '0ed05608-701b-456c-ae12-50009e3fe9a9', 'about', 'about', 'About', 'info', 'ACTIVE') ON CONFLICT DO NOTHING;

INSERT INTO navigation_link(child_id, parent_id, ordinal, type, status)
VALUES ('d7318720-e807-409a-99c5-2ae2e7817289', '0ed05608-701b-456c-ae12-50009e3fe9a9', 0, 'SIDE', 'ACTIVE') ON CONFLICT DO NOTHING;

INSERT INTO container(id, navigation_id, "type", resource_id, locale)
VALUES ('90683fdd-1650-4ef5-9f40-eda0bb16e134', 'd7318720-e807-409a-99c5-2ae2e7817289', 'ARTICLE', '54176cc9-c348-4240-ba5e-0d23affc6bd6', 'en_GB') ON CONFLICT DO NOTHING;

INSERT INTO article(id, name, contents, status)
VALUES ('54176cc9-c348-4240-ba5e-0d23affc6bd6', 'About', '', 'ACTIVE') ON CONFLICT DO NOTHING;

-- About child links START
INSERT INTO navigation(id, parent_id, urn, full_urn, text, icon, status)
VALUES ('87927756-5aa5-440c-80dd-394e2992fe65', 'd7318720-e807-409a-99c5-2ae2e7817289', 'roadmap', 'about/roadmap', 'Roadmap', 'alt_route', 'ACTIVE') ON CONFLICT DO NOTHING;

INSERT INTO navigation_link(child_id, parent_id, ordinal, type, status)
VALUES ('87927756-5aa5-440c-80dd-394e2992fe65', 'd7318720-e807-409a-99c5-2ae2e7817289', 0, 'SIDE', 'ACTIVE') ON CONFLICT DO NOTHING;

INSERT INTO container(id, navigation_id, "type", resource_id, locale)
VALUES ('f9951fa6-c426-45af-ab60-9feaa7c6aa94', '87927756-5aa5-440c-80dd-394e2992fe65', 'ARTICLE', 'a7a47cc3-6542-4970-ad81-5bb37a6c2f6a', 'en_GB') ON CONFLICT DO NOTHING;

INSERT INTO article(id, name, contents, status)
VALUES ('a7a47cc3-6542-4970-ad81-5bb37a6c2f6a', 'Roadmap', '', 'ACTIVE') ON CONFLICT DO NOTHING;

INSERT INTO navigation(id, parent_id, urn, full_urn, text, icon, status)
VALUES ('2e1add8c-e6e9-4583-9ac0-45176aae205a', 'd7318720-e807-409a-99c5-2ae2e7817289', 'changelog', 'about/changelog', 'Changelog', 'change_history', 'ACTIVE') ON CONFLICT DO NOTHING;

INSERT INTO navigation_link(child_id, parent_id, ordinal, type, status)
VALUES ('2e1add8c-e6e9-4583-9ac0-45176aae205a', 'd7318720-e807-409a-99c5-2ae2e7817289', 1, 'SIDE', 'ACTIVE') ON CONFLICT DO NOTHING;

INSERT INTO container(id, navigation_id, "type", resource_id, locale)
VALUES ('0e4f2ffc-ae79-44dd-a020-c0c1ca3b0186', '2e1add8c-e6e9-4583-9ac0-45176aae205a', 'ARTICLE', '6b78270f-b3e6-4bca-9bca-cc262589cdc8', 'en_GB') ON CONFLICT DO NOTHING;


INSERT INTO article(id, name, contents, status)
VALUES ('6b78270f-b3e6-4bca-9bca-cc262589cdc8', 'Changelog', '', 'ACTIVE') ON CONFLICT DO NOTHING;
-- About child links END

INSERT INTO navigation(id, parent_id, urn, full_urn, text, icon, status)
VALUES ('2dc48ef3-6963-40b6-ab6b-574bd4a21d08', '0ed05608-701b-456c-ae12-50009e3fe9a9', 'code', 'code', 'Code', 'code', 'ACTIVE') ON CONFLICT DO NOTHING;

INSERT INTO navigation_link(child_id, parent_id, ordinal, type, status)
VALUES ('2dc48ef3-6963-40b6-ab6b-574bd4a21d08', '0ed05608-701b-456c-ae12-50009e3fe9a9', 1, 'SIDE', 'ACTIVE') ON CONFLICT DO NOTHING;

INSERT INTO container(id, navigation_id, "type", resource_id, locale)
VALUES ('f865589c-540a-4614-9441-5bf487fc7856', '2dc48ef3-6963-40b6-ab6b-574bd4a21d08', 'ARTICLE', 'b6329d46-7b3f-4b96-aec8-0b9d74d32b5f', 'en_GB') ON CONFLICT DO NOTHING;

INSERT INTO article(id, name, contents, status)
VALUES ('b6329d46-7b3f-4b96-aec8-0b9d74d32b5f', 'Code', '', 'ACTIVE') ON CONFLICT DO NOTHING;

INSERT INTO navigation(id, parent_id, urn, full_urn, text, icon, status)
VALUES ('f356fc63-166a-4156-9cc6-e2c42194939e', '0ed05608-701b-456c-ae12-50009e3fe9a9', 'gallery', 'gallery', 'Gallery', 'collections', 'ACTIVE') ON CONFLICT DO NOTHING;

INSERT INTO navigation_link(child_id, parent_id, ordinal, type, status)
VALUES ('f356fc63-166a-4156-9cc6-e2c42194939e', '0ed05608-701b-456c-ae12-50009e3fe9a9', 2, 'SIDE', 'ACTIVE') ON CONFLICT DO NOTHING;

INSERT INTO container(id, navigation_id, "type", resource_id, locale)
VALUES ('1b27513f-b604-4b76-9b3b-3248d2e1a8f0', 'f356fc63-166a-4156-9cc6-e2c42194939e', 'ARTICLE', 'b2fbb747-5272-4aec-b6e7-658ca185e35b', 'en_GB') ON CONFLICT DO NOTHING;

INSERT INTO article(id, name, contents, status)
VALUES ('b2fbb747-5272-4aec-b6e7-658ca185e35b', 'Gallery', '', 'ACTIVE') ON CONFLICT DO NOTHING;

INSERT INTO navigation(id, parent_id, urn, full_urn, text, icon, status)
VALUES ('cae76a5f-7951-4163-8708-b3f43ff43d57', '0ed05608-701b-456c-ae12-50009e3fe9a9', 'chat', 'chat', 'Chat', 'chat', 'INACTIVE') ON CONFLICT DO NOTHING;

INSERT INTO navigation_link(child_id, parent_id, ordinal, type, status)
VALUES ('cae76a5f-7951-4163-8708-b3f43ff43d57', '0ed05608-701b-456c-ae12-50009e3fe9a9', 2, 'SIDE', 'ACTIVE') ON CONFLICT DO NOTHING;

INSERT INTO container(id, navigation_id, "type", resource_id, locale)
VALUES ('5d8653d9-7cdf-4605-9396-0c8a045362b6', 'cae76a5f-7951-4163-8708-b3f43ff43d57', 'ARTICLE', 'b1eaebc6-69b3-4f8d-8e19-ffcffe4b0746', 'en_GB') ON CONFLICT DO NOTHING;

INSERT INTO article(id, name, contents, status)
VALUES ('b1eaebc6-69b3-4f8d-8e19-ffcffe4b0746', 'Chat', '', 'ACTIVE') ON CONFLICT DO NOTHING;

