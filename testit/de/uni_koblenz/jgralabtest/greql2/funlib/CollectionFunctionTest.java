/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2010 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 * 
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 3 of the License, or (at your
 * option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see <http://www.gnu.org/licenses>.
 * 
 * Additional permission under GNU GPL version 3 section 7
 * 
 * If you modify this Program, or any covered work, by linking or combining
 * it with Eclipse (or a modified version of that program or an Eclipse
 * plugin), containing parts covered by the terms of the Eclipse Public
 * License (EPL), the licensors of this Program grant you additional
 * permission to convey the resulting work.  Corresponding Source for a
 * non-source form of such a combination shall include the source code for
 * the parts of JGraLab used as well as that of the covered work.
 */
package de.uni_koblenz.jgralabtest.greql2.funlib;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Map.Entry;

import org.junit.Test;

import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueBoolean;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueImpl;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueList;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueMap;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueSet;
import de.uni_koblenz.jgralabtest.greql2.GenericTests;

public class CollectionFunctionTest extends GenericTests {

	@Test
	public void testAvg() throws Exception {
		assertQueryEquals("let x:= list (5..13) in avg(x)", 9.0);
		assertQueryEquals("let x:= list (3) in avg(x)", 3.0);
		assertQueryEquals("let x:= list () in avg(x)", 0.0);
		assertQueryEquals("let x:= list (3, 100) in avg(x)", 51.5);
		assertQueryEquals("let x:= list (0..10000) in avg(x)", 5000.0);
		assertQueryEquals("let x:= list (-100..100) in avg(x)", 0.0);
		assertQueryEquals("let x:= list (5, -5, 0) in avg(x)", 0.0);
	}

	@Test
	public void testConcatInfix() throws Exception {
		assertQueryEqualsQuery("list(1..3) ++ list(4..6)", "list(1..6)");
		assertQueryEqualsQuery("list(1..2) ++ list(5..6)", "list(1,2,5,6)");
		assertQueryEqualsQuery("list(1,23,3) ++ list(5,2,5)",
				"list(1,23,3,5,2,5)");
		assertQueryEqualsQuery("list() ++ list(5,2,5)", "list(5,2,5)");
		assertQueryEqualsQuery("list(1) ++ list(5,2,5)", "list(1,5,2,5)");
		assertQueryEqualsQuery("list(1,23,3) ++ list()", "list(1,23,3)");
		assertQueryEqualsQuery("list(1,23,3) ++ list(5)", "list(1,23,3,5,)");
	}

	@Test
	public void testConcat() throws Exception {
		assertQueryEqualsQuery("concat(list(1..3), list(4..6))", "list(1..6)");
		assertQueryEqualsQuery("concat(list(1..2), list(5..6))",
				"list(1,2,5,6)");
		assertQueryEqualsQuery("concat(list(1,23,3), list(5,2,5))",
				"list(1,23,3,5,2,5)");
		assertQueryEqualsQuery("concat(list(), list(5,2,5))", "list(5,2,5)");
		assertQueryEqualsQuery("concat(list(1), list(5,2,5))", "list(1,5,2,5)");
		assertQueryEqualsQuery("concat(list(1,23,3), list())", "list(1,23,3)");
		assertQueryEqualsQuery("concat(list(1,23,3), list(5))",
				"list(1,23,3,5,)");
	}

	@Test
	public void testContains() throws Exception {
		String queryString = "let x:= list (5..13) in contains(x, 7)";
		JValue result = evalTestQuery("ContainsTrue", queryString);
		assertEquals(true, (boolean) result.toBoolean());
	}

	@Test
	public void testContains2() throws Exception {
		String queryString = "let x:= list (5..13) in contains(x, 56)";
		JValue result = evalTestQuery("ContainsFalse", queryString);
		assertEquals(false, (boolean) result.toBoolean());
	}

	@Test
	public void testContains3() throws Exception {
		String queryString = "let x:= list (5..13) in contains(x, 13)";
		JValue result = evalTestQuery("ContainsTrue2", queryString);
		assertEquals(true, (boolean) result.toBoolean());
	}

	@Test
	public void testContains4() throws Exception {
		String queryString = "from v : V reportSet contains(eSubgraph{Link}, v) end";
		JValue result = evalTestQuery("Contains", queryString,
				getCyclicTestGraph());
		for (JValue v : result.toCollection()) {
			assertEquals(JValueBoolean.getTrueValue(), v.toBoolean());
		}
	}

	@Test
	public void testContains5() throws Exception {
		String queryString = "from v : V "
				+ "           in eSubgraph{Link}      "
				+ "           reportSet isIn(v) end";
		try {
			evalTestQuery("Contains5", queryString, getCyclicTestGraph());
			fail();
		} catch (EvaluateException e) {
			// an eval exception is expected here
		}
	}

	@Test
	public void testContainsKey1() throws Exception {
		JValueMap map = new JValueMap();
		map.put(new JValueImpl(1), new JValueImpl("a string"));
		setBoundVariable("emap", map);
		String queryString = "using emap: containsKey(emap, 1)";
		JValue result = evalTestQuery("ContainsKey1", queryString);
		assertTrue(result.toBoolean());
	}

	@Test
	public void testContainsKey2() throws Exception {
		JValueMap map = new JValueMap();
		map.put(new JValueImpl(1), new JValueImpl("a string"));
		setBoundVariable("emap", map);
		String queryString = "using emap: containsKey(emap, 2)";
		JValue result = evalTestQuery("ContainsKey2", queryString);
		assertFalse(result.toBoolean());
	}

	@Test
	public void testContainsValue1() throws Exception {
		JValueMap map = new JValueMap();
		map.put(new JValueImpl(1), new JValueImpl("a string"));
		setBoundVariable("emap", map);
		String queryString = "using emap: containsValue(emap, \"a string\")";
		JValue result = evalTestQuery("ContainsValue1", queryString);
		assertTrue(result.toBoolean());
	}

	@Test
	public void testContainsValue2() throws Exception {
		JValueMap map = new JValueMap();
		map.put(new JValueImpl(1), new JValueImpl("a string"));
		setBoundVariable("emap", map);
		String queryString = "using emap: containsValue(emap, 1)";
		JValue result = evalTestQuery("ContainsValue2", queryString);
		assertFalse(result.toBoolean());
	}

	@Test
	public void testContainsValue3() throws Exception {
		JValueMap map = new JValueMap();
		map.put(new JValueImpl(1), new JValueImpl("a string"));
		setBoundVariable("emap", map);
		String queryString = "using emap: containsValue(emap, \"string\")";
		JValue result = evalTestQuery("ContainsValue3", queryString);
		assertFalse(result.toBoolean());
	}

	@Test
	public void testCount() throws Exception {
		String queryString = "let x:= list (5..13) in count(x)";
		JValue result = evalTestQuery("Count", queryString);
		assertEquals(9, (int) result.toInteger());
	}

	@Test
	public void testCount2() throws Exception {
		String queryString = "let x:= 17 in count(x)";
		JValue result = evalTestQuery("Count", queryString);
		assertEquals(1, (int) result.toInteger());
	}

	@Test
	public void testDifference() throws Exception {
		String queryString = "let x:= set(5, 7, 9, 13), y := set(5,6,7,8) in difference(x, y)";
		JValue result = evalTestQuery("Difference", queryString);
		assertEquals(2, result.toCollection().size());
	}

	@Test
	public void testDifference2() throws Exception {
		String queryString = "let x:= set(5, 7, 9, 13), y := list(5,5,6,7,8) in difference(x, y)";
		JValue result = evalTestQuery("Difference", queryString);
		assertEquals(2, result.toCollection().size());
	}

	@Test
	public void testGet() throws Exception {
		String queryString = "let m := map(1 -> \"One\",   2 -> \"Two\",  "
				+ "                        3 -> \"Three\", 4 -> \"Four\", "
				+ "                        5 -> \"Five\",  6 -> \"Six\")  "
				+ "           in                                          "
				+ "           from x : keySet(m) "
				+ "           reportSet get(m, x) end";
		JValue result = evalTestQuery("Get", queryString);
		JValueSet set = result.toJValueSet();
		assertEquals(6, set.size());
		assertTrue(set.contains(new JValueImpl("One")));
		assertTrue(set.contains(new JValueImpl("Two")));
		assertTrue(set.contains(new JValueImpl("Three")));
		assertTrue(set.contains(new JValueImpl("Four")));
		assertTrue(set.contains(new JValueImpl("Five")));
		assertTrue(set.contains(new JValueImpl("Six")));
	}

	@Test
	public void testGet2() throws Exception {
		String queryString = "let m := map(1 -> \"One\",   2 -> \"Two\",  "
				+ "                        3 -> \"Three\", 4 -> \"Four\", "
				+ "                        5 -> \"Five\",  6 -> \"Six\")  "
				+ "           in                                          "
				+ "           from x : keySet(m) "
				+ "           reportSet m[x] end";
		JValue result = evalTestQuery("Get", queryString);
		JValueSet set = result.toJValueSet();
		assertEquals(6, set.size());
		assertTrue(set.contains(new JValueImpl("One")));
		assertTrue(set.contains(new JValueImpl("Two")));
		assertTrue(set.contains(new JValueImpl("Three")));
		assertTrue(set.contains(new JValueImpl("Four")));
		assertTrue(set.contains(new JValueImpl("Five")));
		assertTrue(set.contains(new JValueImpl("Six")));
	}

	@Test
	public void testIntersection() throws Exception {
		String queryString = "let x:= set(5, 7, 9, 13), y := set(5,6,7,8) in intersection(x, y)";
		JValue result = evalTestQuery("Intersection", queryString);
		for (JValue j : result.toCollection()) {
			System.out.println("Element:" + j);
		}
		assertEquals(2, result.toCollection().size());
	}

	@Test
	public void testIsEmpty1() throws Exception {
		JValueSet set1 = new JValueSet();
		set1.add(new JValueImpl(1));
		set1.add(new JValueImpl(2));
		set1.add(new JValueImpl(3));
		setBoundVariable("cset", set1);
		String queryString = "using cset: isEmpty(cset)";
		JValue result = evalTestQuery("IsEmpty1", queryString);
		assertEquals(false, result.toBoolean());
	}

	@Test
	public void testIsEmpty2() throws Exception {
		JValueSet set1 = new JValueSet();
		setBoundVariable("cset", set1);
		String queryString = "using cset: isEmpty(cset)";
		JValue result = evalTestQuery("IsEmpty2", queryString);
		assertEquals(true, result.toBoolean());
	}

	@Test
	public void testIsEmpty3() throws Exception {
		JValueMap map1 = new JValueMap();
		setBoundVariable("cset", map1);
		String queryString = "using cset: isEmpty(cset)";
		JValue result = evalTestQuery("IsEmpty3", queryString);
		assertEquals(true, result.toBoolean());
	}

	@Test
	public void testIsSubSet1() throws Exception {
		String queryString = "let x:= set(5, 7, 9, 13), y := set(5,6,7,8) in isSubSet(x,y)";
		JValue result = evalTestQuery("IsSubset", queryString);
		assertEquals(false, (boolean) result.toBoolean());
	}

	@Test
	public void testIsSubSet2() throws Exception {
		String queryString = "let x:= set(5, 7, 9, 13), y := set(5,7) in isSubSet(x, y)";
		JValue result = evalTestQuery("IsSubset2", queryString);
		assertEquals(false, (boolean) result.toBoolean());
	}

	@Test
	public void testIsSubSet3() throws Exception {
		String queryString = "let x:= set(5, 7, 9, 13), y := set(5,7) in isSubSet(y, x)";
		JValue result = evalTestQuery("IsSubset3", queryString);
		assertEquals(true, (boolean) result.toBoolean());
	}

	@Test
	public void testIsSuperSet1() throws Exception {
		String queryString = "let x:= set(5, 7, 9, 13), y := set(1, 5,7) in isSuperSet(x, y)";
		JValue result = evalTestQuery("IsSuperset1", queryString);
		assertEquals(false, (boolean) result.toBoolean());
	}

	@Test
	public void testIsSuperSet2() throws Exception {
		String queryString = "let x:= set(5, 7, 9, 13), y := set(5,7) in isSuperSet(x, y)";
		JValue result = evalTestQuery("IsSuperset2", queryString);
		assertEquals(true, (boolean) result.toBoolean());
	}

	@Test
	public void testIsSuperSet3() throws Exception {
		String queryString = "let x:= set(5, 7, 9, 13), y := set(5,7) in isSuperSet(y, x)";
		JValue result = evalTestQuery("IsSuperset3", queryString);
		assertEquals(false, (boolean) result.toBoolean());
	}

	@Test
	public void testIsSuperSet4() throws Exception {
		String queryString = "let x:= set(5, 7, 9, 13), y := set(5, 7, 13, 9) in isSuperSet(x, y)";
		JValue result = evalTestQuery("IsSuperset4", queryString);
		assertEquals(true, (boolean) result.toBoolean());
	}

	@Test
	public void testKeySet() throws Exception {
		String queryString = "from x : keySet(map(1 -> \"One\",   2 -> \"Two\", "
				+ "                               3 -> \"Three\", 4 -> \"Four\", "
				+ "                               5 -> \"Five\",  6 -> \"Six\")) "
				+ "           reportSet x end";
		JValue result = evalTestQuery("KeySet", queryString);
		JValueSet set = result.toJValueSet();
		assertEquals(6, set.size());
		assertTrue(set.contains(new JValueImpl(1)));
		assertTrue(set.contains(new JValueImpl(2)));
		assertTrue(set.contains(new JValueImpl(3)));
		assertTrue(set.contains(new JValueImpl(4)));
		assertTrue(set.contains(new JValueImpl(5)));
		assertTrue(set.contains(new JValueImpl(6)));
	}

	@Test
	public void testMergeMaps1() throws Exception {
		// merging equal maps should return an equal map
		assertEquals(
				evalTestQuery("expected MergeMaps",
						"map(tup(1,2) -> set(3), tup(3,4) -> set(7))"),
				evalTestQuery(
						"MergeMaps",
						"mergeMaps(map(tup(1,2) -> set(3), tup(3,4) -> set(7)), map(tup(1,2) -> set(3), tup(3,4) -> set(7)))"));
	}

	@Test
	public void testMergeMaps2() throws Exception {
		assertEquals(
				evalTestQuery("expected MergeMaps",
						"map(tup(1,2) -> set(3,4), tup(3,4) -> set(7,8,9))"),
				evalTestQuery(
						"MergeMaps",
						"mergeMaps(map(tup(1,2) -> set(3), tup(3,4) -> set(7)), map(tup(1,2) -> set(4), tup(3,4) -> set(8,9)))"));
	}

	@Test
	public void testPos() throws Exception {
		String queryString = "let x:= list (5..13) in pos(x, 7)";
		JValue result = evalTestQuery("Pos", queryString);
		assertEquals(2, (int) result.toInteger());
	}

	@Test
	public void testPos2() throws Exception {
		String queryString = "let x:= list (5..13) in pos(x, 2)";
		JValue result = evalTestQuery("Pos2", queryString);
		assertEquals(-1, (int) result.toInteger());
	}

	@Test
	public void testSortBag() throws Exception {
		String queryString = "sort(bag(4, 1, 2, 10, 9, 7, 8, 3, 5, 6))";
		JValueList result = evalTestQuery("Sort1", queryString).toJValueList();
		assertEquals(10, result.size());
		for (int i = 1; i <= 10; i++) {
			assertEquals(new JValueImpl(i), result.get(i - 1));
		}
	}

	@Test
	public void testSortList() throws Exception {
		String queryString = "sort(list(4, 1, 2, 10, 9, 7, 8, 3, 5, 6))";
		JValueList result = evalTestQuery("Sort1", queryString).toJValueList();
		assertEquals(10, result.size());
		for (int i = 1; i <= 10; i++) {
			assertEquals(new JValueImpl(i), result.get(i - 1));
		}
	}

	@Test
	public void testSortMap() throws Exception {
		String queryString = "sort(from i : list (1..10) reportMap i, i*i end)";
		JValueMap result = evalTestQuery("Sort2", queryString).toJValueMap();
		assertEquals(10, result.size());
		int i = 1;
		for (Entry<JValue, JValue> e : result.entrySet()) {
			assertEquals(new JValueImpl(i), e.getKey());
			assertEquals(new JValueImpl(i * i), e.getValue());
			i++;
		}
	}

	@Test
	public void testSortSet() throws Exception {
		String queryString = "sort(set(4, 1, 2, 10, 9, 7, 8, 3, 5, 6))";
		JValueList result = evalTestQuery("Sort1", queryString).toJValueList();
		assertEquals(10, result.size());
		for (int i = 1; i <= 10; i++) {
			assertEquals(new JValueImpl(i), result.get(i - 1));
		}
	}

	@Test
	public void testSum() throws Exception {
		String queryString = "let x:= list (5..13) in sum(x)";
		JValue result = evalTestQuery("Sum", queryString);
		assertEquals(81, (int) (double) result.toDouble());
	}

	@Test
	public void testSymDifference() throws Exception {
		String queryString = "let x:= set(5, 7, 9, 13), y := set(5,6,7,8) in symDifference(x, y)";
		JValue result = evalTestQuery("SymetricDifference", queryString);
		assertEquals(4, result.toCollection().size());
	}

	@Test
	public void testUnion1() throws Exception {
		String queryString = "let x:= set(5, 7, 9, 13), y := set(5,6,7,8) in union(x, y)";
		JValue result = evalTestQuery("Union1", queryString);
		assertEquals(6, result.toCollection().size());
	}

	@Test
	public void testUnion2() throws Exception {
		JValueMap map1 = new JValueMap();
		map1.put(new JValueImpl(1), new JValueImpl("A"));
		map1.put(new JValueImpl(2), new JValueImpl("A"));
		map1.put(new JValueImpl(3), new JValueImpl("B"));
		JValueMap map2 = new JValueMap();
		map2.put(new JValueImpl(4), new JValueImpl("A"));
		map2.put(new JValueImpl(5), new JValueImpl("C"));
		map2.put(new JValueImpl(6), new JValueImpl("D"));

		setBoundVariable("map1", map1);
		setBoundVariable("map2", map2);

		String queryString = "using map1, map2: union(map1, map2, true)";
		JValue result = evalTestQuery("Union2", queryString);
		assertEquals(6, result.toJValueMap().size());
		JValueMap rmap = result.toJValueMap();
		assertEquals(new JValueImpl("A"), rmap.get(new JValueImpl(1)));
		assertEquals(new JValueImpl("A"), rmap.get(new JValueImpl(2)));
		assertEquals(new JValueImpl("B"), rmap.get(new JValueImpl(3)));
		assertEquals(new JValueImpl("A"), rmap.get(new JValueImpl(4)));
		assertEquals(new JValueImpl("C"), rmap.get(new JValueImpl(5)));
		assertEquals(new JValueImpl("D"), rmap.get(new JValueImpl(6)));
	}

	@Test
	public void testUnion3() throws Exception {
		JValueMap map1 = new JValueMap();
		map1.put(new JValueImpl(1), new JValueImpl("A"));
		map1.put(new JValueImpl(2), new JValueImpl("A"));
		map1.put(new JValueImpl(3), new JValueImpl("B"));
		JValueMap map2 = new JValueMap();
		map2.put(new JValueImpl(1), new JValueImpl("A"));
		map2.put(new JValueImpl(3), new JValueImpl("C"));
		map2.put(new JValueImpl(4), new JValueImpl("D"));

		setBoundVariable("map1", map1);
		setBoundVariable("map2", map2);

		String queryString = "using map1, map2: union(map1, map2)";
		try {
			evalTestQuery("Union3", queryString);
			fail("Expected Exception on using union with two non-disjoint maps");
		} catch (Exception ex) {
			// :)
		}
	}

	@Test
	public void testUnion4() throws Exception {
		JValueSet set1 = new JValueSet();
		set1.add(new JValueImpl(1));
		set1.add(new JValueImpl(2));
		set1.add(new JValueImpl(3));

		JValueSet set2 = new JValueSet();
		set2.add(new JValueImpl(1));
		set2.add(new JValueImpl(2));
		set2.add(new JValueImpl(3));

		JValueSet set3 = new JValueSet();
		set3.add(new JValueImpl(3));
		set3.add(new JValueImpl(4));
		set3.add(new JValueImpl(5));

		JValueSet set4 = new JValueSet();
		set4.add(new JValueImpl(7));
		set4.add(new JValueImpl(8));
		set4.add(new JValueImpl(9));

		JValueSet cset = new JValueSet();
		cset.add(set1);
		cset.add(set2);
		cset.add(set3);
		cset.add(set4);

		setBoundVariable("cset", cset);

		String queryString = "using cset: union(cset)";
		JValue result = evalTestQuery("Union4", queryString);
		assertEquals(8, result.toJValueSet().size());
		JValueSet rset = result.toJValueSet();
		assertTrue(rset.contains(new JValueImpl(1)));
		assertTrue(rset.contains(new JValueImpl(2)));
		assertTrue(rset.contains(new JValueImpl(3)));
		assertTrue(rset.contains(new JValueImpl(4)));
		assertTrue(rset.contains(new JValueImpl(5)));
		assertTrue(rset.contains(new JValueImpl(7)));
		assertTrue(rset.contains(new JValueImpl(8)));
		assertTrue(rset.contains(new JValueImpl(9)));
	}

}
