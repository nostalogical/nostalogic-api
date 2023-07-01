CREATE TABLE IF NOT EXISTS server_session (
  tenant VARCHAR(20) NOT NULL DEFAULT 'nostalogic',
  created TIMESTAMP NOT NULL DEFAULT now(),
  creator_id CHAR(36) NOT NULL DEFAULT 'SYSTEM_GENERATED_RECORD_____________',
  id CHAR(36) PRIMARY KEY,
  user_id CHAR(36) NOT NULL,
  refresh_key CHAR(36) NULL,
  start_date_time TIMESTAMP NOT NULL,
  end_date_time TIMESTAMP NOT NULL,
  type VARCHAR(30) NOT NULL,
  ip VARCHAR(250) NULl,
  notes VARCHAR(250) NULL,
  details JSON DEFAULT '{}'
);

CREATE TABLE IF NOT EXISTS server_session_event (
  tenant VARCHAR(20) NOT NULL DEFAULT 'nostalogic',
  created TIMESTAMP NOT NULL DEFAULT now(),
  id bigint GENERATED ALWAYS AS IDENTITY,
  session_id CHAR(36) NOT NULL REFERENCES server_session(id),
  event CHAR(20) NOT NULL,
  ip VARCHAR(250) NULl,
  details JSON DEFAULT '{}'
);
