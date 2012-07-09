package de.uni_koblenz.jgralabtest.greql.funlib;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import de.uni_koblenz.jgralabtest.greql.funlib.arithmetics.RunArithmeticTests;
import de.uni_koblenz.jgralabtest.greql.funlib.bitops.RunBitopTests;

@RunWith(Suite.class)
@Suite.SuiteClasses({ RunArithmeticTests.class, RunBitopTests.class })
public class RunFunlibTests {

}
