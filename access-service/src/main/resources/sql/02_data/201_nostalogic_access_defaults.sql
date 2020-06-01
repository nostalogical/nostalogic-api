
-- This policy allows read access to all content for all users by default
INSERT INTO policy (policy_id, name, priority, created_date)
VALUES ('6aac60f8-1b4d-430e-911f-a86caa8ec1ba', 'Default Read', 0, now()) ON CONFLICT DO NOTHING;

INSERT INTO policy_action (id, policy_id, action_id, allow, created_date)
VALUES ('6aac60f8-1b4d-430e-911f-a86caa8ec1ba', '6aac60f8-1b4d-430e-911f-a86caa8ec1ba', 0, true, now()) ON CONFLICT DO NOTHING;

INSERT INTO policy_application (id, policy_id, resource_id, created_date)
VALUES ('6aac60f8-1b4d-430e-911f-a86caa8ec1ba', '6aac60f8-1b4d-430e-911f-a86caa8ec1ba', null, now()) ON CONFLICT DO NOTHING;

INSERT INTO policy_usage (id, policy_id, subject_id, created_date)
VALUES ('6aac60f8-1b4d-430e-911f-a86caa8ec1ba', '6aac60f8-1b4d-430e-911f-a86caa8ec1ba', null, now()) ON CONFLICT DO NOTHING;



-- This policy allows edit own and delete own access to all users by default
INSERT INTO policy (policy_id, name, priority, created_date)
VALUES ('b7ce50e3-9460-4004-b6ba-1ee1d1a355e3', 'Default Edit/Delete Own', 0, now()) ON CONFLICT DO NOTHING;

INSERT INTO policy_action (id, policy_id, action_id, allow, created_date)
VALUES ('b7ce50e3-9460-4004-b6ba-1ee1d1a355e3::4', 'b7ce50e3-9460-4004-b6ba-1ee1d1a355e3', 4, true, now()) ON CONFLICT DO NOTHING;
INSERT INTO policy_action (id, policy_id, action_id, allow, created_date)
VALUES ('b7ce50e3-9460-4004-b6ba-1ee1d1a355e3::5', 'b7ce50e3-9460-4004-b6ba-1ee1d1a355e3', 5, true, now()) ON CONFLICT DO NOTHING;

INSERT INTO policy_application (id, policy_id, resource_id, created_date)
VALUES ('b7ce50e3-9460-4004-b6ba-1ee1d1a355e3', 'b7ce50e3-9460-4004-b6ba-1ee1d1a355e3', null, now()) ON CONFLICT DO NOTHING;

INSERT INTO policy_usage (id, policy_id, subject_id, created_date)
VALUES ('b7ce50e3-9460-4004-b6ba-1ee1d1a355e3', 'b7ce50e3-9460-4004-b6ba-1ee1d1a355e3', null, now()) ON CONFLICT DO NOTHING;



-- This policy allows all actions for site owners at highest priority
INSERT INTO policy (policy_id, name, priority, created_date)
VALUES ('8cec724c-63db-4154-b261-de0ca48abfb8', 'Default Site Owner', 4, now()) ON CONFLICT DO NOTHING;

INSERT INTO policy_action (id, policy_id, action_id, allow, created_date)
VALUES ('8cec724c-63db-4154-b261-de0ca48abfb8::0', '8cec724c-63db-4154-b261-de0ca48abfb8', 0, true, now()) ON CONFLICT DO NOTHING;
INSERT INTO policy_action (id, policy_id, action_id, allow, created_date)
VALUES ('8cec724c-63db-4154-b261-de0ca48abfb8::1', '8cec724c-63db-4154-b261-de0ca48abfb8', 1, true, now()) ON CONFLICT DO NOTHING;
INSERT INTO policy_action (id, policy_id, action_id, allow, created_date)
VALUES ('8cec724c-63db-4154-b261-de0ca48abfb8::2', '8cec724c-63db-4154-b261-de0ca48abfb8', 2, true, now()) ON CONFLICT DO NOTHING;
INSERT INTO policy_action (id, policy_id, action_id, allow, created_date)
VALUES ('8cec724c-63db-4154-b261-de0ca48abfb8::3', '8cec724c-63db-4154-b261-de0ca48abfb8', 3, true, now()) ON CONFLICT DO NOTHING;
INSERT INTO policy_action (id, policy_id, action_id, allow, created_date)
VALUES ('8cec724c-63db-4154-b261-de0ca48abfb8::4', '8cec724c-63db-4154-b261-de0ca48abfb8', 4, true, now()) ON CONFLICT DO NOTHING;
INSERT INTO policy_action (id, policy_id, action_id, allow, created_date)
VALUES ('8cec724c-63db-4154-b261-de0ca48abfb8::5', '8cec724c-63db-4154-b261-de0ca48abfb8', 5, true, now()) ON CONFLICT DO NOTHING;

