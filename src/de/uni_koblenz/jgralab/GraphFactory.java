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

package de.uni_koblenz.jgralab;

import de.uni_koblenz.jgralab.impl.db.GraphDatabase;

/**
 * Creates instances of graphs, edges and vertices. By changing factory it is
 * possible to extend Graph, Vertex, and Edge classes used in a graph.
 * 
 * @author ist@uni-koblenz.de
 */
public interface GraphFactory {

	// --- Methods for option STDIMPL
	// ---------------------------------------------------

	/**
	 * creates a Graph-object for the specified class. The returned object may
	 * be an instance of a subclass of the specified graphClass.
	 */
	public Graph createGraph(Class<? extends Graph> graphClass, String id,
			int vMax, int eMax);

	/**
	 * creates a Graph-object for the specified class. The returned object may
	 * be an instance of a subclass of the specified graphClass.
	 */
	public Graph createGraph(Class<? extends Graph> graphClass, String id);

	/**
	 * creates a Vertex-object for the specified class. The returned object may
	 * be an instance of a subclass of the specified vertexClass.
	 */
	public Vertex createVertex(Class<? extends Vertex> vertexClass, int id,
			Graph g);

	/**
	 * creates a Edge-object for the specified class. The returned object may be
	 * an instance of a subclass of the specified edgeClass.
	 */
	public Edge createEdge(Class<? extends Edge> edgeClass, int id, Graph g,
			Vertex alpha, Vertex omega);

	public void setGraphImplementationClass(
			Class<? extends Graph> graphM1Class,
			Class<? extends Graph> implementationClass);

	public void setVertexImplementationClass(
			Class<? extends Vertex> vertexM1Class,
			Class<? extends Vertex> implementationClass);

	public void setEdgeImplementationClass(Class<? extends Edge> edgeM1Class,
			Class<? extends Edge> implementationClass);

	/**
	 * Creates an record of class <code>recordDomain</code> in the graph g
	 */
	public <T extends Record> T createRecord(Class<T> recordDomain, Graph g);

	/**
	 * Assigns an implementation class with transaction support for a
	 * <code>Record</code>.
	 * 
	 * @param record
	 * @param implementationClass
	 */
	public void setRecordImplementationClass(Class<? extends Record> record,
			Class<? extends Record> implementationClass);

	// -------------------------------------------------------------------------
	// Methods for the DATABASE option.
	// -------------------------------------------------------------------------

	/**
	 * Creates a graph with database support.
	 * 
	 * @param graphClass
	 *            The graph class.
	 * @param graphDatabase
	 *            Database graph should be contained in.
	 * @param id
	 *            Id of graph.
	 */
	public Graph createGraphWithDatabaseSupport(
			Class<? extends Graph> graphClass, GraphDatabase graphDatabase,
			String id);

	/**
	 * Creates a graph with database support.
	 * 
	 * @param graphClass
	 *            The graph class.
	 * @param graphDatabase
	 *            Database graph should be contained in.
	 * @param id
	 *            Id of graph.
	 * @param vMax
	 *            Maximum initial count of vertices that can be held in graph.
	 * @param eMax
	 *            Maximum initial count of edges that can be held in graph.
	 */
	public Graph createGraphWithDatabaseSupport(
			Class<? extends Graph> graphClass, GraphDatabase graphDatabase,
			String id, int vMax, int eMax);

	/**
	 * Creates a vertex instance of a specified class with database support.
	 * Returned object may be an instance of a subclass of specified vertex
	 * class.
	 * 
	 * @param vertexClass
	 *            Class of vertex to instance.
	 * @param id
	 *            Identifier of vertex.
	 * @param graph
	 *            Graph which should contain created vertex.
	 */
	public Vertex createVertexWithDatabaseSupport(
			Class<? extends Vertex> vertexClass, int id, Graph graph);

	/**
	 * Creates an edge instance of specified class with database support.
	 * Returned object may be an instance of a subclass of specified edge class.
	 * 
	 * @param edgeClass
	 *            Class of edge to instance.
	 * @param id
	 *            Identifier of edge.
	 * @param graph
	 *            Graph which should contain created edge.
	 * @param alpha
	 *            Start vertex of edge.
	 * @param omega
	 *            End vertex of edge.
	 */
	public Edge createEdgeWithDatabaseSupport(Class<? extends Edge> edgeClass,
			int id, Graph graph, Vertex alpha, Vertex omega);

	/**
	 * Assigns an implementation class with database support for a
	 * <code>Graph</code>.
	 * 
	 * @param edgeM1Class
	 * @param implementationClass
	 */
	public void setGraphDatabaseImplementationClass(
			Class<? extends Graph> graphM1Class,
			Class<? extends Graph> implementationClass);

	/**
	 * Assigns an implementation class with database support for a
	 * <code>Vertex</code>.
	 * 
	 * @param edgeM1Class
	 * @param implementationClass
	 */
	public void setVertexDatabaseImplementationClass(
			Class<? extends Vertex> vertexM1Class,
			Class<? extends Vertex> implementationClass);

	/**
	 * Assigns an implementation class with database support for an
	 * <code>Edge</code>.
	 * 
	 * @param edgeM1Class
	 * @param implementationClass
	 */
	public void setEdgeDatabaseImplementationClass(
			Class<? extends Edge> edgeM1Class,
			Class<? extends Edge> implementationClass);

	// --- Methods for option TRANSIMPL
	// ------------------------------------------------

