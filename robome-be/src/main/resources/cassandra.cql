DROP KEYSPACE IF EXISTS robome;

CREATE KEYSPACE robome WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };
  
 CREATE TABLE robome.users (
  id uuid, 
  email varchar,
  password_hash varchar,
  created_at timestamp,
  modified_at timestamp,
  PRIMARY KEY (id)
);

CREATE INDEX uemail ON robome.users (email);
  
 CREATE TABLE robome.tables (
  table_id uuid,
  user_id uuid,
  title varchar,
  description varchar,
  state varchar,
  created_at timestamp,
  modified_at timestamp,
  PRIMARY KEY (user_id, table_id)
);

 CREATE TABLE robome.stages (
  table_id uuid,
  stage_id uuid,
  user_id uuid,
  title varchar,
  state varchar,
  created_at timestamp,
  modified_at timestamp,
  PRIMARY KEY (table_id, stage_id)
);

 CREATE TABLE robome.activities (
  table_id uuid,
  stage_id uuid,
  activity_id uuid,
  name varchar,
  state varchar,
  created_at timestamp,
  modified_at timestamp,
  PRIMARY KEY (table_id, stage_id, activity_id)
);