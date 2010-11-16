/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2010 Institute for Software Technology
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
package de.uni_koblenz.jgralabtest.algolib.nonjunit;

import de.uni_koblenz.jgralab.algolib.algorithms.reachability.ReachableWithSearch;
import de.uni_koblenz.jgralab.algolib.algorithms.search.BreadthFirstSearch;
import de.uni_koblenz.jgralab.algolib.algorithms.search.SearchAlgorithm;
import de.uni_koblenz.jgralabtest.schemas.algolib.simple.SimpleGraph;
import de.uni_koblenz.jgralabtest.schemas.algolib.simple.SimpleSchema;
import de.uni_koblenz.jgralabtest.schemas.algolib.simple.SimpleVertex;

public class TryReachableWithSearch {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SimpleGraph graph = SimpleSchema.instance().createSimpleGraph();
		SimpleVertex v1 = graph.createSimpleVertex();
		SimpleVertex v2 = graph.createSimpleVertex();
		SimpleVertex v3 = graph.createSimpleVertex();
		SimpleVertex v4 = graph.createSimpleVertex();
		SimpleVertex v5 = graph.createSimpleVertex();
		SimpleVertex v6 = graph.createSimpleVertex();
		graph.createSimpleEdge(v1, v2);
		graph.createSimpleEdge(v1, v4);
		graph.createSimpleEdge(v2, v1);
		graph.createSimpleEdge(v1, v3);
		// graph.createSimpleEdge(v1, v5);
		graph.createSimpleEdge(v3, v2);
		graph.createSimpleEdge(v2, v4);
		graph.createSimpleEdge(v3, v4);
		graph.createSimpleEdge(v4, v5);
		graph.createSimpleEdge(v5, v3);
		
		SearchAlgorithm search = new BreadthFirstSearch(graph);
		search.addVisitor(new DebugSearchVisitor());
		ReachableWithSearch solver = new ReachableWithSearch(graph,search);
		solver.execute(v1,v4);
		System.out.println(solver.isReachable());
		solver.reset();
		solver.execute(v2, v6);
		System.out.println(solver.isReachable());
	}

}
