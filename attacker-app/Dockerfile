FROM justb4/jmeter:5.5
WORKDIR /opt/attacker-app
RUN wget https://archive.apache.org/dist/tomcat/tomcat-9/v9.0.108/bin/apache-tomcat-9.0.108.tar.gz \
    && tar -zxvf apache-tomcat-9.0.108.tar.gz \
    && rm -fr apache-tomcat-9.0.108/webapps/* \
    && mv apache-tomcat-9.0.108 tomcat \
    && rm -f apache-tomcat-9.0.108.tar.gz
COPY server.xml tomcat/conf
COPY attacker-app/ tomcat/webapps/attacker-app/
COPY credentials/. .
COPY jmx/. .
COPY download-css.sh ./
RUN chmod +x ./download-css.sh
ENTRYPOINT ["./tomcat/bin/catalina.sh", "run"]
