# ROBOME
Simple CRUD application written with: Java (Akka Streams, Akka Http) and JavaScript (React). Apache Cassandra is used as main data storage.

### Running

#### Prerequisites
1. Running with Docker Compose
- Docker Compose
2. Running locally
- Docker
- Java 11
- Npm

#### Running with Docker Compose
1. Run `docker-compose up`
2. Navigate in browser to `localhost:3000`

#### Running locally 
1. Start Infrastructure (Apache Cassandra): `make deps`
2. Start backend: `make be-all`
3. Start frontend `make fe-all`
4. Navigate in browser to `localhost:3000`


### Misc

#### Backend formatted with [Google Java Format](https://github.com/google/google-java-format)

#### Backend checked with [PMD](https://pmd.github.io/)
1. `cd robome-be && mvn pmd:check`
2. Open file `robome-be/target/site/pmd.html`
