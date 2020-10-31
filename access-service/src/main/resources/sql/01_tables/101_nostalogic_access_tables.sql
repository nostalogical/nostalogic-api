
CREATE EXTENSION IF NOT EXISTS "uuid-ossp" SCHEMA public;

CREATE TABLE IF NOT EXISTS policy (
    tenant VARCHAR(10) NOT NULL DEFAULT 'nostalogic',
    id CHAR(36) PRIMARY KEY DEFAULT public.uuid_generate_v4(),
    created TIMESTAMP NOT NULL DEFAULT now(),
    creator_id CHAR(36) NOT NULL DEFAULT 'SYSTEM_GENERATED_RECORD_____________',
    name VARCHAR(50) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    priority VARCHAR(15) NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_policy_status ON policy(status);
CREATE INDEX IF NOT EXISTS idx_policy_tenant ON policy(tenant);

CREATE TABLE IF NOT EXISTS policy_action (
    tenant VARCHAR(10) NOT NULL DEFAULT 'nostalogic',
    id VARCHAR(50) PRIMARY KEY DEFAULT public.uuid_generate_v4(),
    policy_id CHAR(36) REFERENCES policy(id),
	action VARCHAR(20) NOT NULL,
	allow boolean,
    created TIMESTAMP NOT NULL DEFAULT NOW(),
	UNIQUE(policy_id, action)
);

CREATE TABLE IF NOT EXISTS policy_resource (
    tenant VARCHAR(10) NOT NULL DEFAULT 'nostalogic',
    id VARCHAR(74) PRIMARY KEY DEFAULT public.uuid_generate_v4(),
    policy_id CHAR(36) NOT NULL REFERENCES policy(id),
    resource_id CHAR(36),
    entity VARCHAR(20),
    created TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(policy_id, resource_id)
);

CREATE TABLE IF NOT EXISTS policy_subject (
    tenant VARCHAR(10) NOT NULL DEFAULT 'nostalogic',
    id VARCHAR(74) PRIMARY KEY DEFAULT public.uuid_generate_v4(),
    policy_id CHAR(36) NOT NULL REFERENCES policy(id),
    subject_id CHAR(36),
    entity VARCHAR(20),
    created TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(policy_id, subject_id)
);

CREATE TABLE IF NOT EXISTS terms_of_use (
    tenant VARCHAR(10) NOT NULL DEFAULT 'nostalogic',
	id CHAR(36) PRIMARY KEY DEFAULT public.uuid_generate_v4(),
	scope VARCHAR(300),
	version VARCHAR(300),
	change_log TEXT,
	body TEXT,
    created TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS terms_acceptance (
    tenant VARCHAR(10) NOT NULL DEFAULT 'nostalogic',
    terms_id CHAR(36) REFERENCES terms_of_use(id),
    user_id CHAR(36),
    accepted_date TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(terms_id, user_id)
);
