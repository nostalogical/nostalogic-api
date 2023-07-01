
CREATE EXTENSION IF NOT EXISTS "uuid-ossp" SCHEMA public;

CREATE TABLE IF NOT EXISTS "user" (
  tenant VARCHAR(20) NOT NULL DEFAULT 'nostalogic',
  created TIMESTAMP NOT NULL DEFAULT now(),
  creator_id CHAR(36) NOT NULL DEFAULT 'SYSTEM_GENERATED_RECORD_____________',
  id CHAR(36) PRIMARY KEY DEFAULT public.uuid_generate_v4(),
  username VARCHAR(500) NOT NULL UNIQUE,
  tag VARCHAR(30),
  email VARCHAR(500) NOT NULL UNIQUE,
  status VARCHAR(20) NOT NULL DEFAULT 'INACTIVE',
  locale CHAR(5) NOT NULL DEFAULT 'en_GB',
  details JSON DEFAULT '{}',
  UNIQUE(tenant, username),
  UNIQUE(tenant, email)
);

CREATE TABLE IF NOT EXISTS username (
  tenant VARCHAR(20) NOT NULL DEFAULT 'nostalogic',
  created TIMESTAMP NOT NULL DEFAULT now(),
  creator_id CHAR(36) NOT NULL DEFAULT 'SYSTEM_GENERATED_RECORD_____________',
  id CHAR(36) PRIMARY KEY DEFAULT public.uuid_generate_v4(),
  username VARCHAR(500) NOT NULL,
  display_name VARCHAR(500) NOT NULL,
  tag VARCHAR(30) NOT NULL,
  active BOOLEAN NOT NULL DEFAULT FALSE,
  expiration TIMESTAMP
);

CREATE TABLE IF NOT EXISTS email (
  tenant VARCHAR(20) NOT NULL DEFAULT 'nostalogic',
  created TIMESTAMP NOT NULL DEFAULT now(),
  creator_id CHAR(36) NOT NULL DEFAULT 'SYSTEM_GENERATED_RECORD_____________',
  id CHAR(36) PRIMARY KEY DEFAULT public.uuid_generate_v4(),
  username VARCHAR(500) NOT NULL UNIQUE,
  tag VARCHAR(500),
  email VARCHAR(500) NOT NULL UNIQUE,
  status VARCHAR(20) NOT NULL DEFAULT 'INACTIVE',
  locale CHAR(5) NOT NULL DEFAULT 'en_GB'
);

CREATE TABLE IF NOT EXISTS authentication (
  tenant VARCHAR(20) NOT NULL DEFAULT 'nostalogic',
  created TIMESTAMP NOT NULL DEFAULT now(),
  creator_id CHAR(36) NOT NULL DEFAULT 'SYSTEM_GENERATED_RECORD_____________',
  id CHAR(36) PRIMARY KEY DEFAULT public.uuid_generate_v4(),
  user_id CHAR(36) NOT NULL REFERENCES "user"(id),
  hash TEXT NOT NULL,
  salt VARCHAR(50) NOT NULL,
  iterations int,
  encoder VARCHAR(20) NOT NULL,
  expiration TIMESTAMP,
  expired boolean DEFAULT FALSE,
  invalidation TIMESTAMP,
  invalid boolean DEFAULT FALSE,
  expired_reason VARCHAR(300)
);

CREATE TABLE IF NOT EXISTS "group" (
  tenant VARCHAR(20) NOT NULL DEFAULT 'nostalogic',
  created TIMESTAMP NOT NULL DEFAULT now(),
  creator_id CHAR(36) NOT NULL DEFAULT 'SYSTEM_GENERATED_RECORD_____________',
  id CHAR(36) PRIMARY KEY DEFAULT public.uuid_generate_v4(),
  name VARCHAR(100) NOT NULL UNIQUE,
  description VARCHAR(3000),
  type VARCHAR(20) NOT NULL DEFAULT 'SYSTEM',
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  details JSON DEFAULT '{}'
);

CREATE TABLE IF NOT EXISTS details (
  tenant VARCHAR(20) NOT NULL DEFAULT 'nostalogic',
  created TIMESTAMP NOT NULL DEFAULT now(),
  creator_id CHAR(36) NOT NULL DEFAULT 'SYSTEM_GENERATED_RECORD_____________',
  id CHAR(36) PRIMARY KEY,
  entity VARCHAR(20) NOT NULL,
  details TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS membership (
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
