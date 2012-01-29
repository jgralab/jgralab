/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2011 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 *
 * For bug reports, documentation and further information, visit
 *
 *                         http://jgralab.uni-koblenz.de
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

import java.io.DataOutputStream;
import java.util.Comparator;
import java.util.Map;

import org.pcollections.POrderedSet;

import de.uni_koblenz.jgralab.eca.ECARuleManagerInterface;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.EnumDomain;
import de.uni_koblenz.jgralab.schema.GraphClass;
import de.uni_koblenz.jgralab.schema.RecordDomain;
import de.uni_koblenz.jgralab.schema.VertexClass;
import de.uni_koblenz.jgralab.trans.CommitFailedException;
import de.uni_koblenz.jgralab.trans.InvalidSavepointException;
import de.uni_koblenz.jgralab.trans.Savepoint;
import de.uni_koblenz.jgralab.trans.Transaction;

/**
 * The interface Graph is the base of all JGraLab graphs. It provides access to
 * global graph properties and to the Vertex and Edge sequence. Creation and
 * removal of vertices and edges, as well as validity checks, are provided.
 * 
 * Additionally, convenient methods for traversal, either based on separate
 * calls (getFirst/getNext) or on Iterables, can be used to traverse the graph.
 * 
 * @author ist@uni-koblenz.de
 */
public interface Graph extends AttributedElement<GraphClass, Graph> {

	/**
	 * Creates a vertex of the specified {@link VertexClass} and adds the new
	 * vertex to the Graph.
	 */
	public <T extends Vertex> T createVertex(VertexClass vc);

	/**
	 * Creates an edge of the specified {@link EdgeClass} <code>ec</code> that
	 * connects <code>alpha</code> and </code>omega</code> vertices and adds the
	 * new edge to this Graph.
	 */
	public <T extends Edge> T createEdge(EdgeClass ec, Vertex alpha,
			Vertex omega);

	/**
	 * Retrieves the enum constant of <code>enumDomain</code> given by
	 * <code>constantName</code>.
	 * 
	 * @param enumDomain
	 * @param constantName
	 * @return
	 */
	public Object getEnumConstant(EnumDomain enumDomain, String constantName);

	/**
	 * Creates a record of type <code>RecordDomain</code> with values as
	 * specified by <code>values</code>
	 * 
	 * @param recordDomain
	 * @param values
	 * @return
	 */
	public Record createRecord(RecordDomain recordDomain,
			Map<String, Object> values);

	/**
	 * Checks whether this graph has changed with respect to the given
	 * <code>previousVersion</code>. Every change in the graph, e.g. adding,
	 * creating and reordering of edges and vertices or changes of attributes of
	 * the graph, an edge or a vertex are treated as a change.
	 * 
	 * @param previousVersion
	 *            The version to check against
	 * @return <code>true</code> if the internal graph version of the graph is
	 *         different from the <code>previousVersion</code>.
	 */
	public boolean isGraphModified(long previousVersion);

	/**
	 * Returns the version counter of this graph.
	 * 
	 * @return the graph version
	 * @see #isGraphModified(long)
	 */
	public long getGraphVersion();

	/**
	 * @return true if this graph contains the given vertex <code>v</code>.
	 */
	public boolean containsVertex(Vertex v);

	/**
	 * @return true if this graph contains the given edge <code>e</code>.
	 */
	boolean containsEdge(Edge e);

	/**
	 * Removes the vertex <code>v</code> from the vertex sequence of this graph.
	 * Also, any edges incident to vertex <code>v</code> are deleted. If
	 * <code>v</code> is the parent of a composition, all child vertices are
	 * also deleted.
	 * 
	 * Preconditions: v.isValid()
	 * 
	 * Postconditions: !v.isValid() && !containsVertex(v) &&
	 * getVertex(v.getId()) == null
	 * 
	 * @param v
	 *            the Vertex to be deleted
	 */
	public void deleteVertex(Vertex v);

	/**
	 * Removes the edge <code>e</code> from the edge sequence of this graph.
	 * This implies changes to the incidence lists of the alpha and omega vertex
	 * of <code>e</code>.
	 * 
	 * Preconditions: e.isValid()
	 * 
	 * Postconditions: !e.isValid() && !containsEdge(e) && getEdge(e.getId()) ==
	 * null
	 * 
	 * @param e
	 *            the Edge to be deleted
	 */
	public void deleteEdge(Edge e);

	/**
	 * Returns the first Vertex in the vertex sequence of this Graph.
	 * 
	 * @return the first Vertex, or null if this graph contains no vertices.
	 */
	public Vertex getFirstVertex();

	/**
	 * Returns the last Vertex in the vertex sequence of this Graph.
	 * 
	 * @return the last Vertex, or null if this graph contains no vertices.
	 */
	public Vertex getLastVertex();

