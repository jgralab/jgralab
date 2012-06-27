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

package de.uni_koblenz.jgralab.greql.types;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;

/**
 * This is the entry of the hashmap which stores the references to the parent
 * vertices.
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

	public void setParentVertex(Vertex parentVertex) {
		this.parentVertex = parentVertex;
	}

	public void setParentEdge(Edge parentEdge) {
		this.parentEdge = parentEdge;
	}

	public void setParentStateNumber(int parentStateNumber) {
		this.parentStateNumber = parentStateNumber;
	}

	public void setDistanceToRoot(int distanceToRoot) {
		this.distanceToRoot = distanceToRoot;
	}

	public void setStateIsFinal(boolean stateIsFinal) {
		this.stateIsFinal = stateIsFinal;
	}

	/**
	 * this attribute is true if the state with the given statenumber above was
	 * final in the dfa
	 */
	private boolean stateIsFinal;

	/**
	 * returns the string representation of this entry
	 */
	@Override
	public String toString() {
		if (getParentVertex() != null) {
			return "(V: " + getParentVertex().getId() + ", S: "
					+ getParentStateNumber() + ", E: "
					+ getParentEdge().getId() + " ,D: " + getDistanceToRoot()
					+ ")";
		} else {
			return "(RootVertex Distance: " + getDistanceToRoot() + ")";
		}

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
		this.parentVertex = parentVertex;
		this.parentEdge = parentEdge;
		this.parentStateNumber = parentNumber;
		this.distanceToRoot = distance;
		this.stateIsFinal = finalState;
	}

	public Vertex getParentVertex() {
		return parentVertex;
	}

	public Edge getParentEdge() {
		return parentEdge;
	}

	public int getParentStateNumber() {
		return parentStateNumber;
	}

	public int getDistanceToRoot() {
		return distanceToRoot;
	}

	public boolean getStateIsFinal() {
		return stateIsFinal;
	}
}
