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

# 1. 自己署名証明書の生成
RUN keytool -genkey -alias tomcat -keyalg RSA -keystore ./tomcat/conf/tomcat.keystore \
    -dname "CN=attacker.com, OU=Dev, O=Test, C=JP" \
    -keypass password -storepass password -noprompt

# 2. server.xml をHTTPS有効版に置換 (あるいは sed で書き換え)
# ここでは例として、あらかじめ用意した設定ファイルをコピーするか、sedで編集します
RUN sed -i '/<Service name="Catalina">/a \
    <Connector port="8443" protocol="org.apache.coyote.http11.Http11NioProtocol" \
               maxThreads="150" SSLEnabled="true"> \
        <SSLHostConfig> \
            <Certificate certificateKeystoreFile="conf/tomcat.keystore" \
                         certificateKeystorePassword="password" type="RSA" /> \
        </SSLHostConfig> \
    </Connector>' ./tomcat/conf/server.xml

ENTRYPOINT ["./tomcat/bin/catalina.sh", "run"]
