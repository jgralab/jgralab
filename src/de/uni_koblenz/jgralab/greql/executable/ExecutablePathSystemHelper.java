package de.uni_koblenz.jgralab.greql.executable;

import java.util.Set;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.graphmarker.GraphMarker;

/**
 * This class provides helper methods necessary for the efficient claculation
 * of PathSystems in executable GReQL. Since the final automatons that have been
 * created out of the path descriptions are not available in executable GReQL,
 * the methods provides by the GReQL function lib may not be used for pathsystem
 * calculation in executable GReQL.
 * 
 * @author dbildh
 *
 */
public class ExecutablePathSystemHelper {

	
	public static de.uni_koblenz.jgralab.greql.types.PathSystem createPathSystemFromMarkings(
			GraphMarker<PathSystemMarkerEntry>[] marker, Vertex rootVertex,
			Set<PathSystemMarkerEntry> leafEntries) {
		de.uni_koblenz.jgralab.greql.types.PathSystem pathSystem = new de.uni_koblenz.jgralab.greql.types.PathSystem(
				rootVertex.getGraph());
		PathSystemMarkerEntry rootMarker = marker[0].getMark(rootVertex);
		pathSystem.setRootVertex(rootVertex, rootMarker.stateNumber,
				rootMarker.stateIsFinal);

		for (PathSystemMarkerEntry currentMarker : leafEntries) {
			Vertex currentVertex = currentMarker.vertex;
			while (currentVertex != null) {
				pathSystem.addVertex(currentVertex, currentMarker.stateNumber,
						currentMarker.edgeToParentVertex,
						currentMarker.parentVertex, currentMarker.parentStateNumber,
						currentMarker.distanceToRoot,
						currentMarker.stateIsFinal);
				currentVertex = currentMarker.parentVertex;
				currentMarker = getMarkerWithState(marker, currentVertex,
						currentMarker.parentStateNumber);
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
	private static PathSystemMarkerEntry getMarkerWithState(
			GraphMarker<PathSystemMarkerEntry>[] marker, Vertex v, int stateNumber) {
		if (v == null) {
			return null;
		}
		GraphMarker<PathSystemMarkerEntry> currentMarker = marker[stateNumber];
		PathSystemMarkerEntry entry = currentMarker.getMark(v);
		return entry;
	}
	
	
	/**
	 * marks the given vertex with the given PathSystemMarker 
	 * @return the marker created
	 */
	public static PathSystemMarkerEntry markVertex(
			GraphMarker<PathSystemMarkerEntry>[] marker, Vertex v, int stateNumber,
			boolean stateIsFinal, Vertex parentVertex, Edge e, int parentStateNumber, int d) {
		PathSystemMarkerEntry m = new PathSystemMarkerEntry(v, parentVertex, e,
				stateNumber, stateIsFinal, parentStateNumber, d);

		GraphMarker<PathSystemMarkerEntry> currentMarker = marker[stateNumber];
		currentMarker.mark(v, m);
		return m;
	}

	/**
	 * Checks if the given vertex is marked with the given state 
	 * @return true if the vertex is marked, false otherwise
	 */
	public static boolean isMarked(GraphMarker<PathSystemMarkerEntry>[] marker,
			Vertex v, int stateNumber) {
		GraphMarker<PathSystemMarkerEntry> currentMarker = marker[stateNumber];
		return currentMarker.isMarked(v);
	}
	

}
