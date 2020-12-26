
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
  text VARCHAR(50) NOT NULL,
  icon VARCHAR(50) NOT NULL,
  ordinal SMALLINT NOT NULL DEFAULT 0,
  type varchar(10),
  status VARCHAR(20) NOT NULL DEFAULT 'INACTIVE'
);

CREATE TABLE IF NOT EXISTS container (
  tenant VARCHAR(20) NOT NULL DEFAULT 'nostalogic',
  created TIMESTAMP NOT NULL DEFAULT now(),
  creator_id CHAR(36) NOT NULL DEFAULT 'SYSTEM_GENERATED_RECORD_____________',
  id CHAR(36) PRIMARY KEY DEFAULT public.uuid_generate_v4(),

  navigation_id CHAR(36) REFERENCES navigation(id),
  type VARCHAR(30) NOT NULL,
  resource_id CHAR(36),
  locale CHAR(5),
  UNIQUE(navigation_id, locale)
);

CREATE TABLE IF NOT EXISTS article (
  tenant VARCHAR(20) NOT NULL DEFAULT 'nostalogic',
  created TIMESTAMP NOT NULL DEFAULT now(),
  creator_id CHAR(36) NOT NULL DEFAULT 'SYSTEM_GENERATED_RECORD_____________',
  id CHAR(36) PRIMARY KEY DEFAULT public.uuid_generate_v4(),

  name VARCHAR(100) NOT NULL UNIQUE,
  contents VARCHAR(10000),
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
);

CREATE TABLE IF NOT EXISTS article_revision (
  tenant VARCHAR(20) NOT NULL DEFAULT 'nostalogic',
  created TIMESTAMP NOT NULL DEFAULT now(),
  creator_id CHAR(36) NOT NULL DEFAULT 'SYSTEM_GENERATED_RECORD_____________',
  id CHAR(36) PRIMARY KEY DEFAULT public.uuid_generate_v4(),

  article_id CHAR(36) NOT NULL REFERENCES article(id),
  name VARCHAR(100) NOT NULL UNIQUE,
  contents VARCHAR(10000),
  committed boolean NOT NULL DEFAULT FALSE,
  discarded boolean NOT NULL DEFAULT FALSE
);
