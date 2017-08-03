package org.arquillian.smart.testing.surefire.runorder;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import org.apache.maven.plugin.surefire.runorder.api.RunOrder;
import org.apache.maven.plugin.surefire.runorder.api.RunOrderCalculator;
import org.apache.maven.plugin.surefire.runorder.model.RunOrderFactory;
import org.apache.maven.plugin.surefire.runorder.spi.RunOrderProvider;
import org.apache.maven.surefire.testset.RunOrderParameters;
import org.arquillian.smart.testing.surefire.runorder.models.AffectedRunOrder;
import org.arquillian.smart.testing.surefire.runorder.models.ChangedRunOrder;
import org.arquillian.smart.testing.surefire.runorder.models.FailedRunOrder;
import org.arquillian.smart.testing.surefire.runorder.models.NewRunOrder;

public class SmartTestingRunOrderProvider implements RunOrderProvider {

    @Override
    public Collection<RunOrder> getRunOrders() {
        return Arrays.asList(new NewRunOrder(), new ChangedRunOrder(), new AffectedRunOrder(), new FailedRunOrder(), RunOrderFactory.FILESYSTEM);
    }

    @Override
    public Integer priority() {
        return 1;
    }

    @Override
    public RunOrderCalculator createRunOrderCalculator(RunOrderParameters runOrderParameters, int threadCount) {
        return new SmartTestingRunOrderCalculator(runOrderParameters, threadCount);
    }

    // If you are not using any runorder while running your tests, the one which mentioned below
    // will be used.
    @Override
    public Collection<RunOrder> defaultRunOrder() {
        return Collections.singletonList(RunOrderFactory.FILESYSTEM);
    }
}
