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
  local FIRST_NAME=$3
  local LAST_NAME=$4
  local ROLE_NAME=$5

  # Creating user
  USER_DATA=$(cat <<EOF
{
  "username": "${USERNAME}",
  "enabled": true,
  "email": "${USERNAME}@example.com",
  "firstName": "${FIRST_NAME}",
  "lastName": "${LAST_NAME}",
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

  # Assigning role
  if [ -n "$ROLE_NAME" ]; then
    USER_ID=$(curl -s -X GET "${KEYCLOAK_URL}/admin/realms/${REALM_NAME}/users?username=${USERNAME}" \
      -H "Authorization: Bearer ${ADMIN_TOKEN}" \
      -H "Content-Type: application/json" | jq -r '.[0].id')
    
    if [ -z "$USER_ID" ]; then
      echo "Error: Failed to get user ID for ${USERNAME}."
      exit 1
    fi
    echo "User ID obtained: ${USER_ID}"

    ROLE_ID=$(curl -s -X GET "${KEYCLOAK_URL}/admin/realms/${REALM_NAME}/roles?search=${ROLE_NAME}" \
      -H "Authorization: Bearer ${ADMIN_TOKEN}" \
      -H "Content-Type: application/json" | jq -r '.[0].id')

    if [ -z "$ROLE_ID" ]; then
      echo "Error: Failed to get role ID for ${ROLE_NAME}."
      exit 1
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

    echo "Role '${ROLE_NAME}' assigned to ${USERNAME}."
  else
    echo "No role specified. Skipping role assignment for ${USERNAME}."
  fi
}

create_user "test" "123" "Ichiro" "Suzuki"
create_user "user" "user" "Satoshi" "Nakamoto"
create_user "123" "P@ssw0rd" "Shunsuke" "Nakamura"
create_user "manager" "admin" "Akira" "Kurosawa" "admin"

echo "Script finished."

