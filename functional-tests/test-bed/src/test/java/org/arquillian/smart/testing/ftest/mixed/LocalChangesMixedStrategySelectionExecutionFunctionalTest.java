package org.arquillian.smart.testing.ftest.mixed;

import java.util.List;
import org.arquillian.smart.testing.ftest.testbed.project.Project;
import org.arquillian.smart.testing.ftest.testbed.rules.GitClone;
import org.arquillian.smart.testing.ftest.testbed.rules.TestBed;
import org.arquillian.smart.testing.ftest.testbed.testresults.TestResult;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import static org.arquillian.smart.testing.ftest.testbed.configuration.Strategy.AFFECTED;
import static org.arquillian.smart.testing.ftest.testbed.configuration.Strategy.NEW;
import static org.arquillian.smart.testing.ftest.testbed.configuration.Mode.SELECTING;
import static org.assertj.core.api.Assertions.assertThat;


public class LocalChangesMixedStrategySelectionExecutionFunctionalTest {
    @ClassRule
    public static final GitClone GIT_CLONE = new GitClone();

    @Rule
    public TestBed testBed = new TestBed();

    @Test
    public void should_execute_all_new_tests_and_related_to_production_code_changes() throws Exception {
        // given
        final Project project = testBed.getProject();

        project.configureSmartTesting()
                .executionOrder(NEW, AFFECTED)
                .inMode(SELECTING)
            .enable();

        final List<TestResult> expectedTestResults = project
            .applyAsLocalChanges("Single method body modification - sysout",
                "Inlined variable in a method", "Adds new unit test");

        // when
        final List<TestResult> actualTestResults = project.build();

        // then
        assertThat(actualTestResults).containsAll(expectedTestResults).hasSameSizeAs(expectedTestResults);
    }
}