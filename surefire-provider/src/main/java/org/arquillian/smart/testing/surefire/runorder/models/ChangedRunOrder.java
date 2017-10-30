package org.arquillian.smart.testing.surefire.runorder.models;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.maven.plugin.surefire.runorder.api.RunOrder;
import org.apache.maven.surefire.testset.RunOrderParameters;
import org.apache.maven.surefire.util.TestsToRun;
import org.arquillian.smart.testing.ClassNameExtractor;
import org.arquillian.smart.testing.configuration.Configuration;
import org.arquillian.smart.testing.hub.storage.ChangeStorage;
import org.arquillian.smart.testing.logger.Log;
import org.arquillian.smart.testing.logger.Logger;
import org.arquillian.smart.testing.scm.Change;
import org.arquillian.smart.testing.scm.ChangeType;
import org.arquillian.smart.testing.scm.spi.ChangeResolver;
import org.arquillian.smart.testing.spi.JavaSPILoader;

public class ChangedRunOrder implements RunOrder {

    private static final Logger logger = Log.getLogger();
    private ChangeResolver changeResolver;
    private ChangeStorage changeStorage;
    private File projectDir;
    private Configuration configuration;

    public ChangedRunOrder() {
        this(new JavaSPILoader().onlyOne(ChangeResolver.class).get(),
            new JavaSPILoader().onlyOne(ChangeStorage.class).get(),
            Paths.get("").toFile().getAbsoluteFile());
    }

    public ChangedRunOrder(ChangeResolver changeResolver, ChangeStorage changeStorage, File projectDir) {
        this.changeResolver = changeResolver;
        this.changeStorage = changeStorage;
        this.projectDir = projectDir;
    }

    @Override
    public String getName() {
        return "changed";
    }

    @Override
    public List<Class<?>> orderTestClasses(Collection<Class<?>> collection, RunOrderParameters runOrderParameters,
        int i) {
        TestsToRun testsToRun = new TestsToRun(new LinkedHashSet<>(collection));
        configuration = Configuration.load(projectDir);
        final Collection<Change> files = changeStorage.read(projectDir)
            .orElseGet(() -> {
                logger.warn("No cached changes detected... using direct resolution");
                return changeResolver.diff(projectDir, configuration, getName());
            });

        return files.stream()
            .filter(
                change -> ChangeType.MODIFY.equals(change.getChangeType()) && isTest(change.getLocation(), testsToRun))
            .map(this::getClass)
            .collect(Collectors.toList());
    }

    private boolean isTest(Path location, TestsToRun testsToRun) {
        final String className = new ClassNameExtractor().extractFullyQualifiedName(location);
        return testsToRun.getClassByName(className) != null;
    }

    private Class<?> getClass(Change change) {
        try {
            return Class.forName(new ClassNameExtractor().extractFullyQualifiedName(change.getLocation()));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException();
        }
    }
}
