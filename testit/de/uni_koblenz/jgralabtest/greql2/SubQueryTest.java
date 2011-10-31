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

package de.uni_koblenz.jgralabtest.greql2;

import junit.framework.Assert;

import org.junit.Test;

import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.exception.GreqlException;

public class SubQueryTest extends GenericTest {

	private GreqlEvaluator eval = new GreqlEvaluator((String) null,
			getTestTree(), null);

	@Test
	public void testSimpleSubQuery() {
		eval.setSubQuery("one", "1");
		eval.setSubQuery("two", "2");
		eval.setSubQuery("three", "3");
		eval.setQuery("one() + two() + three()");
		eval.startEvaluation();
		Object r = eval.getResult();
		Assert.assertEquals(6, ((Integer) r).intValue());
	}

	@Test
	public void testSubQueryWithSQsUsingOtherSQs() {
		eval.setSubQuery("one", "1");
		eval.setSubQuery("two", "one() + one()");
		eval.setSubQuery("three", "one() + two() - two() + one() + one()");
		eval.setQuery("one() + two() + three()");
		eval.startEvaluation();
		Object r = eval.getResult();
		Assert.assertEquals(6, ((Integer) r).intValue());
	}

	@Test(expected = GreqlException.class)
	public void testRecursiveSubQueryError() {
		// recursive defs are not allowed
		eval.setSubQuery("x", "using val: (val > 0 ? x(val - 1) : 0)");
	}

	@Test(expected = GreqlException.class)
	public void testShadowingSubQueryError() {
		// A subquery def must error if it shadows a function from the funlib
		eval.setSubQuery("and", "true");
	}

	@Test(expected = GreqlException.class)
	public void testSubQueryArgCountMismatchError1() {
		eval.setSubQuery("add3", "using a, b, c: a + b + c");
		eval.setQuery("add3()");
		eval.startEvaluation();
	}

	@Test(expected = GreqlException.class)
	public void testSubQueryArgCountMismatchError2() {
		eval.setSubQuery("add3", "using a, b, c: a + b + c");
		eval.setQuery("add3(1)");
		eval.startEvaluation();
	}

	@Test(expected = GreqlException.class)
	public void testSubQueryArgCountMismatchError3() {
		eval.setSubQuery("add3", "using a, b, c: a + b + c");
		eval.setQuery("add3(1, 2)");
		eval.startEvaluation();
	}

	@Test(expected = GreqlException.class)
	public void testSubQueryArgCountMismatchError4() {
		eval.setSubQuery("add3", "using a, b, c: a + b + c");
		eval.setQuery("add3(1, 2, 3, 4)");
		eval.startEvaluation();
	}

	@Test
	public void testSubQueryAdd3() {
		eval.setSubQuery("add3", "using a, b, c: a + b + c");
		eval.setSubQuery("one", "1");
		eval.setSubQuery("two", "one() + one()");
		eval.setSubQuery("three", "one() + two() - two() + one() + one()");
		eval.setQuery("add3(one(), two(), three())");
		eval.startEvaluation();
		Object r = eval.getResult();
		Assert.assertEquals(6, ((Integer) r).intValue());
	}
}
