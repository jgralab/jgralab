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

import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmTerminatedException;
import de.uni_koblenz.jgralab.algolib.algorithms.search.DepthFirstSearch;
import de.uni_koblenz.jgralab.algolib.algorithms.search.IterativeDepthFirstSearch;
import de.uni_koblenz.jgralab.algolib.algorithms.search.RecursiveDepthFirstSearch;
import de.uni_koblenz.jgralabtest.schemas.algolib.simple.SimpleGraph;

public class TryDFS {
	public static void main(String[] args) throws AlgorithmTerminatedException {
		// SimpleGraph graph = SimpleSchema.instance().createSimpleGraph();
		// SimpleVertex v1 = graph.createSimpleVertex();
		// SimpleVertex v2 = graph.createSimpleVertex();
		// SimpleVertex v3 = graph.createSimpleVertex();
		// SimpleVertex v4 = graph.createSimpleVertex();
		// SimpleVertex v5 = graph.createSimpleVertex();
		// graph.createSimpleVertex();
		// graph.createSimpleEdge(v1, v2);
		// graph.createSimpleEdge(v1, v4);
		// graph.createSimpleEdge(v2, v1);
		// graph.createSimpleEdge(v1, v3);
		// graph.createSimpleEdge(v1, v5);
		// graph.createSimpleEdge(v3, v2);
		// graph.createSimpleEdge(v2, v4);
		// graph.createSimpleEdge(v3, v4);
		// graph.createSimpleEdge(v4, v5);
		// graph.createSimpleEdge(v3, v5);

		SimpleGraph graph = RandomGraph.createEmptyGraph();
		RandomGraph.addWeakComponent(0, graph, 200000, 200000);

		DepthFirstSearch dfs = new RecursiveDepthFirstSearch(graph);
		// dfs.addVisitor(new DebugSearchVisitor());

		System.out.println("Starting recursive:");
		try {
			dfs.execute();
			System.out.println(dfs.getVertexOrder().length());
			// printResults(dfs);
		} catch (StackOverflowError e) {
			System.err.println("Fail!");
			// e.printStackTrace();
		}

		dfs = new IterativeDepthFirstSearch(graph);
		// dfs.addVisitor(new DebugSearchVisitor());

		System.out.println("Starting iterative:");
		dfs.execute();
		System.out.println(dfs.getVertexOrder().length());

		// printResults(dfs);

		System.out.println("Fini");
	}

	@SuppressWarnings("unused")
	private static void printResults(DepthFirstSearch dfs) {
		System.out.println("vertex order: \n" + dfs.getVertexOrder());
		System.out.println();
		System.out.println("rorder: \n" + dfs.getRorder());
		System.out.println();
		System.out.println("edge order: \n" + dfs.getEdgeOrder());
		System.out.println();
		System.out.println("number: \n" + dfs.getNumber());
		System.out.println();
		System.out.println("level: \n" + dfs.getLevel());
		System.out.println();
		System.out.println("parent: \n" + dfs.getParent());
		System.out.println();
		System.out.println(dfs.getState());
	}
}
