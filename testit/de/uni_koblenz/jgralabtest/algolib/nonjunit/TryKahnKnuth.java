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
package de.uni_koblenz.jgralabtest.algolib.nonjunit;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.ImplementationType;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmTerminatedException;
import de.uni_koblenz.jgralab.algolib.algorithms.search.DepthFirstSearch;
import de.uni_koblenz.jgralab.algolib.algorithms.search.RecursiveDepthFirstSearch;
import de.uni_koblenz.jgralab.algolib.algorithms.topological_order.KahnKnuthAlgorithm;
import de.uni_koblenz.jgralab.algolib.algorithms.topological_order.TopologicalOrderWithDFS;
import de.uni_koblenz.jgralab.algolib.functions.entries.PermutationEntry;
import de.uni_koblenz.jgralab.graphmarker.SubGraphMarker;
import de.uni_koblenz.jgralabtest.schemas.algolib.simple.SimpleGraph;
import de.uni_koblenz.jgralabtest.schemas.algolib.simple.SimpleSchema;
import de.uni_koblenz.jgralabtest.schemas.algolib.simple.SimpleVertex;

public class TryKahnKnuth {

	public static SimpleGraph danielsGraph() {
		SimpleGraph graph = SimpleSchema.instance().createSimpleGraph(ImplementationType.STANDARD);
		int vertexCount = 7;
		SimpleVertex[] vertices = new SimpleVertex[vertexCount];
		for (int i = 1; i < vertexCount; i++) {
			vertices[i] = graph.createSimpleVertex();
		}
		graph.createSimpleEdge(vertices[5], vertices[2]);
		graph.createSimpleEdge(vertices[2], vertices[3]);
		graph.createSimpleEdge(vertices[2], vertices[4]);
		graph.createSimpleEdge(vertices[4], vertices[1]);
		graph.createSimpleEdge(vertices[5], vertices[1]);
		graph.createSimpleEdge(vertices[6], vertices[3]);
		return graph;
	}

	public static SimpleGraph myGraph() {
		SimpleGraph graph = SimpleSchema.instance().createSimpleGraph(ImplementationType.STANDARD);
		int vertexCount = 10;
		SimpleVertex[] vertices = new SimpleVertex[vertexCount];
		for (int i = 1; i < vertexCount; i++) {
			vertices[i] = graph.createSimpleVertex();
		}
		graph.createSimpleEdge(vertices[5], vertices[1]);
		graph.createSimpleEdge(vertices[3], vertices[1]);
		graph.createSimpleEdge(vertices[3], vertices[2]);
		graph.createSimpleEdge(vertices[4], vertices[2]);
		graph.createSimpleEdge(vertices[5], vertices[3]);
		graph.createSimpleEdge(vertices[4], vertices[3]);
		graph.createSimpleEdge(vertices[7], vertices[4]);
		graph.createSimpleEdge(vertices[6], vertices[5]);
		graph.createSimpleEdge(vertices[7], vertices[6]);
		graph.createSimpleEdge(vertices[1], vertices[8]);
		graph.createSimpleEdge(vertices[2], vertices[8]);
		return graph;
	}

	public static SimpleGraph volkersGraph() {
		SimpleGraph graph = SimpleSchema.instance().createSimpleGraph(ImplementationType.STANDARD);
		int vertexCount = 5;
		SimpleVertex[] vertices = new SimpleVertex[vertexCount];
		for (int i = 1; i < vertexCount; i++) {
			vertices[i] = graph.createSimpleVertex();
		}
		graph.createSimpleEdge(vertices[1], vertices[2]);
		graph.createSimpleEdge(vertices[1], vertices[3]);
		graph.createSimpleEdge(vertices[2], vertices[3]);
		return graph;
	}

	public static void main(String[] args) {

		SimpleGraph graph;// = myGraph();

		// // creating a subgraph
		// SubGraphMarker subgraph = createSubgraph(graph);
		// BooleanFunction<Edge> navigable = new
		// MethodCallToBooleanFunctionAdapter<Edge>() {
		//
		// @Override
		// public boolean get(Edge parameter) {
		// if (parameter.getAlpha() == parameter.getGraph().getVertex(1)
		// && parameter.getOmega() == parameter.getGraph()
		// .getVertex(8)) {
		// return false;
		// }
		// return true;
		// }
		//
		// @Override
		// public boolean isDefined(Edge parameter) {
		// return true;
		// }
		//
		// };
		// // graph.createSimpleEdge(vertices[7], vertices[3]);

		graph = volkersGraph();

		KahnKnuthAlgorithm solver = new KahnKnuthAlgorithm(graph);

		try {
			System.out.println("Kahn Knuth:");
			solver.execute();
			System.out.println(solver.isAcyclic());
			System.out.println(solver.getTopologicalOrder());
			for (PermutationEntry<Vertex> e : solver.getTopologicalOrder()) {
				System.out.println(e.getSecond());
			}
		} catch (AlgorithmTerminatedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			System.out.println("DFS:");
			DepthFirstSearch dfs = new RecursiveDepthFirstSearch(graph);
			TopologicalOrderWithDFS solver2 = new TopologicalOrderWithDFS(
					graph, dfs);
			// solver2.setSubgraph(subgraph);
			// solver2.setNavigable(navigable);
			// dfs.addVisitor(new DebugSearchVisitor());

			solver2.execute();
			System.out.println(solver2.isAcyclic());
			System.out.println(solver2.getTopologicalOrder());
			for (PermutationEntry<Vertex> e : solver2.getTopologicalOrder()) {
				System.out.println(e.getSecond());
			}
		} catch (AlgorithmTerminatedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

	@SuppressWarnings("unused")
	private static SubGraphMarker createSubgraph(SimpleGraph graph) {
		SubGraphMarker subgraph = new SubGraphMarker(graph);
		subgraph.mark(graph.getVertex(9));
		for (Edge e : graph.edges()) {
			subgraph.mark(e);
		}
		Vertex v3 = graph.getVertex(3);
		subgraph.removeMark(v3);
		return subgraph;
	}
}
