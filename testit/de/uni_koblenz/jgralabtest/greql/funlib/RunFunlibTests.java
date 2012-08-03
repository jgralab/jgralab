package de.uni_koblenz.jgralabtest.greql.funlib;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import de.uni_koblenz.jgralabtest.greql.funlib.arithmetics.RunArithmeticTests;
import de.uni_koblenz.jgralabtest.greql.funlib.bitops.RunBitopTests;
import de.uni_koblenz.jgralabtest.greql.funlib.collections.RunCollectionsTests;
import de.uni_koblenz.jgralabtest.greql.funlib.logics.RunLogicsTests;
import de.uni_koblenz.jgralabtest.greql.funlib.relations.RunRelationsTests;
import de.uni_koblenz.jgralabtest.greql.funlib.strings.RunStringsTests;

@RunWith(Suite.class)
@Suite.SuiteClasses({ RunArithmeticTests.class, RunBitopTests.class,
		RunLogicsTests.class, RunRelationsTests.class, RunStringsTests.class,
		RunCollectionsTests.class })
public class RunFunlibTests {

}
