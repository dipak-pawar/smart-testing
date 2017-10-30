package org.arquillian.smart.testing.surefire.runorder.models;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.maven.plugin.surefire.runorder.api.RunOrder;
import org.apache.maven.surefire.testset.RunOrderParameters;
import org.apache.maven.surefire.util.TestsToRun;
import org.arquillian.smart.testing.api.TestVerifier;
import org.arquillian.smart.testing.configuration.Configuration;
import org.arquillian.smart.testing.hub.storage.ChangeStorage;
import org.arquillian.smart.testing.logger.Log;
import org.arquillian.smart.testing.logger.Logger;
import org.arquillian.smart.testing.scm.Change;
import org.arquillian.smart.testing.scm.spi.ChangeResolver;
import org.arquillian.smart.testing.spi.JavaSPILoader;
import org.arquillian.smart.testing.strategies.affected.ClassDependenciesGraph;
import org.arquillian.smart.testing.strategies.affected.detector.FileSystemTestClassDetector;
import org.arquillian.smart.testing.strategies.affected.detector.TestClassDetector;

public class AffectedRunOrder implements RunOrder {

    private static final Logger logger = Log.getLogger();


    // TODO TestClassDetector is something that can be moved to extension
    private TestClassDetector testClassDetector;

    private ChangeResolver changeResolver;
    private ChangeStorage changeStorage;
    private File projectDir;
    private TestVerifier testVerifier;
    private Configuration configuration;

    public AffectedRunOrder() {
        this(new FileSystemTestClassDetector(Paths.get("").toFile().getAbsoluteFile()),
            new JavaSPILoader().onlyOne(ChangeStorage.class).get(),
            new JavaSPILoader().onlyOne(ChangeResolver.class).get(),
            Paths.get("").toFile().getAbsoluteFile());
    }

    AffectedRunOrder(TestClassDetector testClassDetector, ChangeStorage changeStorage, ChangeResolver changeResolver,
        File projectDir) {
        this.testClassDetector = testClassDetector;
        this.changeStorage = changeStorage;
        this.changeResolver = changeResolver;
        this.projectDir = projectDir;
    }

    @Override
    public List<Class<?>> orderTestClasses(Collection<Class<?>> collection, RunOrderParameters runOrderParameters,
        int i) {
        configuration = Configuration.load(projectDir);
        testVerifier = getTestVerifier(collection);
        ClassDependenciesGraph classDependenciesGraph = configureTestClassDetector();

        // TODO this operations should be done in extension to avoid scanning for all modules.
        // TODO In case of Arquillian core is an improvement of 500 ms per module
        // Scan disk finding all tests of current project

        final long beforeDetection = System.currentTimeMillis();

        final Set<File> allTestsOfCurrentProject = this.testClassDetector.detect(testVerifier);
        classDependenciesGraph.buildTestDependencyGraph(allTestsOfCurrentProject);

        final Collection<Change> files = changeStorage.read(projectDir)
            .orElseGet(() -> {
                logger.warn("No cached changes detected... using direct resolution");
                return changeResolver.diff(projectDir, this.configuration, getName());
            });

        logger.debug("Time To Build Affected Dependencies Graph %d ms", (System.currentTimeMillis() - beforeDetection));

        final Set<File> mainClasses = files.stream()
            .map(Change::getLocation)
            .filter(testVerifier::isCore)
            .map(Path::toFile)
            .collect(Collectors.toSet());

        final long beforeFind = System.currentTimeMillis();

        final LinkedHashSet<Class<?>> affected = classDependenciesGraph.findTestsDependingOn(mainClasses)
            .stream()
            .map(this::getClass)
            .collect(Collectors.toCollection(LinkedHashSet::new));

        logger.debug("Time To Find Affected Tests %d ms", (System.currentTimeMillis() - beforeFind));

        return new ArrayList<>(affected);

    }

    private TestVerifier getTestVerifier(Collection<Class<?>> collection) {
        final TestsToRun testsToRun = new TestsToRun(new LinkedHashSet<>(collection));

        return className -> testsToRun.getClassByName(className) != null;
    }

    private Class<?> getClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException();
        }
    }

    private ClassDependenciesGraph configureTestClassDetector() {
        return new ClassDependenciesGraph(testVerifier);
    }

    @Override
    public String getName() {
        return "affected";
    }

}
