-- Test user UUID: 5f086280-32d2-4955-9874-0a9d8ee3ca88
-- Test user group UUID: d8d6a9c4-b9ce-4660-b037-2d6d330da846

-- Equal priority, opposite effect access to an article
INSERT INTO policy (id, name, priority)
VALUES ('21f16bbb-a9ca-4583-a6d2-bf6a5b811f5d', 'Equal priority opposite effect 1', 'TWO_STANDARD') ON CONFLICT DO NOTHING;
INSERT INTO policy_action (id, policy_id, action, allow)
VALUES ('21f16bbb-a9ca-4583-a6d2-bf6a5b811f5d', '21f16bbb-a9ca-4583-a6d2-bf6a5b811f5d', 'READ', true) ON CONFLICT DO NOTHING;
INSERT INTO policy_resource (id, policy_id, resource_id, entity)
VALUES ('21f16bbb-a9ca-4583-a6d2-bf6a5b811f5d::b332223b-32bf-42b7-bb07-24174516a410', '21f16bbb-a9ca-4583-a6d2-bf6a5b811f5d', 'b332223b-32bf-42b7-bb07-24174516a410', 'ARTICLE') ON CONFLICT DO NOTHING;
INSERT INTO policy_subject (id, policy_id, subject_id, entity)
VALUES ('21f16bbb-a9ca-4583-a6d2-bf6a5b811f5d::5f086280-32d2-4955-9874-0a9d8ee3ca88', '21f16bbb-a9ca-4583-a6d2-bf6a5b811f5d', '5f086280-32d2-4955-9874-0a9d8ee3ca88', 'USER') ON CONFLICT DO NOTHING;
INSERT INTO policy (id, name, priority)
VALUES ('93b8b29f-ad3f-4697-a716-1c56fe9d93d8', 'Equal priority opposite effect 2', 'TWO_STANDARD') ON CONFLICT DO NOTHING;
INSERT INTO policy_action (id, policy_id, action, allow)
VALUES ('93b8b29f-ad3f-4697-a716-1c56fe9d93d8', '93b8b29f-ad3f-4697-a716-1c56fe9d93d8', 'READ', false) ON CONFLICT DO NOTHING;
INSERT INTO policy_resource (id, policy_id, resource_id, entity)
VALUES ('93b8b29f-ad3f-4697-a716-1c56fe9d93d8::b332223b-32bf-42b7-bb07-24174516a410', '93b8b29f-ad3f-4697-a716-1c56fe9d93d8', 'b332223b-32bf-42b7-bb07-24174516a410', 'ARTICLE') ON CONFLICT DO NOTHING;
INSERT INTO policy_subject (id, policy_id, subject_id, entity)
VALUES ('93b8b29f-ad3f-4697-a716-1c56fe9d93d8::5f086280-32d2-4955-9874-0a9d8ee3ca88', '93b8b29f-ad3f-4697-a716-1c56fe9d93d8', '5f086280-32d2-4955-9874-0a9d8ee3ca88', 'USER') ON CONFLICT DO NOTHING;

