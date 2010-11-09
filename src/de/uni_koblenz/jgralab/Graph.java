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

import java.util.Collection;
import java.util.List;
import java.util.Map;

import de.uni_koblenz.jgralab.schema.EdgeClass;
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
public interface Graph extends AttributedElement {

	/**
	 * Creates a vertex the specified class <code>cls</code> and adds the new
	 * vertex to this Graph.
	 */
	public <T extends Vertex> T createVertex(Class<T> cls);

	/**
	 * Creates an edge of the specified class <code>cls</code> that connects
	 * <code>alpha</code> and <code>omega</code> vertices and adds the new edge
	 * to this Graph.
	 */
	public <T extends Edge> T createEdge(Class<T> cls, Vertex alpha,
			Vertex omega);

	/**
	 * Checks whether this graph is currently being loaded.
	 * 
	 * @return true if the graph is currently being loaded
	 */
	public boolean isLoading();

	/**
	 * Callback method: Called immediately after loading of this graph is
	 * completed. Overwrite this method to perform user defined operations after
	 * loading a graph.
	 */
	public void loadingCompleted();

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
	 * Checks if the vertex sequence of this has changed with respect to the
	 * given <code>previousVersion</code>. Changes in the vertex sequence are
	 * creation and deletion as well as reordering of vertices, but not changes
	 * of attribute values.
	 * 
	 * @return <code>true</code> if the vertex list version of this graph is
	 *         different from <code>previousVersion</code>.
	 */
	public boolean isVertexListModified(long previousVersion);

	/**
	 * Returns the version counter of the vertex sequence of this graph.
	 * 
	 * @return the vertex sequence version
	 * @see #isVertexListModified(long)
	 */
	public long getVertexListVersion();

	/**
	 * Checks if the edge sequence of this has changed with respect to the given
	 * <code>previousVersion</code>. Changes in the edge sequence are creation
	 * and deletion as well as reordering of edges, but not changes of attribute
	 * values.
	 * 
	 * @return <code>true</code> if the edge list version of this graph is
	 *         different from <code>previousVersion</code>.
	 */
	public boolean isEdgeListModified(long edgeListVersion);

	/**
	 * Returns the version counter of the edge sequence of this graph.
	 * 
	 * @return the edge sequence version
	 * @see #isEdgeListModified(long)
	 */
	public long getEdgeListVersion();

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
	 * Returns the first Vertex of the specified <code>vertexClass</code>,
	 * including subclasses only if <code>noSubclasses</code> is set to false,
	 * in the vertex sequence of this Graph.
	 * 
	 * @param vertexClass
	 *            a VertexClass (i.e. an instance of schema.VertexClass)
	 * 
	 * @param noSubclasses
	 *            if set to true, only vertices with the exact class are taken
	 *            into account, false means that also subclasses are valid
	 * 
	 * @return the first Vertex, or null if this graph contains no vertices of
	 *         the specified <code>vertexClass</code>.
	 */
	public Vertex getFirstVertex(VertexClass vertexClass,boolean noSubclasses);

	/**
	 * Returns the first Vertex of the specified <code>vertexClass</code>
	 * (including subclasses) in the vertex sequence of this Graph.
	 * 
	 * @param vertexClass
	 *            a VertexClass (i.e. an M1 interface extending Vertex)
	 * 
	 * @return the first Vertex, or null if this graph contains no vertices of
	 *         the specified <code>vertexClass</code>.
	 */
	public Vertex getFirstVertex(Class<? extends Vertex> vertexClass);

	/**
	 * Returns the first Vertex of the specified <code>vertexClass</code>,
	 * including subclasses only if <code>noSubclasses</code> is set to false,
	 * in the vertex sequence of this Graph.
	 * 
	 * @param vertexClass
	 *            a VertexClass (i.e. an M1 interface extending Vertex)
	 * 
	 * @param noSubclasses
	 *            if set to true, only vertices with the exact class are taken
	 *            into account, false means that also subclasses are valid
	 * 
	 * @return the first Vertex, or null if this graph contains no vertices of
	 *         the specified <code>vertexClass</code>.
	 */
	public Vertex getFirstVertex(Class<? extends Vertex> vertexClass,
			boolean noSubclasses);

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
	 * Returns the first Edge of the specified <code>edgeClass</code>, including
	 * subclasses only if <code>noSubclasses</code> is set to false, in the edge
	 * sequence of this Graph.
	 * 
	 * @param edgeClass
	 *            an EdgeClass (i.e. an instance of schema.EdgeClass)
	 * 
	 * @param noSubclasses
	 *            if set to true, only edges with the exact class are taken into
	 *            account, false means that also subclasses are valid
	 * 
	 * @return the first Edge, or null if this graph contains no edges of the
	 *         specified <code>edgeClass</code>.
	 */
	public Edge getFirstEdge(EdgeClass edgeClass,
			boolean noSubclasses);

