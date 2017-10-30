package org.arquillian.smart.testing.strategies.affected.detector;

import java.io.File;
import java.util.Set;
import org.arquillian.smart.testing.api.TestVerifier;

/**
 * Contract used to get all tests of current project
 */
public interface TestClassDetector {

    Set<File> detect();

    Set<File> detect(TestVerifier testVerifier);

}
