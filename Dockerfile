FROM maven:3.8-jdk-8
COPY . /opt/easybuggy4sb/
WORKDIR /opt/easybuggy4sb/
RUN apt-get update && apt-get install curl vim -y
CMD ["mvn", "clean", "spring-boot:run"]
