FROM maven:3.8-jdk-8 as builder
COPY . /usr/src/easybuggy4sb/
WORKDIR /usr/src/easybuggy4sb/
RUN mvn -B package -Dmaven.test.skip=true

FROM openjdk:8-slim
RUN apt-get update && apt-get install curl -y
WORKDIR /opt/easybuggy4sb
COPY --from=builder /usr/src/easybuggy4sb/target/ROOT.war .
CMD ["java", "-XX:MaxMetaspaceSize=128m", "-Xloggc:logs/gc_%p_%t.log", "-XX:NativeMemoryTracking=summary", "-Xmx256m", "-XX:MaxDirectMemorySize=90m", "-XX:+UseSerialGC", "-XX:+PrintHeapAtGC", "-XX:+PrintGCDetails", "-XX:+PrintGCDateStamps", "-XX:+UseGCLogFileRotation", "-XX:NumberOfGCLogFiles=5", "-XX:GCLogFileSize=10M", "-XX:GCTimeLimit=15", "-XX:GCHeapFreeLimit=50", "-XX:+HeapDumpOnOutOfMemoryError", "-XX:HeapDumpPath=logs/", "-XX:ErrorFile=logs/hs_err_pid%p.log", "-agentlib:jdwp=transport=dt_socket,server=y,address=9009,suspend=n", "-Dderby.stream.error.file=logs/derby.log", "-Dderby.infolog.append=true", "-Dderby.language.logStatementText=true", "-Dderby.locks.deadlockTrace=true", "-Dderby.locks.monitor=true", "-Dderby.storage.rowLocking=true", "-Dcom.sun.management.jmxremote", "-Dcom.sun.management.jmxremote.port=7900", "-Dcom.sun.management.jmxremote.ssl=false", "-Dcom.sun.management.jmxremote.authenticate=false", "-ea", "-jar", "ROOT.war"]
