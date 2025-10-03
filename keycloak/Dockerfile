FROM quay.io/keycloak/keycloak:16.1.1

COPY setup-keycloak.sh /opt/jboss/setup-keycloak.sh
USER root
RUN chown jboss:jboss /opt/jboss/setup-keycloak.sh && chmod +x /opt/jboss/setup-keycloak.sh
RUN microdnf install jq
USER jboss
WORKDIR /opt/jboss/keycloak