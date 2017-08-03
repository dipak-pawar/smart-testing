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
import org.arquillian.smart.testing.scm.git.GitChangeResolver;
import org.arquillian.smart.testing.scm.spi.ChangeResolver;
import org.arquillian.smart.testing.spi.JavaSPILoader;

import static org.arquillian.smart.testing.scm.ChangeType.ADD;

public class NewRunOrder implements RunOrder {

    private final ChangeResolver changeResolver;
    private final ChangeStorage changeStorage;

    Logger logger = Logger.getLogger(NewRunOrder.class);

    public NewRunOrder() {
        this.changeResolver = new GitChangeResolver();
        this.changeStorage = new JavaSPILoader().onlyOne(ChangeStorage.class).get();
    }

    @Override
    public String getName() {
        return "new";
    }

    @Override
    public List<Class<?>> orderTestClasses(Collection<Class<?>> collection, RunOrderParameters runOrderParameters,
        int i) {
        final TestsToRun testsToRun = new TestsToRun(new LinkedHashSet<>(collection));

        final Collection<Change> changes = changeStorage.read()
            .orElseGet(() -> {
                logger.warn("No cached changes detected... using direct resolution");
                return changeResolver.diff();
            });

        return changes.stream()
            .filter(change -> ADD.equals(change.getChangeType()) && isTest(change.getLocation(), testsToRun))
            .map(this::getClass).collect(Collectors.toList());
    }

    private Class<?> getClass(Change change) {
        try {
            return Class.forName(new ClassNameExtractor().extractFullyQualifiedName(change.getLocation()));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException();
        }
    }

    private boolean isTest(Path location, TestsToRun testsToRun) {
        final String className = new ClassNameExtractor().extractFullyQualifiedName(location);
        return testsToRun.getClassByName(className) != null;
    }
}
