package org.arquillian.smart.testing.configuration;

import java.nio.file.Paths;
import org.junit.Test;

public class ConfigurationStrategiesTest {

    @Test
    public void foo() {
        final Configuration actualConfiguration =
            Configuration.load(Paths.get("src/test/resources/configuration/smart-testing-strategy-configuration.yml"));

        System.out.println(actualConfiguration);
    }
}
