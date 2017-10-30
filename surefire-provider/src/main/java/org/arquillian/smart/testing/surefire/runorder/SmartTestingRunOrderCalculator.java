package org.arquillian.smart.testing.surefire.runorder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import org.apache.maven.plugin.surefire.runorder.api.RunOrder;
import org.apache.maven.plugin.surefire.runorder.api.RunOrderCalculator;
import org.apache.maven.surefire.testset.RunOrderParameters;
import org.apache.maven.surefire.util.TestsToRun;
import org.arquillian.smart.testing.logger.Log;
import org.arquillian.smart.testing.surefire.provider.logger.SurefireProviderLoggerFactory;

public class SmartTestingRunOrderCalculator implements RunOrderCalculator {

    private final RunOrderParameters runOrderParameters;
    private final int threadCount;

    private final RunOrder[] runOrder;

    public SmartTestingRunOrderCalculator(RunOrderParameters runOrderParameters, int threadCount) {
        this.runOrder = runOrderParameters.getRunOrders();
        this.runOrderParameters = runOrderParameters;
        this.threadCount = threadCount;
    }

    // TODO: 8/3/17 verifying one runorder for now. Add more once surefire ready
    @Override
    public TestsToRun orderTestClasses(TestsToRun scannedClasses) {

        List<Class<?>> testClasses = new ArrayList<Class<?>>(512);

        for (Class<?> scannedClass : scannedClasses) {
            testClasses.add(scannedClass);
        }

        Log.setLoggerFactory(new SurefireProviderLoggerFactory(runOrderParameters.getLogger(), true));

        // TODO: 8/24/17 you can have all classes for each runOrder & collect it, order/filter it.

        final Collection<Class<?>> classes = runOrder[0].orderTestClasses(testClasses, runOrderParameters, threadCount);

        return new TestsToRun(new LinkedHashSet<>(classes));
    }
}
