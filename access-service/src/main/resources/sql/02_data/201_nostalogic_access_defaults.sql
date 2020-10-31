
-- This policy allows read access to all content for all users by default
INSERT INTO policy (id, name, priority)
VALUES ('6aac60f8-1b4d-430e-911f-a86caa8ec1ba', 'Default Read', 'ONE_AUTO') ON CONFLICT DO NOTHING;

INSERT INTO policy_action (id, policy_id, action, allow)
VALUES ('6aac60f8-1b4d-430e-911f-a86caa8ec1ba', '6aac60f8-1b4d-430e-911f-a86caa8ec1ba', 'READ', true) ON CONFLICT DO NOTHING;

INSERT INTO policy_resource (id, policy_id, resource_id, entity)
VALUES ('6aac60f8-1b4d-430e-911f-a86caa8ec1ba::NAV', '6aac60f8-1b4d-430e-911f-a86caa8ec1ba', null, 'NAV') ON CONFLICT DO NOTHING;

INSERT INTO policy_subject (id, policy_id, subject_id, entity)
VALUES ('6aac60f8-1b4d-430e-911f-a86caa8ec1ba::ALL', '6aac60f8-1b4d-430e-911f-a86caa8ec1ba', null, 'ALL') ON CONFLICT DO NOTHING;



-- This policy allows edit own and delete own access to all users by default
INSERT INTO policy (id, name, priority)
VALUES ('b7ce50e3-9460-4004-b6ba-1ee1d1a355e3', 'Default Edit/Delete Own', 'ONE_AUTO') ON CONFLICT DO NOTHING;

INSERT INTO policy_action (id, policy_id, action, allow)
VALUES ('b7ce50e3-9460-4004-b6ba-1ee1d1a355e3::EDIT_OWN', 'b7ce50e3-9460-4004-b6ba-1ee1d1a355e3', 'EDIT_OWN', true) ON CONFLICT DO NOTHING;
INSERT INTO policy_action (id, policy_id, action, allow)
VALUES ('b7ce50e3-9460-4004-b6ba-1ee1d1a355e3::DELETE_OWN', 'b7ce50e3-9460-4004-b6ba-1ee1d1a355e3', 'DELETE_OWN', true) ON CONFLICT DO NOTHING;

INSERT INTO policy_resource (id, policy_id, resource_id, entity)
VALUES ('b7ce50e3-9460-4004-b6ba-1ee1d1a355e3::ALL', 'b7ce50e3-9460-4004-b6ba-1ee1d1a355e3', null, 'ALL') ON CONFLICT DO NOTHING;

INSERT INTO policy_subject (id, policy_id, subject_id, entity)
VALUES ('b7ce50e3-9460-4004-b6ba-1ee1d1a355e3::ALL', 'b7ce50e3-9460-4004-b6ba-1ee1d1a355e3', null, 'ALL') ON CONFLICT DO NOTHING;



-- This policy allows all actions for site owners at highest priority
INSERT INTO policy (id, name, priority)
VALUES ('8cec724c-63db-4154-b261-de0ca48abfb8', 'Default Site Owner', 'FIVE_SUPER') ON CONFLICT DO NOTHING;

INSERT INTO policy_action (id, policy_id, action, allow)
VALUES ('8cec724c-63db-4154-b261-de0ca48abfb8::READ', '8cec724c-63db-4154-b261-de0ca48abfb8', 'READ', true) ON CONFLICT DO NOTHING;
INSERT INTO policy_action (id, policy_id, action, allow)
VALUES ('8cec724c-63db-4154-b261-de0ca48abfb8::CREATE', '8cec724c-63db-4154-b261-de0ca48abfb8', 'CREATE', true) ON CONFLICT DO NOTHING;
INSERT INTO policy_action (id, policy_id, action, allow)
VALUES ('8cec724c-63db-4154-b261-de0ca48abfb8::EDIT', '8cec724c-63db-4154-b261-de0ca48abfb8', 'EDIT', true) ON CONFLICT DO NOTHING;
INSERT INTO policy_action (id, policy_id, action, allow)
VALUES ('8cec724c-63db-4154-b261-de0ca48abfb8::DELETE', '8cec724c-63db-4154-b261-de0ca48abfb8', 'DELETE', true) ON CONFLICT DO NOTHING;
INSERT INTO policy_action (id, policy_id, action, allow)
VALUES ('8cec724c-63db-4154-b261-de0ca48abfb8::EDIT_OWN', '8cec724c-63db-4154-b261-de0ca48abfb8', 'EDIT_OWN', true) ON CONFLICT DO NOTHING;
INSERT INTO policy_action (id, policy_id, action, allow)
VALUES ('8cec724c-63db-4154-b261-de0ca48abfb8::DELETE_OWN', '8cec724c-63db-4154-b261-de0ca48abfb8', 'DELETE_OWN', true) ON CONFLICT DO NOTHING;

