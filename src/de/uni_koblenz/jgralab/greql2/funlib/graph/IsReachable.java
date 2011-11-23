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

import java.util.LinkedList;
import java.util.Queue;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.graphmarker.BooleanGraphMarker;
import de.uni_koblenz.jgralab.greql2.evaluator.fa.DFA;
import de.uni_koblenz.jgralab.greql2.evaluator.fa.State;
import de.uni_koblenz.jgralab.greql2.evaluator.fa.Transition;
import de.uni_koblenz.jgralab.greql2.funlib.Function;
import de.uni_koblenz.jgralab.greql2.types.pathsearch.PathSearchQueueEntry;

public class IsReachable extends Function {

	public static boolean PRINT_STOP_VERTICES = false;

	public IsReachable() {
		super(
				"Returns true, iff there is a path from vertex given as first argument to vertex "
						+ "given as second argument that matches the path description given as second argument. "
						+ "Usually invoked like so: myVertex (--> | <>--)+ myOtherVertex.",
				50, 1, 0.01, Category.GRAPH,
				Category.PATHS_AND_PATHSYSTEMS_AND_SLICES);
	}

	public Boolean evaluate(Vertex u, Vertex v, DFA dfa) {
		if (u.getGraph() != v.getGraph()) {
			throw new IllegalArgumentException(
					"The vertices are in different graphs, but must be in the same graph.");
		}
		BooleanGraphMarker[] markers = new BooleanGraphMarker[dfa.stateList
				.size()];
		for (State s : dfa.stateList) {
			markers[s.number] = new BooleanGraphMarker(u.getGraph());
		}
		Queue<PathSearchQueueEntry> queue = new LinkedList<PathSearchQueueEntry>();
		PathSearchQueueEntry currentEntry = new PathSearchQueueEntry(u,
				dfa.initialState);
		markers[currentEntry.state.number].mark(currentEntry.vertex);
		queue.add(currentEntry);
		while (!queue.isEmpty()) {
			currentEntry = queue.poll();
			if ((currentEntry.vertex == v) && currentEntry.state.isFinal) {
				return true;
			}
			for (Edge inc : currentEntry.vertex.incidences()) {
				for (Transition currentTransition : currentEntry.state.outTransitions) {
					Vertex nextVertex = currentTransition.getNextVertex(
							currentEntry.vertex, inc);
					boolean isMarked = markers[currentTransition.endState.number]
							.isMarked(nextVertex);
					boolean transitionIsPossible = currentTransition.accepts(
							currentEntry.vertex, inc);
					if (!isMarked && transitionIsPossible) {
						PathSearchQueueEntry nextEntry = new PathSearchQueueEntry(
								nextVertex, currentTransition.endState);
						markers[nextEntry.state.number].mark(nextVertex);
						queue.add(nextEntry);
					}
				}
			}
		}
		return false;
	}
}
