#FROM vulhub/java:8u0-jdk
#COPY . /opt/easybuggy4sb/
#WORKDIR /opt/easybuggy4sb/
#RUN sed -i -e 's/deb.debian.org/archive.debian.org/g' \
#           -e 's/security.debian.org/archive.debian.org/g' \
#           -e '/stretch-updates/d' \
#           -e '/stretch\/updates/d' \
#           -e '/stretch-backports/d' /etc/apt/sources.list && \
#    sed -i '/stretch\/updates/d' /etc/apt/sources.list
#RUN apt-get update && \
#    apt-get install -y --allow-unauthenticated debian-archive-keyring && \
#    apt-get update && update-ca-certificates
#ENV MAVEN_VERSION 3.3.9
#ENV MAVEN_HOME /usr/share/maven
#RUN curl --insecure -fsSL https://archive.apache.org/dist/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.tar.gz | tar -xzf - -C /usr/share && \
#    ln -s /usr/share/apache-maven-$MAVEN_VERSION $MAVEN_HOME
#ENV PATH $MAVEN_HOME/bin:$PATH
#CMD ["mvn", "clean", "spring-boot:run", "-Dmaven.wagon.http.ssl.insecure=true", "-Dmaven.wagon.http.ssl.allowall=true"]

FROM maven:3.8-jdk-8
COPY src /opt/easybuggy4sb/src
COPY catalina.policy /opt/easybuggy4sb/catalina.policy
COPY init.sql /opt/easybuggy4sb/init.sql
COPY pom.xml /opt/easybuggy4sb/pom.xml
WORKDIR /opt/easybuggy4sb/
RUN apt-get update && apt-get install curl vim -y
CMD ["mvn", "clean", "spring-boot:run"]
