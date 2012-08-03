package de.uni_koblenz.jgralabtest.greql.funlib.collections;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.pcollections.PMap;

import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.greql.funlib.FunLib;

public class ContainsValueTest {
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
		assertTrue((Boolean) FunLib.apply("containsValue", map, "Hugo"));
		assertTrue((Boolean) FunLib.apply("containsValue", map, "Foo"));
		assertTrue((Boolean) FunLib.apply("containsValue", map, "Bar"));
		assertTrue((Boolean) FunLib.apply("containsValue", map, "Nichts"));
		assertFalse((Boolean) FunLib.apply("containsValue", map, ""));
		assertFalse((Boolean) FunLib.apply("containsValue", map, "Nothing"));
		assertFalse((Boolean) FunLib.apply("containsValue", map, "Fooo"));
		assertFalse((Boolean) FunLib.apply("containsValue", map, "Volker"));

	}

}
