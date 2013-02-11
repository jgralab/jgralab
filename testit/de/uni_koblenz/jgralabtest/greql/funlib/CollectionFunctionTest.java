/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2013 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 *
 * For bug reports, documentation and further information, visit
 *
 *                         https://github.com/jgralab/jgralab
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
package de.uni_koblenz.jgralabtest.greql.funlib;

import java.util.Arrays;

import org.junit.Test;

import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.greql.exception.GreqlException;
import de.uni_koblenz.jgralab.greql.types.Undefined;
import de.uni_koblenz.jgralabtest.greql.GenericTest;

public class CollectionFunctionTest extends GenericTest {

	@Test
	public void testMean() throws Exception {
		assertQueryEquals("let x:= list (5..13) in mean(x)", 9.0);
		assertQueryEquals("let x:= list (3) in mean(x)", 3.0);
		assertQueryEquals("let x:= list () in mean(x)", Undefined.UNDEFINED);
		assertQueryEquals("let x:= list (3, 100) in mean(x)", 51.5);
		assertQueryEquals("let x:= list (0..10000) in mean(x)", 5000.0);
		assertQueryEquals("let x:= list (-100..100) in mean(x)", 0.0);
		assertQueryEquals("let x:= list (5, -5, 0) in mean(x)", 0.0);
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
				"let x:= set (5, 5, 5, 6, 7, 8, 9, 10, 10, 11, 12, 13) in count(x)",
				9);
		assertQueryEquals("let x:= set(17) in count(x)", 1);
		assertQueryEquals("let x:= set() in count(x)", 0);

		assertQueryEquals(
				"let x:= map (5 -> '', 5 -> 'juhu', 6 -> 'A', 7 -> 'B') in count(x)",
				3);
		assertQueryEquals("let x:= map('' -> 17) in count(x)", 1);
		assertQueryEquals("let x:= map() in count(x)", 0);

