# ROBOME
Simple CRUD application written with: Java (Akka Streams, Akka Http, Alpakka Cassandra) and JavaScript (React). Apache Cassandra is used as main data storage.

## Running

### Running with docker compose

#### Prerequisites
- Docker
- Docker Compose

#### Steps
1. Run `docker-compose up --build`
2. Navigate in browser to `localhost:3000`

### Running locally

#### Prerequisites
- Docker
- Java 18
- Maven
- Npm

#### Steps
1. Start Infrastructure (Apache Cassandra): `make deps`
2. Start backend: `make be-all`
3. Start frontend `make fe-all`
4. Navigate in browser to `localhost:3000`


### Misc
1. Backend checked with [PMD](https://pmd.github.io/)
- Execute `cd robome-be && mvn pmd:check`
- Open file `robome-be/target/site/pmd.html`


In case of `Error: ENOSPC: System limit for number of file watchers reached, watch...`
For Linux:
- edit (as su) `/etc/sysctl.conf`
- add line `fs.inotify.max_user_watches=524288`
- save