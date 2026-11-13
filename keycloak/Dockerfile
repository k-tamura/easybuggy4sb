FROM quay.io/keycloak/keycloak:16.1.1

COPY setup-keycloak.sh health-check.sh entrypoint.sh /opt/jboss/
COPY account.ftl /opt/jboss/keycloak/themes/base/account/
COPY register.ftl /opt/jboss/keycloak/themes/base/login/

USER root
RUN chown jboss:jboss /opt/jboss/setup-keycloak.sh /opt/jboss/health-check.sh /opt/jboss/entrypoint.sh && \
    chmod +x /opt/jboss/setup-keycloak.sh /opt/jboss/health-check.sh /opt/jboss/entrypoint.sh && \
    microdnf install -y jq && microdnf clean all
USER jboss
WORKDIR /opt/jboss/keycloak
ENTRYPOINT ["/opt/jboss/entrypoint.sh"]