-- Higher priority policy on a nav
INSERT INTO policy (id, name, priority)
VALUES ('922119b2-1b5d-42dc-9208-0fd9d17aa5f1', 'High priority test 1', 'TWO_STANDARD') ON CONFLICT DO NOTHING;
INSERT INTO policy_action (id, policy_id, action, allow)
VALUES ('922119b2-1b5d-42dc-9208-0fd9d17aa5f1', '922119b2-1b5d-42dc-9208-0fd9d17aa5f1', 'EDIT', false) ON CONFLICT DO NOTHING;
INSERT INTO policy_resource (id, policy_id, resource_id, entity)
VALUES ('922119b2-1b5d-42dc-9208-0fd9d17aa5f1::04a20bb8-4d01-4b7e-91d9-ce9314d0bfbb', '922119b2-1b5d-42dc-9208-0fd9d17aa5f1', '04a20bb8-4d01-4b7e-91d9-ce9314d0bfbb', 'NAV') ON CONFLICT DO NOTHING;
INSERT INTO policy_subject (id, policy_id, subject_id, entity)
VALUES ('922119b2-1b5d-42dc-9208-0fd9d17aa5f1::5f086280-32d2-4955-9874-0a9d8ee3ca88', '922119b2-1b5d-42dc-9208-0fd9d17aa5f1', '5f086280-32d2-4955-9874-0a9d8ee3ca88', 'USER') ON CONFLICT DO NOTHING;
INSERT INTO policy (id, name, priority)
VALUES ('53d2ed24-bec0-41f1-9609-1532b0849102', 'High priority test 2', 'THREE_HIGH') ON CONFLICT DO NOTHING;
INSERT INTO policy_action (id, policy_id, action, allow)
VALUES ('53d2ed24-bec0-41f1-9609-1532b0849102', '53d2ed24-bec0-41f1-9609-1532b0849102', 'EDIT', true) ON CONFLICT DO NOTHING;
INSERT INTO policy_resource (id, policy_id, resource_id, entity)
VALUES ('53d2ed24-bec0-41f1-9609-1532b0849102::04a20bb8-4d01-4b7e-91d9-ce9314d0bfbb', '53d2ed24-bec0-41f1-9609-1532b0849102', '04a20bb8-4d01-4b7e-91d9-ce9314d0bfbb', 'NAV') ON CONFLICT DO NOTHING;
INSERT INTO policy_subject (id, policy_id, subject_id, entity)
VALUES ('53d2ed24-bec0-41f1-9609-1532b0849102::5f086280-32d2-4955-9874-0a9d8ee3ca88', '53d2ed24-bec0-41f1-9609-1532b0849102', '5f086280-32d2-4955-9874-0a9d8ee3ca88', 'USER') ON CONFLICT DO NOTHING;

-- Equal priorities and effects on a resource stack
INSERT INTO policy (id, name, priority)
VALUES ('f2adeff6-912d-4022-b5b4-267ecef4beee', 'Equal result for resource 1', 'TWO_STANDARD') ON CONFLICT DO NOTHING;
INSERT INTO policy_action (id, policy_id, action, allow)
VALUES ('f2adeff6-912d-4022-b5b4-267ecef4beee', 'f2adeff6-912d-4022-b5b4-267ecef4beee', 'DELETE', true) ON CONFLICT DO NOTHING;
INSERT INTO policy_resource (id, policy_id, resource_id, entity)
VALUES ('f2adeff6-912d-4022-b5b4-267ecef4beee::20590221-63d1-4750-873d-916e24900406', 'f2adeff6-912d-4022-b5b4-267ecef4beee', '20590221-63d1-4750-873d-916e24900406', 'NAV') ON CONFLICT DO NOTHING;
INSERT INTO policy_subject (id, policy_id, subject_id, entity)
VALUES ('f2adeff6-912d-4022-b5b4-267ecef4beee::5f086280-32d2-4955-9874-0a9d8ee3ca88', 'f2adeff6-912d-4022-b5b4-267ecef4beee', '5f086280-32d2-4955-9874-0a9d8ee3ca88', 'USER') ON CONFLICT DO NOTHING;
INSERT INTO policy (id, name, priority)
VALUES ('c013563b-6480-43e4-bc1b-c457d389df95', 'Equal result for resource 2', 'TWO_STANDARD') ON CONFLICT DO NOTHING;
INSERT INTO policy_action (id, policy_id, action, allow)
VALUES ('c013563b-6480-43e4-bc1b-c457d389df95', 'c013563b-6480-43e4-bc1b-c457d389df95', 'DELETE', true) ON CONFLICT DO NOTHING;
INSERT INTO policy_resource (id, policy_id, resource_id, entity)
VALUES ('c013563b-6480-43e4-bc1b-c457d389df95::20590221-63d1-4750-873d-916e24900406', 'c013563b-6480-43e4-bc1b-c457d389df95', '20590221-63d1-4750-873d-916e24900406', 'NAV') ON CONFLICT DO NOTHING;
INSERT INTO policy_subject (id, policy_id, subject_id, entity)
VALUES ('c013563b-6480-43e4-bc1b-c457d389df95::5f086280-32d2-4955-9874-0a9d8ee3ca88', 'c013563b-6480-43e4-bc1b-c457d389df95', '5f086280-32d2-4955-9874-0a9d8ee3ca88', 'USER') ON CONFLICT DO NOTHING;

