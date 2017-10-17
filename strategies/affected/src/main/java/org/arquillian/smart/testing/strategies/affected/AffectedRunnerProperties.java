package org.arquillian.smart.testing.strategies.affected;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringJoiner;
import org.arquillian.smart.testing.configuration.Configuration;
import org.arquillian.smart.testing.configuration.ObjectMapper;
import org.arquillian.smart.testing.spi.StrategyConfiguration;
import org.yaml.snakeyaml.Yaml;

class AffectedRunnerProperties {

    static final String SMART_TESTING_AFFECTED_CONFIG = "smart.testing.affected.config";

    static final String SMART_TESTING_AFFECTED_TRANSITIVITY = "smart.testing.affected.transitivity";

    static final String SMART_TESTING_AFFECTED_EXCLUSIONS = "smart.testing.affected.exclusions";
    static final String SMART_TESTING_AFFECTED_INCLUSIONS = "smart.testing.affected.inclusions";
    static final String INCLUSIONS = "inclusions";
    static final String EXCLUSIONS = "exclusions";

    private final Properties properties = new Properties();
    private AffectedConfiguration affectedConfiguration;

    public static void main(String[] args) throws IOException {
        try (InputStream io = Files.newInputStream(Paths.get(
            "/home/dipak/test-team/smart-testing/strategies/affected/src/main/resources/smart-testing-strategy-configuration.yml"))) {
            final Yaml yaml = new Yaml();
            Map<String, Object> yamlConfiguration = (Map<String, Object>) yaml.load(io);
            final Configuration actualConfiguration = ObjectMapper.mapToObject(Configuration.class, yamlConfiguration);
            actualConfiguration.dump(Paths.get(".").toFile());
            System.out.println(actualConfiguration);
        }
    }

    AffectedRunnerProperties(File rootDirectory) {
        final Configuration configuration = Configuration.loadPrecalculated(rootDirectory);
        final List<StrategyConfiguration> strategyConfigurations = configuration.getStrategiesConfiguration();
        final StrategyConfiguration affectedConfig = strategyConfigurations.stream()
            .filter(strategyConfiguration -> "affected".equals(strategyConfiguration.name()))
            .findFirst()
            .get();

        affectedConfiguration = (AffectedConfiguration) affectedConfig;
        readFile(affectedConfiguration.getConfig());
    }

    AffectedRunnerProperties(String csvLocation) {
        readFile(csvLocation);
    }

    void readFile(String location) {
        if (location == null) {
            return;
        }

        try {
            properties.load(Files.newBufferedReader(Paths.get(location)));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    boolean getSmartTestingAffectedTransitivity() {
        return affectedConfiguration.isTransitivity();
    }

    String getSmartTestingAffectedExclusions() {
        String exclusions = affectedConfiguration.getExclusions();
        String exclusionsFromFile = properties.getProperty(EXCLUSIONS);

        return resolve(exclusions, exclusionsFromFile);
    }

    String getSmartTestingAffectedInclusions() {
        String inclusions = affectedConfiguration.getInclusions();
        String inclusionsFromFile = properties.getProperty(INCLUSIONS);

        return resolve(inclusions, inclusionsFromFile);
    }

    String resolve(String expressions, String fileExpressions) {
        StringJoiner joiner = new StringJoiner(", ");
        if (expressions != null) {
            joiner.add(expressions);
        }

        if (fileExpressions != null) {
            joiner.add(fileExpressions);
        }

        return joiner.toString().trim();
    }

    String getProperty(String key) {
        return properties.getProperty(key);
    }
}
