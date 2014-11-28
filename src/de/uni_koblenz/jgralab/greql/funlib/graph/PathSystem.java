/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2014 Institute for Software Technology
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

package de.uni_koblenz.jgralab.greql.funlib.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.graphmarker.GraphMarker;
import de.uni_koblenz.jgralab.greql.evaluator.InternalGreqlEvaluator;
import de.uni_koblenz.jgralab.greql.evaluator.fa.DFA;
import de.uni_koblenz.jgralab.greql.evaluator.fa.State;
import de.uni_koblenz.jgralab.greql.evaluator.fa.Transition;
import de.uni_koblenz.jgralab.greql.funlib.Description;
import de.uni_koblenz.jgralab.greql.funlib.Function;
import de.uni_koblenz.jgralab.greql.funlib.NeedsEvaluatorArgument;
import de.uni_koblenz.jgralab.greql.types.PathSystem.PathSystemNode;
import de.uni_koblenz.jgralab.greql.types.pathsearch.PathSystemMarkerEntry;

@NeedsEvaluatorArgument
public class PathSystem extends Function {

	@Description(params = { "internal", "startVertex", "fa" }, description = "Returns a path system with the given root vertex, which is structured according to the given path description.", categories = Category.PATHS_AND_PATHSYSTEMS_AND_SLICES)
	public PathSystem() {
		super(1000, 1, 1.0);
	}

	public de.uni_koblenz.jgralab.greql.types.PathSystem evaluate(
			InternalGreqlEvaluator evaluator, Vertex startVertex, DFA dfa) {
		@SuppressWarnings("unchecked")
		GraphMarker<PathSystemMarkerEntry>[] marker = new GraphMarker[dfa.stateList
				.size()];
		for (int i = 0; i < dfa.stateList.size(); i++) {
			marker[i] = new GraphMarker<>(startVertex.getGraph());
		}
		Set<PathSystemMarkerEntry> leaves = markVerticesOfPathSystem(evaluator,
				marker, startVertex, dfa);
		de.uni_koblenz.jgralab.greql.types.PathSystem resultPathSystem = createPathSystemFromMarkings(
				marker, startVertex, leaves);
		return resultPathSystem;
	}

	/**
	 * marks the given vertex with the given PathSystemMarker
	 * 
	 * @return the marker created
	 */
	protected PathSystemMarkerEntry markVertex(
			GraphMarker<PathSystemMarkerEntry>[] marker, Vertex v, State s,
			Vertex parentVertex, Edge e, State ps, int d) {
		PathSystemMarkerEntry m = new PathSystemMarkerEntry(v, parentVertex, e,
				s, ps, d);

		GraphMarker<PathSystemMarkerEntry> currentMarker = marker[s.number];
		currentMarker.mark(v, m);

		return m;
	}

	/**
	 * Checks if the given vertex is marked with the given state
	 * 
	 * @return true if the vertex is marked, false otherwise
	 */
	protected boolean isMarked(GraphMarker<PathSystemMarkerEntry>[] marker,
			Vertex v, State s) {
		GraphMarker<PathSystemMarkerEntry> currentMarker = marker[s.number];
		return currentMarker.isMarked(v);
	}

