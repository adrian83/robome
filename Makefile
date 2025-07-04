

docker:
	sudo systemctl start docker

compose:
	sudo docker-compose up --build

deps:
	echo "starting cassandra image (version 3.11.9)"
	docker rm robome_cassandra ||:
	docker run -d -p 9042:9042 --name=robome_cassandra cassandra:3.11.9
	docker cp src/main/resources/cassandra.cql robome_cassandra:/schema.cql
	sleep 15
	docker exec robome_cassandra cqlsh -f /schema.cql



test: 
	echo "testing backend" 
	mvn clean test 

build: 
	echo "building backend" 
	mvn clean install -DskipTests
	
run: 
	echo "running backend"
	java -jar target/robome-1.0.0-allinone.jar 

check:
	echo "checking backend with pmd"
	mvn pmd:check

be-all: test build run