	/**
	 * Returns the first Vertex of the specified <code>vertexClass</code>
	 * (including subclasses) in the vertex sequence of this Graph.
	 * 
	 * @param vertexClass
	 *            a VertexClass (i.e. an instance of schema.VertexClass)
	 * 
	 * @return the first Vertex, or null if this graph contains no vertices of
	 *         the specified <code>vertexClass</code>.
	 */
	public Vertex getFirstVertex(VertexClass vertexClass);

	/**
	 * Returns the first Vertex of the specified <code>vertexClass</code>
	 * (including subclasses) in the vertex sequence of this Graph.
	 * 
	 * @param vertexClass
	 *            a VertexClass (i.e. an schema interface extending Vertex)
	 * 
	 * @return the first Vertex, or null if this graph contains no vertices of
	 *         the specified <code>vertexClass</code>.
	 */
	public Vertex getFirstVertex(Class<? extends Vertex> vertexClass);

	/**
	 * Returns the first Edge in the edge sequence of this Graph.
	 * 
	 * @return the first Edge, or null if this graph contains no edges.
	 */
	public Edge getFirstEdge();

	/**
	 * Returns the last Edge in the edge sequence of this Graph.
	 * 
	 * @return the last Edge, or null if this graph contains no edges.
	 */
	public Edge getLastEdge();

	/**
	 * Returns the first Edge of the specified <code>edgeClass</code> (including
	 * subclasses) in the edge sequence of this Graph.
	 * 
	 * @param edgeClass
	 *            an EdgeClass (i.e. an instance of schema.EdgeClass)
	 * 
	 * @return the first Edge, or null if this graph contains no edges of the
	 *         specified <code>edgeClass</code>.
	 */
	public Edge getFirstEdge(EdgeClass edgeClass);

	/**
	 * Returns the first Edge of the specified <code>edgeClass</code> (including
	 * subclasses) in the edge sequence of this Graph.
	 * 
	 * @param edgeClass
	 *            an EdgeClass (i.e. an schema interface extending Edge)
	 * 
	 * @return the first Edge, or null if this graph contains no edges of the
	 *         specified <code>edgeClass</code>.
	 */
	public Edge getFirstEdge(Class<? extends Edge> edgeClass);

	/**
	 * Returns the Vertex with the specified <code>id</code> if such a vertex
	 * exists in this Graph.
	 * 
	 * @param id
	 *            the id of the vertex (must be > 0)
	 * @return the Vertex, or null if no such vertex exists
	 */
	public Vertex getVertex(int id);

	/**
	 * Returns the oriented Edge with the specified <code>id</code> if such an
	 * edge exists in this Graph. If <code>id</code> is positive, the normal
	 * edge is returned, otherwise, the reversed Edge is returned.
	 * 
	 * @param id
	 *            the id of the edge (must be != 0)
	 * @return the Edge, or null if no such edge exists
	 */
	public Edge getEdge(int id);

	/**
	 * Returns the number of vertices in this Graph.
	 * 
	 * @return the number of vertices
	 */
	public int getVCount();

	/**
	 * Returns the number of edges in this Graph.
	 * 
	 * @return the number of edges
	 */
	public int getECount();

	/**
	 * Returns the <code>id</code> of this Graph. JGraLab assigns a 128 bit
	 * random id to all Graphs upon creation. This initial id is most likely
	 * (but not guaranteed) unique.
	 * 
	 * @return the id of this graph
	 */
	public String getId();

	/**
	 * Returns an Iterable which iterates over all edges of this Graph in the
	 * order determined by the edge sequence.
	 * 
	 * @return an Iterable for all edges
	 */
	public Iterable<Edge> edges();

	/**
	 * Returns an Iterable which iterates over all edges of this Graph which
	 * have the specified <code>edgeClass</code> (including subclasses), in the
	 * order determined by the edge sequence.
	 * 
	 * @param edgeClass
	 *            an EdgeClass (i.e. instance of schema.EdgeClass)
	 * 
	 * @return an Iterable for all edges of the specified <code>edgeClass</code>
	 */
	public Iterable<Edge> edges(EdgeClass edgeClass);

	/**
	 * Returns an Iterable which iterates over all edges of this Graph which
	 * have the specified <code>edgeClass</code> (including subclasses), in the
	 * order determined by the edge sequence.
	 * 
	 * @param edgeClass
	 *            an EdgeClass (i.e. an schema interface extending Edge)
	 * 
	 * @return an Iterable for all edges of the specified <code>edgeClass</code>
	 */
	public Iterable<Edge> edges(Class<? extends Edge> edgeClass);

	/**
	 * Returns the list of reachable vertices.
	 * 
	 * @param startVertex
	 *            a start vertex
	 * @param pathDescription
	 *            a GReQL path description
	 * @param vertexType
	 *            the type of the reachable vertices (acts as implicit
	 *            GoalRestriction)
	 * @return a List of all vertices of type <code>vertexType</code> reachable
	 *         from <code>startVertex</code> using the given
	 *         <code>pathDescription</code>
	 */
	public <T extends Vertex> POrderedSet<T> reachableVertices(
			Vertex startVertex, String pathDescription, Class<T> vertexType);

