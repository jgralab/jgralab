/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2014 Institute for Software Technology
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

package de.uni_koblenz.jgralabtest.greql;

import java.util.HashSet;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.uni_koblenz.jgralab.greql.GreqlQuery;
import de.uni_koblenz.jgralab.greql.exception.GreqlException;
import de.uni_koblenz.jgralab.greql.funlib.FunLib;

public class GreqlQueryFunctionTest extends GenericTest {
	HashSet<String> functions = new HashSet<>();

	private void registerGreqlFunction(String name, String queryText) {
		GreqlQuery query = GreqlQuery.createQuery(queryText);
		query.setName(name);
		FunLib.registerGreqlQueryFunction(query, true, 1, 1, 1.0);
		functions.add(query.getName());
	}

	@Before
	public void clearNames() {
		functions.clear();
	}

	@After
	public void unregister() {
		for (String name : functions) {
			FunLib.removeGreqlQueryFunction(name);
		}
		functions.clear();
	}

	@Test
	public void testSimpleSubQuery() {
		registerGreqlFunction("one", "1");
		registerGreqlFunction("two", "2");
		registerGreqlFunction("three", "3");
		Object r = GreqlQuery.createQuery("one() + two() + three()").evaluate(
				getTestTree());
		Assert.assertEquals(6, ((Integer) r).intValue());
	}

	@Test
	public void testSubQueryWithSQsUsingOtherSQs() {
		registerGreqlFunction("one", "1");
		registerGreqlFunction("two", "one() + one()");
		registerGreqlFunction("three", "one() + two() - two() + one() + one()");
		Object r = GreqlQuery.createQuery("one() + two() + three()").evaluate(
				getTestTree());
		Assert.assertEquals(6, ((Integer) r).intValue());
	}

	@Test(expected = GreqlException.class)
	public void testRecursiveSubQueryError() {
		// recursive defs are not allowed
		registerGreqlFunction("x", "using val: (val > 0 ? x(val - 1) : 0)");
	}

	@Test(expected = GreqlException.class)
	public void testShadowingSubQueryError() {
		// A subquery def must error if it shadows a function from the funlib
		registerGreqlFunction("and", "true");
	}

	@Test(expected = GreqlException.class)
	public void testSubQueryArgCountMismatchError1() {
		registerGreqlFunction("add3", "using a, b, c: a + b + c");
		GreqlQuery.createQuery("add3()").evaluate(getTestTree());
	}

	@Test(expected = GreqlException.class)
	public void testSubQueryArgCountMismatchError2() {
		registerGreqlFunction("add3", "using a, b, c: a + b + c");
		GreqlQuery.createQuery("add3(1)").evaluate(getTestTree());
	}

	@Test(expected = GreqlException.class)
	public void testSubQueryArgCountMismatchError3() {
		registerGreqlFunction("add3", "using a, b, c: a + b + c");
		GreqlQuery.createQuery("add3(1, 2)").evaluate(getTestTree());
	}

	@Test(expected = GreqlException.class)
	public void testSubQueryArgCountMismatchError4() {
		registerGreqlFunction("add3", "using a, b, c: a + b + c");
		GreqlQuery.createQuery("add3(1, 2, 3, 4)").evaluate(getTestTree());
	}

	@Test
	public void testSubQueryAdd3() {
		registerGreqlFunction("add3", "using a, b, c: a + b + c");
		registerGreqlFunction("one", "1");
		registerGreqlFunction("two", "one() + one()");
		registerGreqlFunction("three", "one() + two() - two() + one() + one()");
		Object r = GreqlQuery.createQuery("add3(one(), two(), three())")
				.evaluate(getTestTree());
		Assert.assertEquals(6, ((Integer) r).intValue());
	}
}