	/**
	 * Marks all vertices that are part of the PathSystem described by the given
	 * rootVertex and the regular path expression which is acceptes by the given
	 * dfa
	 * 
	 * @param startVertex
	 *            the rootVertex of the PathSystem
	 * @param dfa
	 *            the DFA which accepts the regular path expression that
	 *            describes the pathsystem
	 * @return the set of leaves in the pathsystem, that is the set of states
	 *         which are marked with a final state of the dfa
	 * @throws EvaluateException
	 *             if something went wrong, several EvaluateException can be
	 *             thrown
	 */
	private Set<PathSystemMarkerEntry> markVerticesOfPathSystem(
			InternalGreqlEvaluator evaluator,
			GraphMarker<PathSystemMarkerEntry>[] marker, Vertex startVertex,
			DFA dfa) {
		Set<PathSystemMarkerEntry> finalEntries = new HashSet<>();
		Queue<PathSystemMarkerEntry> queue = new LinkedList<>();
		PathSystemMarkerEntry currentEntry = markVertex(marker, startVertex,
				dfa.initialState, null /* no parent vertex */, null /*
																	 * no parent
																	 * edge
																	 */,
				null /* no parent state */, 0);
		if (dfa.initialState.isFinal) {
			finalEntries.add(currentEntry);
		}
		queue.add(currentEntry);
		while (!queue.isEmpty()) {
			currentEntry = queue.poll();
			Vertex currentVertex = currentEntry.vertex;

			for (Edge inc : currentVertex.incidences()) {
				for (Transition currentTransition : currentEntry.state.outTransitions) {
					Vertex nextVertex = currentTransition.getNextVertex(
							currentVertex, inc);

					if (!isMarked(marker, nextVertex,
							currentTransition.endState)) {
						if (currentTransition.accepts(currentVertex, inc,
								evaluator)) {
							Edge traversedEdge = currentTransition
									.consumesEdge() ? inc : null;
							PathSystemMarkerEntry newEntry = markVertex(marker,
									nextVertex, currentTransition.endState,
									currentVertex, traversedEdge,
									currentEntry.state,
									currentEntry.distanceToRoot + 1);
							if (currentTransition.endState.isFinal) {
								finalEntries.add(newEntry);
							}
							queue.add(newEntry);
						}
					}
				}
			}
		}

		return finalEntries;
	}

	/**
	 * Creates a JValuePathSystem-object which contains all paths which start at
	 * the given root vertex and end with one of the given leaves
	 * 
	 * @param leaves
	 * @return
	 */
	private de.uni_koblenz.jgralab.greql.types.PathSystem createPathSystemFromMarkings(
			GraphMarker<PathSystemMarkerEntry>[] marker, Vertex rootVertex,
			Set<PathSystemMarkerEntry> leafEntries) {
		de.uni_koblenz.jgralab.greql.types.PathSystem pathSystem = new de.uni_koblenz.jgralab.greql.types.PathSystem();
		Map<Vertex, PathSystemNode[]> vertex2state2node = new HashMap<>();
		Map<Vertex, PathSystemMarkerEntry[]> vertex2state2marker = new HashMap<>();
		Queue<PathSystemNode> nodesWithoutParentEdge = new LinkedList<>();
		PathSystemMarkerEntry rootMarker = marker[0].getMark(rootVertex);
		PathSystemNode root = pathSystem.setRootVertex(rootVertex,
				rootMarker.state.number, rootMarker.state.isFinal);

		PathSystemNode[] currentState2node = new PathSystemNode[marker.length];
		currentState2node[rootMarker.state.number] = root;
		vertex2state2node.put(rootVertex, currentState2node);

		for (PathSystemMarkerEntry currentMarker : leafEntries) {
			Vertex currentVertex = currentMarker.vertex;
			while (currentVertex != null) {
				// && !isVertexMarkedWithState(currentVertex,
				// currentMarker.state)

				PathSystemNode childNode = addVertexToPathSystem(pathSystem,
						vertex2state2node, marker.length, currentVertex,
						currentMarker.state.number, currentMarker.state.isFinal);
				if (currentMarker.edgeToParentVertex != null) {
					PathSystemNode parentNode = addVertexToPathSystem(
							pathSystem,
							vertex2state2node,
							marker.length,
							currentMarker.parentVertex,
							currentMarker.parentState != null ? currentMarker.parentState.number
									: 0,
							currentMarker.parentState != null ? currentMarker.parentState.isFinal
									: false);
					pathSystem.addEdge(childNode, parentNode,
							currentMarker.edgeToParentVertex);
				} else if ((currentVertex != pathSystem.getRootVertex() || currentMarker.distanceToRoot != 0)
						&& !nodesWithoutParentEdge.contains(childNode)) {
					nodesWithoutParentEdge.add(childNode);
					PathSystemMarkerEntry[] currentMarkerEntry = vertex2state2marker
							.get(currentVertex);
					if (currentMarkerEntry == null) {
						currentMarkerEntry = new PathSystemMarkerEntry[marker.length];
						vertex2state2marker.put(currentVertex,
								currentMarkerEntry);
					}
					assert currentMarkerEntry[currentMarker.state.number] == null : "already exiting:"
							+ currentMarkerEntry[currentMarker.state.number]
							+ " new: " + currentMarker;
					currentMarkerEntry[currentMarker.state.number] = currentMarker;
				}
				currentVertex = currentMarker.parentVertex;
				currentMarker = getMarkerWithState(marker, currentVertex,
						currentMarker.parentState);
			}
		}
		completePathSystem(pathSystem, nodesWithoutParentEdge,
				vertex2state2marker, vertex2state2node);
		pathSystem.finish();
		return pathSystem;
	}

