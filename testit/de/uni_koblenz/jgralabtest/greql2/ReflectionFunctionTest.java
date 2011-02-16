package de.uni_koblenz.jgralabtest.greql2;

import org.junit.Test;

public class ReflectionFunctionTest extends GenericTests {

	@Test
	public void testHasJValueType() throws Exception {
		assertQueryEquals("hasJValueType(false, 'BOOL')", true);
		assertQueryEquals("hasJValueType(true, 'BOOL')", true);
		assertQueryEquals("hasJValueType(1, 'BOOL')", false);
		assertQueryEquals("hasJValueType(1, 'INT')", true);
		// assertQueryEquals("hasJValueType(12L, 'INT')", false);
		// assertQueryEquals("hasJValueType(21L, 'LONG')", true);
		assertQueryEquals("hasJValueType(1.9, 'DOUBLE')", true);
		assertQueryEquals("hasJValueType(1, 'NUMBER')", true);
		// assertQueryEquals("hasJValueType(1L, 'NUMBER')", true);
		assertQueryEquals("hasJValueType(1.0, 'NUMBER')", true);

		assertQueryEquals("hasJValueType(1.0, 'STRING')", false);
		assertQueryEquals("hasJValueType('1.0', 'STRING')", true);
		assertQueryEquals("hasJValueType(1.0, 'NUMBER')", true);
		assertQueryEquals("hasJValueType(1.0, 'NUMBER')", true);
		assertQueryEquals("hasJValueType(1.0, 'NUMBER')", true);
		assertQueryEquals("hasJValueType(1.0, 'NUMBER')", true);
	}
}
