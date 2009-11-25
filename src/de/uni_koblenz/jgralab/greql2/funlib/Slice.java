/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2009 Institute for Software Technology
 *               University of Koblenz-Landau, Germany
 *
 *               ist@uni-koblenz.de
 *
 * Please report bugs to http://serres.uni-koblenz.de/bugzilla
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
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
import de.uni_koblenz.jgralab.graphmarker.BooleanGraphMarker;
import de.uni_koblenz.jgralab.graphmarker.GraphMarker;
import de.uni_koblenz.jgralab.greql2.evaluator.fa.DFA;
import de.uni_koblenz.jgralab.greql2.evaluator.fa.State;
import de.uni_koblenz.jgralab.greql2.evaluator.fa.Transition;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.WrongFunctionParameterException;
import de.uni_koblenz.jgralab.greql2.funlib.pathsearch.PathSystemMarkerEntry;
import de.uni_koblenz.jgralab.greql2.funlib.pathsearch.PathSystemMarkerList;
import de.uni_koblenz.jgralab.greql2.funlib.pathsearch.PathSystemQueueEntry;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueSet;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueSlice;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueType;

/**
 * Returns a slice, based on the current graph and the given dfa, whose root is
 * the given vertex.
 * 
 * <dl>
 * <dt><b>GReQL-signature</b></dt>
 * <dd><code>SLICE slice(v:SET&lt;VERTEX&gt;, dfa:DFA)</code></dd>
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
public class Slice extends AbstractGreql2Function {

	{
		JValueType[][] x = { { JValueType.COLLECTION, JValueType.DFA },
				{ JValueType.COLLECTION, JValueType.NFA },
				{ JValueType.VERTEX, JValueType.DFA },
				{ JValueType.VERTEX, JValueType.NFA } };
		signatures = x;
	}

	/**
	 * for each state in the fa (normally < 10) a seperate GraphMarker is used
	 */
	private ArrayList<GraphMarker<PathSystemMarkerList>> marker;

	/**
	 * The graph the search is performed on
	 */
	private Graph graph;

	/**
	 * Holds a set of states for each AttributedElement
	 */
	private GraphMarker<Set<State>> stateMarker;

	/**
	 * marks the given vertex with the given PathSystemMarker
	 * 
	 * @return true if the vertex was marked successfull, false if it is already
	 *         marked with this state
	 */
	protected boolean markVertex(Vertex v, State s, Vertex parentVertex,
			Edge e, State ps, int d) {
		PathSystemMarkerEntry m = new PathSystemMarkerEntry(parentVertex, e, s,
				ps, d);
		GraphMarker<PathSystemMarkerList> currentMarker = marker.get(s.number);
		if (currentMarker == null) {
			currentMarker = new GraphMarker<PathSystemMarkerList>(graph);
			marker.set(s.number, currentMarker);
		}
		PathSystemMarkerList list = currentMarker.getMark(v);
		if (list == null) {
			list = new PathSystemMarkerList(s, v);
			currentMarker.mark(v, list);
		}
		list.put(parentVertex, m);
		return true;
	}

	/**
	 * Checks if the given vertex is marked with the given state and parent edge
	 * 
	 * @return true if the vertex is marked, false otherwise
	 */
	protected boolean isMarked(Vertex v, State s, Edge parentEdge) {
		GraphMarker<PathSystemMarkerList> currentMarker = marker.get(s.number);
		if (currentMarker == null) {
			return false;
		}
		PathSystemMarkerList list = currentMarker.getMark(v);
		if (list != null) {
			for (PathSystemMarkerEntry entry : list.values()) {
				if (entry.edgeToParentVertex == parentEdge) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Checks if the given vertex is marked with the given state and parent
	 * vertex
	 * 
	 * @return true if the vertex is marked, false otherwise
	 */
	protected boolean isMarked(Vertex v, State s) {
		GraphMarker<PathSystemMarkerList> currentMarker = marker.get(s.number);
		if (currentMarker == null) {
			return false;
		}
		PathSystemMarkerList list = currentMarker.getMark(v);
		return list != null;
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
	 * @param subgraph
	 *             the subgraph all parts of the slice belong to
	 * @return the list of leaves in the slice, that is the set of states which
	 *         are marked with a final state of the dfa
	 * @throws EvaluateException
	 *             if something went wrong, several EvaluateException can be
	 *             thrown
	 */
	private List<Vertex> markVerticesOfSlice(Set<Vertex> sliCritVertices,
			DFA dfa, BooleanGraphMarker subgraph) throws EvaluateException {
		// GreqlEvaluator.errprintln("Start marking vertices of slice");
		ArrayList<Vertex> finalVertices = new ArrayList<Vertex>();
		Queue<PathSystemQueueEntry> queue = new LinkedList<PathSystemQueueEntry>();
		PathSystemQueueEntry currentEntry;

		// fill queue with vertices in slicing criterion and mark these vertices
		for (Vertex v : sliCritVertices) {
			currentEntry = new PathSystemQueueEntry(v, dfa.initialState, null, null, 0);
			queue.offer(currentEntry);
			markVertex(v, dfa.initialState, null /* no parent state */,
					null /* no parent vertex */, null /* no parent state */, 
					0 /* distance to root is null */);
		}

		while (!queue.isEmpty()) {
			currentEntry = queue.poll();
			if (currentEntry.state.isFinal) {
				finalVertices.add(currentEntry.vertex);
			}
			for (Edge inc : currentEntry.vertex.incidences()) {
				for (Transition currentTransition : currentEntry.state.outTransitions) {
					Vertex nextVertex = currentTransition.getNextVertex(currentEntry.vertex, inc);
				    if ((!isMarked(nextVertex, currentTransition.getEndState(), inc)) &&
						 (currentTransition.accepts(currentEntry.vertex, inc, subgraph))) {
						Edge traversedEdge = inc;
						int distanceToRoot = currentEntry.distanceToRoot;
						if (nextVertex == currentEntry.vertex) {
							traversedEdge = null;
						} else {
							distanceToRoot++;
						}						
						/* if the vertex is not marked with the state, add it to
						 * the queue for further processing - the parent edge doesn't
						 * matter but only the state
						 */
						if (!isMarked(nextVertex, currentTransition.getEndState())) {
							queue.add(new PathSystemQueueEntry(nextVertex,
									currentTransition.getEndState(), traversedEdge,
									currentEntry.state, distanceToRoot));
						}
						/* mark the vertex with all reachability information */
						markVertex(nextVertex, currentTransition.getEndState(), 
								   currentEntry.vertex, traversedEdge,
					   			   currentEntry.state, 	distanceToRoot);
					}
				}
			}
		}

		return finalVertices;
	}

	/**
	 * creates the slice
	 */
	public JValue evaluate(Graph graph, BooleanGraphMarker subgraph,
			JValue[] arguments) throws EvaluateException {
		Set<Vertex> sliCritVertices = new HashSet<Vertex>();
		this.graph = graph;
		DFA dfa;
		JValueSet vertices;
		switch (checkArguments(arguments)) {
		case 0:
			dfa = arguments[1].toDFA();
			vertices = arguments[0].toJValueSet();
			break;
		case 1:
			dfa = new DFA(arguments[1].toNFA());
			vertices = arguments[0].toJValueSet();
			break;
		case 2:
			vertices = new JValueSet();
			vertices.add(arguments[0]);
			dfa = arguments[1].toDFA();
			break;
		case 3:
			vertices = new JValueSet();
			vertices.add(arguments[0]);
			dfa = new DFA(arguments[1].toNFA());
			break;
		default:
			throw new WrongFunctionParameterException(this, arguments);
		}

		for (JValue v : vertices) {
			sliCritVertices.add(v.toVertex());
		}

		marker = new ArrayList<GraphMarker<PathSystemMarkerList>>(dfa.stateList
				.size());
		for (int i = 0; i < dfa.stateList.size(); i++) {
			marker.add(new GraphMarker<PathSystemMarkerList>(graph));
		}
		List<Vertex> leaves = markVerticesOfSlice(sliCritVertices, dfa,
				subgraph);
		JValueSlice resultSlice = createSliceFromMarkings(graph,
				sliCritVertices, leaves);
		return resultSlice;
	}

	/**
	 * Creates a JValueSlice-object which contains all path which start at the
	 * given start vertices and end with the given leaves
	 * 
	 * @param leaves
	 * @return
	 */
	private JValueSlice createSliceFromMarkings(Graph graph,
			Set<Vertex> sliCritVertices, List<Vertex> leaves) {
		JValueSlice slice = new JValueSlice(graph);

		PathSystemMarkerList sliCritVertexMarkerList;
		PathSystemMarkerEntry sliCritVertexMarker;
		GraphMarker<PathSystemMarkerList> startStateMarker = marker.get(0);

		// add slicing criterion vertices to slice
		for (Vertex v : sliCritVertices) {
			sliCritVertexMarkerList = startStateMarker.getMark(v);
			sliCritVertexMarker = sliCritVertexMarkerList
					.getPathSystemMarkerEntryWithParentVertex(null);
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
			for (GraphMarker<PathSystemMarkerList> currentGraphMarker : marker) { 
				if (currentGraphMarker.getMark(leaf) != null) {
					// iterate through list of PathSystemMarkerEntrys 
					// for a particular GraphMarker (a particular state)
					for (PathSystemMarkerEntry currentMarker : currentGraphMarker.getMark(leaf).values()) {  
						// if state of current PathSystemMarkerEntry is final or
						if (!currentMarker.state.isFinal || isVertexMarkedWithState(leaf, currentMarker.state)) { 
							// (leaf, state) has already been processed
							continue;
						}
						// remember that (leaf, state) has already been processed
						markVertexWithState(leaf, currentMarker.state); 
						// mark leaf with current state
						currentStateMarker.mark(leaf, currentMarker.state); 
						queue.add(leaf);
						while (!queue.isEmpty()) {
							currentVertex = queue.poll();
							for (PathSystemMarkerEntry marker : getMarkersWithState(currentVertex, currentStateMarker.getMark(currentVertex)).values()) {
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
								if (parentVertex != null && !isVertexMarkedWithState(parentVertex, parentState)) { 
									// if (parentVertex, parentState) has not already
									// been processed, remember that is has been processed now 
									markVertexWithState(parentVertex, parentState); 
									currentStateMarker.mark(parentVertex,parentState);
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
	 *            the vertex for which to return the {@code
	 *            PathSystemMarkerEntry}
	 * @param s
	 *            the state for which to return the {@code
	 *            PathSystemMarkerEntry}
	 * @return the {@code PathSystemMarkerEntry} for {@code v} and {@code s}
	 */
	private PathSystemMarkerList getMarkersWithState(Vertex v, State s) {
		if (v == null) {
			return null;
		}
		GraphMarker<PathSystemMarkerList> currentMarker = marker.get(s.number);
		return currentMarker.getMark(v);
	}

	public long getEstimatedCosts(ArrayList<Long> inElements) {
		return 1000;
	}

	public double getSelectivity() {
		return 0.001f;
	}

	public long getEstimatedCardinality(int inElements) {
		return 1;
	}

}
