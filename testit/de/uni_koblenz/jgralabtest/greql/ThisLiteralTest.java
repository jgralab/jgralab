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

package de.uni_koblenz.jgralabtest.greql;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import de.uni_koblenz.jgralab.greql.optimizer.DefaultOptimizer;
import de.uni_koblenz.jgralab.greql.optimizer.DefaultOptimizerInfo;

public class ThisLiteralTest extends GenericTest {

	@Test
	public void testThisVertex1() throws Exception {
		String queryString = "from c: V{localities.County}, a:V{junctions.Airport} "
				+ "with c --> & {@thisVertex = a} --> & {@thisVertex <> a} a report a "
				+ "end";
		Object result = evalTestQuery("ThisVertex1", queryString,
				TestVersion.ROUTE_MAP_GRAPH);
		Object resultOpt = evalTestQuery("ThisVertex1 (wo)", queryString,
				new DefaultOptimizer(new DefaultOptimizerInfo()),
				TestVersion.ROUTE_MAP_GRAPH);
		assertEquals(0, ((List<?>) result).size());
		assertEquals(result, resultOpt);
	}

	@Test
	public void testThisVertex2() throws Exception {
		String queryString = "from c: V{localities.County}, a1,a2:V{junctions.Airport} "
				+ "with c {@thisVertex = c} & --> & {@thisVertex = a1} <-- a2 report a1 "
				+ "end";
		Object result = evalTestQuery("ThisVertex2", queryString,
				TestVersion.ROUTE_MAP_GRAPH);
		Object resultOpt = evalTestQuery("ThisVertex2 (wo)", queryString,
				new DefaultOptimizer(new DefaultOptimizerInfo()),
				TestVersion.ROUTE_MAP_GRAPH);
		assertEquals(3, ((List<?>) result).size());
		assertEquals(result, resultOpt);
	}

	@Test
	public void testThisVertex3() throws Exception {
		String queryString = "from c: V{localities.County}, a1,a2:V{junctions.Airport} "
				+ "with c {@thisVertex = c} & --> & {@thisVertex <> a1} <-- a2 report a1 "
				+ "end";
		Object result = evalTestQuery("ThisVertex3", queryString,
				TestVersion.ROUTE_MAP_GRAPH);
		Object resultOpt = evalTestQuery("ThisVertex3 (wo)", queryString,
				new DefaultOptimizer(new DefaultOptimizerInfo()),
				TestVersion.ROUTE_MAP_GRAPH);
		assertEquals(airportCount * 2 - 1, ((List<?>) result).size());
		assertEquals(result, resultOpt);
	}

	@Test
	public void testThisEdge1() throws Exception {
		String queryString = "from c1,c2:V{junctions.Crossroad}  with c1 -->{@isLoop(thisEdge)} c2 report c1,c2 end";
		Object result = evalTestQuery(queryString);
		Object resultOpt = evalTestQuery("ThisEdge1 (wo)", queryString,
				new DefaultOptimizer(new DefaultOptimizerInfo()),
				TestVersion.ROUTE_MAP_GRAPH);
		assertEquals(1, ((List<?>) result).size());
		assertEquals(result, resultOpt);
	}

}
