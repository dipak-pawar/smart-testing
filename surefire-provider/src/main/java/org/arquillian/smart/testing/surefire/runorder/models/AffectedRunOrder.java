package org.arquillian.smart.testing.surefire.runorder.models;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;
import org.apache.maven.plugin.surefire.runorder.api.RunOrder;
import org.apache.maven.surefire.testset.RunOrderParameters;
import org.apache.maven.surefire.util.TestsToRun;
import org.arquillian.smart.testing.ClassNameExtractor;
import org.arquillian.smart.testing.Logger;
import org.arquillian.smart.testing.filter.TestVerifier;
import org.arquillian.smart.testing.hub.storage.ChangeStorage;
import org.arquillian.smart.testing.scm.Change;
import org.arquillian.smart.testing.scm.spi.ChangeResolver;
import org.arquillian.smart.testing.spi.JavaSPILoader;
import org.arquillian.smart.testing.strategies.affected.ClassDependenciesGraph;
import org.arquillian.smart.testing.strategies.affected.StandaloneClasspath;
import org.arquillian.smart.testing.strategies.affected.detector.FileSystemTestClassDetector;
import org.arquillian.smart.testing.strategies.affected.detector.TestClassDetector;

public class AffectedRunOrder implements RunOrder {
    private static final Logger logger = Logger.getLogger(AffectedRunOrder.class);

    // TODO TestClassDetector is something that can be moved to extension
    private final TestClassDetector testClassDetector;
    private final String classpath;

    private final ChangeResolver changeResolver;
    private final ChangeStorage changeStorage;

    public AffectedRunOrder() {
        this(new FileSystemTestClassDetector(new File(System.getProperty("user.dir"))), new JavaSPILoader().onlyOne(ChangeStorage.class).get(),
            new JavaSPILoader().onlyOne(ChangeResolver.class).get(),
            "");
    }


    AffectedRunOrder(TestClassDetector testClassDetector, ChangeStorage changeStorage, ChangeResolver changeResolver,
        String classpath) {
        this.testClassDetector = testClassDetector;
        this.changeStorage = changeStorage;
        this.changeResolver = changeResolver;
        this.classpath = classpath;
    }

    @Override
    public String getName() {
        return "affected";
    }

    @Override
    public List<Class<?>> orderTestClasses(Collection<Class<?>> collection, RunOrderParameters runOrderParameters,
        int i) {
        TestVerifier testVerifier = getTestVerifier(collection);

        ClassDependenciesGraph classDependenciesGraph = configureTestClassDetector(testVerifier);

        // TODO this operations should be done in extension to avoid scanning for all modules.
        // TODO In case of Arquillian core is an improvement of 500 ms per module
        // Scan disk finding all tests of current project

        final long beforeDetection = System.currentTimeMillis();

        final Set<File> allTestsOfCurrentProject = this.testClassDetector.detect(testVerifier);
        classDependenciesGraph.buildTestDependencyGraph(allTestsOfCurrentProject);

        final Collection<Change> files = changeStorage.read()
            .orElseGet(() -> {
                logger.warn("No cached changes detected... using direct resolution");
                return changeResolver.diff();
            });

        logger.log(Level.FINER, "Time To Build Affected Dependencies Graph %d ms",
            (System.currentTimeMillis() - beforeDetection));

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

        logger.log(Level.FINER, "Time To Find Affected Tests %d ms", (System.currentTimeMillis() - beforeFind));

        return new ArrayList<>(affected);

    }

    private TestVerifier getTestVerifier(Collection<Class<?>> collection) {
        final TestsToRun testsToRun = new TestsToRun(new LinkedHashSet<>(collection));

        return resource -> {
            final String className = new ClassNameExtractor().extractFullyQualifiedName(resource);
            return testsToRun.getClassByName(className) != null;
        };
    }

    private Class<?> getClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException();
        }
    }

    private ClassDependenciesGraph configureTestClassDetector(
        TestVerifier testVerifier) {
        return new ClassDependenciesGraph(new StandaloneClasspath(Collections.emptyList(), this.classpath), testVerifier);
    }
}
