FROM mcr.microsoft.com/playwright:v1.54.0-jammy
RUN apt update && apt install -y openjdk-8-jdk python3-pip vim
RUN pip install playwright requests
WORKDIR /opt/attacker_app
RUN wget https://archive.apache.org/dist/tomcat/tomcat-9/v9.0.108/bin/apache-tomcat-9.0.108.tar.gz
RUN wget https://archive.apache.org/dist/jmeter/binaries/apache-jmeter-5.6.3.tgz
RUN tar -zxvf apache-tomcat-9.0.108.tar.gz
RUN tar -zxvf apache-jmeter-5.6.3.tgz
RUN rm -fr apache-tomcat-9.0.108/webapps/*
RUN mv apache-tomcat-9.0.108 tomcat
RUN mv apache-jmeter-5.6.3 jmeter
RUN rm -f apache-tomcat-9.0.108.tar.gz
RUN rm -f apache-jmeter-5.6.3.tgz
COPY server.xml tomcat/conf
COPY index.jsp tomcat/webapps/attacker_app/
COPY css/ tomcat/webapps/attacker_app/css/
COPY fonts/ tomcat/webapps/attacker_app/fonts/
COPY img/ tomcat/webapps/attacker_app/img/
COPY picture/ tomcat/webapps/attacker_app/picture/
COPY dictionary-attack.py ./
COPY dos-attack.jmx ./
COPY password-list-attack.jmx ./
COPY brute-force-attack.jmx ./
COPY reverse-brute-force-attack.jmx ./
COPY joe-account-attack.jmx ./
COPY credentials.csv ./
COPY passwords.txt ./
COPY usernames.txt ./
RUN chmod +x ./dictionary-attack.py
ENV PATH="/opt/attacker_app/jmeter/bin:${PATH}"
CMD ["./tomcat/bin/catalina.sh", "run"]
