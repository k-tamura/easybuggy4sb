FROM mcr.microsoft.com/playwright:v1.54.0-jammy
RUN apt update && apt install -y openjdk-8-jdk python3-pip
RUN pip install playwright requests
WORKDIR /opt/attacker_app
RUN wget https://dlcdn.apache.org/tomcat/tomcat-9/v9.0.108/bin/apache-tomcat-9.0.108.tar.gz
RUN tar -zxvf apache-tomcat-9.0.108.tar.gz
RUN mv apache-tomcat-9.0.108 tomcat
RUN rm -f apache-tomcat-9.0.108.tar.gz
COPY server.xml tomcat/conf
COPY index.jsp tomcat/webapps/attacker_app/
COPY css/ tomcat/webapps/attacker_app/css/
COPY fonts/ tomcat/webapps/attacker_app/fonts/
COPY img/ tomcat/webapps/attacker_app/img/
COPY picture/ tomcat/webapps/attacker_app/picture/
COPY brute-force-attack.py ./
RUN chmod +x ./brute-force-attack.py
COPY password_list.txt ./
CMD ["./tomcat/bin/catalina.sh", "run"]
