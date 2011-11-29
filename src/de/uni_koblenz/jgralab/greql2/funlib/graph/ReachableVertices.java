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

package de.uni_koblenz.jgralab.greql2.funlib.graph;

import java.util.HashSet;

import org.pcollections.PSet;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.evaluator.fa.DFA;
import de.uni_koblenz.jgralab.greql2.evaluator.fa.State;
import de.uni_koblenz.jgralab.greql2.evaluator.fa.Transition;
import de.uni_koblenz.jgralab.greql2.funlib.Function;
import de.uni_koblenz.jgralab.greql2.types.pathsearch.VertexStateQueue;

public class ReachableVertices extends Function {
	public ReachableVertices() {
		super(
				"Returns all vertices that are reachable from the given vertex by a path matching the the given path description.",
				100, 10, 1.0, Category.GRAPH,
				Category.PATHS_AND_PATHSYSTEMS_AND_SLICES);
	}

	public PSet<Vertex> evaluate(Vertex v, DFA dfa) {
		return search(v, dfa);
	}

	public static PSet<Vertex> search(Vertex v, DFA dfa) {
		PSet<Vertex> resultSet = JGraLab.set();

		@SuppressWarnings("unchecked")
		HashSet<Vertex>[] markedElements = new HashSet[dfa.stateList.size()];
		// BitSet[] markedElements = new BitSet[dfa.stateList.size()];
		for (State s : dfa.stateList) {
			// markedElements[s.number] = new BitSet();
			markedElements[s.number] = new HashSet<Vertex>(100);
		}
		VertexStateQueue queue = new VertexStateQueue();
		markedElements[dfa.initialState.number].add(v);
		queue.put(v, dfa.initialState);
		while (queue.hasNext()) {
			Vertex vertex = queue.currentVertex;
			State state = queue.currentState;
			if (state.isFinal) {
				resultSet = resultSet.plus(vertex);
			}
			for (Edge inc = vertex.getFirstIncidence(); inc != null; inc = inc
					.getNextIncidence()) {
				int size = state.outTransitions.size();
				for (int i = 0; i < size; i++) {
					Transition currentTransition = state.outTransitions.get(i);
					Vertex nextVertex = currentTransition.getNextVertex(vertex,
							inc);
					if (!markedElements[currentTransition.endState.number]
							.contains(nextVertex)) {
						if (currentTransition.accepts(vertex, inc)) {
							markedElements[currentTransition.endState.number]
									.add(nextVertex);
							queue.put(nextVertex, currentTransition.endState);
						}
					}
				}
			}
		}
		return resultSet;
	}
}
