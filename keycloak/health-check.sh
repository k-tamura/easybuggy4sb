#!/bin/bash

# --- Variables ---
# Keycloak server URL
KEYCLOAK_URL="http://keycloak:8080/auth"
# Target realm name
REALM_NAME="master"

# Target URL
HEALTH_CHECK_URL="${KEYCLOAK_URL}/realms/${REALM_NAME}/.well-known/openid-configuration"

# Get the HTTP status code
STATUS_CODE=$(curl -o /dev/null -s -w "%{http_code}\n" "$HEALTH_CHECK_URL")

# Check if the status code equals 200
if [ "$STATUS_CODE" -eq 200 ]; then
  exit 0
else
  exit 1
fi
