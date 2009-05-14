package de.uni_koblenz.jgralabtest.schema;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses( { GraphClassImplTest.class, EdgeClassImplTest.class,
		AggregationClassImplTest.class, CompositionClassImplTest.class,
		VertexClassImplTest.class })
public class RunAttributedElementTests {
}