INSERT INTO policy_resource (id, policy_id, resource_id, entity)
VALUES ('8cec724c-63db-4154-b261-de0ca48abfb8::ALL', '8cec724c-63db-4154-b261-de0ca48abfb8', null, 'ALL') ON CONFLICT DO NOTHING;

INSERT INTO policy_subject (id, policy_id, subject_id, entity)
VALUES ('8cec724c-63db-4154-b261-de0ca48abfb8::309e6617-2e5a-4d77-b51d-18097383233f', '8cec724c-63db-4154-b261-de0ca48abfb8',
        '309e6617-2e5a-4d77-b51d-18097383233f', 'GROUP') ON CONFLICT DO NOTHING;



-- This policy allows all actions for admins at admin priority
INSERT INTO policy (id, name, priority)
VALUES ('0c8bc815-c64d-4959-be6b-de83bf5fb070', 'Default Admin', 'FOUR_ADMIN') ON CONFLICT DO NOTHING;

INSERT INTO policy_action (id, policy_id, action, allow)
VALUES ('0c8bc815-c64d-4959-be6b-de83bf5fb070::READ', '0c8bc815-c64d-4959-be6b-de83bf5fb070', 'READ', true) ON CONFLICT DO NOTHING;
INSERT INTO policy_action (id, policy_id, action, allow)
VALUES ('0c8bc815-c64d-4959-be6b-de83bf5fb070::CREATE', '0c8bc815-c64d-4959-be6b-de83bf5fb070', 'CREATE', true) ON CONFLICT DO NOTHING;
INSERT INTO policy_action (id, policy_id, action, allow)
VALUES ('0c8bc815-c64d-4959-be6b-de83bf5fb070::EDIT', '0c8bc815-c64d-4959-be6b-de83bf5fb070', 'EDIT', true) ON CONFLICT DO NOTHING;
INSERT INTO policy_action (id, policy_id, action, allow)
VALUES ('0c8bc815-c64d-4959-be6b-de83bf5fb070::DELETE', '0c8bc815-c64d-4959-be6b-de83bf5fb070', 'DELETE', true) ON CONFLICT DO NOTHING;
INSERT INTO policy_action (id, policy_id, action, allow)
VALUES ('0c8bc815-c64d-4959-be6b-de83bf5fb070::EDIT_OWN', '0c8bc815-c64d-4959-be6b-de83bf5fb070', 'EDIT_OWN', true) ON CONFLICT DO NOTHING;
INSERT INTO policy_action (id, policy_id, action, allow)
VALUES ('0c8bc815-c64d-4959-be6b-de83bf5fb070::DELETE_OWN', '0c8bc815-c64d-4959-be6b-de83bf5fb070', 'DELETE_OWN', true) ON CONFLICT DO NOTHING;

INSERT INTO policy_resource (id, policy_id, resource_id, entity)
VALUES ('0c8bc815-c64d-4959-be6b-de83bf5fb070::ALL', '0c8bc815-c64d-4959-be6b-de83bf5fb070', null, 'ALL') ON CONFLICT DO NOTHING;

INSERT INTO policy_subject (id, policy_id, subject_id, entity)
VALUES ('0c8bc815-c64d-4959-be6b-de83bf5fb070::06cd7155-576e-465d-8722-3eb8373351b7', '0c8bc815-c64d-4959-be6b-de83bf5fb070',
        '06cd7155-576e-465d-8722-3eb8373351b7', 'GROUP') ON CONFLICT DO NOTHING;



-- This policy allows creation, deletion, and edit rights for content admins
INSERT INTO policy (id, name, priority)
VALUES ('c947154a-9079-4963-9d70-9f550f524f31', 'Default Content Admin', 'FOUR_ADMIN') ON CONFLICT DO NOTHING;

