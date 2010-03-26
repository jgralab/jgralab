/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2010 Institute for Software Technology
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