package de.uni_koblenz.jgralabtest.genericimpltest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Use this to run all tests for the generic implementation.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({ DomainTest.class, GenericGraphImplTest.class, GenericVertexImplTest.class,
		GenericEdgeImplTest.class })
public class RunGenericTests {

}
