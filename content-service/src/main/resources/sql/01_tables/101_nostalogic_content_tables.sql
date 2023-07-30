
CREATE TABLE IF NOT EXISTS navigation (
  tenant VARCHAR(20) NOT NULL DEFAULT 'NOSTALOGIC',
  created TIMESTAMP NOT NULL DEFAULT now(),
  creator_id CHAR(36) NOT NULL DEFAULT 'SYSTEM_GENERATED_RECORD_____________',
  id CHAR(36) PRIMARY KEY DEFAULT uuid_generate_v4(),

  parent_id CHAR(36) REFERENCES navigation(id),
  urn VARCHAR(500) NOT NULL,
  full_urn VARCHAR(500) NOT NULL UNIQUE,
  text VARCHAR(50) NOT NULL,
  icon VARCHAR(50) NOT NULL DEFAULT 'web',
  system boolean DEFAULT false,
  status VARCHAR(20) NOT NULL DEFAULT 'INACTIVE'
);

CREATE TABLE IF NOT EXISTS navigation_link (
  tenant VARCHAR(20) NOT NULL DEFAULT 'NOSTALOGIC',
  created TIMESTAMP NOT NULL DEFAULT now(),
  creator_id CHAR(36) NOT NULL DEFAULT 'SYSTEM_GENERATED_RECORD_____________',
  id CHAR(36) PRIMARY KEY DEFAULT uuid_generate_v4(),

  parent_id CHAR(36) NOT NULL REFERENCES navigation(id),
  child_id CHAR(36) NOT NULL REFERENCES navigation(id),
  ordinal SMALLINT NOT NULL DEFAULT 0,
  type varchar(10) NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'INACTIVE',
  UNIQUE(parent_id, child_id)
);

CREATE TABLE IF NOT EXISTS navigation_link_mask (
    nav_id CHAR(36) NOT NULL UNIQUE REFERENCES navigation(id),
    mask_id CHAR(36) NOT NULL REFERENCES navigation(id)
);

CREATE TABLE IF NOT EXISTS container (
  tenant VARCHAR(20) NOT NULL DEFAULT 'NOSTALOGIC',
  created TIMESTAMP NOT NULL DEFAULT now(),
  creator_id CHAR(36) NOT NULL DEFAULT 'SYSTEM_GENERATED_RECORD_____________',
  id CHAR(36) PRIMARY KEY DEFAULT uuid_generate_v4(),

  navigation_id CHAR(36) REFERENCES navigation(id),
  type VARCHAR(30) NOT NULL,
  resource_id CHAR(36),
  locale CHAR(5),
  UNIQUE(navigation_id, locale)
);

CREATE TABLE IF NOT EXISTS article (
  tenant VARCHAR(20) NOT NULL DEFAULT 'NOSTALOGIC',
  created TIMESTAMP NOT NULL DEFAULT now(),
  creator_id CHAR(36) NOT NULL DEFAULT 'SYSTEM_GENERATED_RECORD_____________',
  id CHAR(36) PRIMARY KEY DEFAULT uuid_generate_v4(),

  name VARCHAR(100) NOT NULL,
  contents VARCHAR(10000),
  last_updated TIMESTAMP NOT NULL DEFAULT now(),
  last_updater_id CHAR(36) NOT NULL DEFAULT 'SYSTEM_GENERATED_RECORD_____________',
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
);

CREATE TABLE IF NOT EXISTS article_revision (
  tenant VARCHAR(20) NOT NULL DEFAULT 'NOSTALOGIC',
  created TIMESTAMP NOT NULL DEFAULT now(),
  creator_id CHAR(36) NOT NULL DEFAULT 'SYSTEM_GENERATED_RECORD_____________',
  id CHAR(36) PRIMARY KEY DEFAULT uuid_generate_v4(),

  article_id CHAR(36) NOT NULL REFERENCES article(id),
  name VARCHAR(100) NOT NULL,
  contents VARCHAR(10000),
  last_updated TIMESTAMP NOT NULL DEFAULT now(),
  status VARCHAR(20) NOT NULL DEFAULT 'INACTIVE'
);
