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

/**
 * This class modells an entry in the queue which is used for
 * PathSystemConstruction
 * 
 * @author ist@uni-koblenz.de Summer 2006, Diploma Thesis
 * 
 */
public class PathSystemQueueEntry extends PathSearchQueueEntry {

	/**
	 * The edge between v and it's parent vertex in the pathsystem
	 */
	public Edge parentEdge;

	/**
	 * the state in which the parentVertex was visited
	 */
	public State parentState;

	/**
	 * the distance to the root vertex of the pathsystem
	 */
	public int distanceToRoot;

	/**
	 * Creates a new QueueEntry
	 * 
	 * @param v
	 *            the vertex which has to be visited
	 * @param s
	 *            the state in which the automaton is when v has to be visisted
	 * @param parentEdge
	 *            the edge between v and it's parent vertex in the pathsystem
	 * @param parentState
	 *            the state in which the parentVertex was visited
	 * @param distanceToRoot
	 *            the distance to the root vertex
	 */
	public PathSystemQueueEntry(Vertex v, State s, Edge parentEdge,
			State parentState, int distanceToRoot) {
		super(v, s);
		this.distanceToRoot = distanceToRoot;
		this.parentEdge = parentEdge;
		this.parentState = parentState;
	}

}
