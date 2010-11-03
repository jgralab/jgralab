/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2010 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
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

package de.uni_koblenz.jgralab.greql2.funlib.pathsearch;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.evaluator.fa.State;

public class PathSystemMarkerEntry {

	/**
	 * The state this vertex gets marked with
	 */
	public State state;

	/**
	 * The state in which the parent vertex in the pathsystem was visited
	 */
	public State parentState;

	/**
	 * The edge which leads to the parent vertex
	 */
	public Edge edgeToParentVertex;

	/**
	 * The parent vertex itself, needed, because there can be transitions which
	 * takes no edges
	 */
	public Vertex parentVertex;

	/**
	 * the distance to the root vertex of the pathsystem
	 */
	public int distanceToRoot;

	/**
	 * creates a new pathsystem marked
	 * 
	 * @param s
	 *            The state in which the dfa is when visiting the vertex which
	 *            gets marked
	 * @param parentState
	 *            The state in which the dfa was when visiting the parent vertex
	 * @param distance
	 *            the distance to the root vertex of the pathsystem
	 */
	public PathSystemMarkerEntry(Vertex parentVertex, Edge parentEdge, State s,
			State parentState, int distance) {
		this.distanceToRoot = distance;
		this.state = s;
		this.edgeToParentVertex = parentEdge;
		this.parentVertex = parentVertex;
		this.parentState = parentState;
	}
}
