package de.uni_koblenz.jgralab.greql.executable;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.graphmarker.GraphMarker;
import de.uni_koblenz.jgralab.greql.types.PathSystem.PathSystemNode;

/**
 * This class provides helper methods necessary for the efficient calculation of
 * PathSystems in executable GReQL. Since the final automatons that have been
 * created out of the path descriptions are not available in executable GReQL,
 * the methods provides by the GReQL function lib may not be used for pathsystem
 * calculation in executable GReQL.
 * 
 * @author dbildh
 * 
 */
public class ExecutablePathSystemHelper {

	/**
	 * Creates a JValuePathSystem-object which contains all paths which start at
	 * the given root vertex and end with one of the given leaves
	 */
	public static de.uni_koblenz.jgralab.greql.types.PathSystem createPathSystemFromMarkings(
			GraphMarker<PathSystemMarkerEntry>[] marker, Vertex rootVertex,
			Set<PathSystemMarkerEntry> leafEntries) {
		de.uni_koblenz.jgralab.greql.types.PathSystem pathSystem = new de.uni_koblenz.jgralab.greql.types.PathSystem();
		Map<Vertex, PathSystemNode[]> vertex2state2node = new HashMap<>();
		Map<Vertex, PathSystemMarkerEntry[]> vertex2state2marker = new HashMap<>();
		Queue<PathSystemNode> nodesWithoutParentEdge = new LinkedList<>();
		PathSystemMarkerEntry rootMarker = marker[0].getMark(rootVertex);
		PathSystemNode root = pathSystem.setRootVertex(rootVertex,
				rootMarker.stateNumber, rootMarker.stateIsFinal);

		PathSystemNode[] currentState2node = new PathSystemNode[marker.length];
		currentState2node[rootMarker.stateNumber] = root;
		vertex2state2node.put(rootVertex, currentState2node);

		for (PathSystemMarkerEntry currentMarker : leafEntries) {
			Vertex currentVertex = currentMarker.vertex;
			while (currentVertex != null) {
				// && !isVertexMarkedWithState(currentVertex,
				// currentMarker.state)

				PathSystemNode childNode = addVertexToPathSystem(pathSystem,
						vertex2state2node, marker.length, currentVertex,
						currentMarker.stateNumber, currentMarker.stateIsFinal);
				if (currentMarker.edgeToParentVertex != null) {
					PathSystemNode parentNode = addVertexToPathSystem(
							pathSystem, vertex2state2node, marker.length,
							currentMarker.parentVertex,
							currentMarker.parentStateNumber, false);
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
					assert currentMarkerEntry[currentMarker.stateNumber] == null : "already exiting:"
							+ currentMarkerEntry[currentMarker.stateNumber]
							+ " new: " + currentMarker;
					currentMarkerEntry[currentMarker.stateNumber] = currentMarker;
				}

				currentVertex = currentMarker.parentVertex;
				currentMarker = getMarkerWithState(marker, currentVertex,
						currentMarker.parentStateNumber);
			}
		}
		completePathSystem(pathSystem, nodesWithoutParentEdge,
				vertex2state2marker, vertex2state2node);
		pathSystem.finish();
		return pathSystem;
	}

	private static PathSystemNode addVertexToPathSystem(
			de.uni_koblenz.jgralab.greql.types.PathSystem pathSystem,
			Map<Vertex, PathSystemNode[]> vertex2state2node,
			int numberOfStates, Vertex currentVertex, int state, boolean isFinal) {
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
	private static void completePathSystem(
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
				pe = vertex2state2node.get(teMarker[te.state].parentVertex)[teMarker[te.state].parentStateNumber];
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
	private static PathSystemMarkerEntry getMarkerWithState(
			GraphMarker<PathSystemMarkerEntry>[] marker, Vertex v,
			int stateNumber) {
		if (v == null) {
			return null;
		}
		GraphMarker<PathSystemMarkerEntry> currentMarker = marker[stateNumber];
		PathSystemMarkerEntry entry = currentMarker.getMark(v);
		return entry;
	}

	/**
	 * marks the given vertex with the given PathSystemMarker
	 * 
	 * @return the marker created
	 */
	public static PathSystemMarkerEntry markVertex(
			GraphMarker<PathSystemMarkerEntry>[] marker, Vertex v,
			int stateNumber, boolean stateIsFinal, Vertex parentVertex, Edge e,
			int parentStateNumber, int d) {
		PathSystemMarkerEntry m = new PathSystemMarkerEntry(v, parentVertex, e,
				stateNumber, stateIsFinal, parentStateNumber, d);

		GraphMarker<PathSystemMarkerEntry> currentMarker = marker[stateNumber];
		currentMarker.mark(v, m);
		return m;
	}

	/**
	 * Checks if the given vertex is marked with the given state
	 * 
	 * @return true if the vertex is marked, false otherwise
	 */
	public static boolean isMarked(GraphMarker<PathSystemMarkerEntry>[] marker,
			Vertex v, int stateNumber) {
		GraphMarker<PathSystemMarkerEntry> currentMarker = marker[stateNumber];
		return currentMarker.isMarked(v);
	}

}
