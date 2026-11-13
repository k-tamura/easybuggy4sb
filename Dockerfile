FROM maven:3.8-eclipse-temurin-8
RUN apt-get update && apt-get install -y --no-install-recommends vim && rm -rf /var/lib/apt/lists/*
WORKDIR /opt/easybuggy4sb/
COPY pom.xml /opt/easybuggy4sb/
RUN mvn dependency:go-offline -B
COPY src /opt/easybuggy4sb/src
COPY catalina.policy init.sql /opt/easybuggy4sb/
CMD ["mvn", "clean", "spring-boot:run"]
