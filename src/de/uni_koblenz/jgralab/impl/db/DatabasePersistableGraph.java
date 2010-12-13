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
