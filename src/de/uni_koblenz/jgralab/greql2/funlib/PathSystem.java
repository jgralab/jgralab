/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2007 Institute for Software Technology
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

import de.uni_koblenz.jgralab.greql2.evaluator.fa.DFA;
import de.uni_koblenz.jgralab.greql2.evaluator.fa.NFA;
import de.uni_koblenz.jgralab.greql2.evaluator.fa.State;
import de.uni_koblenz.jgralab.greql2.evaluator.fa.Transition;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.WrongFunctionParameterException;
import de.uni_koblenz.jgralab.greql2.funlib.pathsearch.PathSearch;
import de.uni_koblenz.jgralab.greql2.funlib.pathsearch.PathSystemMarkerEntry;
import de.uni_koblenz.jgralab.greql2.funlib.pathsearch.PathSystemMarkerList;
import de.uni_koblenz.jgralab.greql2.funlib.pathsearch.PathSystemQueueEntry;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValuePathSystem;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import de.uni_koblenz.jgralab.BooleanGraphMarker;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphMarker;
import de.uni_koblenz.jgralab.Vertex;

/**
 * Returns a pathsystem, based on the current graph and the given dfa, whose
 * root is the given vertex.
 * 
 * <dl>
 * <dt><b>GReQL-signature</b></dt>
 * <dd><code>PATHSYSTEM pathSystem(v:VERTEX, dfa:DFA)</code></dd>
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
 * <dd>a pathsystem, based on the current graph and the given dfa, whose root
 * is the given vertex</dd>
 * <dd><code>Null</code> if one of the given parameters is <code>Null</code></dd>
 * </dl>
 * </dd>
 * </dl>
 * 
 * @author Daniel Bildhauer <dbildh@uni-koblenz.de> Summer 2006, Diploma Thesis
 * 
 */

/*
 * Calculates the PathSystem that is constructed from the given root vertex, all
 * vertices that are reachable via a path of the given description and all
 * vertices, that are part of one of these parts
 * 
 * @param vertex the rootvertex of the pathsystem to create @param rpe the
 * regular path expression, which describes the structure of the pathsystem.
 * @return a JValuePathSystem, which contains all path in the graph, that start
 * with the givne rootvertex and match the given rpe
 */
public class PathSystem extends PathSearch implements Greql2Function {

	/**
	 * for each state in the fa (normally < 10) a seperate GraphMarker is used
	 */
	ArrayList<GraphMarker<PathSystemMarkerList>> marker;