INSERT INTO policy_action (id, policy_id, action, allow)
VALUES ('c947154a-9079-4963-9d70-9f550f524f31::READ', 'c947154a-9079-4963-9d70-9f550f524f31', 'READ', true) ON CONFLICT DO NOTHING;
INSERT INTO policy_action (id, policy_id, action, allow)
VALUES ('c947154a-9079-4963-9d70-9f550f524f31::CREATE', 'c947154a-9079-4963-9d70-9f550f524f31', 'CREATE', true) ON CONFLICT DO NOTHING;
INSERT INTO policy_action (id, policy_id, action, allow)
VALUES ('c947154a-9079-4963-9d70-9f550f524f31::EDIT', 'c947154a-9079-4963-9d70-9f550f524f31', 'EDIT', true) ON CONFLICT DO NOTHING;
INSERT INTO policy_action (id, policy_id, action, allow)
VALUES ('c947154a-9079-4963-9d70-9f550f524f31::DELETE', 'c947154a-9079-4963-9d70-9f550f524f31', 'DELETE', true) ON CONFLICT DO NOTHING;

INSERT INTO policy_resource (id, policy_id, resource_id, entity)
VALUES ('c947154a-9079-4963-9d70-9f550f524f31::ARTICLE', 'c947154a-9079-4963-9d70-9f550f524f31', null, 'ARTICLE') ON CONFLICT DO NOTHING;
INSERT INTO policy_resource (id, policy_id, resource_id, entity)
VALUES ('c947154a-9079-4963-9d70-9f550f524f31::NAV', 'c947154a-9079-4963-9d70-9f550f524f31', null, 'NAV') ON CONFLICT DO NOTHING;

INSERT INTO policy_subject (id, policy_id, subject_id, entity)
VALUES ('c947154a-9079-4963-9d70-9f550f524f31::77c52c87-f46e-4c12-8eb0-b6a701f0ba36', 'c947154a-9079-4963-9d70-9f550f524f31',
        '77c52c87-f46e-4c12-8eb0-b6a701f0ba36', 'GROUP') ON CONFLICT DO NOTHING;



-- This policy allows creation rights for content creators
INSERT INTO policy (id, name, priority)
VALUES ('a962bb77-48b7-41ec-882b-35ad27afb025', 'Default Content Creator', 'ONE_AUTO') ON CONFLICT DO NOTHING;

INSERT INTO policy_action (id, policy_id, action, allow)
VALUES ('a962bb77-48b7-41ec-882b-35ad27afb025::READ', 'a962bb77-48b7-41ec-882b-35ad27afb025', 'READ', true) ON CONFLICT DO NOTHING;
INSERT INTO policy_action (id, policy_id, action, allow)
VALUES ('a962bb77-48b7-41ec-882b-35ad27afb025::CREATE', 'a962bb77-48b7-41ec-882b-35ad27afb025', 'CREATE', true) ON CONFLICT DO NOTHING;

INSERT INTO policy_resource (id, policy_id, resource_id, entity)
VALUES ('a962bb77-48b7-41ec-882b-35ad27afb025::ARTICLE', 'a962bb77-48b7-41ec-882b-35ad27afb025', null, 'ARTICLE') ON CONFLICT DO NOTHING;
INSERT INTO policy_resource (id, policy_id, resource_id, entity)
VALUES ('a962bb77-48b7-41ec-882b-35ad27afb025::NAV', 'a962bb77-48b7-41ec-882b-35ad27afb025', null, 'NAV') ON CONFLICT DO NOTHING;

INSERT INTO policy_subject (id, policy_id, subject_id, entity)
VALUES ('a962bb77-48b7-41ec-882b-35ad27afb025::817ab8e6-c1ce-435a-86e1-69a4c07bafb3', 'a962bb77-48b7-41ec-882b-35ad27afb025',
        '817ab8e6-c1ce-435a-86e1-69a4c07bafb3', 'GROUP') ON CONFLICT DO NOTHING;



-- This policy allows creation rights for article creators
INSERT INTO policy (id, name, priority)
VALUES ('5b36b9bc-18d0-4d12-8bde-ec4eeb0b78fd', 'Default Article Creator', 'ONE_AUTO') ON CONFLICT DO NOTHING;

