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

package de.uni_koblenz.jgralab.greql2.funlib.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.pcollections.PSet;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.graphmarker.GraphMarker;
import de.uni_koblenz.jgralab.greql2.evaluator.fa.DFA;
import de.uni_koblenz.jgralab.greql2.evaluator.fa.NFA;
import de.uni_koblenz.jgralab.greql2.evaluator.fa.State;
import de.uni_koblenz.jgralab.greql2.evaluator.fa.Transition;
import de.uni_koblenz.jgralab.greql2.funlib.Function;
import de.uni_koblenz.jgralab.greql2.types.pathsearch.PathSystemMarkerEntry;
import de.uni_koblenz.jgralab.greql2.types.pathsearch.PathSystemQueueEntry;

public class Slice extends Function {
	public Slice() {
		super(
				"Returns a slice, starting at the given root vertex or vertices and "
						+ " being structured according to the given path description.",
				1000, 1, 1.0, Category.GRAPH,
				Category.PATHS_AND_PATHSYSTEMS_AND_SLICES);
	}

	private Graph graph;

	public de.uni_koblenz.jgralab.greql2.types.Slice evaluate(Vertex v, NFA nfa) {
		return evaluate(v, nfa.getDFA());
	}

	public de.uni_koblenz.jgralab.greql2.types.Slice evaluate(Vertex v, DFA dfa) {
		return evaluate(JGraLab.<Vertex> set().plus(v), dfa);
	}

	public de.uni_koblenz.jgralab.greql2.types.Slice evaluate(
			PSet<Vertex> roots, NFA nfa) {
		return evaluate(roots, nfa.getDFA());
	}

	public de.uni_koblenz.jgralab.greql2.types.Slice evaluate(
			PSet<Vertex> roots, DFA dfa) {
		Set<Vertex> sliCritVertices = new HashSet<Vertex>();

		for (Vertex v : roots) {
			if (graph == null) {
				graph = v.getGraph();
			}
			sliCritVertices.add(v);
		}

		marker = new ArrayList<GraphMarker<Map<Edge, PathSystemMarkerEntry>>>(
				dfa.stateList.size());
		for (int i = 0; i < dfa.stateList.size(); i++) {
			marker.add(new GraphMarker<Map<Edge, PathSystemMarkerEntry>>(graph));
		}
		List<Vertex> leaves = markVerticesOfSlice(sliCritVertices, dfa);
		return createSliceFromMarkings(graph, sliCritVertices, leaves);
	}

	/**
	 * for each state in the fa (normally < 10) a seperate GraphMarker is used
	 */
	private List<GraphMarker<Map<Edge, PathSystemMarkerEntry>>> marker;

	/**
	 * Holds a set of states for each AttributedElement
	 */
	private GraphMarker<Set<State>> stateMarker;

