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

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.pcollections.PCollection;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.greql2.types.Path;
import de.uni_koblenz.jgralab.greql2.types.PathSystem;

public class GreqlConstructions extends GenericTest {

	/*
	 * Test method for
	 * 'greql2.evaluator.GreqlEvaluator.evaluateForwardVertexSet(ForwardVertexSet,
	 * Graph)'
	 */
	@Test
	public void testPathSystemConstruction() throws Exception {
		String queryString = "from c: V{localities.County} "
				+ "with c.name = 'Rheinland-Pfalz' "
				+ "report pathSystem(c, -->{localities.ContainsLocality} -->{connections.AirRoute}) "
				+ "end";
		Object result = evalTestQuery("PathSystemConstruction", queryString,
				TestVersion.ROUTE_MAP_GRAPH);
		@SuppressWarnings("unchecked")
		PCollection<PathSystem> list = ((PCollection<PathSystem>) result);
		assertEquals(1, list.size());
		for (PathSystem v : list) {
			PathSystem sys = v;
			assertEquals(2, sys.getDepth());
			assertEquals(3, sys.getWeight());
		}
	}

	@Test
	public void testPathSystemOnGreqlGraph() throws Exception {
		String queryString = "extractPath(pathSystem(getVertex(1), (<>--|(<-- <->))*), getVertex(34))";
		Object result = evalTestQuery("PathSystemOnGreqlGraph", queryString,
				loadTestGraph());
		// TODO test seriously
		@SuppressWarnings("unused")
		Path path = (Path) result;
		// System.out.println("Path has length " + path.toPath().pathLength());
		// System.out.println(path);
	}

	@Test
	public void testPathSystemOnGreqlGraph2() throws Exception {
		String queryString = "extractPath(pathSystem(getVertex(1), (<>--|(<-- <->))*))";
		// TODO test seriously
		@SuppressWarnings("unused")
		Object result = evalTestQuery("PathSystemOnGreqlGraph", queryString,
				loadTestGraph());
		// for (JValue e : result.toJValueSet()) {
		// JValuePath path = e.toPath();
		// System.out.println("Path has length " + path.toPath().pathLength());
		// System.out.println(path);
		// }
	}

	private Graph loadTestGraph() throws GraphIOException {
		return GraphIO.loadGraphFromFile("testit/testgraphs/greqltestgraph.tg",
				null);

	}
}