	/**
	 * Returns the first Edge of the specified <code>edgeClass</code> (including
	 * subclasses) in the edge sequence of this Graph.
	 * 
	 * @param edgeClass
	 *            an EdgeClass (i.e. an M1 interface extending Edge)
	 * 
	 * @return the first Edge, or null if this graph contains no edges of the
	 *         specified <code>edgeClass</code>.
	 */
	public Edge getFirstEdge(Class<? extends Edge> edgeClass);

	/**
	 * Returns the first Edge of the specified <code>edgeClass</code>, including
	 * subclasses only if <code>noSubclasses</code> is set to false, in the edge
	 * sequence of this Graph.
	 * 
	 * @param edgeClass
	 *            an EdgeClass (i.e. an M1 interface extending Edge)
	 * 
	 * @param noSubclasses
	 *            if set to true, only edges with the exact class are taken into
	 *            account, false means that also subclasses are valid
	 * 
	 * @return the first Edge, or null if this graph contains no edges of the
	 *         specified <code>edgeClass</code>.
	 */
	public Edge getFirstEdge(Class<? extends Edge> edgeClass,
			boolean noSubclasses);

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
	 * The maximum number of vertices that can be stored in the graph before the
	 * internal array structures are expanded.
	 * 
	 * @return the maximum number of vertices
	 */
	public int getMaxVCount();

	/**
	 * Computes the new maximum number of vertices when expansion is needed.
	 * 
	 * @return the new maximum number of vertices
	 */
	public int getExpandedVertexCount();

	/**
	 * Computes the new maximum number of edges when expansion is needed.
	 * 
	 * @return the new maximum number of edges
	 */
	public int getExpandedEdgeCount();

	/**
	 * The maximum number of edges that can be stored in the graph before the
	 * internal array structures are expanded.
	 * 
	 * @return the maximum number of edges
	 */
	public int getMaxECount();

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
	 * Sets the <code>id</code> of this Graph.
	 * 
	 * Precondition: id != null && id.equals(id.trim()) && !id.equals("")
	 * 
	 * @param id
	 *            the new id
	 */
	public void setId(String id);

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
	 *            an EdgeClass (i.e. an M1 interface extending Edge)
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
	public <T extends Vertex> List<T> reachableVertices(Vertex startVertex,
			String pathDescription, Class<T> vertexType);

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
	 *            a VertexClass (i.e. an M1 interface extending Vertex)
	 * 
	 * @return a iterable for all vertices of the specified
	 *         <code>vertexClass</code>
	 */
	public Iterable<Vertex> vertices(Class<? extends Vertex> vertexClass);

