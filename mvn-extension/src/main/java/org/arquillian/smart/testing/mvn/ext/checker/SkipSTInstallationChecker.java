package org.arquillian.smart.testing.mvn.ext.checker;

import org.apache.maven.project.MavenProject;
import org.arquillian.smart.testing.configuration.Configuration;
import org.arquillian.smart.testing.mvn.ext.dependencies.ExtensionVersion;

import static org.arquillian.smart.testing.configuration.Configuration.SMART_TESTING_DISABLE;

public class SkipSTInstallationChecker {

    private final Configuration configuration;
    private final MavenProject mavenProject;

    private String reason;

    public SkipSTInstallationChecker(Configuration configuration, MavenProject mavenProject) {
        this.configuration = configuration;
        this.mavenProject = mavenProject;
    }

    public String getReason() {
        return reason;
    }

    public boolean shouldSkip() {
        if (configuration.isDisable()) {
            reason = String.format("Disabling Smart Testing %s in %s module. Reason: " + SMART_TESTING_DISABLE + " is set.",
                ExtensionVersion.version().toString(), mavenProject.getArtifactId());
            return true;
        }
        if (!configuration.areStrategiesDefined()) {
           reason = String.format("Smart Testing Extension is installed but no strategies are provided for %s module. It won't influence the way how your tests are executed. "
                    + "For details on how to configure it head over to http://bit.ly/st-config", mavenProject.getArtifactId());
           return true;
        }
        return false;
    }

}