package ch.openem.keycloakadminhelperplugin.it;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.testcontainers.images.ImagePullPolicy;
import org.testcontainers.images.PullPolicy;

import java.io.File;
import java.util.List;

import static ch.openem.keycloakadminhelperplugin.it.KeyCloakContainer.Distribution.quarkus;
import static java.lang.module.ModuleDescriptor.Version;

class KeyCloakContainer {

    enum Distribution {
        quarkus
    }

    private static final Distribution KEYCLOAK_DIST = Distribution.valueOf(
            System.getProperty("keycloak.dist", quarkus.name()));

    private static final String LATEST_VERSION = "latest";
    private static final String NIGHTLY_VERSION = "nightly";
    private static final String KEYCLOAK_VERSION = System.getProperty("keycloak.version", LATEST_VERSION);

    private static final boolean USE_JAR = Boolean.parseBoolean(System.getProperty("useJar", "false"));

    static String get() {
        String imageName = "keycloak";

        if (!isNightlyVersion()) {
            if (!isLatestVersion()) {
                if (getParsedVersion().compareTo(Version.parse("17")) < 0) {
                    if (quarkus.equals(KEYCLOAK_DIST)) {
                        imageName = "keycloak-x";
                    }
                }
            }
        }

        return "quay.io/keycloak/" + imageName + ":" + KEYCLOAK_VERSION;
    }

    static Boolean isNightlyVersion() {
        return NIGHTLY_VERSION.equalsIgnoreCase(KEYCLOAK_VERSION);
    }

    static Boolean isLatestVersion() {
        return LATEST_VERSION.equalsIgnoreCase(KEYCLOAK_VERSION);
    }

    static Version getParsedVersion() {
        if (isLatestVersion()) {
            return null;
        }
        return Version.parse(KEYCLOAK_VERSION);
    }

    static Distribution getDistribution() {
        return KEYCLOAK_DIST;
    }

    static KeycloakContainer createContainer() {
        String fullImage = KeyCloakContainer.get();
        ImagePullPolicy pullPolicy = PullPolicy.defaultPolicy();
        if (isLatestVersion() || isNightlyVersion()) {
            pullPolicy = PullPolicy.alwaysPull();
        }
        KeycloakContainer keycloakContainer = new KeycloakContainer(fullImage)
                .withImagePullPolicy(pullPolicy);
        if (USE_JAR) {
            keycloakContainer = keycloakContainer.withProviderLibsFrom(List.of(new File("target/keycloak-restrict-client-auth.jar")));
        } else {
            keycloakContainer = keycloakContainer.withProviderClassesFrom("target/classes");
        }
        return keycloakContainer;
    }
}