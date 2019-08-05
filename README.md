# ROBOME
Simple CRUD app written in java.

### Running

#### Backend
1. Make sure docker is running `sudo systemctl start docker`
2. Go to robome-be directory `cd robome-be`
3. StartCassadra docker image `./run.sh run-cass`
4. Copy name of the Cassandra container `docker ps`
5. Create tables in Cassandra database `./run.sh run-cass-init <cassandra-container-name>`
6. Build application `mvn clean install`
7. Run applicaton `java -jar ./target/robome-1.0-SNAPSHOT.jar`
8. Open browser and type `http://localhost:6060/health`

#### Frontend
1. Go to robome-fe directory `cd robome-fe`
2. Run application `npm start`
3. Open browser and type `http://localhost:3000/`

### Misc

#### Backend formatted with [Google Java Format](https://github.com/google/google-java-format)

#### Backend checked with [PMD](https://pmd.github.io/)
1. `cd robome-be && mvn pmd:check`
2. Open file `robome-be/target/site/pmd.html`
