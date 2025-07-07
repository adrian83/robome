DROP KEYSPACE IF EXISTS robome;

CREATE KEYSPACE robome WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };
  
CREATE TABLE robome.users (
  id uuid, 
  email varchar,
  password_hash varchar,
  roles varchar,
  created_at timestamp,
  modified_at timestamp,
  PRIMARY KEY (id)
);

CREATE INDEX uemail ON robome.users (email);
  
CREATE TABLE robome.tables (
  user_id uuid,
  table_id uuid,
  title varchar,
  description varchar,
  state varchar,
  created_at timestamp,
  modified_at timestamp,
  PRIMARY KEY (user_id, table_id)
);

CREATE TABLE robome.stages (
  user_id uuid,
  table_id uuid,
  stage_id uuid,
  title varchar,
  state varchar,
  created_at timestamp,
  modified_at timestamp,
  PRIMARY KEY (user_id, table_id, stage_id)
);

CREATE TABLE robome.activities (
  user_id uuid,
  table_id uuid,
  stage_id uuid,
  activity_id uuid,
  name varchar,
  state varchar,
  created_at timestamp,
  modified_at timestamp,
  PRIMARY KEY (user_id, table_id, stage_id, activity_id)
);

CREATE TABLE robome.refresh_tokens (
  token_id uuid,
  user_id uuid,
  refresh_token varchar,
  token_family uuid,
  issued_at timestamp,
  expires_at timestamp,
  PRIMARY KEY (token_id)
);

CREATE INDEX rt_user_id ON robome.refresh_tokens (user_id);
CREATE INDEX rt_token ON robome.refresh_tokens (refresh_token);
