package de.uni_koblenz.jgralabtest.algolib.functions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import de.uni_koblenz.jgralab.algolib.functions.ArrayBinaryDoubleFunction;
import de.uni_koblenz.jgralab.algolib.functions.ArrayBinaryFunction;
import de.uni_koblenz.jgralab.algolib.functions.ArrayBinaryIntFunction;
import de.uni_koblenz.jgralab.algolib.functions.ArrayBinaryLongFunction;
import de.uni_koblenz.jgralab.algolib.functions.ArrayRelation;
import de.uni_koblenz.jgralab.algolib.functions.IntFunction;
import de.uni_koblenz.jgralab.algolib.functions.adapters.MethodCallToIntFunctionAdapter;

public class ArrayBinaryFunctionsTest {

	private int[][] intData;
	private long[][] longData;
	private double[][] doubleData;
	private boolean[][] booleanData;
	private String[][] objectData;

	private IntFunction<String> mapping;

	private ArrayBinaryIntFunction<String> intFunction;
	private ArrayBinaryLongFunction<String> longFunction;
	private ArrayBinaryDoubleFunction<String> doubleFunction;
	private ArrayRelation<String> booleanFunction;
	private ArrayBinaryFunction<String, String> objectFunction;

	private String[] definedDomainElements;
	private String[] undefinedDomainElements;

	@Before
	public void setUp() {
		intData = new int[][] { { 0, 0, 0, 0 }, { 0, 1, 2, 2 },
				{ 0, 9, 23, -1 }, { 0, 34, 3, -16 } };
		longData = new long[][] { { 0l, 0l, 0l, 0l },
				{ 0l, 23l, 23908098l, 99l }, { 0l, -88098450943534l, 2l, 0l },
				{ 0l, 9809899934803943l, 98l, -1l } };
		doubleData = new double[][] { { 0.0, 0.0, 0.0, 0.0 },
				{ 0.0, 1.23990, Math.PI, Math.E },
				{ 0.0, -0.34, -1.111, 34.0 }, { 0.0, 89843.9, 9.0, -16.42 } };
		booleanData = new boolean[][] { { false, false, false, false },
				{ false, true, true, true }, { false, false, true, false },
				{ false, true, false, false } };
		objectData = new String[][] { { null, null, null, null },
				{ null, "eins", "zwei", "drei", "vier" },
				{ null, "zwei", "vier", "sechs", "acht" },
				{ null, "drei", "sechs", "neun", "zwoelf" } };

		definedDomainElements = new String[] { "eins", "zwei", "drei" };
		undefinedDomainElements = new String[] { "foo", "bar", "Hugo", "" };

		mapping = new MethodCallToIntFunctionAdapter<String>() {

			@Override
			public int get(String parameter) {
				if (parameter.equals("eins")) {
					return 1;
				}
				if (parameter.equals("zwei")) {
					return 2;
				}
				if (parameter.equals("drei")) {
					return 3;
				}
				return 0;
			}

			@Override
			public boolean isDefined(String parameter) {
				return parameter.equals("eins") || parameter.equals("zwei")
						|| parameter.equals("drei");
			}
		};
		intFunction = new ArrayBinaryIntFunction<>(intData, mapping);
		longFunction = new ArrayBinaryLongFunction<>(longData, mapping);
		doubleFunction = new ArrayBinaryDoubleFunction<>(doubleData, mapping);
		booleanFunction = new ArrayRelation<>(booleanData, mapping);
		objectFunction = new ArrayBinaryFunction<>(objectData, mapping);
	}

	@Test
	public void testIsDefined() {
		// defined
		for (String first : definedDomainElements) {
			for (String second : definedDomainElements) {
				assertTrue(intFunction.isDefined(first, second));
				assertTrue(longFunction.isDefined(first, second));
				assertTrue(doubleFunction.isDefined(first, second));
				assertTrue(booleanFunction.isDefined(first, second));
				assertTrue(objectFunction.isDefined(first, second));
			}
		}

		// undefined
		for (String first : undefinedDomainElements) {
			for (String second : undefinedDomainElements) {
				assertFalse(intFunction.isDefined(first, second));
				assertFalse(longFunction.isDefined(first, second));
				assertFalse(doubleFunction.isDefined(first, second));
				assertFalse(booleanFunction.isDefined(first, second));
				assertFalse(objectFunction.isDefined(first, second));
			}
		}

		// mixed, but still undefined
		for (String first : definedDomainElements) {
			for (String second : undefinedDomainElements) {
				assertFalse(intFunction.isDefined(first, second));
				assertFalse(longFunction.isDefined(first, second));
				assertFalse(doubleFunction.isDefined(first, second));
				assertFalse(booleanFunction.isDefined(first, second));
				assertFalse(objectFunction.isDefined(first, second));
			}
		}
		for (String first : undefinedDomainElements) {
			for (String second : definedDomainElements) {
				assertFalse(intFunction.isDefined(first, second));
				assertFalse(longFunction.isDefined(first, second));
				assertFalse(doubleFunction.isDefined(first, second));
				assertFalse(booleanFunction.isDefined(first, second));
				assertFalse(objectFunction.isDefined(first, second));
			}
		}
	}

	@Test
	public void testGet() {
		for (String first : definedDomainElements) {
			int i = mapping.get(first);
			for (String second : definedDomainElements) {
				int j = mapping.get(second);
				assertEquals(intData[i][j], intFunction.get(first, second));
				assertEquals(longData[i][j], longFunction.get(first, second));
				assertEquals(doubleData[i][j],
						doubleFunction.get(first, second), 0.00001);
				assertEquals(booleanData[i][j],
						booleanFunction.get(first, second));
				assertEquals(objectData[i][j],
						objectFunction.get(first, second));
			}
		}
	}

	@Test
	public void testSet() {
		try {
			intFunction.set("foo", "bar", 47);
			fail("This should not be allowed.");
		} catch (UnsupportedOperationException e) {
		}
		try {
			longFunction.set("foo", "bar", 47l);
			fail("This should not be allowed.");
		} catch (UnsupportedOperationException e) {
		}
		try {
			doubleFunction.set("foo", "bar", 47.42);
			fail("This should not be allowed.");
		} catch (UnsupportedOperationException e) {
		}
		try {
			booleanFunction.set("foo", "bar", true);
			fail("This should not be allowed.");
		} catch (UnsupportedOperationException e) {
		}
		try {
			objectFunction.set("foo", "bar", "Hugo");
			fail("This should not be allowed.");
		} catch (UnsupportedOperationException e) {
		}
	}
}
