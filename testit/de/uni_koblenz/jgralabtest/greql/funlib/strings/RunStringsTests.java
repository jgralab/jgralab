package de.uni_koblenz.jgralabtest.greql.funlib.strings;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ CapitalizeFirstTest.class, ConcatTest.class,
		EndsWithTest.class, JoinTest.class, LengthTest.class,
		ReMatchTest.class, SplitTest.class, StartsWithTest.class,
		ToStringTest.class })
public class RunStringsTests {

}