	/**
	 * Returns an Iterable which iterates over all vertices of this Graph in the
	 * order determined by the vertex sequence.
	 * 
	 * @return an Iterable for all vertices
	 */
	public Iterable<Vertex> vertices();

	/**
	 * Returns an Iterable which iterates over all vertices of this Graph which
	 * have the specified <code>vertexClass</code> (including subclasses), in
	 * the order determined by the vertex sequence.
	 * 
	 * @param vertexclass
	 *            a VertexClass (i.e. instance of schema.VertexClass)
	 * 
	 * @return an Iterable for all vertices of the specified
	 *         <code>vertexClass</code>
	 */
	public Iterable<Vertex> vertices(VertexClass vertexclass);

	/**
	 * Returns an Iterable which iterates over all vertices of this Graph which
	 * have the specified <code>vertexClass</code> (including subclasses), in
	 * the order determined by the vertex sequence.
	 * 
	 * @param vertexClass
	 *            a VertexClass (i.e. a schema interface extending Vertex)
	 * 
	 * @return a iterable for all vertices of the specified
	 *         <code>vertexClass</code>
	 */
	public Iterable<Vertex> vertices(Class<? extends Vertex> vertexClass);

	// ---- transaction support ----
	/**
	 * @return a read-write-<code>Transaction</code>
	 */
	public Transaction newTransaction();

	/**
	 * @return a read-only-<code>Transaction</code>
	 */
	public Transaction newReadOnlyTransaction();

	/**
	 * Sets the given <code>transaction</code> as the active
	 * <code>Transaction</code> for the current thread.
	 * 
	 * @param transaction
	 */
	public void setCurrentTransaction(Transaction transaction);

	/**
	 * @return the currently active <code>Transaction</code> in the current
	 *         thread
	 */
	public Transaction getCurrentTransaction();

	/**
	 * Delegates to {@link Graph#getCurrentTransaction()
	 * getCurrentTransaction()}.
	 * 
	 * @throws CommitFailedException
	 *             if commit fails
	 */
	public void commit() throws CommitFailedException;

	/**
	 * Delegates to {@link Graph#getCurrentTransaction()
	 * getCurrentTransaction()}.
	 */
	public void abort();

	/**
	 * Delegates to {@link Graph#getCurrentTransaction()
	 * getCurrentTransaction()}.
	 * 
	 * @return the defined <code>Savepoint</code>
	 */
	public Savepoint defineSavepoint();

	/**
	 * Delegates to {@link Graph#getCurrentTransaction()
	 * getCurrentTransaction()}.
	 * 
	 * @param savepoint
	 *            the <code>Savepoint</code> to be restored.
	 * 
	 * @throws InvalidSavepointException
	 */
	public void restoreSavepoint(Savepoint savepoint)
			throws InvalidSavepointException;

	/**
	 * Tells whether this graph instance supports transactions.
	 * 
	 * @return true if this graph instance supports transactions.
	 */
	public boolean hasTransactionSupport();

	/**
	 * Sorts the vertex sequence according to the given comparator in ascending
	 * order.
	 * 
	 * @param comp
	 *            the comparator defining the desired vertex order.
	 */
	public void sortVertices(Comparator<Vertex> comp);

	/**
	 * Sorts the edge sequence according to the given comparator in ascending
	 * order.
	 * 
	 * @param comp
	 *            the comparator defining the desired edge order.
	 */
	public void sortEdges(Comparator<Edge> comp);

	/**
	 * Registers the given <code>newListener</code> to the internal listener
	 * list.
	 * 
	 * @param newListener
	 *            the new <code>GraphStructureChangedListener</code> to
	 *            register.
	 */
	public void addGraphStructureChangedListener(
			GraphStructureChangedListener newListener);

	/**
	 * Removes the given <code>listener</code> from the internal listener list.
	 * 
	 * @param listener
	 *            the <code>GraphStructureChangedListener</code> to be removed.
	 */
	public void removeGraphStructureChangedListener(
			GraphStructureChangedListener listener);

	/**
	 * Removes all <code>GraphStructureChangedListener</code> from the internal
	 * listener list.
	 */
	public void removeAllGraphStructureChangedListeners();

	/**
	 * Returns the amount of registered
	 * <code>GraphStructureChangedListener</code>s.
	 * 
	 * @return the amount of registered
	 *         <code>GraphStructureChangedListener</code>s
	 */
	public int getGraphStructureChangedListenerCount();

	public ECARuleManagerInterface getECARuleManager();

	public ECARuleManagerInterface getECARuleManagerIfThere();

	public TraversalContext setTraversalContext(TraversalContext tc);

	public TraversalContext getTraversalContext();

	public GraphFactory getGraphFactory();

	public void setGraphFactory(GraphFactory graphFactory);

	public void save(String filename) throws GraphIOException;

	public void save(String filename, ProgressFunction pf)
			throws GraphIOException;

	public void save(DataOutputStream out) throws GraphIOException;

	public void save(DataOutputStream out, ProgressFunction pf)
			throws GraphIOException;
}
