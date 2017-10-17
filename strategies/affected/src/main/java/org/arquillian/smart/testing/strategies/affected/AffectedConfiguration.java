package org.arquillian.smart.testing.strategies.affected;

import java.util.ArrayList;
import java.util.List;
import org.arquillian.smart.testing.configuration.ConfigurationItem;
import org.arquillian.smart.testing.spi.StrategyConfiguration;

import static org.arquillian.smart.testing.strategies.affected.AffectedRunnerProperties.SMART_TESTING_AFFECTED_CONFIG;
import static org.arquillian.smart.testing.strategies.affected.AffectedRunnerProperties.SMART_TESTING_AFFECTED_EXCLUSIONS;
import static org.arquillian.smart.testing.strategies.affected.AffectedRunnerProperties.SMART_TESTING_AFFECTED_INCLUSIONS;
import static org.arquillian.smart.testing.strategies.affected.AffectedRunnerProperties.SMART_TESTING_AFFECTED_TRANSITIVITY;

public class AffectedConfiguration implements StrategyConfiguration {

    public String getConfig() {
        return config;
    }

    public boolean isTransitivity() {
        return transitivity;
    }

    public String getExclusions() {
        return exclusions;
    }

    public String getInclusions() {
        return inclusions;
    }

    private String config;
    private boolean transitivity;
    private String exclusions;
    private String inclusions;

    public void setConfig(String config) {
        this.config = config;
    }

    public void setTransitivity(boolean transitivity) {
        this.transitivity = transitivity;
    }

    public void setExclusions(String exclusions) {
        this.exclusions = exclusions;
    }

    public void setInclusions(String inclusions) {
        this.inclusions = inclusions;
    }

    @Override
    public List<ConfigurationItem> registerConfigurationItems() {
        List<ConfigurationItem> configItems = new ArrayList<>();
        configItems.add(new ConfigurationItem("config", SMART_TESTING_AFFECTED_CONFIG));
        configItems.add(new ConfigurationItem("transitivity", SMART_TESTING_AFFECTED_TRANSITIVITY, true));
        configItems.add(new ConfigurationItem("exclusions", SMART_TESTING_AFFECTED_EXCLUSIONS));
        configItems.add(new ConfigurationItem("inclusions", SMART_TESTING_AFFECTED_INCLUSIONS));

        return configItems;
    }

    @Override
    public String name() {
        return "affected";
    }
}