INSERT INTO policy_application (id, policy_id, resource_id, created_date)
VALUES ('8cec724c-63db-4154-b261-de0ca48abfb8', '8cec724c-63db-4154-b261-de0ca48abfb8', null, now()) ON CONFLICT DO NOTHING;

INSERT INTO policy_usage (id, policy_id, subject_id, created_date)
VALUES ('8cec724c-63db-4154-b261-de0ca48abfb8::group_309e6617-2e5a-4d77-b51d-18097383233f', '8cec724c-63db-4154-b261-de0ca48abfb8',
        'group_309e6617-2e5a-4d77-b51d-18097383233f', now()) ON CONFLICT DO NOTHING;



-- This policy allows all actions for admins at admin priority
INSERT INTO policy (policy_id, name, priority, created_date)
VALUES ('0c8bc815-c64d-4959-be6b-de83bf5fb070', 'Default Admin', 3, now()) ON CONFLICT DO NOTHING;

INSERT INTO policy_action (id, policy_id, action_id, allow, created_date)
VALUES ('0c8bc815-c64d-4959-be6b-de83bf5fb070::0', '0c8bc815-c64d-4959-be6b-de83bf5fb070', 0, true, now()) ON CONFLICT DO NOTHING;
INSERT INTO policy_action (id, policy_id, action_id, allow, created_date)
VALUES ('0c8bc815-c64d-4959-be6b-de83bf5fb070::1', '0c8bc815-c64d-4959-be6b-de83bf5fb070', 1, true, now()) ON CONFLICT DO NOTHING;
INSERT INTO policy_action (id, policy_id, action_id, allow, created_date)
VALUES ('0c8bc815-c64d-4959-be6b-de83bf5fb070::2', '0c8bc815-c64d-4959-be6b-de83bf5fb070', 2, true, now()) ON CONFLICT DO NOTHING;
INSERT INTO policy_action (id, policy_id, action_id, allow, created_date)
VALUES ('0c8bc815-c64d-4959-be6b-de83bf5fb070::3', '0c8bc815-c64d-4959-be6b-de83bf5fb070', 3, true, now()) ON CONFLICT DO NOTHING;
INSERT INTO policy_action (id, policy_id, action_id, allow, created_date)
VALUES ('0c8bc815-c64d-4959-be6b-de83bf5fb070::4', '0c8bc815-c64d-4959-be6b-de83bf5fb070', 4, true, now()) ON CONFLICT DO NOTHING;
INSERT INTO policy_action (id, policy_id, action_id, allow, created_date)
VALUES ('0c8bc815-c64d-4959-be6b-de83bf5fb070::5', '0c8bc815-c64d-4959-be6b-de83bf5fb070', 5, true, now()) ON CONFLICT DO NOTHING;

INSERT INTO policy_application (id, policy_id, resource_id, created_date)
VALUES ('0c8bc815-c64d-4959-be6b-de83bf5fb070', '0c8bc815-c64d-4959-be6b-de83bf5fb070', null, now()) ON CONFLICT DO NOTHING;

INSERT INTO policy_usage (id, policy_id, subject_id, created_date)
VALUES ('0c8bc815-c64d-4959-be6b-de83bf5fb070::group_06cd7155-576e-465d-8722-3eb8373351b7', '0c8bc815-c64d-4959-be6b-de83bf5fb070',
        'group_06cd7155-576e-465d-8722-3eb8373351b7', now()) ON CONFLICT DO NOTHING;



-- This policy allows creation, deletion, and edit rights for content admins
INSERT INTO policy (policy_id, name, priority, created_date)
VALUES ('c947154a-9079-4963-9d70-9f550f524f31', 'Default Content Admin', 3, now()) ON CONFLICT DO NOTHING;

