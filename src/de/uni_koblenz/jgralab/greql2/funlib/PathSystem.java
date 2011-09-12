/* JGraLab - The Java Graph Laboratory
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

package de.uni_koblenz.jgralab.greql2.funlib;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;


import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.graphmarker.SubGraphMarker;
import de.uni_koblenz.jgralab.graphmarker.GraphMarker;
import de.uni_koblenz.jgralab.greql2.evaluator.fa.DFA;
import de.uni_koblenz.jgralab.greql2.evaluator.fa.State;
import de.uni_koblenz.jgralab.greql2.evaluator.fa.Transition;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.WrongFunctionParameterException;
import de.uni_koblenz.jgralab.greql2.funlib.pathsearch.PathSystemMarkerEntry;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValuePathSystem;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueType;

/**
 * Returns a pathsystem, based on the current graph and the given dfa, whose
 * root is the given vertex.
 * 
 * <dl>
 * <dt><b>GReQL-signature</b></dt>
 * <dd><code>PATHSYSTEM pathSystem(v:VERTEX, dfa:AUTOMATON)</code></dd>
 * <dd>&nbsp;</dd>
 * <dd>This function can be used with the (<code>:-)</code>)-Operator:
 * <code>v :-) rpe</code></dd>
 * <dd><code>rpe</code> is a regular path expression.</dd>
 * <dd>&nbsp;</dd>
 * </dl>
 * <dl>
 * <dt></dt>
 * <dd>
 * <dl>
 * <dt><b>Parameters:</b></dt>
 * <dd><code>v</code> - root of the returned pathsystem</dd>
 * <dd><code>dfa</code> - a dfa that accepts regular path expressions</dd>
 * <dt><b>Returns:</b></dt>
 * <dd>a pathsystem, based on the current graph and the given dfa, whose root is
 * the given vertex</dd>
 * <dd><code>Null</code> if one of the given parameters is <code>Null</code></dd>
 * </dl>
 * </dd>
 * </dl>
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class PathSystem extends Greql2Function {

	{
		JValueType[][] x = { { JValueType.VERTEX, JValueType.AUTOMATON,
				JValueType.PATHSYSTEM } };
		signatures = x;

		description = "Returns a pathsystem with root vertex, which is structured according to path description.";

		Category[] c = { Category.PATHS_AND_PATHSYSTEMS_AND_SLICES };
		categories = c;
	}

	/**
	 * for each state in the finite automaton a seperate GraphMarker is used
	 */
	private List<GraphMarker<PathSystemMarkerEntry>> marker;

	/**
	 * Holds a set of states for each AttributedElement
	 */
	// private GraphMarker<Set<State>> stateMarker;

	/**
	 * marks the given vertex with the given PathSystemMarker
	 * 
	 * @return the marker created
	 */
	protected PathSystemMarkerEntry markVertex(Vertex v, State s,
			Vertex parentVertex, Edge e, State ps, int d) {
		PathSystemMarkerEntry m = new PathSystemMarkerEntry(v, parentVertex, e,
				s, ps, d);
		
		GraphMarker<PathSystemMarkerEntry> currentMarker = marker.get(s.number);
		currentMarker.mark(v, m);
		
		return m;
	}

	/**
	 * Checks if the given vertex is marked with the given state
	 * 
	 * @return true if the vertex is marked, false otherwise
	 */
	protected boolean isMarked(Vertex v, State s) {
		GraphMarker<PathSystemMarkerEntry> currentMarker = marker
				.get(s.number); 
		
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
	 * @param subgraph
	 *            the subgraph all parts of the pathsystem belong to
	 * @return the set of leaves in the pathsystem, that is the set of states
	 *         which are marked with a final state of the dfa
	 * @throws EvaluateException
	 *             if something went wrong, several EvaluateException can be
	 *             thrown
	 */
	private Set<PathSystemMarkerEntry> markVerticesOfPathSystem(
			Vertex startVertex, DFA dfa,
			SubGraphMarker subgraph)
			throws EvaluateException {
		Set<PathSystemMarkerEntry> finalEntries = new HashSet<PathSystemMarkerEntry>();
		Queue<PathSystemMarkerEntry> queue = new LinkedList<PathSystemMarkerEntry>();
		PathSystemMarkerEntry currentEntry = markVertex(startVertex,
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
				
					if (!isMarked(nextVertex, currentTransition.endState)) {
						if (currentTransition.accepts(currentVertex, inc,
								subgraph)) {
							Edge traversedEdge = currentTransition
									.consumesEdge() ? inc : null;
							PathSystemMarkerEntry newEntry = markVertex(
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
	 * creates the pathsystem
	 */
	@Override
	public JValue evaluate(Graph graph,
			SubGraphMarker subgraph, JValue[] arguments)
			throws EvaluateException {
		DFA dfa = null;
		switch (checkArguments(arguments)) {
		case 0:
			dfa = arguments[1].toAutomaton().getDFA();
			break;
		default:
			throw new WrongFunctionParameterException(this, arguments);
		}

		Vertex startVertex = arguments[0].toVertex();

		marker = new ArrayList<GraphMarker<PathSystemMarkerEntry>>(
				dfa.stateList.size());
		for (int i = 0; i < dfa.stateList.size(); i++) {
			marker.add(new GraphMarker<PathSystemMarkerEntry>(
					graph));
		}
		Set<PathSystemMarkerEntry> leaves = markVerticesOfPathSystem(
				startVertex, dfa, subgraph);
		JValuePathSystem resultPathSystem = createPathSystemFromMarkings(
				startVertex, leaves);
		return resultPathSystem;
	}

	/**
	 * Creates a JValuePathSystem-object which contains all paths which start at
	 * the given root vertex and end with one of the given leaves
	 * 
	 * @param leaves
	 * @return
	 */
	private JValuePathSystem createPathSystemFromMarkings(Vertex rootVertex,
			Set<PathSystemMarkerEntry> leafEntries) {
		JValuePathSystem pathSystem = new JValuePathSystem(
				rootVertex.getGraph());
		PathSystemMarkerEntry rootMarker = marker.get(0)
				.getMark(rootVertex);
		pathSystem.setRootVertex(rootVertex, rootMarker.state.number,
				rootMarker.state.isFinal);
		
		for (PathSystemMarkerEntry currentMarker : leafEntries) {
			Vertex currentVertex = currentMarker.vertex;
			while (currentVertex != null) { // &&
											// !isVertexMarkedWithState(currentVertex,
											// currentMarker.state)
				int parentStateNumber = 0;
				if (currentMarker.parentState != null) {
					parentStateNumber = currentMarker.parentState.number;
				}
				pathSystem.addVertex(currentVertex, currentMarker.state.number,
						currentMarker.edgeToParentVertex,
						currentMarker.parentVertex, parentStateNumber,
						currentMarker.distanceToRoot,
						currentMarker.state.isFinal);
				currentVertex = currentMarker.parentVertex;
				currentMarker = getMarkerWithState(currentVertex,
						currentMarker.parentState);
			}
		}
		pathSystem.finish();
		return pathSystem;
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
	private PathSystemMarkerEntry getMarkerWithState(Vertex v, State s) {
		if (v == null) {
			return null;
		}
		GraphMarker<PathSystemMarkerEntry> currentMarker = marker
				.get(s.number);
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
	public long getEstimatedCardinality(int inElements) {
		return 1;
	}

}
