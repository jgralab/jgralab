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

package de.uni_koblenz.jgralabtest.greql2;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.optimizer.DefaultOptimizer;

public class ThisLiteralTest extends GenericTests {

	@Test
	public void testThisVertex1() throws Exception {
		String queryString = "from v,w:V{WhereExpression} with v {@thisVertex<>v}& --> &{@thisVertex=v} -->  w report v end";
		JValue result = evalTestQuery("ThisVertex1", queryString);
		JValue resultOpt = evalTestQuery("ThisVertex1 (wo)", queryString,
				new DefaultOptimizer());
		assertEquals(0, result.toCollection().size());
		assertEquals(result, resultOpt);
	}

	@Test
	public void testThisVertex2() throws Exception {
		// TODO: Broken, because the GReQL parser removes all WhereExpressions
		// and LetExpressions!
		String queryString = "from v,w:V{WhereExpression}, g:V{Greql2Expression} with v {@thisVertex=v}& --> &{@thisVertex=g} <-- w report v end";
		JValue result = evalTestQuery("ThisVertex2", queryString);
		JValue resultOpt = evalTestQuery("ThisVertex2 (wo)", queryString,
				new DefaultOptimizer());
		assertEquals(1, result.toCollection().size());
		assertEquals(result, resultOpt);
	}

	@Test
	public void testThisVertex3() throws Exception {
		String queryString = "from v,w:V{WhereExpression}, g:V{Greql2Expression} with v {@thisVertex=v}& --> &{@thisVertex<>g} <-- w report v end";
		JValue result = evalTestQuery("ThisVertex3", queryString);
		JValue resultOpt = evalTestQuery("ThisVertex3 (wo)", queryString,
				new DefaultOptimizer());
		assertEquals(0, result.toCollection().size());
		assertEquals(result, resultOpt);
	}

	@Test
	public void testThisEdge1() throws Exception {
		// TODO: Broken, because the GReQL parser removes all WhereExpressions
		// and LetExpressions!
		String queryString = "from v:V{Definition}, w:V{WhereExpression}  with v -->{@id(thisEdge)=15}  w report v,w end";
		JValue result = evalTestQuery("ThisEdge1", queryString);
		JValue resultOpt = evalTestQuery("ThisEdge1 (wo)", queryString,
				new DefaultOptimizer());
		assertEquals(1, result.toCollection().size());
		assertEquals(result, resultOpt);
	}

}
