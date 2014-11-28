/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2014 Institute for Software Technology
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

import java.util.Comparator;
import java.util.List;

import org.pcollections.POrderedSet;

import de.uni_koblenz.jgralab.schema.AggregationKind;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.VertexClass;

/**
 * represents a vertex, schema classes inherit from this class
 *
 * @author ist@uni-koblenz.de
 *
 */
public interface Vertex extends GraphElement<VertexClass, Vertex> {

	/**
	 * Checks if the list of incident edges has changed with respect to the
	 * given <code>incidenceListVersion</code>.
	 */
	public boolean isIncidenceListModified(long incidenceListVersion);

	/**
	 * @return the internal vertex structure version
	 * @see #isIncidenceListModified(long)
	 */
	public long getIncidenceListVersion();

	/**
	 * @return the number of connected incidences to the vertex
	 */
	public int getDegree();

	/**
	 * @param orientation
	 *            of connected incidences,
	 * @return number of IN or OUT incidences connected to the vertex
	 */
	public int getDegree(EdgeDirection orientation);

	/**
	 * @param ec
	 *            an EdgeClass
	 * @return number of IN or OUT incidences of the specified EdgeClass
	 */
	public int getDegree(EdgeClass ec);

	/**
	 * @param ec
	 *            an EdgeClass
	 * @param orientation
	 *            of connected incidences,
	 * @return number of IN or OUT incidences connected to the vertex
	 */
	public int getDegree(EdgeClass ec, EdgeDirection orientation);

	/**
	 * @return the next vertex in vSeq
	 */
	public Vertex getNextVertex();

	/**
	 * @return the previous vertex in vSeq
	 */
	public Vertex getPrevVertex();

	/**
	 * @param vertexClass
	 *            the class of the next vertex
	 * @return the next vertex in vSeq of class aVertexClass or its superclasses
	 */
	public Vertex getNextVertex(VertexClass vertexClass);

	/**
	 * @return first incident edge of this vertex
	 */
	public Edge getFirstIncidence();

	/**
	 * @return last incident edge of this vertex
	 */
	public Edge getLastIncidence();

	/**
	 * @param orientation
	 *            of connected incidences,
	 * @return the first incidence of vertex with direction IN or OUT
	 */
	public Edge getFirstIncidence(EdgeDirection orientation);

	/**
	 * Get the first incident edge which as one of the aggregation semantics
	 * given by <code>kind</code> at either this vertex (thisIncidence == true)
	 * or that vertex (thisIncidence == false). If no <code>kind</code> is
	 * given, it simply returns the first incident edge.<br/>
	 * <br/>
	 * For example, this returns the first edge to a parent vertex in the
	 * containment hierarchy.
	 *
	 * <pre>
	 * v.getFirstIncidence(true, AggregationKind.SHARED, AggregationKind.COMPOSITE)
	 * </pre>
	 *
	 * And this returns the first edge to a child vertex in the containment
	 * hierarchy.
	 *
	 * <pre>
	 * v.getFirstIncidence(false, AggregationKind.SHARED, AggregationKind.COMPOSITE)
	 * </pre>
	 *
	 * @see Edge#getNextIncidence(boolean, AggregationKind...)
	 *
	 * @param thisIncidence
	 *            if true, <code>kinds</code> has to match the incidence at this
	 *            vertex, else it has to match the opposite incidence
	 *
	 * @return the first incident edge where the incidence at this vertex
	 *         (thisIncidence == true) or that vertex (thisIncidence == false)
	 *         has one of the aggregation semantics given by <code>kind</code>.
	 */
	public Edge getFirstIncidence(boolean thisIncidence,
			AggregationKind... kinds);

	/**
	 * @param anEdgeClass
	 *            the edge class to search for
	 * @return the first incidence in iSeq where the corresponding edge is of
	 *         class anEdgeClass
	 */
	public Edge getFirstIncidence(EdgeClass anEdgeClass);

	/**
	 * @param anEdgeClass
	 *            the edge class to search for
	 * @param orientation
	 *            of the edge
	 * @return the first incidence in iSeq where the corresponding edge is of
	 *         class anEdgeClass
	 */
	public Edge getFirstIncidence(EdgeClass anEdgeClass,
			EdgeDirection orientation);

	/**
	 * @param v
	 * @return true, if this vertex is somewhere before v in vSeq
	 */
	public boolean isBefore(Vertex v);

	/**
	 * puts this vertex immediately before v in vSeq
	 *
	 * @param v
	 */
	public void putBefore(Vertex v);

	/**
	 * @param v
	 * @return true, if this vertex is somewhere after v in vSeq
	 */
	public boolean isAfter(Vertex v);

	/**
	 * puts this vertex immediately after v in vSeq
	 *
	 * @param v
	 */
	public void putAfter(Vertex v);

	/**
	 * Using this method, one can simply iterate over all incident edges of this
	 * vertex using the advanced for-loop
	 *
	 * @return a iterable object which can be iterated through using the
	 *         advanced for-loop
	 */
	public Iterable<Edge> incidences();

	/**
	 * @param <T>
	 * @param returnType
	 *            the class of the vertices you can reach with that path (acts
	 *            as implicit GoalRestriction)
	 * @param pathElements
	 *            an array of {@link PathElement}s
	 * @return a Set of vertices reachable by traversing the path given by
	 *         pathElements
	 */
	public <T extends Vertex> POrderedSet<T> reachableVertices(
			Class<T> returnType, PathElement... pathElements);

	/**
	 * Using this method, one can simply iterate over all incident edges of this
	 * vertex using the advanced for-loop
	 *
	 * @param dir
	 *            the direction of the edges which should be iterated, either
	 *            EdgeDirection.IN or EdgeDirection.OUT
	 * @return a iterable object which can be iterated through using the
	 *         advanced for-loop
	 */
	public Iterable<Edge> incidences(EdgeDirection dir);

	/**
	 * Using this method, one can simply iterate over all incident edges of this
	 * vertex using the advanced for-loop
	 *
	 * @param eclass
	 *            the EdgeClass of the edges which should be iterated
	 * @param dir
	 *            the direction of the edges which should be iterated, either
	 *            EdgeDirection.IN or EdgeDirection.OUT
	 * @return a iterable object which can be iterated through using the
	 *         advanced for-loop
	 */
	public Iterable<Edge> incidences(EdgeClass eclass, EdgeDirection dir);

	/**
	 * Using this method, one can simply iterate over all incident edges of this
	 * vertex using the advanced for-loop
	 *
	 * @param eclass
	 *            the EdgeClass of the edges which should be iterated
	 * @return a iterable object which can be iterated through using the
	 *         advanced for-loop
	 */
	public Iterable<Edge> incidences(EdgeClass eclass);

	/**
	 * Sorts the incidence sequence according to the given comparator in
	 * ascending order.
	 *
	 * @param comp
	 *            the comparator that defines the desired incidence order.
	 */
	public void sortIncidences(Comparator<Edge> comp);

	public List<? extends Vertex> adjacences(String role);

	public Edge addAdjacence(String role, Vertex other);

	public List<Vertex> removeAdjacences(String role);

	public void removeAdjacence(String role, Vertex other);

	@Override
	public VertexClass getAttributedElementClass();

}
