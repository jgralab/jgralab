package de.uni_koblenz.jgralabtest.greql.funlib.collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.pcollections.PMap;

import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.greql.funlib.FunLib;

public class ContainsKeyTest {
	private PMap<Integer, String> map;

	@Before
	public void setUp() {
		map = JGraLab.map();
		map = map.plus(42, "Hugo");
		map = map.plus(16, "Foo");
		map = map.plus(-1, "Bar");
		map = map.plus(0, "Nichts");
		map = map.plus(Integer.MIN_VALUE, "Hugo");
	}

	@Test
	public void testMap() {
		assertTrue((Boolean) FunLib.apply("containsKey", map, 16));
		assertTrue((Boolean) FunLib.apply("containsKey", map, 42));
		assertTrue((Boolean) FunLib.apply("containsKey", map, -1));
		assertTrue((Boolean) FunLib.apply("containsKey", map, 0));
		assertTrue((Boolean) FunLib
				.apply("containsKey", map, Integer.MIN_VALUE));
		assertFalse((Boolean) FunLib.apply("containsKey", map, 1));
		assertFalse((Boolean) FunLib.apply("containsKey", map, 17));
		assertFalse((Boolean) FunLib.apply("containsKey", map, 40));
		assertFalse((Boolean) FunLib.apply("containsKey", map, -2));
		assertFalse((Boolean) FunLib.apply("containsKey", map,
				Integer.MAX_VALUE));

	}

}