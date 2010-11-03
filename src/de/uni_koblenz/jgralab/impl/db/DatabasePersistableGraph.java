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
package de.uni_koblenz.jgralab.impl.db;

import de.uni_koblenz.jgralab.Graph;

/**
 * Defines interface of a graph which can be persistent in a graph database.
 * 
 * @author ultbreit@uni-koblenz.de
 */
public interface DatabasePersistableGraph extends DatabasePersistable, Graph {

	/**
	 * Sets primary key of graph in database. When retrieving a graph from
	 * database, always set it's primary key as last.
	 * 
	 * @param primaryKey
	 *            Primary key of graph in database.
	 */
	void setGId(int gId);

	/**
	 * Adds minimal vertex data.
	 * 
	 * @param vId
	 *            Id of vertex in graph.
	 * @param sequenceNumber
	 *            Number mapping vertex's sequence in VSeq.
	 */
	void addVertex(int vId, long sequenceNumber);

	/**
	 * Adds minimal edge data.
	 * 
	 * @param eId
	 *            Id of edge in graph.
	 * @param sequenceNumber
	 *            Number mapping edge's sequence in ESeq.
	 */
	void addEdge(int eId, long sequenceNumber);

	/**
	 * Deletes graph from database.
	 * 
	 * @throws GraphDatabaseException
	 *             Deletion not successful.
	 */
	void delete() throws GraphDatabaseException;
}