	/**
	 * creates a Graph-object for the specified class with transaction support.
	 * The returned object may be an instance of a subclass of the specified
	 * graphClass.
	 */
	public Graph createGraphWithTransactionSupport(
			Class<? extends Graph> graphClass, String id, int vMax, int eMax);

	/**
	 * creates a Graph-object for the specified class with transaction support.
	 * The returned object may be an instance of a subclass of the specified
	 * graphClass.
	 */
	public Graph createGraphWithTransactionSupport(
			Class<? extends Graph> graphClass, String id);

	/**
	 * creates a Vertex-object for the specified class with transaction support.
	 * The returned object may be an instance of a subclass of the specified
	 * vertexClass.
	 */
	public Vertex createVertexWithTransactionSupport(
			Class<? extends Vertex> vertexClass, int id, Graph g);

	/**
	 * creates a Edge-object for the specified class with transaction support.
	 * The returned object may be an instance of a subclass of the specified
	 * edgeClass.
	 */
	public Edge createEdgeWithTransactionSupport(
			Class<? extends Edge> edgeClass, int id, Graph g, Vertex alpha,
			Vertex omega);

	/**
	 * Assigns an implementation class with transaction support for a
	 * <code>Graph</code>.
	 * 
	 * @param graphM1Class
	 * @param implementationClass
	 */
	public void setGraphTransactionImplementationClass(
			Class<? extends Graph> graphM1Class,
			Class<? extends Graph> implementationClass);

	/**
	 * Assigns an implementation class with transaction support for a
	 * <code>Vertex</code>.
	 * 
	 * @param vertexM1Class
	 * @param implementationClass
	 */
	public void setVertexTransactionImplementationClass(
			Class<? extends Vertex> vertexM1Class,
			Class<? extends Vertex> implementationClass);

	/**
	 * Assigns an implementation class with transaction support for an
	 * <code>Edge</code>.
	 * 
	 * @param edgeM1Class
	 * @param implementationClass
	 */
	public void setEdgeTransactionImplementationClass(
			Class<? extends Edge> edgeM1Class,
			Class<? extends Edge> implementationClass);

	/**
	 * Creates an record with transaction support of class
	 * <code>recordDomain</code> in the graph g
	 */
	public <T extends Record> T createRecordWithTransactionSupport(
			Class<T> recordDomain, Graph g);

	/**
	 * Creates an record with database support of class
	 * <code>recordDomain</code> in the graph g
	 */
	public <T extends Record> T createRecordWithDatabaseSupport(
			Class<T> recordDomain, Graph g);

	/**
	 * Assigns an implementation class with transaction support for a
	 * <code>Record</code>.
	 * 
	 * @param record
	 * @param implementationClass
	 */
	public void setRecordTransactionImplementationClass(
			Class<? extends Record> record,
			Class<? extends Record> implementationClass);

	// -------------------------------------------------------------------------
	// Methods for the SAVEMEMIMPL option.
	// -------------------------------------------------------------------------

	/**
	 * creates a Graph-object for the specified class with savemem support. The
	 * returned object may be an instance of a subclass of the specified
	 * graphClass.
	 */
	public Graph createGraphWithSavememSupport(
			Class<? extends Graph> graphClass, String id, int vMax, int eMax);

	/**
	 * creates a Graph-object for the specified class with savemem support. The
	 * returned object may be an instance of a subclass of the specified
	 * graphClass.
	 */
	public Graph createGraphWithSavememSupport(
			Class<? extends Graph> graphClass, String id);

	/**
	 * creates a Vertex-object for the specified class with savemem support. The
	 * returned object may be an instance of a subclass of the specified
	 * vertexClass.
	 */
	public Vertex createVertexWithSavememSupport(
			Class<? extends Vertex> vertexClass, int id, Graph g);

	/**
	 * creates a Edge-object for the specified class with savemem support. The
	 * returned object may be an instance of a subclass of the specified
	 * edgeClass.
	 */
	public Edge createEdgeWithSavememSupport(Class<? extends Edge> edgeClass,
			int id, Graph g, Vertex alpha, Vertex omega);

	/**
	 * Assigns an implementation class with savemem support for a
	 * <code>Graph</code>.
	 * 
	 * @param graphM1Class
	 * @param implementationClass
	 */
	public void setGraphSavememImplementationClass(
			Class<? extends Graph> graphM1Class,
			Class<? extends Graph> implementationClass);

	/**
	 * Assigns an implementation class with savemem support for a
	 * <code>Vertex</code>.
	 * 
	 * @param vertexM1Class
	 * @param implementationClass
	 */
	public void setVertexSavememImplementationClass(
			Class<? extends Vertex> vertexM1Class,
			Class<? extends Vertex> implementationClass);

	/**
	 * Assigns an implementation class with savemem support for an
	 * <code>Edge</code>.
	 * 
	 * @param edgeM1Class
	 * @param implementationClass
	 */
	public void setEdgeSavememImplementationClass(
			Class<? extends Edge> edgeM1Class,
			Class<? extends Edge> implementationClass);

	/**
	 * Assigns an implementation class with savemem support for a
	 * <code>Record</code>.
	 * 
	 * @param record
	 * @param implementationClass
	 */
	public void setRecordSavememImplementationClass(
			Class<? extends Record> record,
			Class<? extends Record> implementationClass);

	/**
	 * Creates an record with savemem support of class <code>recordDomain</code>
	 * in the graph g
	 */
	public <T extends Record> T createRecordWithSavememSupport(
			Class<T> recordDomain, Graph g);

}
