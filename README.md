#ROBOME-BE
Backend of simple web app. 




### Run
1. Download cassandra (tested on version 3.9) and unpact it (lets call this directory <cassandra>)
2. Start docker container with Cassandra
3. Initialize Cassandra keyspace by running <casandra>/bin/cqlsh 127.0.0.1 9043 -f <workspace>/robome-be-akka/src/main/resources/cassandra.cql

