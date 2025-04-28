#!/bin/sh

set -e -x

mvn=/daten/Projects/PRV/mediadb/mvnw

$mvn clean package

rm -fv docker/plugins/*.jar
cp -pv ./target/keycloak-facilities-admin-plugin-*.jar docker/plugins/

cd docker/
docker compose up --build
