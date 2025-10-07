#!/bin/bash
set -e
/opt/jboss/setup-keycloak.sh &
exec /opt/jboss/tools/docker-entrypoint.sh
