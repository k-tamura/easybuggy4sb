FROM justb4/jmeter:5.5

ARG TOMCAT_VERSION=9.0.108
ARG KEYSTORE_PASSWORD=password

WORKDIR /opt/attacker-app
RUN wget https://archive.apache.org/dist/tomcat/tomcat-9/v${TOMCAT_VERSION}/bin/apache-tomcat-${TOMCAT_VERSION}.tar.gz \
    && tar -zxvf apache-tomcat-${TOMCAT_VERSION}.tar.gz \
    && rm -fr apache-tomcat-${TOMCAT_VERSION}/webapps/* \
    && mv apache-tomcat-${TOMCAT_VERSION} tomcat \
    && rm -f apache-tomcat-${TOMCAT_VERSION}.tar.gz
COPY server.xml tomcat/conf
COPY attacker-app/ tomcat/webapps/attacker-app/
COPY credentials/. .
COPY jmx/. .
COPY download-css.sh ./
RUN chmod +x ./download-css.sh

# 1. 自己署名証明書の生成
RUN keytool -genkey -alias tomcat -keyalg RSA -keystore ./tomcat/conf/tomcat.keystore \
    -dname "CN=attacker.com, OU=Dev, O=Test, C=JP" \
    -keypass ${KEYSTORE_PASSWORD} -storepass ${KEYSTORE_PASSWORD} -noprompt

# 2. server.xml をHTTPS有効版に置換 (あるいは sed で書き換え)
# ここでは例として、あらかじめ用意した設定ファイルをコピーするか、sedで編集します
RUN sed -i '/<Service name="Catalina">/a \
    <Connector port="8443" protocol="org.apache.coyote.http11.Http11NioProtocol" \
               maxThreads="150" SSLEnabled="true"> \
        <SSLHostConfig> \
            <Certificate certificateKeystoreFile="conf/tomcat.keystore" \
                         certificateKeystorePassword="${KEYSTORE_PASSWORD}" type="RSA" /> \
        </SSLHostConfig> \
    </Connector>' ./tomcat/conf/server.xml

ENTRYPOINT ["./tomcat/bin/catalina.sh", "run"]
