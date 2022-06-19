FROM maven:3-openjdk-18-slim

ADD . /robome-be
WORKDIR /robome-be

RUN mvn clean install -DskipTests

EXPOSE 6060

CMD ["bash", "start.sh"]