package de.uni_koblenz.jgralabtest.greql.funlib.relations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.uni_koblenz.jgralab.greql.funlib.FunLib;

public class NequalsTest extends RelationsTest {

	@Test
	public void testInteger() {
		for (int i = 0; i < intValues.length; i++) {
			int first = intValues[i];
			for (int j = 0; j < intValues.length; j++) {
				int second = intValues[j];
				boolean expected = first != second;
				Object result = FunLib.apply("nequals", first, second);
				assertTrue(result instanceof Boolean);
				assertEquals(expected, result);
			}
			for (int j = 0; j < longValues.length; j++) {
				long second = longValues[j];
				boolean expected = first != second;
				Object result = FunLib.apply("nequals", first, second);
				assertTrue(result instanceof Boolean);
				assertEquals(expected, result);
			}
			for (int j = 0; j < doubleValues.length; j++) {
				double second = doubleValues[j];
				boolean expected = first != second;
				Object result = FunLib.apply("nequals", first, second);
				assertTrue(result instanceof Boolean);
				assertEquals(expected, result);
			}
		}
	}

	@Test
	public void testLong() {
		for (int i = 0; i < longValues.length; i++) {
			long first = longValues[i];
			for (int j = 0; j < intValues.length; j++) {
				int second = intValues[j];
				boolean expected = first != second;
				Object result = FunLib.apply("nequals", first, second);
				assertTrue(result instanceof Boolean);
				assertEquals(expected, result);
			}
			for (int j = 0; j < longValues.length; j++) {
				long second = longValues[j];
				boolean expected = first != second;
				Object result = FunLib.apply("nequals", first, second);
				assertTrue(result instanceof Boolean);
				assertEquals(expected, result);
			}
			for (int j = 0; j < doubleValues.length; j++) {
				double second = doubleValues[j];
				boolean expected = first != second;
				Object result = FunLib.apply("nequals", first, second);
				assertTrue(result instanceof Boolean);
				assertEquals(expected, result);
			}
		}
	}

	@Test
	public void testDouble() {
		for (int i = 0; i < doubleValues.length; i++) {
			double first = doubleValues[i];
			for (int j = 0; j < intValues.length; j++) {
				int second = intValues[j];
				boolean expected = first != second;
				Object result = FunLib.apply("nequals", first, second);
				assertTrue(result instanceof Boolean);
				assertEquals(expected, result);
			}
			for (int j = 0; j < longValues.length; j++) {
				long second = longValues[j];
				boolean expected = first != second;
				Object result = FunLib.apply("nequals", first, second);
				assertTrue(result instanceof Boolean);
				assertEquals(expected, result);
			}
			for (int j = 0; j < doubleValues.length; j++) {
				double second = doubleValues[j];
				boolean expected = first != second;
				Object result = FunLib.apply("nequals", first, second);
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
				boolean expected = !first.equals(second);
				Object result = FunLib.apply("nequals", first, second);
				assertTrue(result instanceof Boolean);
				assertEquals(expected, result);
			}
			for (int j = 0; j < enumValues.length; j++) {
				// special case
				Enum<?> second = enumValues[j];
				String secondAsString = second.toString();
				boolean expected = !first.equals(secondAsString);
				Object result = FunLib.apply("nequals", first, second);
				assertTrue(result instanceof Boolean);
				assertEquals(expected, result);
			}
		}
	}

	@Test
	public void testEnum() {
		for (int i = 0; i < enumValues.length; i++) {
			Enum<?> first = enumValues[i];
			for (int j = 0; j < objectValues.length; j++) {
				String second = objectValues[j];
				// special case
				boolean expected = !first.toString().equals(second);
				Object result = FunLib.apply("nequals", first, second);
				assertTrue(result instanceof Boolean);
				assertEquals(expected, result);
			}
			for (int j = 0; j < enumValues.length; j++) {
				Enum<?> second = enumValues[j];
				boolean expected = !first.equals(second);
				Object result = FunLib.apply("nequals", first, second);
				assertTrue(result instanceof Boolean);
				assertEquals(expected, result);
			}
		}
	}
}
