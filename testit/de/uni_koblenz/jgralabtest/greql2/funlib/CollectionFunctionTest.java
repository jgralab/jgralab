/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2011 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 * 
 * For bug reports, documentation and further information, visit
 * 
 *                         http://jgralab.uni-koblenz.de
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

import static org.junit.Assert.fail;

import java.util.Arrays;

import org.junit.Test;

import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
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
	public void testConcat() throws Exception {
		assertQueryEqualsQuery("concat(list(1..3), list(4..6))", "list(1..6)");
		assertQueryEqualsQuery("concat(list(1..2), list(5..6))",
				"list(1,2,5,6)");
		assertQueryEqualsQuery("concat(list(1,23,3), list(5,2,5))",
				"list(1,23,3,5,2,5)");
		assertQueryEqualsQuery("concat(list(), list())", "list()");
		assertQueryEqualsQuery("concat(list(), list(5,2,5))", "list(5,2,5)");
		assertQueryEqualsQuery("concat(list(1), list(5,2,5))", "list(1,5,2,5)");
		assertQueryEqualsQuery("concat(list(1,23,3), list())", "list(1,23,3)");
		assertQueryEqualsQuery("concat(list(1,23,3), list(5))",
				"list(1,23,3,5)");
	}

	@Test
	public void testConcatInfix() throws Exception {
		assertQueryEqualsQuery("list(1..3) ++ list(4..6)", "list(1..6)");
		assertQueryEqualsQuery("list(1..2) ++ list(5..6)", "list(1,2,5,6)");
		assertQueryEqualsQuery("list(1,23,3) ++ list(5,2,5)",
				"list(1,23,3,5,2,5)");
		assertQueryEqualsQuery("list() ++ list()", "list()");
		assertQueryEqualsQuery("list() ++ list(5,2,5)", "list(5,2,5)");
		assertQueryEqualsQuery("list(1) ++ list(5,2,5)", "list(1,5,2,5)");
		assertQueryEqualsQuery("list(1,23,3) ++ list()", "list(1,23,3)");
		assertQueryEqualsQuery("list(1,23,3) ++ list(5)", "list(1,23,3,5)");
	}

	@Test
	public void testContainsBag() throws Exception {
		evalTestQuery("bag (5, 5, 5, 6, 7, 8, 9, 10, 11, 12, 13) store as x");
		assertQueryEquals("using x: contains(x, 7)", true);
		assertQueryEquals("using x: contains(x, 56)", false);
		assertQueryEquals("using x: contains(x, 13)", true);
		assertQueryEquals("using x: contains(x, 14)", false);
		assertQueryEquals("using x: contains(x, 5)", true);
		assertQueryEquals("using x: contains(x, 4)", false);

		evalTestQuery("bag (5) store as x");
		assertQueryEquals("using x: contains(x, 4)", false);
		assertQueryEquals("using x: contains(x, 5)", true);
		assertQueryEquals("using x: contains(x, 6)", false);

		evalTestQuery("bag () store as x");
		assertQueryEquals("using x: contains(x, 0)", false);
		assertQueryEquals("using x: contains(x, 5)", false);
		assertQueryEquals("using x: contains(x, 6)", false);
	}

	@Test
	public void testContainsKey() throws Exception {
		evalTestQuery("map() store as x");
		assertQueryEquals("using x: containsKey(x, 1)", false);
		assertQueryEquals("using x: containsKey(x, 2)", false);
		assertQueryEquals("using x: containsKey(x, 0)", false);

		evalTestQuery("map(1 -> 'a string' ) store as x");
		assertQueryEquals("using x: containsKey(x, 1)", true);
		assertQueryEquals("using x: containsKey(x, 2)", false);
		assertQueryEquals("using x: containsKey(x, 0)", false);

		evalTestQuery("map(1 -> 'a string', 2 -> 'another string', 1 -> '') store as x");
		assertQueryEquals("using x: containsKey(x, 1)", true);
		assertQueryEquals("using x: containsKey(x, 2)", true);
		assertQueryEquals("using x: containsKey(x, 3)", false);
		assertQueryEquals("using x: containsKey(x, 0)", false);
	}

	@Test
	public void testContainsList() throws Exception {
		evalTestQuery("list (5..13) store as x");
		assertQueryEquals("using x: contains(x, 7)", true);
		assertQueryEquals("using x: contains(x, 56)", false);
		assertQueryEquals("using x: contains(x, 13)", true);
		assertQueryEquals("using x: contains(x, 14)", false);
		assertQueryEquals("using x: contains(x, 5)", true);
		assertQueryEquals("using x: contains(x, 4)", false);

		evalTestQuery("list (5) store as x");
		assertQueryEquals("using x: contains(x, 4)", false);
		assertQueryEquals("using x: contains(x, 5)", true);
		assertQueryEquals("using x: contains(x, 6)", false);

		evalTestQuery("list () store as x");
		assertQueryEquals("using x: contains(x, 0)", false);
		assertQueryEquals("using x: contains(x, 5)", false);
		assertQueryEquals("using x: contains(x, 6)", false);
	}

	@Test
	public void testContainsSet() throws Exception {
		evalTestQuery("set(5, 5, 5, 6, 7, 8, 9, 10, 11, 12, 13) store as x");
		assertQueryEquals("using x: contains(x, 7)", true);
		assertQueryEquals("using x: contains(x, 56)", false);
		assertQueryEquals("using x: contains(x, 13)", true);
		assertQueryEquals("using x: contains(x, 14)", false);
		assertQueryEquals("using x: contains(x, 5)", true);
		assertQueryEquals("using x: contains(x, 4)", false);

		evalTestQuery("set(5) store as x");
		assertQueryEquals("using x: contains(x, 4)", false);
		assertQueryEquals("using x: contains(x, 5)", true);
		assertQueryEquals("using x: contains(x, 6)", false);

		evalTestQuery("set() store as x");
		assertQueryEquals("using x: contains(x, 0)", false);
		assertQueryEquals("using x: contains(x, 5)", false);
		assertQueryEquals("using x: contains(x, 6)", false);
	}

	@Test
	public void testContainsValue() throws Exception {
		evalTestQuery("map() store as x");
		assertQueryEquals("using x: containsValue(x, 'a string')", false);
		assertQueryEquals("using x: containsValue(x, 1)", false);
		assertQueryEquals("using x: containsValue(x, 'string')", false);

		evalTestQuery("map(1 -> 'a string') store as x");
		assertQueryEquals("using x: containsValue(x, 'a string')", true);
		assertQueryEquals("using x: containsValue(x, 1)", false);
		assertQueryEquals("using x: containsValue(x, 'string')", false);

		evalTestQuery("map(1 -> 'a string', 2 -> 'another string', 1 -> '') store as x");
		assertQueryEquals("using x: containsValue(x, 'a string')", false);
		assertQueryEquals("using x: containsValue(x, '')", true);
		assertQueryEquals("using x: containsValue(x, 1)", false);
		assertQueryEquals("using x: containsValue(x, 'another string')", true);
	}

	@Test
	public void testCount() throws Exception {
		assertQueryEquals("let x:= list (5..13) in count(x)", 9);
		assertQueryEquals("let x:= list(17) in count(x)", 1);
		assertQueryEquals("let x:= list() in count(x)", 0);

		assertQueryEquals(
				"let x:= bag (5, 5, 5, 6, 7, 8, 9, 10, 11, 12, 13) in count(x)",
				11);
		assertQueryEquals("let x:= bag(17) in count(x)", 1);
		assertQueryEquals("let x:= bag() in count(x)", 0);

		assertQueryEquals(
				"let x:= set (5, 5, 5, 6, 7, 8, 9, 10, 10, 11, 12, 13) in count(x)",
				9);
		assertQueryEquals("let x:= set(17) in count(x)", 1);
		assertQueryEquals("let x:= set() in count(x)", 0);

		assertQueryEquals(
				"let x:= map (5 -> '', 5 -> 'juhu', 6 -> 'A', 7 -> 'B') in count(x)",
				3);
		assertQueryEquals("let x:= map('' -> 17) in count(x)", 1);
		assertQueryEquals("let x:= map() in count(x)", 0);

		assertQueryEquals("let x:= 17 in count(x)", 1);
	}

	@Test
	public void testDifference() throws Exception {
		assertQueryEqualsQuery(
				"let x:= set(5, 7, 9, 13), y := set(5, 6, 7, 8) in difference(x, y)",
				"set(9,13)");
		assertQueryEqualsQuery(
				"let x:= set(5, 7, 9, 13), y := list(5, 5, 6, 7, 8) in difference(x, y)",
				"set(9,13)");
		assertQueryEqualsQuery(
				"let x:= set(5, 7, 9, 13), y := list(6, 8, 10, 11, 12) in difference(x, y)",
				"set(5, 7, 9, 13)");
		assertQueryEqualsQuery(
				"let x:= list(5, 5, 6, 7, 9, 13), y := list(6, 8, 10, 11, 12) in difference(x, y)",
				"set(5, 7, 9, 13)");
		assertQueryEquals(
				"let x:= set(5), y := list(5, 6) in difference(x, y)",
				Arrays.asList());
	}

	@Test
	public void testElements() throws Exception {
		JValue value = evalTestQuery("set()");
		assertQueryEquals("elements( set())", value);
		assertQueryEquals("elements(list())", value);
		assertQueryEquals("elements( bag())", value);

		value = evalTestQuery("set(41)");
		assertQueryEquals("elements( set(41))", value);
		assertQueryEquals("elements( set(41, 41, 41))", value);
		assertQueryEquals("elements(list(41))", value);
		assertQueryEquals("elements(list(41, 41, 41))", value);
		assertQueryEquals("elements( bag(41))", value);
		assertQueryEquals("elements( bag(41, 41, 41))", value);

		value = evalTestQuery("set(5, 7, 9, 13)");

		assertQueryEquals("elements( set(7, 5, 9, 13, 5))", value);
		assertQueryEquals("elements(list(7, 5, 9, 13, 5))", value);
		assertQueryEquals("elements( bag(7, 5, 9, 13, 5))", value);
	}

	@Test
	public void testEntrySet() throws Exception {
		evalTestQuery("map() store as m");
		assertQueryEqualsQuery("using m: entrySet(m)", "set()");

		evalTestQuery("map(1 -> 'a string') store as m");
		assertQueryEqualsQuery("using m: entrySet(m)",
				"set(tup(1, 'a string'))");

		evalTestQuery("map(1 -> 'a string', 2 -> 'another string') store as m");
		assertQueryEqualsQuery("using m: entrySet(m)",
				"set(tup(1, 'a string'), tup(2, 'another string'))");

		evalTestQuery("map('milk' -> 1, 'honey' -> 2, 'milk' -> 3) store as m");
		assertQueryEqualsQuery("using m: entrySet(m)",
				"set(tup('honey', 2), tup('milk', 3))");
	}

	@Test
	public void testFlattenBag() throws Exception {
		// TODO the result of flatten is somehow correct, but must be sorted.
		// The order cannot be determined.
		evalTestQuery("bag(bag(1, 2, 3), bag(1, 2, 3), bag(3, 4, 5), "
				+ "bag(7, 8, 9)) store as bag1");
		assertQueryEqualsQuery("using bag1: sort(flatten(bag1))",
				"list(1, 1, 2, 2, 3, 3, 3, 4, 5, 7, 8, 9)");

		evalTestQuery("set(bag(1, 2, 3), bag(1, 2, 3), bag(3, 4, 5), "
				+ "bag(7, 8, 9)) store as bag1");
		assertQueryEqualsQuery("using bag1: sort(flatten(bag1))",
				"list(1, 2, 3, 3, 4, 5, 7, 8, 9)");
	}

	@Test
	public void testFlattenList() throws Exception {
		evalTestQuery("list(list(1, 2, 3, 3), list(3, 4, 5), "
				+ "list(7, 8, 9, 1)) store as list1");
		assertQueryEqualsQuery("using list1: flatten(list1)",
				"list(1, 2, 3, 3, 3, 4, 5, 7, 8, 9, 1)");

		evalTestQuery("set(list(1, 2, 3, 3), list(3, 4, 5), "
				+ "list(7, 8, 9, 1)) store as list1");
		assertQueryEqualsQuery("using list1: flatten(list1)",
				"list(1, 2, 3, 3, 3, 4, 5, 7, 8, 9, 1)");
	}

	@Test
	public void testFlattenSet() throws Exception {
		evalTestQuery("set(set(1, 2, 3), set(1, 2, 3), set(3, 4, 5), "
				+ "set(7, 8, 9)) store as set1");
		assertQueryEqualsQuery("using set1: flatten(set1)",
				"list(1, 2, 3,  3, 4, 5,  7, 8, 9)");

		evalTestQuery("set(set(1, 2, 4), set(1, 2, 3), set(3, 4, 5), "
				+ "set(7, 8, 9)) store as set1");
		assertQueryEqualsQuery("using set1: flatten(set1)",
				"list(1, 2, 4,  1, 2, 3,  3, 4, 5,  7, 8, 9)");
	}

	@Test
	public void testGet() throws Exception {
		evalTestQuery("map(1 -> 'One', 2 -> 'Two', 3 -> 'Three', "
				+ "4 -> 'Four', 5 -> 'Five', 6 -> 'Six') store as m");
		assertQueryEqualsQuery(
				"using m: from x: keySet(m) reportSet get(m, x) end",
				"using m: toSet(values(m))");

		evalTestQuery("list ('bratwurst', 'currywurst', 'steak', 'kaenguruhfleisch', 'spiessbraten') store as x");
		assertQueryEquals("using x: get(x, 3)", "kaenguruhfleisch");
		assertQueryEquals("using x: get(x, 0)", "bratwurst");
		assertQueryEquals("using x: get(x, 4)", "spiessbraten");
		assertQueryEquals("using x: get(x, 2)", "steak");
		expectException("using x: get(x, -1)",
				ArrayIndexOutOfBoundsException.class);
		expectException("using x: get(x, 5)", IndexOutOfBoundsException.class);
	}

	@Test
	public void testGetSuffix() throws Exception {
		evalTestQuery("map(1 -> 'One', 2 -> 'Two', 3 -> 'Three', "
				+ "4 -> 'Four', 5 -> 'Five', 6 -> 'Six') store as m");
		assertQueryEqualsQuery("using m: from x: keySet(m) reportSet m[x] end",
				"using m: toSet(values(m))");

		evalTestQuery("list ('bratwurst', 'currywurst', 'steak', 'kaenguruhfleisch', 'spiessbraten') store as x");
		assertQueryEquals("using x: x[3]", "kaenguruhfleisch");
		assertQueryEquals("using x: x[0]", "bratwurst");
		assertQueryEquals("using x: x[4]", "spiessbraten");
		assertQueryEquals("using x: x[2]", "steak");

		expectException("using x: x[-1]", ArrayIndexOutOfBoundsException.class);
		expectException("using x: x[5]", IndexOutOfBoundsException.class);
	}

	@Test
	public void testIntersection() throws Exception {
		evalTestQuery("set() store as x");
		evalTestQuery("set()  store as y");
		assertQueryEqualsQuery("using x,y: intersection(x, y)", "set()");
		assertQueryEqualsQuery("using x,y: intersection(y, x)", "set()");
		assertQueryEqualsQuery("using x: intersection(x, x)", "set()");
		assertQueryEqualsQuery("using y: intersection(y, y)", "set()");

		evalTestQuery("set(5) store as x");
		evalTestQuery("set(6)  store as y");
		assertQueryEqualsQuery("using x: intersection(x, x)", "using x: x");
		assertQueryEqualsQuery("using y: intersection(y, y)", "using y: y");
		assertQueryEquals("using x, y: intersection(x, y)", Arrays.asList());
		assertQueryEquals("using x, y: intersection(y, x)", Arrays.asList());

		evalTestQuery("set(5, 7, 9, 13) store as x");
		evalTestQuery("set(5, 6, 7, 8)  store as y");
		assertQueryEqualsQuery("using x,y: intersection(x, y)", "set(5, 7)");
		assertQueryEqualsQuery("using x,y: intersection(y, x)", "set(5, 7)");
		assertQueryEqualsQuery("using x: intersection(x, x)", "using x : x");
		assertQueryEqualsQuery("using y: intersection(y, y)", "using y : y");
	}

	@Test
	public void testIsEmpty() throws Exception {
		evalTestQuery("set(1, 2, 3) store as x");
		assertQueryEquals("using x: isEmpty(x)", false);
		evalTestQuery("list(1..3) store as x");
		assertQueryEquals("using x: isEmpty(x)", false);
		evalTestQuery("bag(1, 2, 3) store as x");
		assertQueryEquals("using x: isEmpty(x)", false);
		evalTestQuery("map(1 -> '') store as x");
		assertQueryEquals("using x: isEmpty(x)", false);

		assertQueryEquals("isEmpty(list())", true);
		assertQueryEquals("isEmpty(set())", true);
		assertQueryEquals("isEmpty(bag())", true);
		assertQueryEquals("isEmpty(map())", true);
	}

	@Test
	public void testIsSubSet() throws Exception {
		evalTestQuery("set(5, 7, 9, 13) store as x");
		evalTestQuery("set(5, 6, 7, 8)  store as y");
		assertQueryEquals("using x,y: isSubSet(x,y)", false);

		evalTestQuery("set(5, 7) store as y");
		assertQueryEquals("using x,y: isSubSet(x,y)", false);
		assertQueryEquals("using x,y: isSubSet(y,x)", true);

		assertQueryEquals("using x: isSubSet(x,x)", true);

		evalTestQuery("set() store as y");
		assertQueryEquals("using x,y: isSubSet(x,y)", false);
		assertQueryEquals("using x,y: isSubSet(y,x)", true);
	}

	@Test
	public void testIsSuperSet() throws Exception {
		evalTestQuery("set(5, 7, 9, 13) store as x");
		evalTestQuery("set(1, 5, 7)  store as y");
		assertQueryEquals("using x,y: isSuperSet(x, y)", false);

		evalTestQuery("set(5, 7)  store as y");
		assertQueryEquals("using x,y: isSuperSet(x, y)", true);
		assertQueryEquals("using x,y: isSuperSet(y, x)", false);

		assertQueryEquals("using x: isSuperSet(x, x)", true);

		evalTestQuery("set()  store as y");
		assertQueryEquals("using x,y: isSuperSet(x, y)", true);
		assertQueryEquals("using x,y: isSuperSet(y, x)", false);
	}

	@Test
	public void testIsUniqueBag() throws Exception {
		assertQueryEquals("let x := bag() in isUnique(x)", true);
		assertQueryEquals("let x := bag(1) in isUnique(x)", true);
		assertQueryEquals("let x := bag(1, 1) in isUnique(x)", false);
		assertQueryEquals("let x := bag(1, 2, 3, 4, 99) in isUnique(x)", true);
		assertQueryEquals("let x := bag(1, 2, 3, 4, 1, 99) in isUnique(x)",
				false);
	}

	@Test
	public void testIsUniqueList() throws Exception {
		assertQueryEquals("let x := list() in isUnique(x)", true);
		assertQueryEquals("let x := list(1) in isUnique(x)", true);
		assertQueryEquals("let x := list(1, 1) in isUnique(x)", false);
		assertQueryEquals("let x := list(1, 2, 3, 4, 99) in isUnique(x)", true);
		assertQueryEquals("let x := list(1, 2, 3, 4, 1, 99) in isUnique(x)",
				false);
	}

	@Test
	public void testIsUniqueSet() throws Exception {
		assertQueryEquals("let x := set() in isUnique(x)", true);
		assertQueryEquals("let x := set(1) in isUnique(x)", true);
		assertQueryEquals("let x := set(1, 1) in isUnique(x)", true);
		assertQueryEquals("let x := set(1, 2, 3, 4, 99) in isUnique(x)", true);
		assertQueryEquals("let x := set(1, 2, 3, 4, 1, 99) in isUnique(x)",
				true);
	}

	@Test
	public void testKeySet() throws Exception {
		evalTestQuery("map()  store as m");
		assertQueryEqualsQuery("using m: keySet(m)", "set()");

		evalTestQuery("map(4 -> 'Four')  store as m");
		assertQueryEqualsQuery("using m: keySet(m)", "set(4)");

		evalTestQuery("map(3 -> 'Three', 5 -> 'Five')  store as m");
		assertQueryEqualsQuery("using m: keySet(m)", "set(3,5)");

		evalTestQuery("map(1 -> 'One',   2 -> 'Two', 3 -> 'Three'"
				+ ", 4 -> 'Four', 5 -> 'Five', 6 -> 'Six')  store as m");
		assertQueryEqualsQuery("using m: keySet(m)", "set(1,2,3,4,5,6)");

	}

	@Test
	public void testMaxBag() throws Exception {
		assertQueryEquals("max(bag())", Long.MIN_VALUE);
		assertQueryEquals("max(bag(-5))", -5);
		assertQueryEquals("max(bag(6))", 6);
		assertQueryEquals("max(bag(-5, 6))", 6);
		assertQueryEquals("max(bag(6 , 5))", 6);
		assertQueryEquals("max(bag(1, 2, 4, -6, 65, 73, 65, 322, 1))", 322);
	}

	@Test
	public void testMaxList() throws Exception {
		assertQueryEquals("max(list())", Long.MIN_VALUE);
		assertQueryEquals("max(list(-5))", -5);
		assertQueryEquals("max(list(6))", 6);
		assertQueryEquals("max(list(-5, 6))", 6);
		assertQueryEquals("max(list(6 , 5))", 6);
		assertQueryEquals("max(list(1, 2, 4, -6, 65, 73, 65, 322, 1))", 322);
	}

	@Test
	public void testMaxSet() throws Exception {
		assertQueryEquals("max(set())", Long.MIN_VALUE);
		assertQueryEquals("max(set(-5))", -5);
		assertQueryEquals("max(set(6))", 6);
		assertQueryEquals("max(set(-5, 6))", 6);
		assertQueryEquals("max(set(6 , 5))", 6);
		assertQueryEquals("max(set(1, 2, 4, -6, 65, 73, 65, 322, 1))", 322);
	}

	@Test
	public void testMergeMaps() throws Exception {
		evalTestQuery("map(tup(1,2) -> set(3), tup(3,4) -> set(7)) store as m1");
		evalTestQuery("map(tup(1,2) -> set(3,4), tup(3,4) -> set(7,8,9)) store as m2");
		evalTestQuery("map(tup(1,2) -> set(4), tup(3,4) -> set(8,9)) store as m3");
		// merging equal maps should return an equal map
		assertQueryEqualsQuery("using m1: mergeMaps(m1, m1)", "using m1: m1");
		assertQueryEqualsQuery("using m1, m3: mergeMaps(m1, m3)",
				"using m2: m2");

		assertQueryEqualsQuery("using m3: mergeMaps(map(), m3)", "using m3: m3");
		assertQueryEqualsQuery("using m1: mergeMaps(m1, map())", "using m1: m1");
		assertQueryEqualsQuery("mergeMaps(map(), map())", "map()");

	}

	@Test
	public void testMinBag() throws Exception {
		assertQueryEquals("min(bag())", Long.MAX_VALUE);
		assertQueryEquals("min(bag(-5))", -5);
		assertQueryEquals("min(bag(6))", 6);
		assertQueryEquals("min(bag(-5, 6))", -5);
		assertQueryEquals("min(bag(6 , 5))", 5);
		assertQueryEquals("min(bag(1, 2, 4, -6, 65, 73, 65, 322, 1))", -6);
	}

	@Test
	public void testMinList() throws Exception {
		assertQueryEquals("min(list())", Long.MAX_VALUE);
		assertQueryEquals("min(list(-5))", -5);
		assertQueryEquals("min(list(6))", 6);
		assertQueryEquals("min(list(-5, 6))", -5);
		assertQueryEquals("min(list(6 , 5))", 5);
		assertQueryEquals("min(list(1, 2, 4, -6, 65, 73, 65, 322, 1))", -6);
	}

	@Test
	public void testMinSet() throws Exception {
		assertQueryEquals("min(set())", Long.MAX_VALUE);
		assertQueryEquals("min(set(-5))", -5);
		assertQueryEquals("min(set(6))", 6);
		assertQueryEquals("min(set(-5, 6))", -5);
		assertQueryEquals("min(set(6 , 5))", 5);
		assertQueryEquals("min(set(1, 2, 4, -6, 65, 73, 65, 322, 1))", -6);
	}

	@Test
	public void testPos() throws Exception {
		assertQueryEquals("let x:= list (5..13) in pos(x, 7)", 2);
		assertQueryEquals("let x:= list (5..13) in pos(x, 2)", -1);
		assertQueryEquals("let x:= list (5..13) in pos(x, 5)", 0);
		assertQueryEquals("let x:= list (5..13) in pos(x, 13)", 8);
		assertQueryEquals("let x:= list (5..13) in pos(x, 14)", -1);
		assertQueryEquals("let x:= list (5..13) in pos(x, 4)", -1);
	}

	@Test
	public void testPredecessorBag() throws Exception {
		assertQueryEqualsNull("let x := bag() in predecessor(1, x)");
		assertQueryEqualsNull("let x := bag(1) in predecessor(1, x)");
		assertQueryEqualsNull("let x := bag(1, 1) in predecessor(1, x)");

		evalTestQuery("sort(bag(1, 2, 3, 4, 1, 99)) store as x");
		assertQueryEqualsNull("using x: predecessor(1, x)");
		assertQueryEquals("using x: predecessor(2, x)", 1);
		assertQueryEquals("using x: predecessor(3, x)", 2);
		assertQueryEquals("using x: predecessor(4, x)", 3);
		assertQueryEquals("using x: predecessor(99, x)", 4);
	}

	@Test
	public void testPredecessorList() throws Exception {
		assertQueryEqualsNull("let x := list() in predecessor(1, x)");
		assertQueryEqualsNull("let x := list(1) in predecessor(1, x)");
		assertQueryEqualsNull("let x := list(1, 1) in predecessor(1, x)");

		evalTestQuery("list(1, 2, 3, 4, 1, 99) store as x");
		assertQueryEqualsNull("using x: predecessor(1, x)");
		assertQueryEquals("using x: predecessor(2, x)", 1);
		assertQueryEquals("using x: predecessor(3, x)", 2);
		assertQueryEquals("using x: predecessor(4, x)", 3);
		assertQueryEquals("using x: predecessor(99, x)", 1);
	}

	@Test
	public void testPredecessorSet() throws Exception {
		assertQueryEqualsNull("let x := set() in predecessor(1, x)");
		assertQueryEqualsNull("let x := set(1) in predecessor(1, x)");
		assertQueryEqualsNull("let x := set(1, 1) in predecessor(1, x)");
		evalTestQuery("sort(set(1, 2, 3, 4, 1, 99)) store as x");
		assertQueryEqualsNull("using x: predecessor(1, x)");
		assertQueryEquals("using x: predecessor(2, x)", 1);
		assertQueryEquals("using x: predecessor(3, x)", 2);
		assertQueryEquals("using x: predecessor(4, x)", 3);
		assertQueryEquals("using x: predecessor(99, x)", 4);
	}

	@Test
	public void testSortBag() throws Exception {
		assertQueryEqualsQuery("sort(bag())", "bag()");
		assertQueryEqualsQuery("sort(bag(4))", "bag(4)");
		assertQueryEqualsQuery("sort(bag(4, 1, 2, 10, 9, 7, 8, 3, 5, 6))",
				"bag(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)");
	}

	@Test
	public void testSortList() throws Exception {
		assertQueryEqualsQuery("sort(list())", "list()");
		assertQueryEqualsQuery("sort(list(4))", "list(4)");
		assertQueryEqualsQuery("sort(list(4, 1, 2, 10, 9, 7, 8, 3, 5, 6))",
				"list(1..10)");
	}

	@Test
	public void testSortMap() throws Exception {
		assertQueryEqualsQuery("sort(map())", "map()");
		assertQueryEqualsQuery("sort(map(4 -> 16))", "map(4 -> 16)");
		assertQueryEqualsQuery("sort(map(4 -> 16, 1 -> 1, 2 -> 4, 10 -> 100, "
				+ "9 -> 81, 7 -> 49, 8 -> 64, 3 -> 9, 5 -> 25, 6 -> 36))",
				"from i : list (1..10) reportMap i -> i*i end");
	}

	@Test
	public void testSortSet() throws Exception {
		assertQueryEqualsQuery("sort(set())", "set()");
		assertQueryEqualsQuery("sort(set(4))", "set(4)");
		assertQueryEqualsQuery("sort(set(4, 1, 2, 10, 9, 7, 8, 3, 5, 6))",
				"set(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)");
	}

	@Test
	public void testSum() throws Exception {
		assertQueryEquals("let x:= list() in sum(x)", 0.0);
		assertQueryEquals("let x:= list(5) in sum(x)", 5.0);
		assertQueryEquals("let x:= list(5..13) in sum(x)", 81.0);

		assertQueryEquals("let x:= set() in sum(x)", 0.0);
		assertQueryEquals("let x:= set(5) in sum(x)", 5.0);
		assertQueryEquals(
				"let x:= set(5, 6, 7, 8, 9, 10, 11, 12, 13) in sum(x)", 81.0);

		assertQueryEquals("let x:= bag() in sum(x)", 0.0);
		assertQueryEquals("let x:= bag(5) in sum(x)", 5.0);
		assertQueryEquals(
				"let x:= bag(5, 6, 7, 8, 9, 10, 11, 12, 13) in sum(x)", 81.0);

		expectException("let x:= bag('test') in sum(x)",
				EvaluateException.class);
		expectException("let x:= bag(true) in sum(x)", EvaluateException.class);
		expectException("let x:= bag(rec(name: 'Daniel') in sum(x)",
				EvaluateException.class);
	}

	@Test
	public void testSymDifference() throws Exception {
		evalTestQuery("set() store as x");
		assertQueryEqualsQuery("using x: symDifference(x, x)", "set()");

		evalTestQuery("set(5)  store as x");
		evalTestQuery("set(6)  store as y");
		assertQueryEquals("using x: symDifference(x, x)", Arrays.asList());
		assertQueryEquals("using y: symDifference(y, y)", Arrays.asList());
		assertQueryEqualsQuery("using x, y: symDifference(x, y)", "set(5, 6)");
		assertQueryEqualsQuery("using x, y: symDifference(y, x)", "set(5, 6)");

		assertQueryEquals("using x: symDifference(x, x)", Arrays.asList());
		assertQueryEquals("using y: symDifference(y, y)", Arrays.asList());

		assertQueryEquals("using x: symDifference(x, x)", Arrays.asList());
		assertQueryEquals("using y: symDifference(y, y)", Arrays.asList());

		evalTestQuery("set(5, 7, 9, 13) store as x");
		evalTestQuery("set(5, 6, 7, 8)  store as y");
		assertQueryEqualsQuery("using x, y: symDifference(x, y)",
				"set(6, 8, 9, 13)");

		assertQueryEquals("using x: symDifference(x, x)", Arrays.asList());
		assertQueryEquals("using y: symDifference(y, y)", Arrays.asList());

	}

	@Test
	public void testTheElementBag() throws Exception {
		expectException("let x := bag() in theElement(x)",
				EvaluateException.class);
		assertQueryEquals("let x := bag(-1) in theElement(x)", -1);
		assertQueryEquals("let x := bag(123) in theElement(x)", 123);
		expectException("let x := bag(5, 4) in theElement(x)",
				EvaluateException.class);
	}

	@Test
	public void testTheElementList() throws Exception {
		expectException("let x := list() in theElement(x)",
				EvaluateException.class);
		assertQueryEquals("let x := list(-1) in theElement(x)", -1);
		assertQueryEquals("let x := list(123) in theElement(x)", 123);
		expectException("let x := list(5, 4) in theElement(x)",
				EvaluateException.class);
	}

	@Test
	public void testTheElementSet() throws Exception {
		expectException("let x := set() in theElement(x)",
				EvaluateException.class);
		assertQueryEquals("let x := set(-1) in theElement(x)", -1);
		assertQueryEquals("let x := set(123) in theElement(x)", 123);
		expectException("let x := set(5, 4) in theElement(x)",
				EvaluateException.class);
	}

	@Test
	public void testUnionMapAndMap() throws Exception {
		evalTestQuery("map(1 -> 'A', 2 -> 'A', 3 -> 'B') store as map1");
		evalTestQuery("map(4 -> 'A', 5 -> 'C', 6 -> 'D') store as map2");
		evalTestQuery("map(1 -> 'A', 2 -> 'A', 3 -> 'B', 4 -> 'A', "
				+ "5 -> 'C', 6 -> 'D') store as map3");
		assertQueryEqualsQuery("using map1, map2: union(map1, map2, true)",
				"using map3: map3");

		evalTestQuery("map(1 -> 'A', 3 -> 'C', 4 -> 'D') store as map2");
		try {
			assertQueryEqualsQuery("using map1, map2: union(map1, map2, true)",
					"using map3: map3");
			fail("Expected Exception on using union with two non-disjoint maps");
		} catch (Exception ex) {
			// :)
		}
	}

	@Test
	public void testUnionSetAndSet() throws Exception {
		evalTestQuery("set(5, 7, 9, 13) store as x");
		evalTestQuery("set(5, 6, 7, 8)  store as y");
		assertQueryEqualsQuery("using x, y: union(x, y)",
				"set(5, 6, 7, 8, 9, 13)");
	}

	@Test
	public void testUnionSetOfSets() throws Exception {
		evalTestQuery("set(set(1, 2, 3), set(1, 2, 3), set(3, 4, 5), "
				+ "set(7, 8, 9)) store as set1");
		assertQueryEqualsQuery("using set1: union(set1)",
				"set(1, 2, 3, 4, 5, 7, 8, 9)");
	}

	@Test
	public void testValues() throws Exception {
		evalTestQuery("map() store as m");
		assertQueryEqualsQuery("using m: values(m)", "bag()");

		evalTestQuery("map(1 -> 'a string') store as m");
		assertQueryEqualsQuery("using m: values(m)", "bag('a string')");

		evalTestQuery("map(1 -> 'a string', 2 -> 'another string') store as m");
		assertQueryEqualsQuery("using m: values(m)",
				"bag('a string', 'another string')");

		evalTestQuery("map('milk' -> 1, 'honey' -> 2, 'milk' -> 3) store as m");
		assertQueryEqualsQuery("using m: values(m)", "bag(2, 3)");

		evalTestQuery("map('milk' -> 1, 'honey' -> 1, 'milk' -> 1) store as m");
		assertQueryEqualsQuery("using m: values(m)", "bag(1,1)");
	}
}
