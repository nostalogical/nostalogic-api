CREATE TABLE IF NOT EXISTS server_session (
  tenant VARCHAR(10) NOT NULL DEFAULT 'nostalogic',
  id VARCHAR(36) UNIQUE PRIMARY KEY NOT NULL,
  created TIMESTAMP NOT NULL DEFAULT now(),
  creator_id varchar(36) NOT NULL DEFAULT 'SYSTEM_GENERATED_RECORD_____________',
  user_id VARCHAR(36) NOT NULL,
  additional TEXT NULL,
  start_date_time TIMESTAMP NOT NULL,
  end_date_time TIMESTAMP NOT NULL,
  type int NOT NULL,
  details VARCHAR(256) NULL
);

CREATE TABLE IF NOT EXISTS server_session_event (
  id bigint GENERATED ALWAYS AS IDENTITY,
  session_id VARCHAR(36) NOT NULL REFERENCES server_session(id),
  event VARCHAR(20) NOT NULL,
  created TIMESTAMP NOT NULL
);
