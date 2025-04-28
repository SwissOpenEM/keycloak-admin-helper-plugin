ARG KEYCLOAK_IMAGE

FROM $KEYCLOAK_IMAGE

USER root
COPY plugins/*.jar /opt/keycloak/providers/
USER 1000
RUN /opt/keycloak/bin/kc.sh build
RUN /opt/keycloak/bin/kc.sh show-config | grep keycloak-facilities-admin-plugin

ENTRYPOINT ["/opt/keycloak/bin/kc.sh", "start-dev"]
