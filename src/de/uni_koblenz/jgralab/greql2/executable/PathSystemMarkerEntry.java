package de.uni_koblenz.jgralab.greql2.executable;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;

public class PathSystemMarkerEntry {
	
	/**
	 * The vertex that is marked 
	 */
	public Vertex vertex;

	/**
	 * The number of the state this vertex gets marked with
	 */
	public int stateNumber;
	
	
	public boolean stateIsFinal;

	/**
	 * The number of the state in which the parent vertex in the pathsystem was visited
	 */
	public int parentStateNumber;

	/**
	 * The edge which leads to the parent vertex
	 */
	public Edge edgeToParentVertex;

	/**
	 * The parent vertex itself, needed, because there can be transitions  
	 * that don't consume edges
	 */
	public Vertex parentVertex;

	/**
	 * the distance to the root vertex of the pathsystem
	 */
	public int distanceToRoot;

	public PathSystemMarkerEntry(Vertex vertex, Vertex parentVertex, Edge parentEdge, int stateNumber,	boolean stateIsFinal, int parentStateNumber, int distance) {
		this.distanceToRoot = distance;
		this.stateNumber = stateNumber;
		this.stateIsFinal = stateIsFinal;
		this.edgeToParentVertex = parentEdge;
		this.parentVertex = parentVertex;
		this.parentStateNumber = parentStateNumber;
		this.vertex = vertex;
	}
}	
