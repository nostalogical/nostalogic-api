
CREATE TABLE IF NOT EXISTS email (
    tenant VARCHAR(20) NOT NULL DEFAULT 'nostalogic',
    id CHAR(36) PRIMARY KEY,
    recipient_id VARCHAR(50),
    recipient_email_address VARCHAR(100) NOT NULL,
    from_email_address VARCHAR(100) NOT NULL,
    subject VARCHAR(100) NOT NULL,
    body_html TEXT NOT NULL,
    body_plain TEXT NOT NULL,
    type VARCHAR(30) NOT NULL,
    locale CHAR(5) NOT NULL DEFAULT 'en_GB',
    status VARCHAR(20) NOT NULL DEFAULT 'SENT',
    fail_reason TEXT,
    created TIMESTAMP NOT NULL DEFAULT now(),
    creator_id CHAR(36) NOT NULL DEFAULT 'SYSTEM_GENERATED_RECORD_____________'
);

CREATE TABLE IF NOT EXISTS email_template (
    tenant VARCHAR(20) NOT NULL DEFAULT 'nostalogic',
    id CHAR(36) PRIMARY KEY,
    subject VARCHAR(100) NOT NULL,
    body_html TEXT NOT NULL,
    body_plain TEXT NOT NULL,
    type VARCHAR(30) NOT NULL,
    locale CHAR(5) NOT NULL DEFAULT 'en_GB',
    from_email_address VARCHAR(100) NOT NULL,
    created TIMESTAMP NOT NULL DEFAULT now(),
    creator_id CHAR(36) NOT NULL DEFAULT 'SYSTEM_GENERATED_RECORD_____________',
    unique (type, locale)
);
