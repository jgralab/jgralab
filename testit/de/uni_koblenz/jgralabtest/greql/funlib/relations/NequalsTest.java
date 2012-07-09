package de.uni_koblenz.jgralabtest.greql.funlib.relations;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import de.uni_koblenz.jgralab.greql.funlib.relations.Nequals;

public class NequalsTest extends RelationsTest {

	private Nequals nequals;

	@Before
	public void setUp() {
		nequals = new Nequals();
	}

	@Test
	public void testInteger() {
		for (int i = 0; i < intValues.length; i++) {
			int first = intValues[i];
			for (int j = 0; j < intValues.length; j++) {
				int second = intValues[j];
				boolean expected = first != second;
				Boolean result = nequals.evaluate(first, second);
				assertEquals(expected, result);
			}
			for (int j = 0; j < longValues.length; j++) {
				long second = longValues[j];
				boolean expected = first != second;
				Boolean result = nequals.evaluate(first, second);
				assertEquals(expected, result);
			}
			for (int j = 0; j < doubleValues.length; j++) {
				double second = doubleValues[j];
				boolean expected = first != second;
				Boolean result = nequals.evaluate(first, second);
				assertEquals(expected, result);
			}
			for (int j = 0; j < objectValues.length; j++) {
				String second = objectValues[j];
				boolean expected = !Integer.valueOf(first).equals(second);
				Boolean result = nequals.evaluate(first, second);
				assertEquals(expected, result);
			}
			for (int j = 0; j < enumValues.length; j++) {
				Enum<?> second = enumValues[j];
				boolean expected = !Integer.valueOf(first).equals(second);
				Boolean result = nequals.evaluate(first, second);
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
				Boolean result = nequals.evaluate(first, second);
				assertEquals(expected, result);
			}
			for (int j = 0; j < longValues.length; j++) {
				long second = longValues[j];
				boolean expected = first != second;
				Boolean result = nequals.evaluate(first, second);
				assertEquals(expected, result);
			}
			for (int j = 0; j < doubleValues.length; j++) {
				double second = doubleValues[j];
				boolean expected = first != second;
				Boolean result = nequals.evaluate(first, second);
				assertEquals(expected, result);
			}
			for (int j = 0; j < objectValues.length; j++) {
				String second = objectValues[j];
				boolean expected = !Long.valueOf(first).equals(second);
				Boolean result = nequals.evaluate(first, second);
				assertEquals(expected, result);
			}
			for (int j = 0; j < enumValues.length; j++) {
				Enum<?> second = enumValues[j];
				boolean expected = !Long.valueOf(first).equals(second);
				Boolean result = nequals.evaluate(first, second);
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
				Boolean result = nequals.evaluate(first, second);
				assertEquals(expected, result);
			}
			for (int j = 0; j < longValues.length; j++) {
				long second = longValues[j];
				boolean expected = first != second;
				Boolean result = nequals.evaluate(first, second);
				assertEquals(expected, result);
			}
			for (int j = 0; j < doubleValues.length; j++) {
				double second = doubleValues[j];
				boolean expected = first != second;
				Boolean result = nequals.evaluate(first, second);
				assertEquals(expected, result);
			}
			for (int j = 0; j < objectValues.length; j++) {
				String second = objectValues[j];
				boolean expected = !Double.valueOf(first).equals(second);
				Boolean result = nequals.evaluate(first, second);
				assertEquals(expected, result);
			}
			for (int j = 0; j < enumValues.length; j++) {
				Enum<?> second = enumValues[j];
				boolean expected = !Double.valueOf(first).equals(second);
				Boolean result = nequals.evaluate(first, second);
				assertEquals(expected, result);
			}
		}
	}

	@Test
	public void testObject() {
		for (int i = 0; i < objectValues.length; i++) {
			String first = objectValues[i];
			for (int j = 0; j < intValues.length; j++) {
				int second = intValues[j];
				boolean expected = !first.equals(second);
				Boolean result = nequals.evaluate(first, second);
				assertEquals(expected, result);
			}
			for (int j = 0; j < longValues.length; j++) {
				long second = longValues[j];
				boolean expected = !first.equals(second);
				Boolean result = nequals.evaluate(first, second);
				assertEquals(expected, result);
			}
			for (int j = 0; j < doubleValues.length; j++) {
				double second = doubleValues[j];
				boolean expected = !first.equals(second);
				Boolean result = nequals.evaluate(first, second);
				assertEquals(expected, result);
			}
			for (int j = 0; j < objectValues.length; j++) {
				String second = objectValues[j];
				boolean expected = !first.equals(second);
				Boolean result = nequals.evaluate(first, second);
				assertEquals(expected, result);
			}
			for (int j = 0; j < enumValues.length; j++) {
				// special case
				String second = enumValues[j].toString();
				boolean expected = !first.equals(second);
				Boolean result = nequals.evaluate(first, second);
				assertEquals(expected, result);
			}
		}
	}

	@Test
	public void testEnum() {
		for (int i = 0; i < enumValues.length; i++) {
			Enum<?> first = enumValues[i];
			for (int j = 0; j < intValues.length; j++) {
				int second = intValues[j];
				boolean expected = !first.equals(second);
				Boolean result = nequals.evaluate(first, second);
				assertEquals(expected, result);
			}
			for (int j = 0; j < longValues.length; j++) {
				long second = longValues[j];
				boolean expected = !first.equals(second);
				Boolean result = nequals.evaluate(first, second);
				assertEquals(expected, result);
			}
			for (int j = 0; j < doubleValues.length; j++) {
				double second = doubleValues[j];
				boolean expected = !first.equals(second);
				Boolean result = nequals.evaluate(first, second);
				assertEquals(expected, result);
			}
			for (int j = 0; j < objectValues.length; j++) {
				String second = objectValues[j];
				// special case
				boolean expected = !first.toString().equals(second);
				Boolean result = nequals.evaluate(first, second);
				assertEquals(expected, result);
			}
			for (int j = 0; j < enumValues.length; j++) {
				Enum<?> second = enumValues[j];
				boolean expected = !first.equals(second);
				Boolean result = nequals.evaluate(first, second);
				assertEquals(expected, result);
			}
		}
	}
}