INSERT INTO policy_action (id, policy_id, action, allow)
VALUES ('5b36b9bc-18d0-4d12-8bde-ec4eeb0b78fd::READ', '5b36b9bc-18d0-4d12-8bde-ec4eeb0b78fd', 'READ', true) ON CONFLICT DO NOTHING;
INSERT INTO policy_action (id, policy_id, action, allow)
VALUES ('5b36b9bc-18d0-4d12-8bde-ec4eeb0b78fd::CREATE', '5b36b9bc-18d0-4d12-8bde-ec4eeb0b78fd', 'CREATE', true) ON CONFLICT DO NOTHING;

INSERT INTO policy_resource (id, policy_id, resource_id, entity)
VALUES ('5b36b9bc-18d0-4d12-8bde-ec4eeb0b78fd::ARTICLE', '5b36b9bc-18d0-4d12-8bde-ec4eeb0b78fd', null, 'ARTICLE') ON CONFLICT DO NOTHING;

INSERT INTO policy_subject (id, policy_id, subject_id, entity)
VALUES ('5b36b9bc-18d0-4d12-8bde-ec4eeb0b78fd::6b3a5490-548c-41c8-a12c-e85fa854520a', '5b36b9bc-18d0-4d12-8bde-ec4eeb0b78fd',
        '6b3a5490-548c-41c8-a12c-e85fa854520a', 'GROUP') ON CONFLICT DO NOTHING;



-- This policy allows creation rights for navigation creators
INSERT INTO policy (id, name, priority)
VALUES ('a4dc6477-d104-4179-9c78-881a58b6fdef', 'Default Navigation Creator', 'ONE_AUTO') ON CONFLICT DO NOTHING;

INSERT INTO policy_action (id, policy_id, action, allow)
VALUES ('a4dc6477-d104-4179-9c78-881a58b6fdef::READ', 'a4dc6477-d104-4179-9c78-881a58b6fdef', 'READ', true) ON CONFLICT DO NOTHING;
INSERT INTO policy_action (id, policy_id, action, allow)
VALUES ('a4dc6477-d104-4179-9c78-881a58b6fdef::CREATE', 'a4dc6477-d104-4179-9c78-881a58b6fdef', 'CREATE', true) ON CONFLICT DO NOTHING;

INSERT INTO policy_resource (id, policy_id, resource_id, entity)
VALUES ('a4dc6477-d104-4179-9c78-881a58b6fdef::NAV', 'a4dc6477-d104-4179-9c78-881a58b6fdef', null, 'NAV') ON CONFLICT DO NOTHING;

INSERT INTO policy_subject (id, policy_id, subject_id, entity)
VALUES ('a4dc6477-d104-4179-9c78-881a58b6fdef::44ee8b61-c4bf-46d5-8c94-7b9cfd7332fb', 'a4dc6477-d104-4179-9c78-881a58b6fdef',
        '44ee8b61-c4bf-46d5-8c94-7b9cfd7332fb', 'GROUP') ON CONFLICT DO NOTHING;



-- This policy allows creation rights for user/group admins
INSERT INTO policy (id, name, priority)
VALUES ('4e278e27-d1f5-4275-9727-39dd765d919c', 'Default User Admin', 'FOUR_ADMIN') ON CONFLICT DO NOTHING;

INSERT INTO policy_action (id, policy_id, action, allow)
VALUES ('4e278e27-d1f5-4275-9727-39dd765d919c::READ', '4e278e27-d1f5-4275-9727-39dd765d919c', 'READ', true) ON CONFLICT DO NOTHING;
INSERT INTO policy_action (id, policy_id, action, allow)
VALUES ('4e278e27-d1f5-4275-9727-39dd765d919c::CREATE', '4e278e27-d1f5-4275-9727-39dd765d919c', 'CREATE', true) ON CONFLICT DO NOTHING;
INSERT INTO policy_action (id, policy_id, action, allow)
VALUES ('4e278e27-d1f5-4275-9727-39dd765d919c::DELETE', '4e278e27-d1f5-4275-9727-39dd765d919c', 'DELETE', true) ON CONFLICT DO NOTHING;
INSERT INTO policy_action (id, policy_id, action, allow)
VALUES ('4e278e27-d1f5-4275-9727-39dd765d919c::EDIT_OWN', '4e278e27-d1f5-4275-9727-39dd765d919c', 'EDIT_OWN', true) ON CONFLICT DO NOTHING;

