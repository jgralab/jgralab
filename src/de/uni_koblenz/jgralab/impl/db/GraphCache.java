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
package de.uni_koblenz.jgralab.impl.db;

import de.uni_koblenz.jgralab.Graph;

public interface GraphCache {

	/**
	 * Adds a vertex to cache.
	 * @param vertex Vertex to add.
	 */
	public abstract void addVertex(DatabasePersistableVertex vertex);

	/**
	 * Checks if cache contains vertex with given id of given a given graph. 
	 * @param graph Graph the vertex belongs to.
	 * @param vId Id of vertex.
	 * @return true if cache contains vertex with given id of a given graph, otherwise false.
	 */
	public abstract boolean containsVertex(Graph graph, int vId);

	/**
	 * Gets a vertex with given id of given graph from cache.
	 * @param graph Graph the vertex belongs to.
	 * @param vId Id of vertex.
	 * @return A vertex or null if cache does not contain requested vertex. 
	 */
	public abstract DatabasePersistableVertex getVertex(Graph graph, int vId);

	/**
	 * Removes a vertex from cache.
	 * 
	 * @param graph
	 *            Graph the vertex to remove belongs to.
	 * @param vId
	 *            Identifier of vertex to remove from cache.
	 */
	public abstract void removeVertex(DatabasePersistableGraph graph, int vId);

	/**
	 * Adds an edge to cache.
	 * @param edge Edge to add.
	 */
	public abstract void addEdge(DatabasePersistableEdge edge);

	/**
	 * Checks if cache contains edge with given id of given a given graph. 
	 * @param graph Graph the edge belongs to.
	 * @param eId Id of edge.
	 * @return true if cache contains edge with given id of given a given graph, otherwise false.
	 */
	public abstract boolean containsEdge(Graph graph, int eId);

	/**
	 * Gets an edge with given id of given graph from cache.
	 * @param graph Graph the edge belongs to.
	 * @param eId Id of edge.
	 * @return A edge or null if cache does not contain requested edge. 
	 */
	public abstract DatabasePersistableEdge getEdge(Graph graph, int eId);

	/**
	 * Removes an edge from cache.
	 * 
	 * @param graph
	 *            Graph that contains the edge.
	 * @param eId
	 *            Identifier of edge to remove from cache.
	 */
	public abstract void removeEdge(DatabasePersistableGraph graph, int eId);

	/**
	 * Clears cache.
	 */
	public abstract void clear();

}
