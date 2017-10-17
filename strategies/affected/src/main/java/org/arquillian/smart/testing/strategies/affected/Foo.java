package org.arquillian.smart.testing.strategies.affected;

import java.util.List;
import org.arquillian.smart.testing.configuration.ConfigurationItem;
import org.arquillian.smart.testing.configuration.ConfigurationSection;
import org.arquillian.smart.testing.spi.StrategyConfiguration;

public class Foo implements StrategyConfiguration {

    @Override
    public List<ConfigurationItem> registerConfigurationItems() {
        return null;
    }

    @Override
    public String name() {
        return "foo";
    }
}
