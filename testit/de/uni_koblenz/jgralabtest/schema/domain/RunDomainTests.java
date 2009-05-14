package de.uni_koblenz.jgralabtest.schema.domain;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses( { BooleanDomainTest.class, IntDomainTest.class,
		LongDomainTest.class, DoubleDomainTest.class, StringDomainTest.class,
		SetDomainTest.class, ListDomainTest.class, MapDomainTest.class,
		EnumDomainTest.class, RecordDomainTest.class })
public class RunDomainTests {

}