CREATE TABLE IF NOT EXISTS server_session (
  session_id VARCHAR(36) UNIQUE PRIMARY KEY NOT NULL,
  user_id VARCHAR(36) NOT NULL,
  additional TEXT NULL,
  start_date_time TIMESTAMP NOT NULL,
  end_date_time TIMESTAMP NOT NULL,
  type int NOT NULL,
  details VARCHAR(256) NULL
);

CREATE TABLE IF NOT EXISTS server_session_event (
  id bigint GENERATED ALWAYS AS IDENTITY,
  session_id VARCHAR(36) NOT NULL REFERENCES server_session(session_id),
  event VARCHAR(20) NOT NULL,
  created TIMESTAMP NOT NULL
);
