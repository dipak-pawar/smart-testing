package org.arquillian.smart.testing.surefire.runorder.models;

import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.maven.plugin.surefire.runorder.api.RunOrder;
import org.apache.maven.surefire.testset.RunOrderParameters;
import org.apache.maven.surefire.util.TestsToRun;
import org.arquillian.smart.testing.ClassNameExtractor;
import org.arquillian.smart.testing.Logger;
import org.arquillian.smart.testing.hub.storage.ChangeStorage;
import org.arquillian.smart.testing.scm.Change;
import org.arquillian.smart.testing.scm.ChangeType;
import org.arquillian.smart.testing.scm.git.GitChangeResolver;
import org.arquillian.smart.testing.scm.spi.ChangeResolver;
import org.arquillian.smart.testing.spi.JavaSPILoader;

public class ChangedRunOrder implements RunOrder {

    private static final Logger logger = Logger.getLogger(ChangedRunOrder.class);

    private final ChangeResolver changeResolver;
    private final ChangeStorage changeStorage;

    public ChangedRunOrder() {
        this(new GitChangeResolver(), new JavaSPILoader().onlyOne(ChangeStorage.class).get());
    }

    public ChangedRunOrder(ChangeResolver changeResolver, ChangeStorage changeStorage) {
        this.changeResolver = changeResolver;
        this.changeStorage = changeStorage;
    }

    @Override
    public String getName() {
        return "changed";
    }

    @Override
    public List<Class<?>> orderTestClasses(Collection<Class<?>> collection, RunOrderParameters runOrderParameters,
        int i) {
        TestsToRun testsToRun = new TestsToRun(new LinkedHashSet<>(collection));

        final Collection<Change> files = changeStorage.read()
            .orElseGet(() -> {
                logger.warn("No cached changes detected... using direct resolution");
                return changeResolver.diff();
            });

        return files.stream()
            .filter(change -> ChangeType.MODIFY.equals(change.getChangeType()) && isTest(change.getLocation(), testsToRun))
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
