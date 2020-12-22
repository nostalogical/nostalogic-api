
CREATE EXTENSION IF NOT EXISTS "uuid-ossp" SCHEMA public;
CREATE EXTENSION IF NOT EXISTS "citext" SCHEMA public;

CREATE TABLE IF NOT EXISTS navigation (
  tenant VARCHAR(20) NOT NULL DEFAULT 'nostalogic',
  created TIMESTAMP NOT NULL DEFAULT now(),
  creator_id CHAR(36) NOT NULL DEFAULT 'SYSTEM_GENERATED_RECORD_____________',
  id CHAR(36) PRIMARY KEY DEFAULT public.uuid_generate_v4(),
  parent_id CHAR(36) REFERENCES navigation(id),
  urn public.citext NOT NULL,
  full_urn public.citext NOT NULL UNIQUE,
  name VARCHAR(50) NOT NULL,
  icon VARCHAR(50) NOT NULL,
  ordinal SMALLINT NOT NULL DEFAULT 0,
  status VARCHAR(20) NOT NULL DEFAULT 'INACTIVE'
);

CREATE TABLE IF NOT EXISTS container (
  tenant VARCHAR(20) NOT NULL DEFAULT 'nostalogic',
  created TIMESTAMP NOT NULL DEFAULT now(),
  creator_id CHAR(36) NOT NULL DEFAULT 'SYSTEM_GENERATED_RECORD_____________',
  id CHAR(36) PRIMARY KEY DEFAULT public.uuid_generate_v4(),
  navigation_id CHAR(36) REFERENCES navigation(id),
  type VARCHAR(20) NOT NULL DEFAULT 'SYSTEM',
  ordinal SMALLINT NOT NULL DEFAULT 0,
  locale CHAR(5)
);

CREATE TABLE IF NOT EXISTS article (
  tenant VARCHAR(20) NOT NULL DEFAULT 'nostalogic',
  created TIMESTAMP NOT NULL DEFAULT now(),
  creator_id CHAR(36) NOT NULL DEFAULT 'SYSTEM_GENERATED_RECORD_____________',
  id CHAR(36) PRIMARY KEY DEFAULT public.uuid_generate_v4(),
  name VARCHAR(100) NOT NULL UNIQUE,
  description VARCHAR(3000),
  type VARCHAR(20) NOT NULL DEFAULT 'SYSTEM',
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
);

CREATE TABLE IF NOT EXISTS link (
  tenant VARCHAR(20) NOT NULL DEFAULT 'nostalogic',
  created TIMESTAMP NOT NULL DEFAULT now(),
  creator_id CHAR(36) NOT NULL DEFAULT 'SYSTEM_GENERATED_RECORD_____________',
  id CHAR(36) PRIMARY KEY,
  entity VARCHAR(20) NOT NULL,
  details TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS container_rule (
  tenant VARCHAR(20) NOT NULL DEFAULT 'nostalogic',
  created TIMESTAMP NOT NULL DEFAULT now(),
  creator_id CHAR(36) NOT NULL DEFAULT 'SYSTEM_GENERATED_RECORD_____________',
  id CHAR(36) PRIMARY KEY DEFAULT public.uuid_generate_v4(),
  user_id CHAR(36) NOT NULL REFERENCES "user"(id),
  group_id CHAR(36) NOT NULL REFERENCES "group"(id),
  status VARCHAR(20) NOT NULL DEFAULT 'INACTIVE',
  role VARCHAR(20) NOT NULL DEFAULT 'REGULAR',
  UNIQUE(user_id, group_id)
);
