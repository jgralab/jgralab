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

package de.uni_koblenz.jgralab.greql2.jvalue;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;

/**
 * This is the entry of the hashmap which stores the references to the parent
 * vertices. It is _not_ a JValue
 */
public class PathSystemEntry {

	/**
	 * the parent vertex
	 */
	private Vertex parentVertex;

	/**
	 * the edge from the vertex to the parentVertex
	 */
	private Edge parentEdge;

	/**
	 * the number of the DFAState in which the parentvertex was visited
	 */
	private int parentStateNumber;

	/**
	 * the distance to the root vertex
	 */
	private int distanceToRoot;

	/**
	 * this attribute is true if the state with the given statenumber above was
	 * final in the dfa
	 */
	private boolean stateIsFinal;

	/**
	 * returns the string representation of this entry
	 */
	public String toString() {
		if (getParentVertex() != null)
			return "(V: " + getParentVertex().getId() + ", S: "
					+ getParentStateNumber() + ", E: "
					+ getParentEdge().getId() + " ,D: " + getDistanceToRoot()
					+ ")";
		else
			return "(RootVertex Distance: " + getDistanceToRoot() + ")";

	}

	/**
	 * Creates a new pathSystemEntry
	 * 
	 * @param parentVertex
	 *            The parent Vertex
	 * @param parentEdge
	 *            The edge to the parent vertex
	 * @param parentNumber
	 *            the number of the DFAState in which the parentvertex was
	 *            visited
	 * @param distance
	 *            the distance to the root vertex
	 */
	public PathSystemEntry(Vertex parentVertex, Edge parentEdge,
			int parentNumber, int distance, boolean finalState) {
		this.setParentVertex(parentVertex);
		this.setParentEdge(parentEdge);
		this.setParentStateNumber(parentNumber);
		this.setDistanceToRoot(distance);
		this.setStateIsFinal(finalState);
	}

	public void setParentVertex(Vertex parentVertex) {
		this.parentVertex = parentVertex;
	}

	public Vertex getParentVertex() {
		return parentVertex;
	}

	public void setParentEdge(Edge parentEdge) {
		this.parentEdge = parentEdge;
	}

	public Edge getParentEdge() {
		return parentEdge;
	}

	public void setParentStateNumber(int parentStateNumber) {
		this.parentStateNumber = parentStateNumber;
	}

	public int getParentStateNumber() {
		return parentStateNumber;
	}

	public void setDistanceToRoot(int distanceToRoot) {
		this.distanceToRoot = distanceToRoot;
	}

	public int getDistanceToRoot() {
		return distanceToRoot;
	}

	public void setStateIsFinal(boolean stateIsFinal) {
		this.stateIsFinal = stateIsFinal;
	}

	public boolean isStateIsFinal() {
		return stateIsFinal;
	}

}