	/**
	 * Optimizes edge and vertex ids such that after defragmentation
	 * getMaxECount() == getECount() and getMaxVCount() == getVCount(). That
	 * means that gaps in the vertex and edge IDs are deleted (defragmented) and
	 * that the internal arrays are shortened such that they hold exactly the
	 * required number of vertices/edges.
	 * 
	 * <b>Attention:</b> defragment() possibly changes vertex and edge IDs! *
	 * <b>Attention:</b> Not supported within when using transactions!
	 */
	public void defragment();

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
	 * @return if there have been conflicts
	 */
	public boolean isInConflict();

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
	 *             if {@link Savepoint#getGraph() <code>savepoint</code>
	 *             .getGraph()} != {@link Graph#getCurrentTransaction()
	 * 
	 */
	public void restoreSavepoint(Savepoint savepoint)
			throws InvalidSavepointException;

	/**
	 * Tells whether this graph instance has standard support.
	 * 
	 * @return true if this graph instance has standard support
	 */

	public boolean hasStandardSupport();

	/**
	 * Tells whether this graph instance supports transactions.
	 * 
	 * @return true if this graph instance supports transactions.
	 */
	public boolean hasTransactionSupport();

	/**
	 * Tells whether this graph instanse supports savemem.
	 * 
	 * @return true if this graph instance supports savemem.
	 */
	public boolean hasSavememSupport();

	/**
	 * 
	 * @param <T>
	 *            the generic type
	 * @param cls
	 *            the class for the generic type of the list
	 * @return
	 */
	public <T> JGraLabList<T> createList();

	/**
	 * 
	 * @param <T>
	 *            the generic type
	 * @param cls
	 *            the class for the generic type of the list
	 * @param collection
	 * @return
	 */
	public <T> JGraLabList<T> createList(Collection<? extends T> collection);

	/**
	 * 
	 * @param <T>
	 *            the generic type
	 * @param cls
	 *            the class for the generic type of the list
	 * @param initialCapacity
	 * @return
	 */
	public <T> JGraLabList<T> createList(int initialCapacity);

	/**
	 * 
	 * @param <T>
	 *            the generic type
	 * @param cls
	 *            the class for the generic type of the set
	 * @return
	 */
	public <T> JGraLabSet<T> createSet();

	/**
	 * 
	 * @param <T>
	 *            the generic type
	 * @param cls
	 *            the class for the generic type of the set
	 * @param collection
	 * @return
	 */
	public <T> JGraLabSet<T> createSet(Collection<? extends T> collection);

	/**
	 * 
	 * @param <T>
	 *            the generic type
	 * @param cls
	 *            the class for the generic type of the set
	 * @param initialCapacity
	 * @return
	 */
	public <T> JGraLabSet<T> createSet(int initialCapacity);

	/**
	 * 
	 * @param <T>
	 *            the generic type
	 * @param cls
	 *            the class for the generic type of the set
	 * @param initialCapacity
	 * @param loadFactor
	 * @return
	 */
	public <T> JGraLabSet<T> createSet(int initialCapacity, float loadFactor);

	/**
	 * 
	 * @param <K>
	 *            the generic type for the key
	 * @param <V>
	 *            the generic type for the value
	 * @param key
	 *            the class for the generic type of the key
	 * @param value
	 *            the class for the generic type of the value
	 * @return
	 */
	public <K, V> JGraLabMap<K, V> createMap();

	/**
	 * 
	 * @param <K>
	 *            the generic type for the key
	 * @param <V>
	 *            the generic type for the value
	 * @param key
	 *            the class for the generic type of the key
	 * @param value
	 *            the class for the generic type of the value
	 * @param map
	 * @return
	 */
	public <K, V> JGraLabMap<K, V> createMap(Map<? extends K, ? extends V> map);

	/**
	 * 
	 * @param key
	 *            the class for the generic type of the key
	 * @param value
	 *            the class for the generic type of the value
	 * @param initialCapacity
	 * @return
	 */
	public <K, V> JGraLabMap<K, V> createMap(int initialCapacity);

	/**
	 * 
	 * @param <K>
	 *            the generic type for the key
	 * @param <V>
	 *            the generic type for the value
	 * @param key
	 *            the class for the generic type of the key
	 * @param value
	 *            the class for the generic type of the value
	 * @param initialCapacity
	 * @param loadFactor
	 * @return
	 */
	public <K, V> JGraLabMap<K, V> createMap(int initialCapacity,
			float loadFactor);

	/**
	 * Generic creation of records.
	 * 
	 * @param <T>
	 *            *
	 * @param recordClass
	 * @param io
	 * @return
	 */
	public <T extends Record> T createRecord(Class<T> recordClass, GraphIO io);

	/**
	 * 
	 * @param <T>
	 * @param recordClass
	 * @param io
	 * @return
	 */
	public <T extends Record> T createRecord(Class<T> recordClass,
			Map<String, Object> fields);

	/**
	 * 
	 * @param <T>
	 * @param recordClass
	 * @param io
	 * @return
	 */
	public <T extends Record> T createRecord(Class<T> recordClass,
			Object... components);

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
}
