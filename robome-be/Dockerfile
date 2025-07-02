FROM maven:3.9-eclipse-temurin-24-alpine

ADD . /robome-be
WORKDIR /robome-be


RUN mvn clean install -DskipTests

EXPOSE 6060

CMD ["bash", "start.sh"]