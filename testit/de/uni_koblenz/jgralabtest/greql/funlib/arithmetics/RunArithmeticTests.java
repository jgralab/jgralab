package de.uni_koblenz.jgralabtest.greql.funlib.arithmetics;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ AbsTest.class, AddTest.class, CeilTest.class,
		CosTest.class, DivTest.class, ExpTest.class, FloorTest.class,
		LnTest.class, ModTest.class, MulTest.class, NegTest.class,
		RoundTest.class, SinTest.class, SqrtTest.class, SubTest.class,
		TanTest.class, ToDoubleTest.class, ToIntegerTest.class,
		ToLongTest.class })
public class RunArithmeticTests {
	public static final double EPSILON = 0.0000001;
}
