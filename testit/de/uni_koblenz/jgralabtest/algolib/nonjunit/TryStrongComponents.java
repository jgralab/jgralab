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
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmTerminatedException;
import de.uni_koblenz.jgralab.algolib.algorithms.search.DepthFirstSearch;
import de.uni_koblenz.jgralab.algolib.algorithms.search.IterativeDepthFirstSearch;
import de.uni_koblenz.jgralab.algolib.algorithms.search.RecursiveDepthFirstSearch;
import de.uni_koblenz.jgralab.algolib.algorithms.strong_components.StrongComponentsWithDFS;
import de.uni_koblenz.jgralab.algolib.algorithms.strong_components.visitors.ReducedGraphVisitorAdapter;
import de.uni_koblenz.jgralabtest.schemas.algolib.simple.SimpleGraph;
import de.uni_koblenz.jgralabtest.schemas.algolib.simple.SimpleSchema;
import de.uni_koblenz.jgralabtest.schemas.algolib.simple.SimpleVertex;

public class TryStrongComponents {
	public static void main(String[] args) throws AlgorithmTerminatedException {
		SimpleGraph graph = SimpleSchema.instance().createSimpleGraph();
		int vertexCount = 7;
		SimpleVertex[] vertices = new SimpleVertex[vertexCount];
		for (int i = 1; i < vertexCount; i++) {
			vertices[i] = graph.createSimpleVertex();
		}

		graph.createSimpleEdge(vertices[1], vertices[2]);
		graph.createSimpleEdge(vertices[2], vertices[3]);
		graph.createSimpleEdge(vertices[3], vertices[4]);
		graph.createSimpleEdge(vertices[3], vertices[1]);
		graph.createSimpleEdge(vertices[2], vertices[4]);
		graph.createSimpleEdge(vertices[1], vertices[5]);
		graph.createSimpleEdge(vertices[5], vertices[6]);
		graph.createSimpleEdge(vertices[6], vertices[3]);
		graph.createSimpleEdge(vertices[6], vertices[5]);
		graph.createSimpleEdge(vertices[1], vertices[6]);

		DepthFirstSearch dfs = new IterativeDepthFirstSearch(graph);
		dfs.addVisitor(new DebugSearchVisitor());
		StrongComponentsWithDFS solver = new StrongComponentsWithDFS(graph, dfs);
		solver.addVisitor(new ReducedGraphVisitorAdapter(){

			@Override
			public void visitReducedEdge(Edge e) {
				System.out.println("Visiting reduced edge: " + e);
			}

			@Override
			public void visitRepresentativeVertex(Vertex v) {
				System.out.println("Visiting representative vertex: " + v);
			}
			
		});
		solver.execute();
		System.out.println(solver.getLowlink());
		System.out.println(solver.getStrongComponents());
	}
}
