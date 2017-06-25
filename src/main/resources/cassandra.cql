DROP  KEYSPACE IF EXISTS robome;

CREATE KEYSPACE robome WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };
  
 CREATE TABLE robome.users (
  email varchar,
  password_hash varchar,
  created_at timestamp,
  modified_at timestamp,
  PRIMARY KEY (email)
);
  
 CREATE TABLE robome.tables (
  table_id uuid,
  name varchar,
  state varchar,
  created_at timestamp,
  modified_at timestamp,
  PRIMARY KEY (id, name)
);

 CREATE TABLE robome.stages (
  table_id uuid,
  stage_id uuid,
  name varchar,
  state varchar,
  created_at timestamp,
  modified_at timestamp,
  PRIMARY KEY (table_id, id)
);

 CREATE TABLE robome.activities (
  table_id uuid,
  stage_id uuid,
  activity_id uuid,
  name varchar,
  state varchar,
  created_at timestamp,
  modified_at timestamp,
  PRIMARY KEY (table_id, stage_id, id)
);