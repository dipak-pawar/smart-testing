package org.arquillian.smart.testing.configuration;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import net.jcip.annotations.NotThreadSafe;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.experimental.categories.Category;
import org.junit.rules.TemporaryFolder;

import static org.arquillian.smart.testing.RunMode.ORDERING;
import static org.arquillian.smart.testing.configuration.Configuration.SMART_TESTING;
import static org.arquillian.smart.testing.configuration.ConfigurationLoader.SMART_TESTING_YML;
import static org.arquillian.smart.testing.configuration.ConfigurationOverWriteUsingInheretTest.CONFIG;
import static org.arquillian.smart.testing.configuration.ConfigurationOverWriteUsingInheretTest.dumpData;
import static org.arquillian.smart.testing.scm.ScmRunnerProperties.HEAD;
import static org.arquillian.smart.testing.scm.ScmRunnerProperties.SCM_LAST_CHANGES;
import static org.assertj.core.api.Assertions.assertThat;

@Category(NotThreadSafe.class)
public class ConfigurationOverWriteUsingInheretWithSystemPropertyTest {

    @Rule
    public final RestoreSystemProperties restoreSystemProperties = new RestoreSystemProperties();

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void system_properties_should_take_precedence_over_config_file() throws IOException {
        // given
        System.setProperty(SMART_TESTING, "changed");
        System.setProperty(SCM_LAST_CHANGES, "3");

        temporaryFolder.newFolder(CONFIG);
        final String root = temporaryFolder.getRoot().toString();
        Map<String, Object> child = new HashMap<>();
        child.put("mode", "ordering");
        child.put("inherit", "../smart-testing.yml");

        dumpData(Paths.get(root, CONFIG, SMART_TESTING_YML), child);

        Map<String, Object> parent = new HashMap<>();
        parent.put("strategies", "new, changed, affected");
        dumpData(Paths.get(root, SMART_TESTING_YML), parent);

        final Range range = new Range();
        range.setHead(HEAD);
        range.setTail(HEAD + "~3");

        // when
        final Configuration configuration = ConfigurationLoader.load(Paths.get(root, CONFIG));

        // then
        assertThat(configuration.getStrategies()).isEqualTo(new String[] {"changed"});
        assertThat(configuration.getScm().getRange()).isEqualToComparingFieldByField(range);
        assertThat(configuration.getMode()).isEqualTo(ORDERING);
    }
}
