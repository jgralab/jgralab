package de.uni_koblenz.jgralabtest.instancetest.internal;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses( { GraphBaseTest.class, EdgeBaseTest.class,
		VertexBaseTest.class })
public class RunInternalTests {

}