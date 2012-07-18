package de.uni_koblenz.jgralabtest.greql.funlib.relations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.uni_koblenz.jgralab.greql.funlib.FunLib;

public class LeEqualTest extends RelationsTest {

	private Directions[] enumValues1 = Directions.values();
	private Temperatures[] enumValues2 = Temperatures.values();

	@Test
	public void testInteger() {
		for (int i = 0; i < intValues.length; i++) {
			int first = intValues[i];
			for (int j = 0; j < intValues.length; j++) {
				int second = intValues[j];
				boolean expected = first <= second;
				Object result = FunLib.apply("leEqual", first, second);
				assertTrue(result instanceof Boolean);
				assertEquals(expected, result);
			}

			for (int j = 0; j < longValues.length; j++) {
				long second = longValues[j];
				boolean expected = first <= second;
				Object result = FunLib.apply("leEqual", first, second);
				assertTrue(result instanceof Boolean);
				assertEquals(expected, result);
			}

			for (int j = 0; j < doubleValues.length; j++) {
				double second = doubleValues[j];
				boolean expected = Double.compare(first, second) <= 0;
				Object result = FunLib.apply("leEqual", first, second);
				assertTrue(result instanceof Boolean);
				assertEquals(expected, result);
			}
		}
	}

	@Test
	public void testLong() {
		for (int i = 0; i < longValues.length; i++) {
			long first = longValues[i];

			for (int j = 0; j < longValues.length; j++) {
				long second = longValues[j];
				boolean expected = first <= second;
				Object result = FunLib.apply("leEqual", first, second);
				assertTrue(result instanceof Boolean);
				assertEquals(expected, result);
			}

			for (int j = 0; j < intValues.length; j++) {
				int second = intValues[j];
				boolean expected = first <= second;
				Object result = FunLib.apply("leEqual", first, second);
				assertTrue(result instanceof Boolean);
				assertEquals(expected, result);
			}

			for (int j = 0; j < doubleValues.length; j++) {
				double second = doubleValues[j];
				boolean expected = Double.compare(first, second) <= 0;
				Object result = FunLib.apply("leEqual", first, second);
				assertTrue(result instanceof Boolean);
				assertEquals("Expected " + first + " <= " + second + " = "
						+ expected + ", but was " + result, expected, result);
			}

		}
	}

	@Test
	public void testDouble() {
		for (int i = 0; i < doubleValues.length; i++) {
			double first = doubleValues[i];
			for (int j = 0; j < doubleValues.length; j++) {
				double second = doubleValues[j];
				boolean expected = Double.compare(first, second) <= 0;
				Object result = FunLib.apply("leEqual", first, second);
				assertTrue(result instanceof Boolean);
				assertEquals("Expected " + first + " <= " + second + " = "
						+ expected + ", but was " + result, expected, result);
			}

			for (int j = 0; j < intValues.length; j++) {
				int second = intValues[j];
				boolean expected = first <= second;
				Object result = FunLib.apply("leEqual", first, second);
				assertTrue(result instanceof Boolean);
				assertEquals(expected, result);
			}

			for (int j = 0; j < longValues.length; j++) {
				long second = longValues[j];
				boolean expected = first <= second;
				Object result = FunLib.apply("leEqual", first, second);
				assertTrue(result instanceof Boolean);
				assertEquals(expected, result);
			}

		}
	}

	@Test
	public void testObject() {
		for (int i = 0; i < objectValues.length; i++) {
			String first = objectValues[i];
			for (int j = 0; j < objectValues.length; j++) {
				String second = objectValues[j];
				boolean expected = first.compareTo(second) <= 0;
				Object result = FunLib.apply("leEqual", first, second);
				assertTrue(result instanceof Boolean);
				assertEquals(expected, result);
			}
		}
	}

	@Test
	public void testEnum() {
		for (int i = 0; i < enumValues1.length; i++) {
			Directions first = enumValues1[i];
			for (int j = 0; j < enumValues1.length; j++) {
				Directions second = enumValues1[j];
				boolean expected = first.compareTo(second) <= 0;
				Object result = FunLib.apply("leEqual", first, second);
				assertTrue(result instanceof Boolean);
				assertEquals(expected, result);
			}
		}

		for (int i = 0; i < enumValues2.length; i++) {
			Temperatures first = enumValues2[i];
			for (int j = 0; j < enumValues2.length; j++) {
				Temperatures second = enumValues2[j];
				boolean expected = first.compareTo(second) <= 0;
				Object result = FunLib.apply("leEqual", first, second);
				assertTrue(result instanceof Boolean);
				assertEquals(expected, result);
			}
		}
	}
}
