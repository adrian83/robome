DROP  KEYSPACE IF EXISTS robome;

CREATE KEYSPACE robome WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };
  
 CREATE TABLE robome.tables (
  id uuid,
  name varchar,
  state varchar,
  created_at timestamp,
  modified_at timestamp,
  PRIMARY KEY (id, name)
);

 CREATE TABLE robome.stages (
  table_id uuid,
  id uuid,
  name varchar,
  state varchar,
  created_at timestamp,
  modified_at timestamp,
  PRIMARY KEY (table_id, id)
);