package org.arquillian.smart.testing.surefire.runorder.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.maven.plugin.surefire.runorder.api.RunOrder;
import org.apache.maven.surefire.testset.RunOrderParameters;
import org.arquillian.smart.testing.spi.JavaSPILoader;
import org.arquillian.smart.testing.strategies.failed.InProjectTestReportLoader;
import org.arquillian.smart.testing.strategies.failed.TestReportLoader;

public class FailedRunOrder implements RunOrder {

    @Override
    public List<Class<?>> orderTestClasses(Collection<Class<?>> collection, RunOrderParameters runOrderParameters,
        int i) {
        final LinkedHashSet<Class<?>> classes = testReportLoader.loadTestResults()
            .stream()
            .map(this::getClass)
            .collect(Collectors.toCollection(LinkedHashSet::new));

        return new ArrayList<>(classes);
    }

    private TestReportLoader testReportLoader = new InProjectTestReportLoader(new JavaSPILoader());

    private Class<?> getClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException();
        }
    }

    @Override
    public String getName() {
        return "failed";
    }
}
