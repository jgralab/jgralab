package de.uni_koblenz.jgralabtest.greql.funlib.arithmetics;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ AbsTest.class, CeilTest.class, CosTest.class,
		FloorTest.class, LnTest.class, NegTest.class, RoundTest.class,
		SinTest.class, SqrtTest.class, TanTest.class, ToDoubleTest.class,
		ToIntegerTest.class, ToLongTest.class })
public class RunArithmeticTests {
	public static final double EPSILON = 0.0000001;
}
