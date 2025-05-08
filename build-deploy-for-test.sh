#!/bin/sh

set -e -x

# build
mvn clean package

# copy binary to docker folder
rm -fv docker/plugins/*.jar
mkdir -p docker/plugins/
cp -pv ./target/keycloak-facilities-admin-plugin-*.jar docker/plugins/

# start docker environment
cd docker/
docker compose up --build

# open it in browser: http://localhost:8024/admin/master/console/