
echo "Waiting for cassandra"
# it's stupid but i don't want to waist more time for checking 
#    if cassandra is ready for accepting connections

sleep 60


# APPLICATION
java -jar -Dconfig.file=./src/main/resources/compose.conf target/robome-1.0-SNAPSHOT-allinone.jar 