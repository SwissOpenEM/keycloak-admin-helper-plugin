ARG KEYCLOAK_IMAGE

FROM $KEYCLOAK_IMAGE

USER root
COPY plugins/*.jar /opt/keycloak/providers/
USER 1000
RUN /opt/keycloak/bin/kc.sh build \
 && /opt/keycloak/bin/kc.sh show-config | grep -q keycloak-facilities-admin-plugin
