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

import de.uni_koblenz.jgralab.schema.AggregationKind;
import de.uni_koblenz.jgralab.schema.EdgeClass;

/**
 * represents a signed edge, has an orientation
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public interface Edge extends GraphElement {

	/**
	 * @return the next incidence object in iSeq of current vertex
	 */
	public Edge getNextIncidence();

	/**
	 * @return the previous incidence object in iSeq of current vertex
	 */
	public Edge getPrevIncidence();

	/**
	 * @param orientation
	 *            the orientation the next incidence should have
	 * @return the next incidence object in iSeq of current vertex
	 */
	public Edge getNextIncidence(EdgeDirection orientation);

	/**
	 * Gets the next incident edge at the current vertex, which has one of
	 * <code>kinds</code> aggregation semantics at this (
	 * <code>thisIncidence == true</code>) or that (
	 * <code>thisIncidence == false</code>) side.
	 * 
	 * If no <code>kind</code> is given, it simply returns the first incident
	 * edge.
	 * 
	 * @see Vertex#getFirstIncidence(boolean, AggregationKind...)
	 * 
	 * @param thisIncidence
	 *            if true, <code>kinds</code> has to match the incidence at the
	 *            current vertex, else it has to matche the incedence at the
	 *            opposite vertex
	 * @param kinds
	 *            the acceptable aggregation kinds
	 * @return the next incident edge at the current vertex, which has one of
	 *         <code>kinds</code> aggregation semantics at this (
	 *         <code>thisIncidence == true</code>) or that (
	 *         <code>thisIncidence == false</code>) side.
	 */
	public Edge getNextIncidence(boolean thisIncidence, AggregationKind... kinds);

	/**
	 * @param anEdgeClass
	 *            the edge class to search for
	 * @return the next incidence in iSeq where the corresponding edge is of
	 *         class anEdgeClass
	 */
	public Edge getNextIncidence(EdgeClass anEdgeClass);

	/**
	 * @param anEdgeClass
	 *            the edge class to search for
	 * @return the next incidence in iSeq where the corresponding edge is of
	 *         class anEdgeClass
	 */
	public Edge getNextIncidence(Class<? extends Edge> anEdgeClass);

	/**
	 * @param anEdgeClass
	 *            the edge class to search for
	 * @param orientation
	 *            the orientation the next incidence should have
	 * @return the next incidence in iSeq where the corresponding edge is of
	 *         class anEdgeClass
	 */
	public Edge getNextIncidence(EdgeClass anEdgeClass,
			EdgeDirection orientation);

	/**
	 * @param anEdgeClass
	 *            the edge class to search for
	 * @param orientation
	 *            the orientation the next incidence should have
	 * @return the next incidence in iSeq where the corresponding edge is of
	 *         class anEdgeClass
	 */
	public Edge getNextIncidence(Class<? extends Edge> anEdgeClass,
			EdgeDirection orientation);

	/**
	 * @param anEdgeClass
	 *            the edge class to search for
	 * @param noSubclasses
	 *            if true, no subclasses are returned
	 * @return the next incidence in iSeq where the corresponding edge is of
	 *         explicit class anEdgeClass
	 */
	public Edge getNextIncidence(EdgeClass anEdgeClass, boolean noSubclasses);

	/**
	 * @param anEdgeClass
	 *            the edge class to search for
	 * @param noSubclasses
	 *            if true, no subclasses are returned
	 * @return the next incidence in iSeq where the corresponding edge is of
	 *         explicit class anEdgeClass
	 */
	public Edge getNextIncidence(Class<? extends Edge> anEdgeClass,
			boolean noSubclasses);

	/**
	 * @param anEdgeClass
	 *            the edge class to search for
	 * @param orientation
	 *            the orientation the next incidence should have
	 * @param noSubclasses
	 *            if true, no subclasses are returned
	 * @return the next incidence in iSeq where the corresponding edge is of
	 *         explicit class anEdgeClass
	 */
	public Edge getNextIncidence(EdgeClass anEdgeClass,
			EdgeDirection orientation, boolean noSubclasses);

	/**
	 * @param anEdgeClass
	 *            the edge class to search for
	 * @param orientation
	 *            the orientation the next incidence should have
	 * @param noSubclasses
	 *            if true, no subclasses are returned
	 * @return the next incidence in iSeq where the corresponding edge is of
	 *         explicit class anEdgeClass
	 */
	public Edge getNextIncidence(Class<? extends Edge> anEdgeClass,
			EdgeDirection orientation, boolean noSubclasses);

	/**
	 * @return the "this" vertex object, that is the object this directed edge
	 *         starts at
	 */
	public Vertex getThis();

	/**
	 * @return the "that" vertex object, that is the object this directed edge
	 *         ends at
	 */
	public Vertex getThat();

	/**
	 * @return the rolename of the edge at the this-vertex
	 */
	public String getThisRole();

	/**
	 * @return the rolename of the edge at the that-vertex
	 */
	public String getThatRole();

	/**
	 * @return next edge in eSeq
	 */
	public Edge getNextEdge();

	/**
	 * @return previous edge in eSeq
	 */
	public Edge getPrevEdge();

	/**
	 * @param anEdgeClass
	 * @return next edge of anEdgeClass or its superclasses in eSeq
	 */
	public Edge getNextEdge(EdgeClass anEdgeClass);

	/**
	 * @param anEdgeClass
	 * @return next edge of anEdgeClass or its superclasses in eSeq
	 */
	public Edge getNextEdge(Class<? extends Edge> anEdgeClass);

	/**
	 * @param anEdgeClass
	 * @param noSubclasses
	 *            if true, no subclasses are returned
	 * @return next edge object of explicit anEdgeClass in eSeq
	 */
	public Edge getNextEdge(EdgeClass anEdgeClass,
			boolean noSubclasses);

	/**
	 * @param anEdgeClass
	 * @param noSubclasses
	 *            if true, no subclasses are returned
	 * @return next edge object of explicit anEdgeClass in eSeq
	 */
	public Edge getNextEdge(Class<? extends Edge> anEdgeClass,
			boolean noSubclasses);

	/**
	 * @return the alpha vertex of this edge
	 */
	public Vertex getAlpha();

	/**
	 * @return the omega vertex of this edge
	 */
	public Vertex getOmega();

	/**
	 * @param e
	 * @return true if this edge is somewhere before e in the lambda sequence of
	 *         the this-vertex
	 */
	public boolean isBeforeIncidence(Edge e);

	/**
	 * @param e
	 * @return true if this edge is somewhere after e in the lambda sequence of
	 *         the this-vertex
	 */
	public boolean isAfterIncidence(Edge e);

	/**
	 * @param e
	 * @return true if this edge is somewhere before e in eSeq
	 */
	public boolean isBeforeEdge(Edge e);

	/**
	 * puts this edge immediately before e in eSeq
	 * 
	 * @param e
	 */
	public void putBeforeEdge(Edge e);

	/**
	 * @param e
	 * @return true if this edge is somewhere after e in eSeq
	 */
	public boolean isAfterEdge(Edge e);

	/**
	 * puts this edge immediately after anEdge in eSeq
	 * 
	 * @param e
	 */
	public void putAfterEdge(Edge e);

	/**
	 * removes this edge from eSeq and erases its attributes @ if used on an
	 * incidence
	 */
	public void delete();

	/**
	 * sets the alpha vertex to v
	 * 
	 * @param v
	 *            a vertex
	 */
	void setAlpha(Vertex v);

	/**
	 * sets the omega vertex to v
	 * 
	 * @param v
	 *            a vertex
	 */
	void setOmega(Vertex v);

	/**
	 * sets the this vertex to v
	 * 
	 * @param v
	 *            a vertex
	 */
	void setThis(Vertex v);

	/**
	 * sets the that vertex to v
	 * 
	 * @param v
	 *            a vertex
	 */
	void setThat(Vertex v);

	/**
	 * puts this edge immediately before the given edge <code>e</code> in the
	 * incidence list of the <code>this-vertex</code> of this edge. This does
	 * neither affect the global edge sequence eSeq nor the alpha or omega
	 * vertices, only the order of the edges at the <code>this-vertex</code> of
	 * this edge is changed.
	 */
	public void putIncidenceBefore(Edge e);

	/**
	 * puts this edge after the after given edge <code>previousEdge</code> in
	 * the incidence list of the <code>this-vertex</code> of this edge. This
	 * does neither affect the global edge sequence eSeq nor the alpha or omega
	 * vertices, only the order of the edges at the <code>this-vertex</code> of
	 * this edge is changed.
	 */
	public void putIncidenceAfter(Edge e);

	/**
	 * returns the normal edge of this edge
	 */
	public Edge getNormalEdge();

	/**
	 * returns the reversed edge of this edge, e.g. for the edge -1 the reversed
	 * edge is 1, for the edge 1 the reversed edge is -1.
	 */
	public Edge getReversedEdge();

	/**
	 * returns true if this edge is the "normal" edge, false otherwise
	 */
	public boolean isNormal();

	/**
	 * @return the semantics of this edge, e.g. AggregationKind.NONE, SHARED or
	 *         COMPOSITE
	 */
	public AggregationKind getSemantics();

	/**
	 * @return the semantics of the alpha end of this edge, e.g.
	 *         AggregationKind.NONE, SHARED or COMPOSITE
	 */
	public AggregationKind getAlphaSemantics();

	/**
	 * @return the semantics of the omega end of this edge, e.g.
	 *         AggregationKind.NONE, SHARED or COMPOSITE
	 */
	public AggregationKind getOmegaSemantics();

	/**
	 * @return the semantics of the this end of this edge, e.g.
	 *         AggregationKind.NONE, SHARED or COMPOSITE
	 */
	public AggregationKind getThisSemantics();

	/**
	 * @return the semantics of the that end of this edge, e.g.
	 *         AggregationKind.NONE, SHARED or COMPOSITE
	 */
	public AggregationKind getThatSemantics();

}
