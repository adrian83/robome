

docker:
	sudo systemctl start docker

compose-build:
	sudo docker-compose build

compose-up:
	sudo docker-compose up

deps:
	echo "starting cassandra image (version 3.11.9)"
	docker rm robome_cassandra ||:
	docker run -d -p 9042:9042 --name=robome_cassandra cassandra:3.11.9
	sleep 5
	docker cp robome-be/src/main/resources/cassandra.cql robome_cassandra:/schema.cql
	sleep 15
	docker exec robome_cassandra cqlsh -f /schema.cql

fe-get:
	echo "getting frontend dependencies" 
	cd robome-fe && npm install 

fe-run: 
	echo "running frontend"
	cd robome-fe && npm run start

fe-all: fe-get fe-run


be-test: 
	echo "testing backend" 
	cd robome-be && mvn clean test 

be-build: 
	echo "building backend" 
	cd robome-be && mvn clean install -DskipTests
	
be-run: 
	echo "running backend"
	java -jar robome-be/target/robome-1.0.0-allinone.jar 

be-check:
	echo "checking backend with pmd"
	cd robome-be && mvn pmd:check

be-all: be-test be-build be-run
