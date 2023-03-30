FROM maven:3.9.0-eclipse-temurin-19-alpine

ADD . /robome-be
WORKDIR /robome-be


RUN mvn clean install -DskipTests

EXPOSE 6060

CMD ["bash", "start.sh"]