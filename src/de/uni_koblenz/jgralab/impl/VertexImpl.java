/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2008 Institute for Software Technology
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

package de.uni_koblenz.jgralab.impl;

import de.uni_koblenz.jgralab.Aggregation;
import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Composition;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.VertexClass;

/**
 * @author riediger
 * 
 */
/**
 * @author riediger
 * 
 */
public abstract class VertexImpl extends GraphElementImpl implements Vertex {

	/**
	 * the id of the vertex
	 */
	protected int id;

	/**
	 * holds the version of the vertex strcutre, for every modification of the
	 * structure (e.g. adding or deleting an incident edge or changing the
	 * incidence sequence) this version number is increased by one. It is set to
	 * 0 when the vertex is created or the graph is loaded.
	 */
	protected int vertexStructureVersion = 0;

	/**
	 * @param anId
	 *            the id of the vertex
	 * @param aGraph
	 *            its corresponding graph
	 * @param theClass
	 *            the class of the vertex
	 */
	protected VertexImpl(int anId, Graph theGraph, VertexClass cls) {
		super(theGraph, cls);
		id = anId;
		this.myGraph = theGraph;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Vertex#getDegree()
	 */
	@Override
	public int getDegree() {
		return myGraph.getDegree(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Vertex#getDegree(de.uni_koblenz.jgralab.EdgeDirection)
	 */
	@Override
	public int getDegree(EdgeDirection orientation) {
		return myGraph.getDegree(this, orientation);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Vertex#getNextVertex()
	 */
	@Override
	public Vertex getNextVertex() {
		return myGraph.getNextVertex(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Vertex#getNextVertexOfClass(de.uni_koblenz.jgralab.schema.VertexClass)
	 */
	@Override
	public Vertex getNextVertexOfClass(VertexClass aVertexClass) {
		return myGraph.getNextVertexOfClass(this, aVertexClass);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Vertex#getNextVertexOfClass(java.lang.Class)
	 */
	@Override
	public Vertex getNextVertexOfClass(Class<? extends Vertex> aM1VertexClass) {
		return myGraph.getNextVertexOfClass(this, aM1VertexClass);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Vertex#getNextVertexOfClass(de.uni_koblenz.jgralab.schema.VertexClass,
	 *      boolean)
	 */
	@Override
	public Vertex getNextVertexOfClass(VertexClass aVertexClass,
			boolean explicitType) {
		return myGraph.getNextVertexOfClass(this, aVertexClass, explicitType);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Vertex#getNextVertexOfClass(java.lang.Class,
	 *      boolean)
	 */
	@Override
	public Vertex getNextVertexOfClass(Class<? extends Vertex> aM1VertexClass,
			boolean explicitType) {
		return myGraph.getNextVertexOfClass(this, aM1VertexClass, explicitType);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Vertex#isBefore(de.uni_koblenz.jgralab.Vertex)
	 */
	@Override
	public boolean isBefore(Vertex v) {
		return myGraph.isBeforeVertex(v, this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Vertex#putBefore(de.uni_koblenz.jgralab.Vertex)
	 */
	@Override
	public void putBefore(Vertex v) {
		myGraph.putBeforeVertex(v, this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Vertex#isAfter(de.uni_koblenz.jgralab.Vertex)
	 */
	@Override
	public boolean isAfter(Vertex v) {
		return myGraph.isAfterVertex(v, this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Vertex#putAfter(de.uni_koblenz.jgralab.Vertex)
	 */
	@Override
	public void putAfter(Vertex v) {
		myGraph.putAfterVertex(v, this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Vertex#getFirstEdge()
	 */
	@Override
	public Edge getFirstEdge() {
		return myGraph.getFirstEdge(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Vertex#getFirstEdge(de.uni_koblenz.jgralab.EdgeDirection)
	 */
	@Override
	public Edge getFirstEdge(EdgeDirection orientation) {
		return myGraph.getFirstEdge(this, orientation);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Vertex#getFirstEdgeOfClass(de.uni_koblenz.jgralab.schema.EdgeClass)
	 */
	@Override
	public Edge getFirstEdgeOfClass(EdgeClass anEdgeClass) {
		return myGraph.getFirstEdgeOfClass(this, anEdgeClass, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Vertex#getFirstEdgeOfClass(java.lang.Class)
	 */
	@Override
	public Edge getFirstEdgeOfClass(Class<? extends Edge> anEdgeClass) {
		return myGraph.getFirstEdgeOfClass(this, anEdgeClass, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Vertex#getFirstEdgeOfClass(de.uni_koblenz.jgralab.schema.EdgeClass,
	 *      de.uni_koblenz.jgralab.EdgeDirection)
	 */
	@Override
	public Edge getFirstEdgeOfClass(EdgeClass anEdgeClass,
			EdgeDirection orientation) {
		return myGraph.getFirstEdgeOfClass(this, anEdgeClass, orientation,
				false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Vertex#getFirstEdgeOfClass(java.lang.Class,
	 *      de.uni_koblenz.jgralab.EdgeDirection)
	 */
	@Override
	public Edge getFirstEdgeOfClass(Class<? extends Edge> anEdgeClass,
			EdgeDirection orientation) {
		return myGraph.getFirstEdgeOfClass(this, anEdgeClass, orientation,
				false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Vertex#getFirstEdgeOfClass(de.uni_koblenz.jgralab.schema.EdgeClass,
	 *      boolean)
	 */
	@Override
	public Edge getFirstEdgeOfClass(EdgeClass anEdgeClass, boolean explicitType) {
		return myGraph.getFirstEdgeOfClass(this, anEdgeClass, explicitType);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Vertex#getFirstEdgeOfClass(java.lang.Class,
	 *      boolean)
	 */
	@Override
	public Edge getFirstEdgeOfClass(Class<? extends Edge> anEdgeClass,
			boolean explicitType) {
		return myGraph.getFirstEdgeOfClass(this, anEdgeClass, explicitType);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Vertex#getFirstEdgeOfClass(de.uni_koblenz.jgralab.schema.EdgeClass,
	 *      de.uni_koblenz.jgralab.EdgeDirection, boolean)
	 */
	@Override
	public Edge getFirstEdgeOfClass(EdgeClass anEdgeClass,
			EdgeDirection orientation, boolean explicitType) {
		return myGraph.getFirstEdgeOfClass(this, anEdgeClass, orientation,
				explicitType);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Vertex#getFirstEdgeOfClass(java.lang.Class,
	 *      de.uni_koblenz.jgralab.EdgeDirection, boolean)
	 */
	@Override
	public Edge getFirstEdgeOfClass(Class<? extends Edge> anEdgeClass,
			EdgeDirection orientation, boolean explicitType) {
		return myGraph.getFirstEdgeOfClass(this, anEdgeClass, orientation,
				explicitType);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Vertex#delete()
	 */
	@Override
	public void delete() {
		myGraph.deleteVertex(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Vertex#putEdgeBefore(de.uni_koblenz.jgralab.Edge,
	 *      de.uni_koblenz.jgralab.Edge)
	 */
	@Override
	public void putEdgeBefore(Edge edge, Edge nextEdge) {
		if ((edge.getThis() != this) || (nextEdge.getThis() != this))
			return;
		myGraph.putEdgeBefore(edge, nextEdge);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Vertex#putEdgeAfter(de.uni_koblenz.jgralab.Edge,
	 *      de.uni_koblenz.jgralab.Edge)
	 */
	@Override
	public void putEdgeAfter(Edge edge, Edge previousEdge) {
		if ((edge.getThis() != this) || (previousEdge.getThis() != this))
			return;
		myGraph.putEdgeAfter(edge, previousEdge);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Vertex#getVertexVersion()
	 */
	@Override
	public long getIncidenceListVersion() {
		return vertexStructureVersion;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Vertex#isVertexModified()
	 */
	@Override
	public final boolean isIncidenceListModified(long vertexStructureVersion) {
		return (this.vertexStructureVersion != vertexStructureVersion);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Vertex#vertexModified()
	 */
	@Override
	public final void incidenceListModified() {
		vertexStructureVersion++;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Vertex#getDegree(jgralab.EdgeClass)
	 */
	@Override
	public int getDegree(EdgeClass ec) {
		return getDegree(ec, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Vertex#getDegree(Class)
	 */
	@Override
	public int getDegree(Class<? extends Edge> ec) {
		return getDegree(ec, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Vertex#getDegree(jgralab.EdgeClass, boolean)
	 */
	@Override
	public int getDegree(EdgeClass ec, boolean noSubClasses) {
		int degree = 0;
		Edge e = getFirstEdgeOfClass(ec, noSubClasses);
		while (e != null) {
			++degree;
			e = e.getNextEdgeOfClass(ec, noSubClasses);
		}
		return degree;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Vertex#getDegree(Class, boolean)
	 */
	@Override
	public int getDegree(Class<? extends Edge> ec, boolean noSubClasses) {
		int degree = 0;
		Edge e = getFirstEdgeOfClass(ec, noSubClasses);
		while (e != null) {
			++degree;
			e = e.getNextEdgeOfClass(ec, noSubClasses);
		}
		return degree;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Vertex#getDegree(jgralab.EdgeClass, jgralab.EdgeDirection)
	 */
	@Override
	public int getDegree(EdgeClass ec, EdgeDirection orientation) {
		return getDegree(ec, orientation, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Vertex#getDegree(Class, jgralab.EdgeDirection)
	 */
	@Override
	public int getDegree(Class<? extends Edge> ec, EdgeDirection orientation) {
		return getDegree(ec, orientation, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Vertex#getDegree(jgralab.EdgeClass, jgralab.EdgeDirection,
	 *      boolean)
	 */
	@Override
	public int getDegree(EdgeClass ec, EdgeDirection orientation,
			boolean noSubClasses) {
		int degree = 0;
		Edge e = getFirstEdgeOfClass(ec, orientation, noSubClasses);
		while (e != null) {
			++degree;
			e = e.getNextEdgeOfClass(ec, orientation, noSubClasses);
		}
		return degree;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Vertex#getDegree(Class, jgralab.EdgeDirection, boolean)
	 */
	@Override
	public int getDegree(Class<? extends Edge> ec, EdgeDirection orientation,
			boolean noSubClasses) {
		int degree = 0;
		Edge e = getFirstEdgeOfClass(ec, orientation, noSubClasses);
		while (e != null) {
			++degree;
			e = e.getNextEdgeOfClass(ec, orientation, noSubClasses);
		}
		return degree;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Vertex#getId()
	 */
	@Override
	public final int getId() {
		return id;
	}

	/**
	 * sets the id field of this vertex
	 * 
	 * @param id
	 *            an id
	 */
	void setId(int anId) {
		id = anId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "v" + id + ": " + getAttributedElementClass().getQualifiedName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(AttributedElement a) {
		if (a instanceof Vertex) {
			Vertex v = (Vertex) a;
			return id - v.getId();
		}
		return -1;
	}

	public Composition getFirstComposition() {
		return (Composition) getFirstEdgeOfClass(Composition.class);
	}

	public Composition getFirstComposition(EdgeDirection orientation) {
		return (Composition) getFirstEdgeOfClass(Composition.class, orientation);
	}

	public Composition getFirstComposition(boolean noSubClasses) {
		return (Composition) getFirstEdgeOfClass(Composition.class,
				noSubClasses);
	}

	public Composition getFirstComposition(EdgeDirection orientation,
			boolean noSubClasses) {
		return (Composition) getFirstEdgeOfClass(Composition.class,
				orientation, noSubClasses);
	}

	public Aggregation getFirstAggregation() {
		return (Aggregation) getFirstEdgeOfClass(Aggregation.class);
	}

	public Aggregation getFirstAggregation(EdgeDirection orientation) {
		return (Aggregation) getFirstEdgeOfClass(Aggregation.class, orientation);
	}

	public Aggregation getFirstAggregation(boolean noSubClasses) {
		return (Aggregation) getFirstEdgeOfClass(Aggregation.class,
				noSubClasses);
	}

	public Aggregation getFirstAggregation(EdgeDirection orientation,
			boolean noSubClasses) {
		return (Aggregation) getFirstEdgeOfClass(Aggregation.class,
				orientation, noSubClasses);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Vertex#incidences()
	 */
	@Override
	public Iterable<Edge> incidences() {
		return new IncidenceIterable<Edge>(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Vertex#incidences(de.uni_koblenz.jgralab.EdgeDirection)
	 */
	@Override
	public Iterable<Edge> incidences(EdgeDirection dir) {
		return new IncidenceIterable<Edge>(this, dir);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Vertex#incidences(de.uni_koblenz.jgralab.schema.EdgeClass,
	 *      de.uni_koblenz.jgralab.EdgeDirection)
	 */
	@Override
	public Iterable<Edge> incidences(EdgeClass eclass, EdgeDirection dir) {
		return new IncidenceIterable<Edge>(this, eclass, dir);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Vertex#incidences(java.lang.Class,
	 *      de.uni_koblenz.jgralab.EdgeDirection)
	 */
	@Override
	public Iterable<Edge> incidences(Class<? extends Edge> eclass,
			EdgeDirection dir) {
		return new IncidenceIterable<Edge>(this, eclass, dir);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Vertex#incidences(de.uni_koblenz.jgralab.schema.EdgeClass)
	 */
	@Override
	public Iterable<Edge> incidences(EdgeClass eclass) {
		return new IncidenceIterable<Edge>(this, eclass);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Vertex#incidences(java.lang.Class)
	 */
	@Override
	public Iterable<Edge> incidences(Class<? extends Edge> eclass) {
		return new IncidenceIterable<Edge>(this, eclass);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Vertex#isValid()
	 */
	@Override
	public final boolean isValid() {
		return myGraph.containsVertex(this);
	}
}
