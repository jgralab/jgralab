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
package de.uni_koblenz.jgralabtest.algolib.nonjunit;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmTerminatedException;
import de.uni_koblenz.jgralab.algolib.algorithms.reachability.WarshallAlgorithm;
import de.uni_koblenz.jgralab.algolib.functions.BinaryFunction;
import de.uni_koblenz.jgralab.algolib.functions.IntFunction;
import de.uni_koblenz.jgralab.algolib.functions.Relation;
import de.uni_koblenz.jgralabtest.schemas.algolib.simple.SimpleGraph;
import de.uni_koblenz.jgralabtest.schemas.algolib.simple.SimpleSchema;
import de.uni_koblenz.jgralabtest.schemas.algolib.simple.SimpleVertex;

@SuppressWarnings("unused")
public class TryWarshall {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws AlgorithmTerminatedException {
		SimpleGraph graph = SimpleSchema.instance().createSimpleGraph(8, 7);
		SimpleVertex v1 = graph.createSimpleVertex();
		SimpleVertex v2 = graph.createSimpleVertex();
		SimpleVertex v3 = graph.createSimpleVertex();
		SimpleVertex v4 = graph.createSimpleVertex();
		SimpleVertex v5 = graph.createSimpleVertex();
		SimpleVertex v6 = graph.createSimpleVertex();
		SimpleVertex v7 = graph.createSimpleVertex();
		SimpleVertex v8 = graph.createSimpleVertex();

		graph.createSimpleEdge(v1, v2);
		graph.createSimpleEdge(v1, v3);
		graph.createSimpleEdge(v2, v4);
		graph.createSimpleEdge(v3, v4);
		graph.createSimpleEdge(v4, v5);
		graph.createSimpleEdge(v6, v5);
		graph.createSimpleEdge(v8, v7);

		final WarshallAlgorithm w = new WarshallAlgorithm(graph);
		
		// TransitiveVisitorAdapter visitor = new TransitiveVisitorAdapter() {
		//
		// private Edge[][] successor = w.getInternalSuccessor();
		// private IntFunction<Vertex> indexMapping = w.getIndexMapping();
		//
		// @Override
		// public void visitVertexTriple(Vertex u, Vertex v, Vertex w) {
		// System.out.println("From " + u + " with "
		// + successor[indexMapping.get(u)][indexMapping.get(w)]
		// + " over " + v + " eventually reaching " + w);
		// }
		//
		// };
		
		// w.addVisitor(visitor);
		w.setTraversalDirection(EdgeDirection.INOUT);
		w.execute();
		// System.out.println(w.getVertexOrder());
		// System.out.println(w.getReachabilityRelation());
		// System.out.println(w.getSuccessor());
		System.out.println();
		printResult(graph, w.getReachable(), w.getSuccessor());
		System.out.println("Fini");
	}

	private static void printResult(SimpleGraph graph,
			Relation<Vertex, Vertex> reachable,
			BinaryFunction<Vertex, Vertex, Edge> successor) {
		for (Vertex v : graph.vertices()) {
			for (Vertex w : graph.vertices()) {
				if (v != w && reachable.get(v, w)) {
					System.out.println("From " + v + " to " + w + " follow "
							+ successor.get(v, w));
				}
			}
		}
	}

}