INSERT INTO policy_action (id, policy_id, action_id, allow, created_date)
VALUES ('c947154a-9079-4963-9d70-9f550f524f31::0', 'c947154a-9079-4963-9d70-9f550f524f31', 0, true, now()) ON CONFLICT DO NOTHING;
INSERT INTO policy_action (id, policy_id, action_id, allow, created_date)
VALUES ('c947154a-9079-4963-9d70-9f550f524f31::1', 'c947154a-9079-4963-9d70-9f550f524f31', 1, true, now()) ON CONFLICT DO NOTHING;
INSERT INTO policy_action (id, policy_id, action_id, allow, created_date)
VALUES ('c947154a-9079-4963-9d70-9f550f524f31::2', 'c947154a-9079-4963-9d70-9f550f524f31', 2, true, now()) ON CONFLICT DO NOTHING;
INSERT INTO policy_action (id, policy_id, action_id, allow, created_date)
VALUES ('c947154a-9079-4963-9d70-9f550f524f31::3', 'c947154a-9079-4963-9d70-9f550f524f31', 3, true, now()) ON CONFLICT DO NOTHING;

INSERT INTO policy_application (id, policy_id, resource_id, created_date)
VALUES ('c947154a-9079-4963-9d70-9f550f524f31::article', 'c947154a-9079-4963-9d70-9f550f524f31', 'article', now()) ON CONFLICT DO NOTHING;
INSERT INTO policy_application (id, policy_id, resource_id, created_date)
VALUES ('c947154a-9079-4963-9d70-9f550f524f31::nav', 'c947154a-9079-4963-9d70-9f550f524f31', 'nav', now()) ON CONFLICT DO NOTHING;

INSERT INTO policy_usage (id, policy_id, subject_id, created_date)
VALUES ('c947154a-9079-4963-9d70-9f550f524f31::group_77c52c87-f46e-4c12-8eb0-b6a701f0ba36', 'c947154a-9079-4963-9d70-9f550f524f31',
        'group_77c52c87-f46e-4c12-8eb0-b6a701f0ba36', now()) ON CONFLICT DO NOTHING;



-- This policy allows creation rights for content creators
INSERT INTO policy (policy_id, name, priority, created_date)
VALUES ('a962bb77-48b7-41ec-882b-35ad27afb025', 'Default Content Creator', 0, now()) ON CONFLICT DO NOTHING;

INSERT INTO policy_action (id, policy_id, action_id, allow, created_date)
VALUES ('a962bb77-48b7-41ec-882b-35ad27afb025::0', 'a962bb77-48b7-41ec-882b-35ad27afb025', 0, true, now()) ON CONFLICT DO NOTHING;
INSERT INTO policy_action (id, policy_id, action_id, allow, created_date)
VALUES ('a962bb77-48b7-41ec-882b-35ad27afb025::1', 'a962bb77-48b7-41ec-882b-35ad27afb025', 1, true, now()) ON CONFLICT DO NOTHING;

INSERT INTO policy_application (id, policy_id, resource_id, created_date)
VALUES ('a962bb77-48b7-41ec-882b-35ad27afb025::article', 'a962bb77-48b7-41ec-882b-35ad27afb025', 'article', now()) ON CONFLICT DO NOTHING;
INSERT INTO policy_application (id, policy_id, resource_id, created_date)
VALUES ('a962bb77-48b7-41ec-882b-35ad27afb025::nav', 'a962bb77-48b7-41ec-882b-35ad27afb025', 'nav', now()) ON CONFLICT DO NOTHING;

INSERT INTO policy_usage (id, policy_id, subject_id, created_date)
VALUES ('a962bb77-48b7-41ec-882b-35ad27afb025::group_817ab8e6-c1ce-435a-86e1-69a4c07bafb3', 'a962bb77-48b7-41ec-882b-35ad27afb025',
        'group_817ab8e6-c1ce-435a-86e1-69a4c07bafb3', now()) ON CONFLICT DO NOTHING;



-- This policy allows creation rights for article creators
INSERT INTO policy (policy_id, name, priority, created_date)
VALUES ('5b36b9bc-18d0-4d12-8bde-ec4eeb0b78fd', 'Default Article Creator', 0, now()) ON CONFLICT DO NOTHING;

