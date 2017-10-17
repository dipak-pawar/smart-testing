package org.arquillian.smart.testing.strategies.affected;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import net.jcip.annotations.NotThreadSafe;
import org.arquillian.smart.testing.configuration.Configuration;
import org.arquillian.smart.testing.strategies.affected.fakeproject.main.D;
import org.arquillian.smart.testing.strategies.affected.fakeproject.test.ATest;
import org.arquillian.smart.testing.strategies.affected.fakeproject.test.BTest;
import org.arquillian.smart.testing.strategies.affected.fakeproject.test.CTest;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.experimental.categories.Category;
import org.junit.rules.TemporaryFolder;

import static org.assertj.core.api.Assertions.assertThat;

@Category(NotThreadSafe.class)
public class ClassDependenciesGraphUsingPropertyTest {
    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder(Paths.get(".").toFile());

    @Rule
    public final RestoreSystemProperties restoreSystemProperties = new RestoreSystemProperties();

    @Test
    public void should_not_detect_all_changes_transitive_if_transitivity_is_disabled() {
        // given
        System.setProperty(AffectedRunnerProperties.SMART_TESTING_AFFECTED_TRANSITIVITY, "false");
        Configuration.load().dump(temporaryFolder.getRoot());
        final ClassDependenciesGraph
            classDependenciesGraph = new ClassDependenciesGraph(new EndingWithTestTestVerifier(), temporaryFolder.getRoot());

        final String testLocation = ATest.class.getResource("ATest.class").getPath();
        final String testLocation2 = BTest.class.getResource("BTest.class").getPath();
        final String testLocation3 = CTest.class.getResource("CTest.class").getPath();
        classDependenciesGraph.buildTestDependencyGraph(Arrays.asList(new File(testLocation), new File(testLocation2),
            new File(testLocation3)));

        // when
        Set<File> mainObjectsChanged = new HashSet<>();
        mainObjectsChanged.add(new File(D.class.getResource("D.class").getPath()));

        final Set<String> testsDependingOn = classDependenciesGraph.findTestsDependingOn(mainObjectsChanged);

        // then
        assertThat(testsDependingOn)
            .isEmpty();
    }

    @Test
    public void should_exclude_imports_if_property_set() {
        // given
        System.setProperty(AffectedRunnerProperties.SMART_TESTING_AFFECTED_EXCLUSIONS, "org.arquillian.smart.testing.strategies.affected.fakeproject.main.B");
        Configuration.load().dump(temporaryFolder.getRoot());
        final ClassDependenciesGraph
            classDependenciesGraph = new ClassDependenciesGraph(new EndingWithTestTestVerifier(), temporaryFolder.getRoot());

        final String testLocation = ATest.class.getResource("ATest.class").getPath();
        final String testLocation2 = BTest.class.getResource("BTest.class").getPath();
        final String testLocation3 = CTest.class.getResource("CTest.class").getPath();
        classDependenciesGraph.buildTestDependencyGraph(Arrays.asList(new File(testLocation), new File(testLocation2),
            new File(testLocation3)));

        // when
        Set<File> mainObjectsChanged = new HashSet<>();
        mainObjectsChanged.add(new File(D.class.getResource("D.class").getPath()));

        final Set<String> testsDependingOn = classDependenciesGraph.findTestsDependingOn(mainObjectsChanged);

        // then
        assertThat(testsDependingOn)
            .isEmpty();
    }

    @Test
    public void should_include_only_imports_if_property_set() {
        // given
        System.setProperty(AffectedRunnerProperties.SMART_TESTING_AFFECTED_INCLUSIONS, "org.arquillian.smart.testing.strategies.affected.fakeproject.main.A");
        Configuration.load().dump(temporaryFolder.getRoot());
        final ClassDependenciesGraph
            classDependenciesGraph = new ClassDependenciesGraph(new EndingWithTestTestVerifier(), temporaryFolder.getRoot());

        final String testLocation = ATest.class.getResource("ATest.class").getPath();
        final String testLocation2 = BTest.class.getResource("BTest.class").getPath();
        final String testLocation3 = CTest.class.getResource("CTest.class").getPath();
        classDependenciesGraph.buildTestDependencyGraph(Arrays.asList(new File(testLocation), new File(testLocation2),
            new File(testLocation3)));

        // when
        Set<File> mainObjectsChanged = new HashSet<>();
        mainObjectsChanged.add(new File(D.class.getResource("A.class").getPath()));

        final Set<String> testsDependingOn = classDependenciesGraph.findTestsDependingOn(mainObjectsChanged);

        // then
        assertThat(testsDependingOn)
            .containsExactlyInAnyOrder(
                "org.arquillian.smart.testing.strategies.affected.fakeproject.test.ATest");
    }
}
