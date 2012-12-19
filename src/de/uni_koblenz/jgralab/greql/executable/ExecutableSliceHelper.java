package de.uni_koblenz.jgralab.greql.executable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.graphmarker.GraphMarker;
import de.uni_koblenz.jgralab.graphmarker.SubGraphMarker;

/**
 * This class provides helper methods necessary for the efficient calculation of
 * Slices in executable GReQL. Since the final automatons that have been created
 * out of the path descriptions are not available in executable GReQL, the
 * methods provides by the GReQL function lib may not be used for slice
 * calculation in executable GReQL.
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class ExecutableSliceHelper {

	/**
	 * Checks if the given vertex is marked with the given state
	 * 
	 * @return true if the vertex is marked, false otherwise
	 */
	public static boolean isMarked(
			List<GraphMarker<Map<Edge, PathSystemMarkerEntry>>> marker,
			Vertex v, int state) {
		GraphMarker<Map<Edge, PathSystemMarkerEntry>> currentMarker = marker
				.get(state);

		Map<Edge, PathSystemMarkerEntry> map = currentMarker.getMark(v);
		return map != null;
	}

	/**
	 * Checks if the given vertex is marked with the given state and parent edge
	 * 
	 * @return true if the vertex is marked, false otherwise
	 */
	public static boolean isMarked(
			List<GraphMarker<Map<Edge, PathSystemMarkerEntry>>> marker,
			Vertex v, int state, Edge parentEdge) {
		GraphMarker<Map<Edge, PathSystemMarkerEntry>> currentMarker = marker
				.get(state);

		Map<Edge, PathSystemMarkerEntry> map = currentMarker.getMark(v);
		if (map != null) {
			return map.containsKey(parentEdge);
		} else {
			return false;
		}
	}

	/**
	 * marks the given vertex with the given SliceMarker
	 * 
	 * @return true if the vertex was marked successful, false if it is already
	 *         marked with this parentEdge
	 */
	public static boolean markVertex(
			List<GraphMarker<Map<Edge, PathSystemMarkerEntry>>> marker,
			Vertex v, int state, boolean isFinal, Vertex parentVertex, Edge e,
			int parentState, int d) {
		PathSystemMarkerEntry m = new PathSystemMarkerEntry(v, parentVertex, e,
				state, isFinal, parentState, d);
		GraphMarker<Map<Edge, PathSystemMarkerEntry>> currentMarker = marker
				.get(state);

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
	 * Creates a Slice-object which contains all path which start at the given
	 * start vertices and end with the given leaves
	 * 
	 * @param leaves
	 * @return
	 */
	public static SubGraphMarker createSliceFromMarkings(Graph graph,
			Set<Vertex> sliCritVertices, List<Vertex> leaves,
			List<GraphMarker<Map<Edge, PathSystemMarkerEntry>>> marker) {
		SubGraphMarker sliceSubGraph = new SubGraphMarker(graph);

		for (Vertex v : sliCritVertices) {
			sliceSubGraph.mark(v);
		}

		Queue<Vertex> queue = new LinkedList<Vertex>();
		Vertex currentVertex, parentVertex;
		int parentState = -1;
		GraphMarker<Set<Integer>> stateMarker = new GraphMarker<Set<Integer>>(
				graph);
		GraphMarker<Integer> currentStateMarker = new GraphMarker<Integer>(
				graph);

		for (Vertex leaf : leaves) { // iterate through leaves
			// iterate through GraphMarkers (one for each state)
			for (GraphMarker<Map<Edge, PathSystemMarkerEntry>> currentGraphMarker : marker) {
				if (currentGraphMarker.getMark(leaf) != null) {
					// iterate through list of PathSystemMarkerEntrys
					// for a particular GraphMarker (a particular state)
					for (PathSystemMarkerEntry currentMarker : currentGraphMarker
							.getMark(leaf).values()) {
						// if state of current PathSystemMarkerEntry is final or
						if (!currentMarker.stateIsFinal
								|| isVertexMarkedWithState(leaf,
										currentMarker.stateNumber, stateMarker)) {
							// (leaf, state) has already been processed
							continue;
						}
						// remember that (leaf, state) has already been
						// processed
						markVertexWithState(leaf, currentMarker.stateNumber,
								stateMarker);
						// mark leaf with current state
						currentStateMarker
								.mark(leaf, currentMarker.stateNumber);
						queue.add(leaf);
						while (!queue.isEmpty()) {
							currentVertex = queue.poll();
							for (PathSystemMarkerEntry mark : getMarkersWithState(
									currentVertex,
									currentStateMarker.getMark(currentVertex),
									marker).values()) {
								parentState = mark.parentStateNumber;
								sliceSubGraph.mark(currentVertex);
								if (mark.edgeToParentVertex != null) {
									sliceSubGraph.mark(mark.edgeToParentVertex);
								}
								parentVertex = mark.parentVertex;
								if ((parentVertex != null)
										&& !isVertexMarkedWithState(
												parentVertex, parentState,
												stateMarker)) {
									// if (parentVertex, parentState) has not
									// already
									// been processed, remember that is has been
									// processed now
									markVertexWithState(parentVertex,
											parentState, stateMarker);
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

		return sliceSubGraph;
	}

	/**
	 * Checks if the given vertex' state set contains the given state.
	 * 
	 * @param v
	 *            the vertex to be checked
	 * @param state
	 *            the state to be checked for
	 * @return true, if {@code v}'s set of states contains {@code s}, false else
	 */
	private static boolean isVertexMarkedWithState(Vertex v, Integer state,
			GraphMarker<Set<Integer>> stateMarker) {
		if (stateMarker.getMark(v) == null) {
			return false;
		}
		return stateMarker.getMark(v).contains(state);
	}

	/**
	 * Adds the given state to the set of states maintained for the given
	 * vertex.
	 * 
	 * @param v
	 *            the vertex to be marked
	 * @param state
	 *            the state which shall be added to {@code v}'s state set
	 */
	private static void markVertexWithState(Vertex v, Integer state,
			GraphMarker<Set<Integer>> stateMarker) {
		if (stateMarker.getMark(v) == null) {
			stateMarker.mark(v, new HashSet<Integer>());
		}
		stateMarker.getMark(v).add(state);
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
	private static Map<Edge, PathSystemMarkerEntry> getMarkersWithState(
			Vertex v, Integer state,
			List<GraphMarker<Map<Edge, PathSystemMarkerEntry>>> marker) {
		if (v == null) {
			return null;
		}
		GraphMarker<Map<Edge, PathSystemMarkerEntry>> currentMarker = marker
				.get(state);
		return currentMarker.getMark(v);
	}

}
