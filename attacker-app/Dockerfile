# Stage 1: Base stage providing JMeter
# ---------------------------------------------------
FROM justb4/jmeter:5.5 AS jmeter-base

# ---------------------------------------------------
# Stage 2: Main runtime environment (Playwright Python + Java)
# ---------------------------------------------------
FROM mcr.microsoft.com/playwright/python:v1.49.0-jammy

ARG TOMCAT_VERSION=9.0.108
ARG KEYSTORE_PASSWORD=password

# --- Install OpenJDK and curl ---
RUN apt-get update && apt-get install -y --no-install-recommends \
    default-jdk \
    curl \
    vim \
    && rm -rf /var/lib/apt/lists/*

# --- Copy only JMeter from Stage 1 ---
COPY --from=jmeter-base /opt/apache-jmeter-5.5 /opt/apache-jmeter-5.5

ENV JMETER_HOME=/opt/apache-jmeter-5.5
ENV PATH="${JMETER_HOME}/bin:${PATH}"

WORKDIR /opt/attacker-app

# --- Install Python dependencies ---
RUN pip install --no-cache-dir requests "playwright==1.49.0"

# --- Set up Tomcat ---
RUN curl -fsSL https://archive.apache.org/dist/tomcat/tomcat-9/v${TOMCAT_VERSION}/bin/apache-tomcat-${TOMCAT_VERSION}.tar.gz -o apache-tomcat-${TOMCAT_VERSION}.tar.gz \
    && tar -zxvf apache-tomcat-${TOMCAT_VERSION}.tar.gz \
    && rm -fr apache-tomcat-${TOMCAT_VERSION}/webapps/* \
    && mv apache-tomcat-${TOMCAT_VERSION} tomcat \
    && rm -f apache-tomcat-${TOMCAT_VERSION}.tar.gz

COPY server.xml tomcat/conf/
COPY attacker-app/ tomcat/webapps/attacker-app/
COPY credentials/. .
COPY jmx/. .
COPY py/. .
COPY download-css.sh ./
RUN chmod +x ./*.sh ./*.py

# Generate a self-signed certificate (using keytool from the installed Java)
RUN keytool -genkey -alias tomcat -keyalg RSA -keystore ./tomcat/conf/tomcat.keystore \
    -dname "CN=attacker.com, OU=Dev, O=Test, C=JP" \
    -keypass ${KEYSTORE_PASSWORD} -storepass ${KEYSTORE_PASSWORD} -noprompt

# Replace the password placeholder inside server.xml
RUN sed -i \
    "s|\${KEYSTORE_PASSWORD}|${KEYSTORE_PASSWORD}|g" \
    ./tomcat/conf/server.xml

ENTRYPOINT ["./tomcat/bin/catalina.sh", "run"]