INSERT INTO policy_action (id, policy_id, action_id, allow, created_date)
VALUES ('5b36b9bc-18d0-4d12-8bde-ec4eeb0b78fd::0', '5b36b9bc-18d0-4d12-8bde-ec4eeb0b78fd', 0, true, now()) ON CONFLICT DO NOTHING;
INSERT INTO policy_action (id, policy_id, action_id, allow, created_date)
VALUES ('5b36b9bc-18d0-4d12-8bde-ec4eeb0b78fd::1', '5b36b9bc-18d0-4d12-8bde-ec4eeb0b78fd', 1, true, now()) ON CONFLICT DO NOTHING;

INSERT INTO policy_application (id, policy_id, resource_id, created_date)
VALUES ('5b36b9bc-18d0-4d12-8bde-ec4eeb0b78fd::article', '5b36b9bc-18d0-4d12-8bde-ec4eeb0b78fd', 'article', now()) ON CONFLICT DO NOTHING;

INSERT INTO policy_usage (id, policy_id, subject_id, created_date)
VALUES ('5b36b9bc-18d0-4d12-8bde-ec4eeb0b78fd::group_6b3a5490-548c-41c8-a12c-e85fa854520a', '5b36b9bc-18d0-4d12-8bde-ec4eeb0b78fd',
        'group_6b3a5490-548c-41c8-a12c-e85fa854520a', now()) ON CONFLICT DO NOTHING;



-- This policy allows creation rights for navigation creators
INSERT INTO policy (policy_id, name, priority, created_date)
VALUES ('a4dc6477-d104-4179-9c78-881a58b6fdef', 'Default Navigation Creator', 0, now()) ON CONFLICT DO NOTHING;

INSERT INTO policy_action (id, policy_id, action_id, allow, created_date)
VALUES ('a4dc6477-d104-4179-9c78-881a58b6fdef::0', 'a4dc6477-d104-4179-9c78-881a58b6fdef', 0, true, now()) ON CONFLICT DO NOTHING;
INSERT INTO policy_action (id, policy_id, action_id, allow, created_date)
VALUES ('a4dc6477-d104-4179-9c78-881a58b6fdef::1', 'a4dc6477-d104-4179-9c78-881a58b6fdef', 1, true, now()) ON CONFLICT DO NOTHING;

INSERT INTO policy_application (id, policy_id, resource_id, created_date)
VALUES ('a4dc6477-d104-4179-9c78-881a58b6fdef::nav', 'a4dc6477-d104-4179-9c78-881a58b6fdef', 'nav', now()) ON CONFLICT DO NOTHING;

INSERT INTO policy_usage (id, policy_id, subject_id, created_date)
VALUES ('a4dc6477-d104-4179-9c78-881a58b6fdef::group_44ee8b61-c4bf-46d5-8c94-7b9cfd7332fb', 'a4dc6477-d104-4179-9c78-881a58b6fdef',
        'group_44ee8b61-c4bf-46d5-8c94-7b9cfd7332fb', now()) ON CONFLICT DO NOTHING;



-- This policy allows creation rights for user/group admins
INSERT INTO policy (policy_id, name, priority, created_date)
VALUES ('4e278e27-d1f5-4275-9727-39dd765d919c', 'Default User Admin', 3, now()) ON CONFLICT DO NOTHING;

INSERT INTO policy_action (id, policy_id, action_id, allow, created_date)
VALUES ('4e278e27-d1f5-4275-9727-39dd765d919c::0', '4e278e27-d1f5-4275-9727-39dd765d919c', 0, true, now()) ON CONFLICT DO NOTHING;
INSERT INTO policy_action (id, policy_id, action_id, allow, created_date)
VALUES ('4e278e27-d1f5-4275-9727-39dd765d919c::1', '4e278e27-d1f5-4275-9727-39dd765d919c', 1, true, now()) ON CONFLICT DO NOTHING;
INSERT INTO policy_action (id, policy_id, action_id, allow, created_date)
VALUES ('4e278e27-d1f5-4275-9727-39dd765d919c::3', '4e278e27-d1f5-4275-9727-39dd765d919c', 3, true, now()) ON CONFLICT DO NOTHING;
INSERT INTO policy_action (id, policy_id, action_id, allow, created_date)
VALUES ('4e278e27-d1f5-4275-9727-39dd765d919c::4', '4e278e27-d1f5-4275-9727-39dd765d919c', 4, true, now()) ON CONFLICT DO NOTHING;

