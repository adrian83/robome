# ROBOME
Simple CRUD application written with: Java (Akka Streams, Akka Http, Alpakka Cassandra) and JavaScript (React). Apache Cassandra is used as main data storage.

## Running

### Running with docker compose

#### Prerequisites
- Docker
- Docker Compose

#### Steps
1. Run `docker-compose up`
2. Navigate in browser to `localhost:3000`

### Running locally

#### Prerequisites
- Docker
- Java 14
- Maven
- Npm

#### Steps
1. Start Infrastructure (Apache Cassandra): `make deps`
2. Start backend: `make be-all`
3. Start frontend `make fe-all`
4. Navigate in browser to `localhost:3000`


### Misc
1. Backend formatted with [Google Java Format](https://github.com/google/google-java-format)
2. Backend checked with [PMD](https://pmd.github.io/)
- Execute `cd robome-be && mvn pmd:check`
- Open file `robome-be/target/site/pmd.html`
