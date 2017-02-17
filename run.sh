docker run --name some-cassandra -v /my/own/datadir:/var/lib/cassandra -d cassandra:tag


#!/bin/bash

usage() {
	cat <<EOF
    Usage: $(basename $0) <command>
    run-docker            Starts systemd Docker daemon.
    run-cass              Starts Cassandra docker image.
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
		docker run -p 9043:9042 -v $PWD/infra/cassandra:/var/lib/cassandra -d cassandra:latest
		echo "Cassandra is listening on port 9043. Data is stored inside 'infra/cassandra' directory"
	set +e
}

run-infra() {
	set -e
		run-docker
		run-cass
	set +e
}


CMD="$1"
shift
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
	*)
		usage
	;;
esac