	/**
	 * The graph the search is performed on
	 */
	Graph graph;

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
			currentMarker = new GraphMarker<PathSystemMarkerList>();
			marker.set(s.number, currentMarker);
		}
		PathSystemMarkerList list = currentMarker.getMark(v);
		if (list == null) {
			list = new PathSystemMarkerList(s, v);
			currentMarker.mark(v, list);
		}
		list.add(m);
		return true;
	}

	/**
	 * Checks if the given vertex is marked with the given state
	 * 
	 * @return true if the vertex is marked, false otherwise
	 */
	protected boolean isMarked(Vertex v, State s) {
		GraphMarker<PathSystemMarkerList> currentMarker = marker.get(s.number);
		if (currentMarker == null)
			return false;
		PathSystemMarkerList list = currentMarker.getMark(v);
		return (list != null);
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
	 * @return the list of leaves in the pathsystem, that is the set of states
	 *         which are marked with a final state of the dfa
	 * @throws EvaluateException
	 *             if something went wrong, several EvaluateException can be
	 *             thrown
	 */
	private List<Vertex> markVerticesOfPathSystem(Vertex startVertex, DFA dfa,
			BooleanGraphMarker subgraph) throws EvaluateException {
		// System.err.println("Start marking vertices of path system");
		ArrayList<Vertex> finalVertices = new ArrayList<Vertex>();
		Queue<PathSystemQueueEntry> queue = new LinkedList<PathSystemQueueEntry>();
		PathSystemQueueEntry currentEntry = new PathSystemQueueEntry(
				startVertex, dfa.initialState, null, null, 0);
		markVertex(startVertex, dfa.initialState, null /* no parent state */,
				null /* no parent vertex */, null /* no parent state */, 0 /*
																		 * distance
																		 * to
																		 * root
																		 * is
																		 * null
																		 */);
		while (currentEntry != null) {
			if (currentEntry.state.isFinal) {
				finalVertices.add(currentEntry.vertex);
			}
			Edge inc = currentEntry.vertex.getFirstEdge();
			while (inc != null) {
				Iterator<Transition> transitionIter = currentEntry.state.outTransitions
						.iterator();
				while (transitionIter.hasNext()) {
					Transition currentTransition = transitionIter.next();
					Vertex nextVertex = currentTransition.getNextVertex(
							currentEntry.vertex, inc);
					if (!isMarked(nextVertex, currentTransition.getEndState())) {
						if (currentTransition.accepts(currentEntry.vertex, inc,
								subgraph)) {
							markVertex(nextVertex, currentTransition
									.getEndState(), currentEntry.vertex, inc,
									currentEntry.state,
									currentEntry.distanceToRoot + 1);
							PathSystemQueueEntry nextEntry = new PathSystemQueueEntry(
									nextVertex,
									currentTransition.getEndState(), inc,
									currentEntry.state,
									currentEntry.distanceToRoot + 1);
							queue.add(nextEntry);
						}
					}
				}
				inc = inc.getNextEdge();
			}
			currentEntry = queue.poll();
		}
		// System.err.println("Marking vertices of path system finished");
		return finalVertices;
	}

	/**
	 * creates the pathsystem
	 */
	public JValue evaluate(Graph graph, BooleanGraphMarker subgraph,
			JValue[] arguments) throws EvaluateException {
		Vertex startVertex;
		this.graph = graph;
		DFA dfa;
		try {
			startVertex = arguments[0].toVertex();
			if (arguments[1].isNFA()) {
				NFA nfa = arguments[1].toNFA();
				dfa = new DFA(nfa);
			} else
				dfa = arguments[1].toDFA();
		} catch (Exception ex) {
			throw new WrongFunctionParameterException(this, null, arguments);
		}
		marker = new ArrayList<GraphMarker<PathSystemMarkerList>>(dfa.stateList
				.size());
		for (int i = 0; i < dfa.stateList.size(); i++)
			marker.add(new GraphMarker<PathSystemMarkerList>());
		List<Vertex> leaves = markVerticesOfPathSystem(startVertex, dfa,
				subgraph);
		JValuePathSystem resultPathSystem = createPathSystemFromMarkings(
				startVertex, leaves);
		return resultPathSystem;
	}

	/**
	 * Creates a JValuePathSystem-object which contains all path which start at
	 * the given root vertex andend with the given leaves
	 * 
	 * @param leaves
	 * @return
	 */
	private JValuePathSystem createPathSystemFromMarkings(Vertex rootVertex,
			List<Vertex> leaves) {
		JValuePathSystem pathSystem = new JValuePathSystem(rootVertex
				.getGraph());
		PathSystemMarkerList rootMarkerList = marker.get(0).getMark(rootVertex);
		PathSystemMarkerEntry rootMarker = rootMarkerList
				.getPathSystemMarkerEntryWithParentVertex(null);
		pathSystem.setRootVertex(rootVertex, rootMarker.state.number,
				rootMarker.state.isFinal);
		Iterator<Vertex> iter = leaves.iterator();
		while (iter.hasNext()) {
			Vertex leaf = iter.next();
			for (GraphMarker<PathSystemMarkerList> currentGraphMarker : marker) {
				Object tempAttribute = currentGraphMarker.getMark(leaf);
				if ((tempAttribute != null)
						&& (tempAttribute instanceof PathSystemMarkerList)) {
					PathSystemMarkerList leafMarkerList = (PathSystemMarkerList) tempAttribute;
					Iterator<PathSystemMarkerEntry> entryIter = leafMarkerList
							.iterator();
					while (entryIter.hasNext()) {
						PathSystemMarkerEntry currentMarker = entryIter.next();
						Vertex currentVertex = leaf;
						while (currentVertex != null) {
							int parentStateNumber = 0;
							if (currentMarker.parentState != null)
								parentStateNumber = currentMarker.parentState.number;
							pathSystem.addVertex(currentVertex,
									currentMarker.state.number,
									currentMarker.edgeToParentVertex,
									currentMarker.parentVertex,
									parentStateNumber,
									currentMarker.distanceToRoot,
									currentMarker.state.isFinal);
							currentVertex = currentMarker.parentVertex;
							currentMarker = getMarkerWithState(currentVertex,
									currentMarker.parentState);
						}
					}
				}
			}
		}
		return pathSystem;
	}

	private PathSystemMarkerEntry getMarkerWithState(Vertex v, State s) {
		if (v == null)
			return null;
		GraphMarker<PathSystemMarkerList> currentMarker = marker.get(s.number);
		PathSystemMarkerList list = currentMarker.getMark(v);
		Iterator<PathSystemMarkerEntry> iter = list.iterator();
		if (iter.hasNext()) {
			return iter.next();
		}
		return null;
	}

	public int getEstimatedCosts(ArrayList<Integer> inElements) {
		return 1000;
	}

	public double getSelectivity() {
		return 0.001f;
	}

	public int getEstimatedCardinality(int inElements) {
		return 1;
	}

	public String getExpectedParameters() {
		return "(Vertex, DFA, Subgraph" + "TempAttribute)";
	}

	@Override
	public boolean isPredicate() {
		return false;
	}
}
