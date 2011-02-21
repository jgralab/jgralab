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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.optimizer.DefaultOptimizer;

public class ThisLiteralTest extends GenericTests {

	@Test
	public void testThisVertex1() throws Exception {
		String queryString = "from c: V{localities.County}, a:V{junctions.Airport} "
				+ "with c --> & {@thisVertex = a} --> & {@thisVertex <> a} a report a "
				+ "end";
		JValue result = evalTestQuery("ThisVertex1", queryString,
				TestVersion.CITY_MAP_GRAPH);
		JValue resultOpt = evalTestQuery("ThisVertex1 (wo)", queryString,
				new DefaultOptimizer(), TestVersion.CITY_MAP_GRAPH);
		assertEquals(0, result.toCollection().size());
		assertEquals(result, resultOpt);
	}

	@Test
	public void testThisVertex2() throws Exception {
		String queryString = "from c: V{localities.County}, a1,a2:V{junctions.Airport} "
				+ "with c {@thisVertex = c} & --> & {@thisVertex = a1} <-- a2 report a1 "
				+ "end";
		JValue result = evalTestQuery("ThisVertex2", queryString,
				TestVersion.CITY_MAP_GRAPH);
		JValue resultOpt = evalTestQuery("ThisVertex2 (wo)", queryString,
				new DefaultOptimizer(), TestVersion.CITY_MAP_GRAPH);
		assertEquals(3, result.toCollection().size());
		assertEquals(result, resultOpt);
	}

	@Test
	public void testThisVertex3() throws Exception {
		String queryString = "from c: V{localities.County}, a1,a2:V{junctions.Airport} "
				+ "with c {@thisVertex = c} & --> & {@thisVertex <> a1} <-- a2 report a1 "
				+ "end";
		JValue result = evalTestQuery("ThisVertex3", queryString,
				TestVersion.CITY_MAP_GRAPH);
		JValue resultOpt = evalTestQuery("ThisVertex3 (wo)", queryString,
				new DefaultOptimizer(), TestVersion.CITY_MAP_GRAPH);
		assertEquals(airportCount * 2 - 1, result.toCollection().size());
		assertEquals(result, resultOpt);
	}

	@Test
	public void testThisEdge1() throws Exception {
		String queryString = "from c1,c2:V{junctions.Crossroad}  with c1 -->{@id(thisEdge)=100} c2 report c1,c2 end";
		JValue result = evalTestQuery("ThisEdge1", queryString,
				TestVersion.CITY_MAP_GRAPH);
		JValue resultOpt = evalTestQuery("ThisEdge1 (wo)", queryString,
				new DefaultOptimizer(), TestVersion.CITY_MAP_GRAPH);
		assertEquals(1, result.toCollection().size());
		assertEquals(result, resultOpt);
	}

}
