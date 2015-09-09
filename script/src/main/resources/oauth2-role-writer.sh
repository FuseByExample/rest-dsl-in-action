#!/usr/bin/env bash

REALM=fuse
USER=admin
PASSWORD=admin123
CLIENT_ID=fuse
HOST=fusehost
PORT_HTTP=8080
PORT_HTTPS=8443

#TOKEN=$(curl -X POST http://127.0.0.1:8080/auth/realms/$REALM/protocol/openid-connect/token  -H "Content-Type: application/x-www-form-urlencoded" -d 'username=$USER' -d 'password=$PASSWORD' -d 'grant_type=password' -d 'client_id=$USER' | jq -r '.access_token')
auth_result=$(http --verify=no -f http://$HOST:$PORT_HTTP/auth/realms/$REALM/protocol/openid-connect/token username=$USER password=$PASSWORD grant_type=password client_id=$CLIENT_ID)
access_token=$(echo -e "$auth_result" | awk -F"," '{print $1}' | awk -F":" '{print $2}' | sed s/\"//g | tr -d ' ')

APIGATEWAY=https://$HOST:$PORT_HTTPS/apiman-gateway
ORG=fuse
SERVICE=blog-service
VERSION=6.0
URL=$APIGATEWAY/$ORG/$SERVICE/$VERSION/

echo ">>> Token query"
#echo "curl -X POST http://127.0.0.1:8080/auth/realms/$REALM/protocol/openid-connect/token  -H 'Content-Type: application/x-www-form-urlencoded' -d 'username=$USER' -d 'password=$PASSWORD' -d 'grant_type=password' -d 'client_id=$USER'"
echo "http --verify=no -f http://$HOST:$PORT_HTTP/auth/realms/$REALM/protocol/openid-connect/token username=$USER password=$PASSWORD grant_type=password client_id=$CLIENT_ID"

echo ">>> TOKEN Received"
echo $access_token

echo ">>> Gateway Service URL"
echo "$URL"

echo ">>> GET Blog article : 1"
http --verify=no GET $URL/search/id/1 "Authorization: Bearer $access_token"

echo ">>> PUT Blog article n° 10"
echo '{ "user": "cmoulliard", "postDate": "2015-09-15T10:10", "body": "Integration is hard - 10", "title": "On distributed search" }' | http --verify=no PUT $URL/10 "Authorization: Bearer $access_token"

echo ">>> DELETE Blog Article n° 10"
http --verify=no DELETE $URL/10 "Authorization: Bearer $access_token"

echo ">>> GET Blog article : 10"
http --verify=no GET $URL/search/id/10 "Authorization: Bearer $access_token"

echo ">>> GET Blog article for user cmoulliard"
http --verify=no GET $URL/search/user/cmoulliard "Authorization: Bearer $access_token"

