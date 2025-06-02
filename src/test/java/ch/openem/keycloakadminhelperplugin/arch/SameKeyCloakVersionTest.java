package ch.openem.keycloakadminhelperplugin.arch;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SameKeyCloakVersionTest {
    @Test
    public void sameVersionInKeyCloakReferences() throws IOException {
        String vEnv = getVersionFromEnvFile(Path.of("docker/.env"));
        String vMvn = getVersionFromMvnFile(Path.of("pom.xml"));
        Assertions.assertEquals(vEnv, vMvn, "Versions don't match");
    }

    private String getVersionFromEnvFile(Path file) throws IOException {
        String prefix = "keycloak/keycloak:";
        String versionLine = Files.readAllLines(file).stream().filter(line -> line.contains(prefix)).findFirst().get();
        String version = versionLine.substring(versionLine.indexOf(prefix) + prefix.length());
        System.out.println("Version in " + file + ": " + version);
        return version;
    }

    private String getVersionFromMvnFile(Path file) throws IOException {
        String prefix = "<keycloak.version>";
        String versionLine = Files.readAllLines(file).stream().filter(line -> line.contains(prefix)).findFirst().get();
        String version = versionLine.substring(versionLine.indexOf(prefix) + prefix.length(), versionLine.indexOf("</"));
        System.out.println("Version in " + file + ": " + version);
        return version;
    }
}
