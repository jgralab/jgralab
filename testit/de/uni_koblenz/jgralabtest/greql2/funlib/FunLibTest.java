package de.uni_koblenz.jgralabtest.greql2.funlib;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.uni_koblenz.jgralab.greql2.funlib.FunLib;

public class FunLibTest {
	enum Color {
		RED, GREEN, BLUE
	};

	@Test
	public void testEquals() {
		assertTrue((Boolean) FunLib.apply("equals", Color.RED, "RED"));
		assertFalse((Boolean) FunLib.apply("equals", Color.GREEN, "RED"));
		assertTrue((Boolean) FunLib.apply("equals", "RED", Color.RED));
	}

	@Test
	public void testNequals() {
		assertFalse((Boolean) FunLib.apply("nequals", Color.RED, "RED"));
		assertTrue((Boolean) FunLib.apply("nequals", Color.GREEN, "RED"));
		assertFalse((Boolean) FunLib.apply("nequals", "RED", Color.RED));
	}

}