	private PathSystemNode addVertexToPathSystem(
			de.uni_koblenz.jgralab.greql.types.PathSystem pathSystem,
			Map<Vertex, PathSystemNode[]> vertex2state2node,
			int numberOfStates, Vertex currentVertex, int state, boolean isFinal) {
		assert currentVertex != null;
		PathSystemNode[] currentState2node = vertex2state2node
				.get(currentVertex);
		if (currentState2node == null) {
			currentState2node = new PathSystemNode[numberOfStates];
			vertex2state2node.put(currentVertex, currentState2node);
		} else {
			PathSystemNode currentNode = currentState2node[state];
			if (currentNode != null) {
				if (isFinal) {
					pathSystem.addLeaf(currentNode);
				}
				return currentNode;
			}
		}
		PathSystemNode currentNode = pathSystem.addVertex(currentVertex, state,
				isFinal);
		currentState2node[state] = currentNode;
		return currentNode;
	}

	/**
	 * to some vertices there is a path with an vertex restriction on the end
	 * and thus the last transition in the dfa does not accept an edge - hence,
	 * the parent edge is not set. This method finds those vertices and set the
	 * edge information
	 */
	private void completePathSystem(
			de.uni_koblenz.jgralab.greql.types.PathSystem pathSystem,
			Queue<PathSystemNode> nodesWithoutParentEdge,
			Map<Vertex, PathSystemMarkerEntry[]> vertex2state2marker,
			Map<Vertex, PathSystemNode[]> vertex2state2node) {
		while (!nodesWithoutParentEdge.isEmpty()) {
			PathSystemNode te = nodesWithoutParentEdge.poll();
			PathSystemNode pe = null;
			PathSystemMarkerEntry[] teMarker = vertex2state2marker
					.get(te.currentVertex);
			assert teMarker != null;
			if (teMarker[te.state] != null) {
				pe = vertex2state2node.get(teMarker[te.state].parentVertex)[teMarker[te.state].parentState.number];
			} else {
				pe = pathSystem.getRoot();
			}

			// if pe is null, te is the entry of the root vertex
			if (pe != null) {
				Set<PathSystemNode> parents = pathSystem.getParents(pe);
				assert parents.size() <= 1;
				for (PathSystemNode parent : parents) {
					pathSystem.addEdge(te, parent, pe.edge2parent);
				}
			}
		}
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
	private PathSystemMarkerEntry getMarkerWithState(
			GraphMarker<PathSystemMarkerEntry>[] marker, Vertex v, State s) {
		if (v == null) {
			return null;
		}
		GraphMarker<PathSystemMarkerEntry> currentMarker = marker[s.number];
		PathSystemMarkerEntry entry = currentMarker.getMark(v);
		return entry;
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
	public long getEstimatedCardinality(long inElements) {
		return 1;
	}

}
