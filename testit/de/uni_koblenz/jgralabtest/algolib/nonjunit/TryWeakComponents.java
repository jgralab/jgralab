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
package de.uni_koblenz.jgralabtest.algolib.nonjunit;

import java.util.HashSet;
import java.util.Set;

import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmTerminatedException;
import de.uni_koblenz.jgralab.algolib.algorithms.search.BreadthFirstSearch;
import de.uni_koblenz.jgralab.algolib.algorithms.search.visitors.SearchVisitorAdapter;
import de.uni_koblenz.jgralab.algolib.algorithms.weak_components.WeakComponentsWithBFS;
import de.uni_koblenz.jgralab.algolib.functions.Function;
import de.uni_koblenz.jgralab.algolib.problems.WeakComponentsSolver;
import de.uni_koblenz.jgralabtest.schemas.algolib.simple.SimpleGraph;

public class TryWeakComponents {

	private static final int KAPPA = 500;
	private static final int VERTICES_PER_COMPONENT = 1000;
	private static final int ADDITIONAL_EDGES_PER_COMPONENT = 500;

	public static void main(String[] args) {
		SimpleGraph graph = RandomGraph.createEmptyGraph();
		for(int i = 0; i < KAPPA; i++){
			RandomGraph.addWeakComponent(0, graph, VERTICES_PER_COMPONENT, ADDITIONAL_EDGES_PER_COMPONENT);
		}
		System.out.println("Expected Kappa: " + KAPPA);
		BreadthFirstSearch bfs = new BreadthFirstSearch(graph);
		final Set<Vertex> representatives = new HashSet<Vertex>();
		bfs.addVisitor(new SearchVisitorAdapter(){

			@Override
			public void visitRoot(Vertex v) throws AlgorithmTerminatedException {
				representatives.add(v);
			}
			
		});
		WeakComponentsSolver solver = new WeakComponentsWithBFS(graph, bfs);
		try {
			solver.execute();
		} catch (AlgorithmTerminatedException e) {
		}
		System.out.println("Computed kappa: " + solver.getKappa());
		
		Function<Vertex, Vertex> weakComponents = solver.getWeakComponents();
		
		System.out.println("Representative vertices: " + representatives.size());
		
		for(Vertex current : graph.vertices()){
			Vertex currentRep = weakComponents.get(current);
			if(!representatives.contains(currentRep)){
				System.err.println("Wrong representative: " + currentRep);
			}
		}
		
		System.out.println("Fini.");
		
	}
}
