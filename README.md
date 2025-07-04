# ROBOME
Simple CRUD application written with: Java (Akka Streams, Akka Http, Alpakka Cassandra). Apache Cassandra is used as main data storage.

## Running

### Running with docker compose

#### Prerequisites
- Docker
- Docker Compose

#### Steps
1. Run `docker-compose up --build`
2. Navigate in browser to `localhost:6060`

### Running locally

#### Prerequisites
- Docker
- Java 24
- Maven

#### Steps
1. Start Infrastructure (Apache Cassandra): `make deps`
2. Start app: `make all`
4. Navigate in browser to `localhost:3000`


### Misc
1. App checked with [PMD](https://pmd.github.io/)
- Execute `mvn pmd:check`
- Open file `target/site/pmd.html`


In case of `Error: ENOSPC: System limit for number of file watchers reached, watch...`
For Linux:
- edit (as su) `/etc/sysctl.conf`
- add line `fs.inotify.max_user_watches=524288`
- save