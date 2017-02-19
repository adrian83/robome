CREATE KEYSPACE robome
  WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };
  
 CREATE TABLE robome.tables (
  id uuid,
  name varchar,
  PRIMARY KEY (id)
);