FROM quay.io/keycloak/keycloak:16.1.1

COPY setup-keycloak.sh /opt/jboss/setup-keycloak.sh
COPY health-check.sh /opt/jboss/health-check.sh
COPY entrypoint.sh /opt/jboss/entrypoint.sh
USER root
RUN chown jboss:jboss /opt/jboss/setup-keycloak.sh /opt/jboss/health-check.sh /opt/jboss/entrypoint.sh && \
    chmod +x /opt/jboss/setup-keycloak.sh /opt/jboss/health-check.sh /opt/jboss/entrypoint.sh && \
    microdnf install jq && microdnf clean all
USER jboss
WORKDIR /opt/jboss/keycloak
ENTRYPOINT ["/opt/jboss/entrypoint.sh"]
