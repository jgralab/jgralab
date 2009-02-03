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
 
package de.uni_koblenz.jgralab.greql2.funlib.pathsearch;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.evaluator.fa.State;

/**
 * This class modells an entry in the queue which is used for PathSystemConstruction
 * @author ist@uni-koblenz.de
 * Summer 2006, Diploma Thesis
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
	 * @param v the vertex which has to be visited
	 * @param s the state in which the automaton is when v has to be visisted
	 * @param parentEdge the edge between v and it's parent vertex in the pathsystem
	 * @param parentState the state in which the parentVertex was visited
	 * @param distanceToRoot the distance to the root vertex
	 */
	public PathSystemQueueEntry(Vertex v, State s, Edge parentEdge, State parentState, int distanceToRoot) {
		super(v,s);
		this.distanceToRoot = distanceToRoot;
		this.parentEdge = parentEdge;
		this.parentState = parentState;
	}
	
	
	
}
