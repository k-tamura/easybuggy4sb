FROM tomcat:8.5.84-jdk8-corretto-al2
COPY server.xml /usr/local/tomcat/conf/
COPY index.html /usr/local/tomcat/webapps/attacker_app/
CMD ["catalina.sh", "run"]

