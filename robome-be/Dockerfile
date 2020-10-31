FROM maven:3.6-jdk-14

ADD . /robome-be
WORKDIR /robome-be

RUN mvn clean install -DskipTests

EXPOSE 6060

CMD ["bash", "start.sh"]