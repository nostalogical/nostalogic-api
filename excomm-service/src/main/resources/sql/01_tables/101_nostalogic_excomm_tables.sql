
CREATE TABLE IF NOT EXISTS email (
    tenant VARCHAR(10) NOT NULL DEFAULT 'nostalogic',
    id VARCHAR(36) PRIMARY KEY,
    recipient_id VARCHAR(50),
    recipient_email_address VARCHAR(100) NOT NULL,
    from_email_address VARCHAR(100) NOT NULL,
    subject VARCHAR(64) NOT NULL,
    body_html TEXT NOT NULL,
    body_plain TEXT NOT NULL,
    type SMALLINT NOT NULL,
    locale CHAR(5) NOT NULL DEFAULT 'en_GB',
    status SMALLINT NOT NULL DEFAULT 0,
    fail_reason text,
    created TIMESTAMP NOT NULL DEFAULT now(),
    creator_id varchar(36) NOT NULL DEFAULT 'SYSTEM_GENERATED_RECORD_____________'
);

CREATE TABLE IF NOT EXISTS email_template (
    tenant VARCHAR(10) NOT NULL DEFAULT 'nostalogic',
    id VARCHAR(36) PRIMARY KEY,
    subject VARCHAR(64) NOT NULL,
    body_html TEXT NOT NULL,
    body_plain TEXT NOT NULL,
    type SMALLINT NOT NULL,
    locale CHAR(5) NOT NULL DEFAULT 'en_GB',
    from_email_address VARCHAR(100) NOT NULL,
    created TIMESTAMP NOT NULL DEFAULT now(),
    creator_id varchar(36) NOT NULL DEFAULT 'SYSTEM_GENERATED_RECORD_____________',
    unique (type, locale)
);