	/**
	 * marks the given vertex with the given SliceMarker
	 * 
	 * @return true if the vertex was marked successful, false if it is already
	 *         marked with this parentEdge
	 */
	protected boolean markVertex(Vertex v, State s, Vertex parentVertex,
			Edge e, State ps, int d) {
		PathSystemMarkerEntry m = new PathSystemMarkerEntry(v, parentVertex, e,
				s, ps, d);
		GraphMarker<Map<Edge, PathSystemMarkerEntry>> currentMarker = marker
				.get(s.number);

		Map<Edge, PathSystemMarkerEntry> map = currentMarker.getMark(v);
		if (map == null) {
			map = new HashMap<Edge, PathSystemMarkerEntry>();
			currentMarker.mark(v, map);
		}

		PathSystemMarkerEntry entry = map.get(e);
		if (entry == null) {
			map.put(e, m);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Checks if the given vertex is marked with the given state and parent edge
	 * 
	 * @return true if the vertex is marked, false otherwise
	 */
	protected boolean isMarked(Vertex v, State s, Edge parentEdge) {
		GraphMarker<Map<Edge, PathSystemMarkerEntry>> currentMarker = marker
				.get(s.number);

		Map<Edge, PathSystemMarkerEntry> map = currentMarker.getMark(v);
		if (map != null) {
			return map.containsKey(parentEdge);
		} else {
			return false;
		}
	}

	/**
	 * Checks if the given vertex is marked with the given state
	 * 
	 * @return true if the vertex is marked, false otherwise
	 */
	protected boolean isMarked(Vertex v, State s) {
		GraphMarker<Map<Edge, PathSystemMarkerEntry>> currentMarker = marker
				.get(s.number);

		Map<Edge, PathSystemMarkerEntry> map = currentMarker.getMark(v);
		return map != null;
	}

	/**
	 * Marks all vertices that are part of the slice described by the given
	 * rootVertex and the regular path expression which is acceptes by the given
	 * dfa
	 * 
	 * @param sliCritVertices
	 *            the start vertices of the slice
	 * @param dfa
	 *            the DFA which accepts the regular path expression that
	 *            describes the slice
	 * @return the list of leaves in the slice, that is the set of states which
	 *         are marked with a final state of the dfa
	 * @throws EvaluateException
	 *             if something went wrong, several EvaluateException can be
	 *             thrown
	 */
	private List<Vertex> markVerticesOfSlice(Set<Vertex> sliCritVertices,
			DFA dfa) {
		// GreqlEvaluator.errprintln("Start marking vertices of slice");
		ArrayList<Vertex> finalVertices = new ArrayList<Vertex>();
		Queue<PathSystemQueueEntry> queue = new LinkedList<PathSystemQueueEntry>();
		PathSystemQueueEntry currentEntry;

		// fill queue with vertices in slicing criterion and mark these vertices
		for (Vertex v : sliCritVertices) {
			currentEntry = new PathSystemQueueEntry(v, dfa.initialState, null,
					null, 0);
			queue.offer(currentEntry);
			markVertex(v, dfa.initialState, null /* no parent state */,
					null /* no parent vertex */, null /* no parent state */, 0);
		}

		while (!queue.isEmpty()) {
			currentEntry = queue.poll();
			if (currentEntry.state.isFinal) {
				finalVertices.add(currentEntry.vertex);
			}
			for (Edge inc : currentEntry.vertex.incidences()) {
				for (Transition currentTransition : currentEntry.state.outTransitions) {
					Vertex nextVertex = currentTransition.getNextVertex(
							currentEntry.vertex, inc);
					if (!isMarked(nextVertex, currentTransition.endState, inc)
							&& currentTransition.accepts(currentEntry.vertex,
									inc)) {
						Edge traversedEdge = currentTransition.consumesEdge() ? inc
								: null;
						/*
						 * if the vertex is not marked with the state, add it to
						 * the queue for further processing - the parent edge
						 * doesn't matter but only the state
						 */
						if (!isMarked(nextVertex, currentTransition.endState)) {
							queue.add(new PathSystemQueueEntry(nextVertex,
									currentTransition.endState, traversedEdge,
									currentEntry.state, 0));
						}
						/* mark the vertex with all reachability information */
						markVertex(nextVertex, currentTransition.endState,
								currentEntry.vertex, traversedEdge,
								currentEntry.state, 0);
					}
				}
			}
		}

		return finalVertices;
	}

	/**
	 * Creates a JValueSlice-object which contains all path which start at the
	 * given start vertices and end with the given leaves
	 * 
	 * @param leaves
	 * @return
	 */
	private de.uni_koblenz.jgralab.greql2.types.Slice createSliceFromMarkings(
			Graph graph, Set<Vertex> sliCritVertices, List<Vertex> leaves) {
		de.uni_koblenz.jgralab.greql2.types.Slice slice = new de.uni_koblenz.jgralab.greql2.types.Slice(
				graph);

		Map<Edge, PathSystemMarkerEntry> sliCritVertexMarkerMap;
		PathSystemMarkerEntry sliCritVertexMarker;
		GraphMarker<Map<Edge, PathSystemMarkerEntry>> startStateMarker = marker
				.get(0);

		// add slicing criterion vertices to slice
		for (Vertex v : sliCritVertices) {
			sliCritVertexMarkerMap = startStateMarker.getMark(v);
			sliCritVertexMarker = sliCritVertexMarkerMap.get(null);
			slice.addSlicingCriterionVertex(v,
					sliCritVertexMarker.state.number,
					sliCritVertexMarker.state.isFinal);
		}

		Queue<Vertex> queue = new LinkedList<Vertex>();
		Vertex currentVertex, parentVertex;
		State parentState;
		stateMarker = new GraphMarker<Set<State>>(graph);
		GraphMarker<State> currentStateMarker = new GraphMarker<State>(graph);

		for (Vertex leaf : leaves) { // iterate through leaves
			// iterate through GraphMarkers (one for each state)
			for (GraphMarker<Map<Edge, PathSystemMarkerEntry>> currentGraphMarker : marker) {
				if (currentGraphMarker.getMark(leaf) != null) {
					// iterate through list of PathSystemMarkerEntrys
					// for a particular GraphMarker (a particular state)
					for (PathSystemMarkerEntry currentMarker : currentGraphMarker
							.getMark(leaf).values()) {
						// if state of current PathSystemMarkerEntry is final or
						if (!currentMarker.state.isFinal
								|| isVertexMarkedWithState(leaf,
										currentMarker.state)) {
							// (leaf, state) has already been processed
							continue;
						}
						// remember that (leaf, state) has already been
						// processed
						markVertexWithState(leaf, currentMarker.state);
						// mark leaf with current state
						currentStateMarker.mark(leaf, currentMarker.state);
						queue.add(leaf);
						while (!queue.isEmpty()) {
							currentVertex = queue.poll();
							for (PathSystemMarkerEntry marker : getMarkersWithState(
									currentVertex,
									currentStateMarker.getMark(currentVertex))
									.values()) {
								int parentStateNumber = 0;
								parentState = marker.parentState;
								if (parentState != null) {
									parentStateNumber = parentState.number;
								}
								slice.addVertex(currentVertex,
										marker.state.number,
										marker.edgeToParentVertex,
										marker.parentVertex, parentStateNumber,
										marker.state.isFinal);
								parentVertex = marker.parentVertex;
								if ((parentVertex != null)
										&& !isVertexMarkedWithState(
												parentVertex, parentState)) {
									// if (parentVertex, parentState) has not
									// already
									// been processed, remember that is has been
									// processed now
									markVertexWithState(parentVertex,
											parentState);
									currentStateMarker.mark(parentVertex,
											parentState);
									queue.add(parentVertex); // add parentVertex
									// to queue
								}
							}
						}
					}
				}
			}
		}

		return slice;
	}

	/**
	 * Adds the given state to the set of states maintained for the given
	 * vertex.
	 * 
	 * @param v
	 *            the vertex to be marked
	 * @param s
	 *            the state which shall be added to {@code v}'s state set
	 */
	private void markVertexWithState(Vertex v, State s) {
		if (stateMarker.getMark(v) == null) {
			stateMarker.mark(v, new HashSet<State>());
		}
		stateMarker.getMark(v).add(s);
	}

	/**
	 * Checks if the given vertex' state set contains the given state.
	 * 
	 * @param v
	 *            the vertex to be checked
	 * @param s
	 *            the state to be checked for
	 * @return true, if {@code v}'s set of states contains {@code s}, false else
	 */
	private boolean isVertexMarkedWithState(Vertex v, State s) {
		if (stateMarker.getMark(v) == null) {
			return false;
		}
		return stateMarker.getMark(v).contains(s);
	}

	/**
	 * Returns the {@code PathSystemMarkerEntry} for a given vertex and state.
	 * 
	 * @param v
	 *            the vertex for which to return the
	 *            {@code PathSystemMarkerEntry}
	 * @param s
	 *            the state for which to return the
	 *            {@code PathSystemMarkerEntry}
	 * @return the {@code PathSystemMarkerEntry} for {@code v} and {@code s}
	 */
	private Map<Edge, PathSystemMarkerEntry> getMarkersWithState(Vertex v,
			State s) {
		if (v == null) {
			return null;
		}
		GraphMarker<Map<Edge, PathSystemMarkerEntry>> currentMarker = marker
				.get(s.number);
		return currentMarker.getMark(v);
	}

	@Override
	public long getEstimatedCosts(ArrayList<Long> inElements) {
		return 1000;
	}

	@Override
	public double getSelectivity() {
		return 0.001f;
	}

	@Override
	public long getEstimatedCardinality(int inElements) {
		return 1;
	}

}
