package de.uni_koblenz.jgralabtest.greql.funlib.arithmetics;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import de.uni_koblenz.jgralab.greql.funlib.artithmetics.ToDouble;

public class ToDoubleTest extends UnaryFunctionTest {
	private ToDouble toDouble;

	@Before
	public void setUp() {
		toDouble = new ToDouble();
	}

	@Test
	public void testInt() {
		for (int i = 0; i < intValues.length; i++) {
			assertEquals(Integer.valueOf(intValues[i]).doubleValue(),
					toDouble.evaluate(intValues[i]), RunArithmeticTests.EPSILON);
		}
	}

	@Test
	public void testLong() {
		for (int i = 0; i < longValues.length; i++) {
			assertEquals(Long.valueOf(longValues[i]).doubleValue(),
					toDouble.evaluate(longValues[i]),
					RunArithmeticTests.EPSILON);
		}
	}

	@Test
	public void testDouble() {
		for (int i = 0; i < doubleValues.length; i++) {
			assertEquals(Double.valueOf(doubleValues[i]).doubleValue(),
					toDouble.evaluate(doubleValues[i]),
					RunArithmeticTests.EPSILON);
		}
	}
}
