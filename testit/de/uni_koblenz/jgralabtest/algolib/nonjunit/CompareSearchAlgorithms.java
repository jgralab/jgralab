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

import java.util.Iterator;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmTerminatedException;
import de.uni_koblenz.jgralab.algolib.algorithms.search.IterativeDepthFirstSearch;
import de.uni_koblenz.jgralab.algolib.algorithms.search.RecursiveDepthFirstSearch;
import de.uni_koblenz.jgralab.algolib.algorithms.search.SearchAlgorithm;
import de.uni_koblenz.jgralab.algolib.functions.Permutation;
import de.uni_koblenz.jgralabtest.schemas.algolib.simple.SimpleGraph;

public class CompareSearchAlgorithms {
	protected Graph graph;
	protected SearchAlgorithm alg1;
	protected SearchAlgorithm alg2;

	public CompareSearchAlgorithms(Graph graph, SearchAlgorithm alg1,
			SearchAlgorithm alg2) {
		super();
		this.graph = graph;
		this.alg1 = alg1;
		this.alg2 = alg2;
	}

	public boolean compare(boolean printVertexOrder)
			throws AlgorithmTerminatedException {
		alg1.disableOptionalResults();
		alg2.disableOptionalResults();
		alg1.setGraph(graph);
		alg2.setGraph(graph);
		alg1.execute();
		alg2.execute();
		Permutation<Vertex> vOrder1 = alg1.getVertexOrder();
		Permutation<Vertex> vOrder2 = alg2.getVertexOrder();
		Iterator<Vertex> vs1 = vOrder1.getRangeElements().iterator();
		Iterator<Vertex> vs2 = vOrder2.getRangeElements().iterator();
		boolean out = true;
		while (vs1.hasNext() && vs2.hasNext()) {
			Vertex v1 = vs1.next();
			Vertex v2 = vs2.next();
			out &= v1 == v2;
			if (printVertexOrder) {
				System.out.println(v1);
				System.out.println(v2);
				System.out.println();
			} else if (!out) {
				break;
			}
		}
		return out;
	}

	public static void main(String[] args) throws AlgorithmTerminatedException {
		SimpleGraph graph = RandomGraph.createEmptyGraph();
		RandomGraph.addWeakComponent(0, graph, 10, 10);
		RandomGraph.addWeakComponent(0, graph, 3, 0);
		RecursiveDepthFirstSearch alg1 = new RecursiveDepthFirstSearch(graph);
		// TODO replace with simulated implementation
		IterativeDepthFirstSearch alg2 = new IterativeDepthFirstSearch(graph);
		CompareSearchAlgorithms comp = new CompareSearchAlgorithms(graph, alg1,
				alg2);
		// TODO set to false if doing a bigger graph
		System.out.println(comp.compare(true));
	}
}
