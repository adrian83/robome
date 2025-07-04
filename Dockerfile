FROM maven:3.9-eclipse-temurin-24-alpine

ADD . /app
WORKDIR /app


RUN mvn clean install -DskipTests

EXPOSE 6060

CMD ["bash", "start.sh"]