INSERT INTO policy_resource (id, policy_id, resource_id, entity)
VALUES ('4e278e27-d1f5-4275-9727-39dd765d919c::USER', '4e278e27-d1f5-4275-9727-39dd765d919c', null, 'USER') ON CONFLICT DO NOTHING;
INSERT INTO policy_resource (id, policy_id, resource_id, entity)
VALUES ('4e278e27-d1f5-4275-9727-39dd765d919c::GROUP', '4e278e27-d1f5-4275-9727-39dd765d919c', null, 'GROUP') ON CONFLICT DO NOTHING;

INSERT INTO policy_subject (id, policy_id, subject_id, entity)
VALUES ('4e278e27-d1f5-4275-9727-39dd765d919c::a6a5c68e-13d0-4dfa-a688-b41c4255b3c2', '4e278e27-d1f5-4275-9727-39dd765d919c',
        'a6a5c68e-13d0-4dfa-a688-b41c4255b3c2', 'GROUP') ON CONFLICT DO NOTHING;



-- This policy allows creation rights for user creators
INSERT INTO policy (id, name, priority)
VALUES ('37ec1100-86ec-41da-a1ac-40c3e14e30ef', 'Default User Creator', 'ONE_AUTO') ON CONFLICT DO NOTHING;

INSERT INTO policy_action (id, policy_id, action, allow)
VALUES ('37ec1100-86ec-41da-a1ac-40c3e14e30ef::READ', '37ec1100-86ec-41da-a1ac-40c3e14e30ef', 'READ', true) ON CONFLICT DO NOTHING;
INSERT INTO policy_action (id, policy_id, action, allow)
VALUES ('37ec1100-86ec-41da-a1ac-40c3e14e30ef::CREATE', '37ec1100-86ec-41da-a1ac-40c3e14e30ef', 'CREATE', true) ON CONFLICT DO NOTHING;

INSERT INTO policy_resource (id, policy_id, resource_id, entity)
VALUES ('37ec1100-86ec-41da-a1ac-40c3e14e30ef::USER', '37ec1100-86ec-41da-a1ac-40c3e14e30ef', null, 'USER') ON CONFLICT DO NOTHING;

INSERT INTO policy_subject (id, policy_id, subject_id, entity)
VALUES ('37ec1100-86ec-41da-a1ac-40c3e14e30ef::7708b85f-e09b-431b-b2ba-9fea59dfa2b8', '37ec1100-86ec-41da-a1ac-40c3e14e30ef',
        '7708b85f-e09b-431b-b2ba-9fea59dfa2b8', 'GROUP') ON CONFLICT DO NOTHING;



-- This policy allows creation rights for user group creators
INSERT INTO policy (id, name, priority)
VALUES ('de63a100-b01f-4fa8-a527-8c3950a7e213', 'Default User Group Creator', 'ONE_AUTO') ON CONFLICT DO NOTHING;

INSERT INTO policy_action (id, policy_id, action, allow)
VALUES ('de63a100-b01f-4fa8-a527-8c3950a7e213::READ', 'de63a100-b01f-4fa8-a527-8c3950a7e213', 'READ', true) ON CONFLICT DO NOTHING;
INSERT INTO policy_action (id, policy_id, action, allow)
VALUES ('de63a100-b01f-4fa8-a527-8c3950a7e213::CREATE', 'de63a100-b01f-4fa8-a527-8c3950a7e213', 'CREATE', true) ON CONFLICT DO NOTHING;

INSERT INTO policy_resource (id, policy_id, resource_id, entity)
VALUES ('de63a100-b01f-4fa8-a527-8c3950a7e213::GROUP', 'de63a100-b01f-4fa8-a527-8c3950a7e213', null, 'GROUP') ON CONFLICT DO NOTHING;

INSERT INTO policy_subject (id, policy_id, subject_id, entity)
VALUES ('de63a100-b01f-4fa8-a527-8c3950a7e213::c9253a33-4936-4c59-ab85-e358d4460bc6', 'de63a100-b01f-4fa8-a527-8c3950a7e213',
        'c9253a33-4936-4c59-ab85-e358d4460bc6', 'GROUP') ON CONFLICT DO NOTHING;