		expectException("let x:= 17 in count(x)", GreqlException.class);
	}

	@Test
	public void testDifference() throws Exception {
		assertQueryEqualsQuery(
				"let x:= set(5, 7, 9, 13), y := set(5, 6, 7, 8) in difference(x, y)",
				"set(9,13)");
		assertQueryEqualsQuery(
				"let x:= set(5, 7, 9, 13), y := set(6, 8, 10, 11, 12) in difference(x, y)",
				"set(5, 7, 9, 13)");
		assertQueryEquals("let x:= set(5), y := set(5, 6) in difference(x, y)",
				JGraLab.set());
	}

	@Test
	public void testEntrySet() throws Exception {
		evalTestQuery("map() store as m");
		assertQueryEqualsQuery("using m: entrySet(m)", "set()");

		evalTestQuery("map(1 -> 'a string') store as m");
		assertQueryEqualsQuery("using m: entrySet(m)",
				"set(map('key' -> 1, 'value' -> 'a string'))");

		evalTestQuery("map(1 -> 'a string', 2 -> 'another string') store as m");
		assertQueryEqualsQuery(
				"using m: entrySet(m)",
				"set(map ('key' ->1, 'value' -> 'a string'), map('key' -> 2, 'value' -> 'another string'))");

		evalTestQuery("map('milk' -> 1, 'honey' -> 2, 'milk' -> 3) store as m");
		assertQueryEqualsQuery("using m: entrySet(m)",
				"set(map('key' -> 'milk', 'value' -> 3), map('key' -> 'honey', 'value' -> 2))");
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
		assertQueryIsUndefined("using x: get(x, -1)");
		assertQueryIsUndefined("using x: get(x, 5)");
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

		assertQueryIsUndefined("using x: x[-1]");
		assertQueryIsUndefined("using x: x[5]");
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
		evalTestQuery("map(1 -> '') store as x");
		assertQueryEquals("using x: isEmpty(x)", false);

		assertQueryEquals("isEmpty(list())", true);
		assertQueryEquals("isEmpty(set())", true);
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
	public void testMaxList() throws Exception {
		assertQueryIsUndefined("max(list())");
		assertQueryEquals("max(list(-5))", -5);
		assertQueryEquals("max(list(6))", 6);
		assertQueryEquals("max(list(-5, 6))", 6);
		assertQueryEquals("max(list(6 , 5))", 6);
		assertQueryEquals("max(list(1, 2, 4, -6, 65, 73, 65, 322, 1))", 322);
	}

	@Test
	public void testMaxSet() throws Exception {
		assertQueryIsUndefined("max(set())");
		assertQueryEquals("max(set(-5))", -5);
		assertQueryEquals("max(set(6))", 6);
		assertQueryEquals("max(set(-5, 6))", 6);
		assertQueryEquals("max(set(6 , 5))", 6);
		assertQueryEquals("max(set(1, 2, 4, -6, 65, 73, 65, 322, 1))", 322);
	}

	@Test
	public void testMinList() throws Exception {
		assertQueryIsUndefined("min(list())");
		assertQueryEquals("min(list(-5))", -5);
		assertQueryEquals("min(list(6))", 6);
		assertQueryEquals("min(list(-5, 6))", -5);
		assertQueryEquals("min(list(6 , 5))", 5);
		assertQueryEquals("min(list(1, 2, 4, -6, 65, 73, 65, 322, 1))", -6);
		assertQueryEquals("min(list(2.0, 1.01))", 1.01);
		assertQueryEquals("min(list(1.0, 2.0))", 1.0);
	}

	@Test
	public void testMinSet() throws Exception {
		assertQueryIsUndefined("min(set())");
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
	public void testSortList() throws Exception {
		assertQueryEqualsQuery("sort(list())", "list()");
		assertQueryEqualsQuery("sort(list(4))", "list(4)");
		assertQueryEqualsQuery("sort(list(4, 1, 2, 10, 9, 7, 8, 3, 5, 6))",
				"list(1..10)");
	}

	@Test
	public void testSortSet() throws Exception {
		assertQueryEqualsQuery("sort(set())", "list()");
		assertQueryEqualsQuery("sort(set(4))", "list(4)");
		assertQueryEqualsQuery("sort(set(4, 1, 2, 10, 9, 7, 8, 3, 5, 6))",
				"list(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)");
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

		expectException("let x:= list('test') in sum(x)", GreqlException.class);
		expectException("let x:= list(true) in sum(x)", GreqlException.class);
		expectException("let x:= list(rec(name: 'Daniel')) in sum(x)",
				GreqlException.class);
	}

	@Test
	public void testTheElementList() throws Exception {
		assertQueryIsUndefined("let x := list() in theElement(x)");
		assertQueryEquals("let x := list(-1) in theElement(x)", -1);
		assertQueryEquals("let x := list(123) in theElement(x)", 123);
		assertQueryIsUndefined("let x := list(5, 4) in theElement(x)");
	}

	@Test
	public void testTheElementSet() throws Exception {
		assertQueryIsUndefined("let x := set() in theElement(x)");
		assertQueryEquals("let x := set(-1) in theElement(x)", -1);
		assertQueryEquals("let x := set(123) in theElement(x)", 123);
		assertQueryIsUndefined("let x := set(5, 4) in theElement(x)");
	}

	@Test
	public void testUnionMapAndMap() throws Exception {
		evalTestQuery("map(1 -> 'A', 2 -> 'A', 3 -> 'B') store as map1");
		evalTestQuery("map(4 -> 'A', 5 -> 'C', 6 -> 'D') store as map2");
		evalTestQuery("map(1 -> 'A', 2 -> 'A', 3 -> 'B', 4 -> 'A', "
				+ "5 -> 'C', 6 -> 'D') store as map3");
		assertQueryEqualsQuery("using map1, map2: union(map1, map2)",
				"using map3: map3");

		evalTestQuery("map(1 -> 'A', 3 -> 'C', 4 -> 'D') store as map2");
	}

	@Test
	public void testUnionSetAndSet() throws Exception {
		evalTestQuery("set(5, 7, 9, 13) store as x");
		evalTestQuery("set(5, 6, 7, 8)  store as y");
		assertQueryEqualsQuery("using x, y: union(x, y)",
				"set(5, 6, 7, 8, 9, 13)");
	}

	@Test
	public void testValues() throws Exception {
		evalTestQuery("map() store as m");
		assertQueryEqualsQuery("using m: values(m)", "list()");

		evalTestQuery("map(1 -> 'a string') store as m");
		assertQueryEqualsQuery("using m: values(m)", "list('a string')");

		evalTestQuery("map(1 -> 'a string', 2 -> 'another string') store as m");
		assertQueryEqualsQuery("using m: values(m)",
				"list('a string', 'another string')");

		evalTestQuery("map('milk' -> 1, 'honey' -> 2, 'milk' -> 3) store as m");
		assertQueryEqualsQuery("using m: values(m)", "list(3,2)");

		evalTestQuery("map('milk' -> 1, 'honey' -> 1, 'milk' -> 1) store as m");
		assertQueryEqualsQuery("using m: values(m)", "list(1,1)");
	}

}
