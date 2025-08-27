FROM quay.io/keycloak/keycloak:16.1.1

COPY add-kc-user.sh /opt/jboss/add-kc-user.sh
USER root
RUN chown jboss:jboss /opt/jboss/add-kc-user.sh && chmod +x /opt/jboss/add-kc-user.sh
RUN microdnf install jq
USER jboss
