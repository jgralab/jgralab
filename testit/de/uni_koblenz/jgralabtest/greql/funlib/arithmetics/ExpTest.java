package de.uni_koblenz.jgralabtest.greql.funlib.arithmetics;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import de.uni_koblenz.jgralab.greql.funlib.artithmetics.Exp;

public class ExpTest extends ArithmeticTest {

	private Exp exp;

	@Before
	public void setUp() {
		exp = new Exp();
	}

	@Test
	public void testInt() {
		for (int i = 0; i < intValues.length; i++) {
			assertEquals(Math.exp(intValues[i]), exp.evaluate(intValues[i]),
					RunArithmeticTests.EPSILON);
		}
	}

	@Test
	public void testLong() {
		for (int i = 0; i < longValues.length; i++) {
			assertEquals(Math.exp(longValues[i]), exp.evaluate(longValues[i]),
					RunArithmeticTests.EPSILON);
		}
	}

	@Test
	public void testDouble() {
		for (int i = 0; i < doubleValues.length; i++) {
			assertEquals(Math.exp(doubleValues[i]),
					exp.evaluate(doubleValues[i]), RunArithmeticTests.EPSILON);
		}
	}
}
