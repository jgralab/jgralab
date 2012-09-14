/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2012 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 *
 * For bug reports, documentation and further information, visit
 *
 *                         https://github.com/jgralab/jgralab
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

import de.uni_koblenz.jgralab.eca.ECARuleManagerInterface;
import de.uni_koblenz.jgralab.exception.GraphIOException;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.EnumDomain;
import de.uni_koblenz.jgralab.schema.GraphClass;
import de.uni_koblenz.jgralab.schema.RecordDomain;
import de.uni_koblenz.jgralab.schema.VertexClass;

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
	 * Creates a {@link Vertex} of the specified {@link VertexClass} and adds
	 * the new vertex to this {@link Graph}.
	 * 
	 * @param vc
	 *            the {@link VertexClass} of the new {@link Vertex}
	 */
	public <T extends Vertex> T createVertex(VertexClass vc);

	/**
	 * Creates an {@link Edge} of the specified {@link EdgeClass}
	 * <code>ec</code> that connects <code>alpha</code> and <code>omega</code>
	 * vertices and adds the new edge to this {@link Graph}.
	 * 
	 * @param ec
	 *            the {@link EdgeClass} of the new {@link Edge}
	 * @param alpha
	 *            the alpha {@link Vertex} of the new {@link Edge}
	 * @param omega
	 *            the omega {@link Vertex} of the new {@link Edge}
	 */
	public <T extends Edge> T createEdge(EdgeClass ec, Vertex alpha,
			Vertex omega);

	/**
	 * Creates a {@link TemporaryVertex} and adds it to this {@link Graph}
	 */
	public TemporaryVertex createTemporaryVertex();

	/**
	 * Creates a {@link TemporaryVertex} with a give preliminary
	 * {@link VertexClass} and adds it to this {@link Graph}
	 */
	public TemporaryVertex createTemporaryVertex(VertexClass preliminaryType);

	/**
	 * Creates a {@link TemporaryEdge} that connects <code>alpha</code> and
	 * <code>omega</code> vertices and adds the new edge to this {@link Graph}
	 * 
	 * @param alpha
	 *            the alpha {@link Vertex} of the new {@link TemporaryEdge}
	 * @param omega
	 *            the omega {@link Vertex} of the new {@link TemporaryEdge}
	 */
	public TemporaryEdge createTemporaryEdge(Vertex alpha, Vertex omega);

	/**
	 * Creates a {@link TemporaryEdge} with a given preliminary
	 * {@link EdgeClass} that connects <code>alpha</code> and <code>omega</code>
	 * vertices and adds the new edge to this {@link Graph}
	 * 
	 * @param peliminaryType
	 *            the preliminary {@link EdgeClass} of this
	 *            {@link TemporaryEdge}
	 * 
	 * @param alpha
	 *            the alpha {@link Vertex} of the new {@link TemporaryEdge}
	 * @param omega
	 *            the omega {@link Vertex} of the new {@link TemporaryEdge}
	 */
	public TemporaryEdge createTemporaryEdge(EdgeClass preliminaryType,
			Vertex alpha, Vertex omega);

	/**
	 * Retrieves the enum constant of an {@link EnumDomain} given by
	 * <code>constantName</code>.
	 * 
	 * @param enumDomain
	 *            the {@link EnumDomain} to create a constant for
	 * @param constantName
	 *            the {@code String} value of the constant to create
	 * 
	 * @return the retrieved constant
	 */
	public Object getEnumConstant(EnumDomain enumDomain, String constantName);

	/**
	 * Creates a {@link Record} of type {@link RecordDomain} with values as
	 * specified by <code>values</code>.
	 * 
	 * @param recordDomain
	 *            the {@link RecordDomain} to create a {@link Record} for
	 * @param values
	 *            the {@code Map} with the records components
	 * 
	 * @return the created {@link Record}
	 */
	public Record createRecord(RecordDomain recordDomain,
			Map<String, Object> values);

	/**
	 * Checks whether this {@link Graph} has changed with respect to the given
	 * <code>previousVersion</code>. Every change in this {@link Graph}, e.g.
	 * adding, creating and reordering of {@link Edge} and {@link Vertex}
	 * instances or changes of attributes of the graph, an {@link Edge} or a
	 * {@link Vertex} are treated as a change.
	 * 
	 * @param previousVersion
	 *            The version to check against
	 * @return <code>true</code> if the internal graph version of the graph is
	 *         different from the <code>previousVersion</code>
	 */
	public boolean isGraphModified(long previousVersion);

	/**
	 * Returns the version counter of this {@link Graph}.
	 * 
	 * @return the graph version
	 * @see #isGraphModified(long)
	 */
	public long getGraphVersion();

	/**
	 * @return true if this {@link Graph} contains the given {@link Vertex}
	 *         <code>v</code>.
	 */
	public boolean containsVertex(Vertex v);

	/**
	 * @return true if this {@link Graph} contains the given {@link Edge}
	 *         <code>e</code>.
	 */
	boolean containsEdge(Edge e);

	/**
	 * Removes the {@link Vertex} <code>v</code> from the vertex sequence of
	 * this {@link Graph}. Also, any edges incident to {@link Vertex}
	 * <code>v</code> are deleted. If <code>v</code> is the parent of a
	 * composition, all child vertices are also deleted.
	 * 
	 * Preconditions: v.isValid()
	 * 
	 * Postconditions: !v.isValid() && !containsVertex(v) &&
	 * getVertex(v.getId()) == null
	 * 
	 * @param v
	 *            the {@link Vertex} to be deleted
	 */
	public void deleteVertex(Vertex v);

	/**
	 * Removes the {@link Edge} <code>e</code> from the edge sequence of this
	 * {@link Graph}. This implies changes to the incidence lists of the alpha
	 * and omega {@link Vertex} of <code>e</code>.
	 * 
	 * Preconditions: e.isValid()
	 * 
	 * Postconditions: !e.isValid() && !containsEdge(e) && getEdge(e.getId()) ==
	 * null
	 * 
	 * @param e
	 *            the {@link Edge} to be deleted
	 */
	public void deleteEdge(Edge e);

	/**
	 * Returns the first {@link Vertex} in the vertex sequence of this
	 * {@link Graph}.
	 * 
	 * @return the first {@link Vertex}, or null if this {@link Graph} contains
	 *         no vertices.
	 */
	public Vertex getFirstVertex();

	/**
	 * Returns the last {@link Vertex} in the vertex sequence of this
	 * {@link Graph}.
	 * 
	 * @return the last {@link Vertex}, or null if this graph contains no
	 *         vertices.
	 */
	public Vertex getLastVertex();

	/**
	 * Returns the first {@link Vertex} of the specified {@link VertexClass}
	 * (including subclasses) in the vertex sequence of this {@link Graph}.
	 * 
	 * @param vertexClass
	 *            a {@link VertexClass} (i.e. an instance of schema.VertexClass)
	 * 
	 * @return the first {@link Vertex}, or null if this {@link Graph} contains
	 *         no vertices of the specified {@link VertexClass}
	 */
	public Vertex getFirstVertex(VertexClass vertexClass);

	/**
	 * Returns the first {@link Edge} in the edge sequence of this {@link Graph}
	 * .
	 * 
	 * @return the first {@link Edge}, or null if this {@link Graph} contains no
	 *         edges.
	 */
	public Edge getFirstEdge();

	/**
	 * Returns the last {@link Edge} in the edge sequence of this {@link Graph}.
	 * 
	 * @return the last Edge, or null if this graph contains no edges.
	 */
	public Edge getLastEdge();

	/**
	 * Returns the first {@link Edge} of the specified {@link EdgeClass}
	 * (including subclasses) in the edge sequence of this {@link Graph}.
	 * 
	 * @param edgeClass
	 *            an {@link EdgeClass} (i.e. an instance of schema.EdgeClass)
	 * 
	 * @return the first {@link Edge}, or null if this {@link Graph} contains no
	 *         edges of the specified {@link EdgeClass}.
	 */
	public Edge getFirstEdge(EdgeClass edgeClass);

	/**
	 * Returns the {@link Vertex} with the specified <code>id</code> if such a
	 * vertex exists in this {@link Graph}.
	 * 
	 * @param id
	 *            the id of the {@link Vertex} (must be > 0)
	 * @return the {@link Vertex}, or null if no such vertex exists
	 */
	public Vertex getVertex(int id);

	/**
	 * Returns the oriented {@link Edge} with the specified <code>id</code> if
	 * such an edge exists in this {@link Graph}. If <code>id</code> is
	 * positive, the normal edge is returned, otherwise, the reversed Edge is
	 * returned.
	 * 
	 * @param id
	 *            the id of the edge (must be != 0)
	 * @return the Edge, or null if no such edge exists
	 */
	public Edge getEdge(int id);

	/**
	 * Returns the number of vertices in this {@link Graph}.
	 * 
	 * @return the number of vertices
	 */
	public int getVCount();

	/**
	 * Returns the number of edges in this {@link Graph}.
	 * 
	 * @return the number of edges
	 */
	public int getECount();

	/**
	 * Returns the <code>id</code> of this {@link Graph}. JGraLab assigns a 128
	 * bit random id to all Graphs upon creation. This initial id is most likely
	 * (but not guaranteed) unique.
	 * 
	 * @return the id of this graph
	 */
	public String getId();

	/**
	 * Returns an {@code Iterable} which iterates over all edges of this
	 * {@link Graph} in the order determined by the edge sequence.
	 * 
	 * @return an {@code Iterable} for all edges
	 */
	public Iterable<Edge> edges();

	/**
	 * Returns an {@code Iterable} which iterates over all edges of this
	 * {@link Graph} which have the specified {@link EdgeClass} (including
	 * subclasses), in the order determined by the edge sequence.
	 * 
	 * @param edgeClass
	 *            an {@link EdgeClass} (i.e. instance of schema.EdgeClass)
	 * 
	 * @return an {@code Iterable} for all edges of the specified
	 *         {@link EdgeClass}
	 */
	public Iterable<Edge> edges(EdgeClass edgeClass);

	/**
	 * Returns an {@code Iterable} which iterates over all vertices of this
	 * {@link Graph} in the order determined by the vertex sequence.
	 * 
	 * @return an {@code Iterable} for all vertices
	 */
	public Iterable<Vertex> vertices();

	public Iterable<Vertex> vertices(VertexFilter<Vertex> filter);

	/**
	 * Returns an {@code Iterable} which iterates over all vertices of this
	 * {@link Graph} which have the specified {@link VertexClass} (including
	 * subclasses), in the order determined by the vertex sequence.
	 * 
	 * @param vertexclass
	 *            a {@link VertexClass} (i.e. instance of schema.VertexClass)
	 * 
	 * @return an {@code Iterable} for all vertices of the specified
	 *         {@link VertexClass}
	 */
	public Iterable<Vertex> vertices(VertexClass vertexclass);

	public Iterable<Vertex> vertices(VertexClass vertexclass,
			VertexFilter<Vertex> filter);

	/**
	 * @return true if there are {@link TemporaryVertex} or
	 *         {@link TemporaryEdge} elements in this {@link Graph}
	 */
	public boolean hasTemporaryElements();

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

	/**
	 * Returns the {@link de.uni_koblenz.jgralab.eca.ECARuleManager} of this
	 * {@link Graph}, if the {@link de.uni_koblenz.jgralab.eca.ECARuleManager}
	 * is not instantiated, an instance is created
	 * 
	 * @return the {@link de.uni_koblenz.jgralab.eca.ECARuleManager} of this
	 *         {@link Graph}
	 */
	public ECARuleManagerInterface getECARuleManager();

	/**
	 * @return whether the {@link de.uni_koblenz.jgralab.eca.ECARuleManager} of
	 *         this {@link Graph} is instantiated
	 */
	public boolean hasECARuleManager();

	public TraversalContext setTraversalContext(TraversalContext tc);

	public TraversalContext getTraversalContext();

	/**
	 * Returns the {@link GraphFactory} this {@link Graph} uses to create
	 * {@link Vertex} and {@link Edge} instances
	 * 
	 * @return the {@link GraphFactory} of this {@link Graph}
	 */
	public GraphFactory getGraphFactory();

	/**
	 * Set the {@link GraphFactory} this {@link Graph} uses to create
	 * {@link Vertex} and {@link Edge} instances
	 * 
	 * @param graphFactory
	 *            the {@link GraphFactory} to replace the current
	 *            {@link GraphFactory} of this {@link Graph}
	 */
	public void setGraphFactory(GraphFactory graphFactory);

	/**
	 * Saves this {@link Graph} to the file named <code>filename</code>.
	 * 
	 * @param filename
	 *            the name of the TG file to be written
	 * 
	 * @throws GraphIOException
	 *             if an IOException occurs
	 */
	public void save(String filename) throws GraphIOException;

	/**
	 * Saves this {@link Graph} to the file named <code>filename</code>. A
	 * {@link ProgressFunction} <code>pf</code> can be used to monitor progress.
	 * 
	 * @param filename
	 *            the name of the TG file to be written
	 * @param pf
	 *            a {@link ProgressFunction}, may be <code>null</code>
	 * 
	 * @throws GraphIOException
	 *             if an IOException occurs
	 */
	public void save(String filename, ProgressFunction pf)
			throws GraphIOException;

	/**
	 * Saves this {@link Graph} to the stream <code>out</code>. The stream is
	 * <em>not</em> closed.
	 * 
	 * @param out
	 *            a DataOutputStream
	 * 
	 * @throws GraphIOException
	 *             if an IOException occurs
	 */
	public void save(DataOutputStream out) throws GraphIOException;

	/**
	 * Saves this {@link Graph} to the stream <code>out</code>. A
	 * {@link ProgressFunction} <code>pf</code> can be used to monitor progress.
	 * The stream is <em>not</em> closed.
	 * 
	 * @param out
	 *            a DataOutputStream
	 * @param pf
	 *            a {@link ProgressFunction}, may be <code>null</code>
	 * 
	 * @throws GraphIOException
	 *             if an IOException occurs
	 */
	public void save(DataOutputStream out, ProgressFunction pf)
			throws GraphIOException;

	/**
	 * @return the {@link GraphClass} of this {@link Graph}
	 */
	@Override
	public GraphClass getAttributedElementClass();
}
