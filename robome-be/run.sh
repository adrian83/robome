

#!/bin/bash

usage() {
	cat <<EOF
    Usage: $(basename $0) <command>
    run-docker            Starts systemd Docker daemon.
    run-cass              Starts Cassandra docker image.
    run-cass-init         Initializes Cassandra database.
    run-infra             Starts Docker and Cassandra
EOF
	exit 1
}

run-docker() {
	set -e
		sudo systemctl start docker
	set +e
}

run-cass() {
	set -e
		docker run -p 9042:9042 -v $PWD/infra/cassandra:/var/lib/cassandra -d cassandra:latest
		echo "Cassandra is listening on port 9042. Data is stored inside 'infra/cassandra' directory"
	set +e
}

run-infra() {
	set -e
		run-docker
		run-cass
	set +e
}

run-cass-init() {
	set -e

		echo $3
	
		#cqlsh 127.0.0.1 9042 -f src/main/resources/cassandra.cql
		docker cp src/main/resources/cassandra.cql $3:/file.cql
		docker exec $3 cqlsh -f /file.cql
   
	set +e
}


CMD="$1"
case "$CMD" in
	run-docker)
		run-docker
	;;
	run-cass)
		run-cass
	;;
	run-infra)
		run-infra
	;;
	run-cass-init)
		echo $0 $1 $2
		run-cass-init $0 $1 $2
	;;
	*)
		usage
	;;
esac