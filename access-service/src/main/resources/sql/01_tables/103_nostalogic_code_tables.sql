

CREATE TABLE IF NOT EXISTS short_mixed_case_code (
    tenant VARCHAR(20) NOT NULL DEFAULT 'nostalogic',
    created TIMESTAMP NOT NULL DEFAULT now(),
    creator_id CHAR(36) NOT NULL DEFAULT 'SYSTEM_GENERATED_RECORD_____________',
    id CHAR(36) PRIMARY KEY DEFAULT public.uuid_generate_v4(),
    code CHAR(6) NOT NULL,
    value TEXT NOT NULL,
    type VARCHAR(20) NOT NULL,
    expiration TIMESTAMP NOT NULL DEFAULT now() + interval '1 hour',
    UNIQUE(code, type)
);

CREATE TABLE IF NOT EXISTS long_phrase_code (
    tenant VARCHAR(20) NOT NULL DEFAULT 'nostalogic',
    created TIMESTAMP NOT NULL DEFAULT now(),
    creator_id CHAR(36) NOT NULL DEFAULT 'SYSTEM_GENERATED_RECORD_____________',
    id CHAR(36) PRIMARY KEY DEFAULT public.uuid_generate_v4(),
    code VARCHAR(3000) NOT NULL,
    value TEXT NOT NULL,
    type VARCHAR(20) NOT NULL,
    expiration TIMESTAMP,
    UNIQUE(code, type)
);
