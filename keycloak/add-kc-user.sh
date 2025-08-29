#!/bin/bash

# --- Variables ---
# Keycloak server URL
KEYCLOAK_URL="http://localhost:8080/auth"
# Target realm name
REALM_NAME="master"
# Admin user credentials
ADMIN_USERNAME="admin"
ADMIN_PASSWORD="password"

# --- Obtain access token ---
TOKEN_ENDPOINT="${KEYCLOAK_URL}/realms/${REALM_NAME}/protocol/openid-connect/token"

ADMIN_TOKEN=$(curl -s -X POST "${TOKEN_ENDPOINT}" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=admin-cli" \
  -d "username=${ADMIN_USERNAME}" \
  -d "password=${ADMIN_PASSWORD}" | jq -r .access_token)

if [ -z "$ADMIN_TOKEN" ]; then
  echo "Error: Failed to get admin access token."
  exit 1
fi

echo "Access token obtained."

# --- User registration function ---
create_user() {
  local USERNAME=$1
  local PASSWORD=$2

  USER_DATA=$(cat <<EOF
{
  "username": "${USERNAME}",
  "enabled": true,
  "email": "${USERNAME}@example.com",
  "firstName": "Test",
  "lastName": "User",
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

  echo "Creating user: ${USERNAME}..."

  curl -s -X POST "${KEYCLOAK_URL}/admin/realms/${REALM_NAME}/users" \
    -H "Authorization: Bearer ${ADMIN_TOKEN}" \
    -H "Content-Type: application/json" \
    -d "${USER_DATA}"

  echo "User ${USERNAME} created."
}

# --- Create users ---
create_user "test" "123"
create_user "user" "user"
create_user "123" "P@ssw0rd"
create_user "manager" "admin"

echo "Script finished."
