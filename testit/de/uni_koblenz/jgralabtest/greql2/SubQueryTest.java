/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2012 Institute for Software Technology
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

package de.uni_koblenz.jgralabtest.greql2;

import junit.framework.Assert;

import org.junit.Test;

import de.uni_koblenz.jgralab.greql.Query;
import de.uni_koblenz.jgralab.greql.evaluator.QueryImpl;
import de.uni_koblenz.jgralab.greql.exception.GreqlException;

public class SubQueryTest extends GenericTest {

	@Test
	public void testSimpleSubQuery() {
		Query query = new QueryImpl("one() + two() + three()");
		query.setSubQuery("one", "1");
		query.setSubQuery("two", "2");
		query.setSubQuery("three", "3");
		Object r = query.evaluate(getTestTree());
		Assert.assertEquals(6, ((Integer) r).intValue());
	}

	@Test
	public void testSubQueryWithSQsUsingOtherSQs() {
		Query query = new QueryImpl("one() + two() + three()");
		query.setSubQuery("one", "1");
		query.setSubQuery("two", "one() + one()");
		query.setSubQuery("three", "one() + two() - two() + one() + one()");
		Object r = query.evaluate(getTestTree());
		Assert.assertEquals(6, ((Integer) r).intValue());
	}

	@Test(expected = GreqlException.class)
	public void testRecursiveSubQueryError() {
		Query query = new QueryImpl(null);
		// recursive defs are not allowed
		query.setSubQuery("x", "using val: (val > 0 ? x(val - 1) : 0)");
	}

	@Test(expected = GreqlException.class)
	public void testShadowingSubQueryError() {
		Query query = new QueryImpl(null);
		// A subquery def must error if it shadows a function from the funlib
		query.setSubQuery("and", "true");
	}

	@Test(expected = GreqlException.class)
	public void testSubQueryArgCountMismatchError1() {
		Query query = new QueryImpl("add3()");
		query.setSubQuery("add3", "using a, b, c: a + b + c");
		query.evaluate(getTestTree());
	}

	@Test(expected = GreqlException.class)
	public void testSubQueryArgCountMismatchError2() {
		Query query = new QueryImpl("add3(1)");
		query.setSubQuery("add3", "using a, b, c: a + b + c");
		query.evaluate(getTestTree());
	}

	@Test(expected = GreqlException.class)
	public void testSubQueryArgCountMismatchError3() {
		Query query = new QueryImpl("add3(1, 2)");
		query.setSubQuery("add3", "using a, b, c: a + b + c");
		query.evaluate(getTestTree());
	}

	@Test(expected = GreqlException.class)
	public void testSubQueryArgCountMismatchError4() {
		Query query = new QueryImpl("add3(1, 2, 3, 4)");
		query.setSubQuery("add3", "using a, b, c: a + b + c");
		query.evaluate(getTestTree());
	}

	@Test
	public void testSubQueryAdd3() {
		Query query = new QueryImpl("add3(one(), two(), three())");
		query.setSubQuery("add3", "using a, b, c: a + b + c");
		query.setSubQuery("one", "1");
		query.setSubQuery("two", "one() + one()");
		query.setSubQuery("three", "one() + two() - two() + one() + one()");
		Object r = query.evaluate(getTestTree());
		Assert.assertEquals(6, ((Integer) r).intValue());
	}
}
