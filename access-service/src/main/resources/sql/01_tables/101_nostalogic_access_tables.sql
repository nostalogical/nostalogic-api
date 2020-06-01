
CREATE TABLE IF NOT EXISTS policy (
    policy_id VARCHAR(36) NOT NULL UNIQUE PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    priority SMALLINT NOT NULL,
    created_date TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS policy_action (
    id VARCHAR(40) PRIMARY KEY,
    policy_id VARCHAR(36) REFERENCES policy(policy_id),
	action_id SMALLINT NOT NULL,
	allow boolean,
    created_date TIMESTAMP NOT NULL DEFAULT NOW(),
	UNIQUE(policy_id, action_id)
);

CREATE TABLE IF NOT EXISTS policy_application (
    id VARCHAR(85) PRIMARY KEY,
    policy_id VARCHAR(36) REFERENCES policy(policy_id),
    resource_id VARCHAR(50),
    created_date TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(policy_id, resource_id)
);

CREATE TABLE IF NOT EXISTS policy_usage (
    id VARCHAR(85) PRIMARY KEY,
    policy_id VARCHAR(36) REFERENCES policy(policy_id),
    subject_id VARCHAR(50),
    created_date TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(policy_id, subject_id)
);

CREATE TABLE IF NOT EXISTS terms_of_use (
	terms_id VARCHAR(36) NOT NULL UNIQUE PRIMARY KEY,
	scope VARCHAR(50),
	version VARCHAR(50),
	change_log TEXT,
	body TEXT,
    created_date TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS terms_acceptance (
    terms_id VARCHAR(36) REFERENCES terms_of_use(terms_id),
    user_id VARCHAR(50),
    accepted_date TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(terms_id, user_id)
);