INSERT INTO policy_application (id, policy_id, resource_id, created_date)
VALUES ('4e278e27-d1f5-4275-9727-39dd765d919c::user', '4e278e27-d1f5-4275-9727-39dd765d919c', 'user', now()) ON CONFLICT DO NOTHING;
INSERT INTO policy_application (id, policy_id, resource_id, created_date)
VALUES ('4e278e27-d1f5-4275-9727-39dd765d919c::group', '4e278e27-d1f5-4275-9727-39dd765d919c', 'group', now()) ON CONFLICT DO NOTHING;

INSERT INTO policy_usage (id, policy_id, subject_id, created_date)
VALUES ('4e278e27-d1f5-4275-9727-39dd765d919c::group_a6a5c68e-13d0-4dfa-a688-b41c4255b3c2', '4e278e27-d1f5-4275-9727-39dd765d919c',
        'group_a6a5c68e-13d0-4dfa-a688-b41c4255b3c2', now()) ON CONFLICT DO NOTHING;



-- This policy allows creation rights for user creators
INSERT INTO policy (policy_id, name, priority, created_date)
VALUES ('37ec1100-86ec-41da-a1ac-40c3e14e30ef', 'Default User Creator', 0, now()) ON CONFLICT DO NOTHING;

INSERT INTO policy_action (id, policy_id, action_id, allow, created_date)
VALUES ('37ec1100-86ec-41da-a1ac-40c3e14e30ef::0', '37ec1100-86ec-41da-a1ac-40c3e14e30ef', 0, true, now()) ON CONFLICT DO NOTHING;
INSERT INTO policy_action (id, policy_id, action_id, allow, created_date)
VALUES ('37ec1100-86ec-41da-a1ac-40c3e14e30ef::1', '37ec1100-86ec-41da-a1ac-40c3e14e30ef', 1, true, now()) ON CONFLICT DO NOTHING;

INSERT INTO policy_application (id, policy_id, resource_id, created_date)
VALUES ('37ec1100-86ec-41da-a1ac-40c3e14e30ef::user', '37ec1100-86ec-41da-a1ac-40c3e14e30ef', 'user', now()) ON CONFLICT DO NOTHING;

INSERT INTO policy_usage (id, policy_id, subject_id, created_date)
VALUES ('37ec1100-86ec-41da-a1ac-40c3e14e30ef::group_7708b85f-e09b-431b-b2ba-9fea59dfa2b8', '37ec1100-86ec-41da-a1ac-40c3e14e30ef',
        'group_7708b85f-e09b-431b-b2ba-9fea59dfa2b8', now()) ON CONFLICT DO NOTHING;



-- This policy allows creation rights for user group creators
INSERT INTO policy (policy_id, name, priority, created_date)
VALUES ('de63a100-b01f-4fa8-a527-8c3950a7e213', 'Default User Group Creator', 0, now()) ON CONFLICT DO NOTHING;

INSERT INTO policy_action (id, policy_id, action_id, allow, created_date)
VALUES ('de63a100-b01f-4fa8-a527-8c3950a7e213::0', 'de63a100-b01f-4fa8-a527-8c3950a7e213', 0, true, now()) ON CONFLICT DO NOTHING;
INSERT INTO policy_action (id, policy_id, action_id, allow, created_date)
VALUES ('de63a100-b01f-4fa8-a527-8c3950a7e213::1', 'de63a100-b01f-4fa8-a527-8c3950a7e213', 1, true, now()) ON CONFLICT DO NOTHING;

INSERT INTO policy_application (id, policy_id, resource_id, created_date)
VALUES ('de63a100-b01f-4fa8-a527-8c3950a7e213::group', 'de63a100-b01f-4fa8-a527-8c3950a7e213', 'group', now()) ON CONFLICT DO NOTHING;

INSERT INTO policy_usage (id, policy_id, subject_id, created_date)
VALUES ('de63a100-b01f-4fa8-a527-8c3950a7e213::group_c9253a33-4936-4c59-ab85-e358d4460bc6', 'de63a100-b01f-4fa8-a527-8c3950a7e213',
        'group_c9253a33-4936-4c59-ab85-e358d4460bc6', now()) ON CONFLICT DO NOTHING;

