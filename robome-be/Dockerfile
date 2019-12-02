FROM maven:3.6.2-jdk-13

ADD . /robome-be
WORKDIR /robome-be

RUN mvn clean install -DskipTests

EXPOSE 6060

CMD ["bash", "start.sh"]