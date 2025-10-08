#!/bin/bash

if [[ -f /opt/jboss/setup.log ]] && [[ "$(tail -n 1 /opt/jboss/setup.log)" == "Setup script finished." ]]; then
  exit 0
fi

# --- Variables ---
# Keycloak server URL
KEYCLOAK_URL="http://keycloak:8080/auth"
# Target realm name
REALM_NAME="master"
# Admin user credentials
ADMIN_USERNAME="admin"
ADMIN_PASSWORD="password"

# Target URL
HEALTH_CHECK_URL="${KEYCLOAK_URL}/realms/${REALM_NAME}/.well-known/openid-configuration"

# Retry count and interval
MAX_RETRIES=50
INTERVAL=5 # seconds

# Keycloak takes at least 15 seconds to start up
sleep  10

# Retry loop
for ((i=1; i<=MAX_RETRIES; i++)); do
  # Wait on all attempts
  echo "Waiting for $INTERVAL seconds, then retrying... (Attempt $i/$MAX_RETRIES)"  >> /opt/jboss/setup.log
  sleep "$INTERVAL"

  # Get the HTTP status code
  STATUS_CODE=$(curl -o /dev/null -s -w "%{http_code}\n" "$HEALTH_CHECK_URL")

  # Check if the status code equals 200
  if [ "$STATUS_CODE" -eq 200 ]; then
    echo "Keycloak started successfully. HTTP status code: $STATUS_CODE" >> /opt/jboss/setup.log
    break
  elif [ "$i" -eq "$MAX_RETRIES" ]; then
    # Check if this is the last attempt and the check failed
    echo "All retries failed. Exiting with error. Last status code: $STATUS_CODE" >> /opt/jboss/setup.log
    exit 1
  fi

  echo "HTTP status code: $STATUS_CODE. Retrying..." >> /opt/jboss/setup.log
done

/opt/jboss/keycloak/bin/kcadm.sh config credentials --server ${KEYCLOAK_URL} --realm ${REALM_NAME} --user ${ADMIN_USERNAME} --password ${ADMIN_PASSWORD} >> /opt/jboss/setup.log
/opt/jboss/keycloak/bin/kcadm.sh update realms/${REALM_NAME} -s registrationAllowed=true -s accountTheme=keycloak >> /opt/jboss/setup.log
/opt/jboss/keycloak/bin/jboss-cli.sh --connect "/subsystem=undertow/server=default-server/host=default-host/setting=access-log:add(directory=/opt/jboss/keycloak/standalone/log)" >> /opt/jboss/setup.log

# --- Obtain access token ---
TOKEN_ENDPOINT="${KEYCLOAK_URL}/realms/${REALM_NAME}/protocol/openid-connect/token"

ADMIN_TOKEN=$(curl -s -X POST "${TOKEN_ENDPOINT}" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=admin-cli" \
  -d "username=${ADMIN_USERNAME}" \
  -d "password=${ADMIN_PASSWORD}" | jq -r .access_token)

if [ -z "$ADMIN_TOKEN" ]; then
  echo "Error: Failed to get admin access token." >> /opt/jboss/setup.log
  exit 1
fi

echo "Access token obtained." >> /opt/jboss/setup.log

# --- User registration function ---
create_user() {
  local USERNAME=$1
  local PASSWORD=$2
  local FIRST_NAME=$3
  local LAST_NAME=$4
  local PICTURE=$5
  local ROLE_NAME=$6

  # Creating user
  USER_DATA=$(cat <<EOF
{
  "username": "${USERNAME}",
  "enabled": true,
  "email": "${USERNAME}@example.com",
  "firstName": "${FIRST_NAME}",
  "lastName": "${LAST_NAME}",
  "attributes": {
    "picture": [ "$PICTURE" ]
  },
  "credentials": [
    {
      "type": "password",
      "value": "${PASSWORD}",
      "temporary": false
    }
  ]
}
EOF
)

  echo "Creating user: ${USERNAME}..." >> /opt/jboss/setup.log
  curl -s -X POST "${KEYCLOAK_URL}/admin/realms/${REALM_NAME}/users" \
    -H "Authorization: Bearer ${ADMIN_TOKEN}" \
    -H "Content-Type: application/json" \
    -d "${USER_DATA}"

  echo "User ${USERNAME} created." >> /opt/jboss/setup.log

  # Assigning role
  if [ -n "$ROLE_NAME" ]; then
    USER_ID=$(curl -s -X GET "${KEYCLOAK_URL}/admin/realms/${REALM_NAME}/users?username=${USERNAME}" \
      -H "Authorization: Bearer ${ADMIN_TOKEN}" \
      -H "Content-Type: application/json" | jq -r '.[0].id')

    if [ -z "$USER_ID" ]; then
      echo "Error: Failed to get user ID for ${USERNAME}." >> /opt/jboss/setup.log
    fi

    ROLE_ID=$(curl -s -X GET "${KEYCLOAK_URL}/admin/realms/${REALM_NAME}/roles?search=${ROLE_NAME}" \
      -H "Authorization: Bearer ${ADMIN_TOKEN}" \
      -H "Content-Type: application/json" | jq -r '.[0].id')

    if [ -z "$ROLE_ID" ]; then
      echo "Error: Failed to get role ID for ${ROLE_NAME}." >> /opt/jboss/setup.log
    fi
    echo "Role ID obtained: ${ROLE_ID}"

    ROLE_DATA=$(cat <<EOF
[
  {
    "id": "${ROLE_ID}",
    "name": "${ROLE_NAME}"
  }
]
EOF
)
    echo "Assigning '${ROLE_NAME}' role to ${USERNAME}..."
    curl -s -X POST "${KEYCLOAK_URL}/admin/realms/${REALM_NAME}/users/${USER_ID}/role-mappings/realm" \
      -H "Authorization: Bearer ${ADMIN_TOKEN}" \
      -H "Content-Type: application/json" \
      -d "${ROLE_DATA}"

    echo "Role '${ROLE_NAME}' assigned to ${USERNAME}." >> /opt/jboss/setup.log
  else
    echo "No role specified. Skipping role assignment for ${USERNAME}." >> /opt/jboss/setup.log
  fi
}

create_user "test" "123" "" "" "images/avatar_man.png"
create_user "user" "user" "Naomi" "Sato" "images/avatar_woman.png"
create_user "123" "P@ssw0rd" "Ichiro" "Suzuki" ""
create_user "manager" "admin" "" "" ""  "admin"

echo "Setup script finished." >> /opt/jboss/setup.log
