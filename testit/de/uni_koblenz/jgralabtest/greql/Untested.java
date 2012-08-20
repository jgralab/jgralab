package de.uni_koblenz.jgralabtest.greql;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ AdditionalTest.class, ExecutableGreqlTest.class,
		GreqlConstructions.class, RecordTest.class, StoreJValueTest.class,
		StoreValuesTest.class, SubgraphRestrictionTest.class })
public class Untested {

}
