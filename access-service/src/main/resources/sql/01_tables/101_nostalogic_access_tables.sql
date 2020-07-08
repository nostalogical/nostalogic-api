
CREATE TABLE IF NOT EXISTS policy (
    tenant VARCHAR(10) NOT NULL DEFAULT 'nostalogic',
    id VARCHAR(36) UNIQUE PRIMARY KEY NOT NULL,
    created TIMESTAMP NOT NULL DEFAULT now(),
    creator_id varchar(36) NOT NULL DEFAULT 'SYSTEM_GENERATED_RECORD_____________',
    name VARCHAR(50) NOT NULL UNIQUE,
    status SMALLINT NOT NULL DEFAULT 0,
    priority SMALLINT NOT NULL
);

CREATE TABLE IF NOT EXISTS policy_action (
    id VARCHAR(50) PRIMARY KEY,
    policy_id VARCHAR(36) REFERENCES policy(id),
	action VARCHAR(10) NOT NULL,
	allow boolean,
    created TIMESTAMP NOT NULL DEFAULT NOW(),
	UNIQUE(policy_id, action)
);

CREATE TABLE IF NOT EXISTS policy_resource (
    id VARCHAR(74) NOT NULL PRIMARY KEY,
    policy_id VARCHAR(36) NOT NULL REFERENCES policy(id),
    resource_id VARCHAR(36),
    entity VARCHAR(16),
    created TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(policy_id, resource_id)
);

CREATE TABLE IF NOT EXISTS policy_subject (
    id VARCHAR(74) NOT NULL PRIMARY KEY,
    policy_id VARCHAR(36) NOT NULL REFERENCES policy(id),
    subject_id VARCHAR(36),
    entity VARCHAR(16),
    created TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(policy_id, subject_id)
);

CREATE TABLE IF NOT EXISTS terms_of_use (
	id VARCHAR(36) NOT NULL UNIQUE PRIMARY KEY,
	scope VARCHAR(50),
	version VARCHAR(50),
	change_log TEXT,
	body TEXT,
    created TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS terms_acceptance (
    terms_id VARCHAR(36) REFERENCES terms_of_use(id),
    user_id VARCHAR(50),
    accepted_date TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(terms_id, user_id)
);