-- Equal priorities and effects on multiple subjects stack
INSERT INTO policy (id, name, priority)
VALUES ('35929e52-4f15-4f41-a445-e4e68ef3f30e', 'Multiple subjects report 1', 'TWO_STANDARD') ON CONFLICT DO NOTHING;
INSERT INTO policy_action (id, policy_id, action, allow)
VALUES ('35929e52-4f15-4f41-a445-e4e68ef3f30e', '35929e52-4f15-4f41-a445-e4e68ef3f30e', 'DELETE', true) ON CONFLICT DO NOTHING;
INSERT INTO policy_resource (id, policy_id, resource_id, entity)
VALUES ('35929e52-4f15-4f41-a445-e4e68ef3f30e::e6285bef-d8d1-461a-9507-f9b8a7426b3e', '35929e52-4f15-4f41-a445-e4e68ef3f30e', 'e6285bef-d8d1-461a-9507-f9b8a7426b3e', 'EMAIL') ON CONFLICT DO NOTHING;
INSERT INTO policy_subject (id, policy_id, subject_id, entity)
VALUES ('35929e52-4f15-4f41-a445-e4e68ef3f30e::5f086280-32d2-4955-9874-0a9d8ee3ca88', '35929e52-4f15-4f41-a445-e4e68ef3f30e', '5f086280-32d2-4955-9874-0a9d8ee3ca88', 'USER') ON CONFLICT DO NOTHING;
INSERT INTO policy (id, name, priority)
VALUES ('1f88a5b8-55af-47a9-b43d-c22da8d1721a', 'Multiple subjects report 2', 'TWO_STANDARD') ON CONFLICT DO NOTHING;
INSERT INTO policy_action (id, policy_id, action, allow)
VALUES ('1f88a5b8-55af-47a9-b43d-c22da8d1721a', '1f88a5b8-55af-47a9-b43d-c22da8d1721a', 'DELETE', true) ON CONFLICT DO NOTHING;
INSERT INTO policy_resource (id, policy_id, resource_id, entity)
VALUES ('1f88a5b8-55af-47a9-b43d-c22da8d1721a::EMAIL', '1f88a5b8-55af-47a9-b43d-c22da8d1721a', null, 'EMAIL') ON CONFLICT DO NOTHING;
INSERT INTO policy_subject (id, policy_id, subject_id, entity)
VALUES ('1f88a5b8-55af-47a9-b43d-c22da8d1721a::d8d6a9c4-b9ce-4660-b037-2d6d330da846', '1f88a5b8-55af-47a9-b43d-c22da8d1721a', 'd8d6a9c4-b9ce-4660-b037-2d6d330da846', 'GROUP') ON CONFLICT DO NOTHING;

-- All access
INSERT INTO policy (id, name, priority)
VALUES ('c179b678-7442-435c-ae4a-97b6bddb2a3c', 'All access', 'FIVE_SUPER') ON CONFLICT DO NOTHING;
INSERT INTO policy_action (id, policy_id, action, allow)
VALUES ('c179b678-7442-435c-ae4a-97b6bddb2a3c', 'c179b678-7442-435c-ae4a-97b6bddb2a3c', 'DELETE', true) ON CONFLICT DO NOTHING;
INSERT INTO policy_resource (id, policy_id, resource_id, entity)
VALUES ('c179b678-7442-435c-ae4a-97b6bddb2a3c::ALL', 'c179b678-7442-435c-ae4a-97b6bddb2a3c', null, 'ALL') ON CONFLICT DO NOTHING;
INSERT INTO policy_subject (id, policy_id, subject_id, entity)
VALUES ('c179b678-7442-435c-ae4a-97b6bddb2a3c::74790c40-30b2-462b-9125-ee38200b94cd', 'c179b678-7442-435c-ae4a-97b6bddb2a3c', '74790c40-30b2-462b-9125-ee38200b94cd', 'USER') ON CONFLICT DO NOTHING;
