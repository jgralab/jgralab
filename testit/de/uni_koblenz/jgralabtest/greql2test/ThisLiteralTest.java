/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2007 Institute for Software Technology
 *               University of Koblenz-Landau, Germany
 *
 *               ist@uni-koblenz.de
 *
 * Please report bugs to http://serres.uni-koblenz.de/bugzilla
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package de.uni_koblenz.jgralabtest.greql2test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.uni_koblenz.jgralab.greql2.jvalue.JValue;

public class ThisLiteralTest extends GenericTests {

	@Test
	public void testThisVertex1() throws Exception {
		String queryString = "from v,w:V{WhereExpression} with v {thisVertex<>v}& --> &{thisVertex=v} -->  w report v end";
		JValue result = evalTestQuery("ThisVertex1", queryString);
		assertEquals(0, result.toCollection().size());
	}

	// TODO: We get a NullpointerException here, cause somewhere Equals gets a
	// null value as first parameter
	@Test
	public void testThisVertex2() throws Exception {
		String queryString = "from v,w:V{WhereExpression}, g:V{Greql2Expression} with v {thisVertex=v}& --> &{thisVertex=g} <-- w report v end";
		JValue result = evalTestQuery("ThisVertex2", queryString);
		assertEquals(1, result.toCollection().size());
	}

	// TODO: We get a NullpointerException here, cause somewhere Equals gets a
	// null value as first parameter
	@Test
	public void testThisVertex3() throws Exception {
		String queryString = "from v,w:V{WhereExpression}, g:V{Greql2Expression} with v {thisVertex=v}& --> &{thisVertex<>g} <-- w report v end";
		JValue result = evalTestQuery("ThisVertex3", queryString);
		assertEquals(0, result.toCollection().size());
	}

	@Test
	public void testThisEdge1() throws Exception {
		String queryString = "from v,w:V{WhereExpression} with v {id(thisEdge)=-15}& --> <--  w report v end";
		JValue result = evalTestQuery("ThisEdge1", queryString);
		assertEquals(1, result.toCollection().size());
	}

	@Test
	public void testThisEdge2() throws Exception {
		String queryString = "from v,w:V{WhereExpression} with v {id(thisEdge)=15}& --> <--  w report v end";
		JValue result = evalTestQuery("ThisEdge2", queryString);
		assertEquals(0, result.toCollection().size());
	}

}
