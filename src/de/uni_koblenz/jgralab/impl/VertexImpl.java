/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2007 Institute for Software Technology
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
public abstract class VertexImpl extends GraphElementImpl implements Vertex {
	
	/**
	 * the id of the vertex 
	 */
	protected int id;
	
	
	/**
	 * holds the version of the vertex strcutre, for every modification of the
	 * structure
	 * (e.g. adding or deleting an incident edge or changing the incidence sequence) 
	 * this version number is increased by one. It is set to 0 when the vertex is created
	 * or the graph is loaded.
	 */
	protected int vertexStructureVersion = 0;
	
	
	/**
	 * @param anId the id of the vertex
	 * @param aGraph its corresponding graph
	 * @param theClass the class of the vertex
	 */
	protected VertexImpl(int anId, Graph theGraph, VertexClass cls) {
		super(theGraph, cls);
		id = anId;
		this.myGraph = theGraph;
	}
	
	@Override
	public int getDegree() {
		return myGraph.getDegree(this);
	}

	@Override
	public int getDegree(EdgeDirection orientation) {
		return myGraph.getDegree(this, orientation);
	}

	@Override
	public Vertex getNextVertex() {
		return myGraph.getNextVertex(this);
	}

	@Override
	public Vertex getNextVertexOfClass(VertexClass aVertexClass) {
		return myGraph.getNextVertexOfClass(this, aVertexClass);
	}

	@Override
	public Vertex getNextVertexOfClass(Class<? extends Vertex> aM1VertexClass) {
		return myGraph.getNextVertexOfClass(this, aM1VertexClass);
	}

	@Override
	public Vertex getNextVertexOfClass(VertexClass aVertexClass,
			boolean explicitType) {
		return myGraph.getNextVertexOfClass(this, aVertexClass, explicitType);
	}

	@Override
	public Vertex getNextVertexOfClass(Class<? extends Vertex> aM1VertexClass,
			boolean explicitType) {
		return myGraph.getNextVertexOfClass(this, aM1VertexClass, explicitType);
	}

	@Override
	public boolean isBefore(Vertex v) {
		return myGraph.isBeforeVertex(v, this);
	}

	@Override
	public void putBefore(Vertex v) {
		myGraph.putBeforeVertex(v, this);
	}

	@Override
	public boolean isAfter(Vertex v) {
		return myGraph.isAfterVertex(v, this);
	}

	@Override
	public void putAfter(Vertex v) {
		myGraph.putAfterVertex(v, this);
	}

	@Override
	public Edge getFirstEdge() {
		return myGraph.getFirstEdge(this);
	}

	@Override
	public Edge getFirstEdge(EdgeDirection orientation) {
		return myGraph.getFirstEdge(this, orientation);
	}

	@Override
	public Edge getFirstEdgeOfClass(EdgeClass anEdgeClass) {
		return myGraph.getFirstEdgeOfClass(this, anEdgeClass, false);
	}

	@Override
	public Edge getFirstEdgeOfClass(Class<? extends Edge> anEdgeClass) {
		return myGraph.getFirstEdgeOfClass(this, anEdgeClass, false);
	}

	@Override
	public Edge getFirstEdgeOfClass(EdgeClass anEdgeClass,
			EdgeDirection orientation) {
		return myGraph.getFirstEdgeOfClass(this, anEdgeClass, orientation, false);
	}

	@Override
	public Edge getFirstEdgeOfClass(Class<? extends Edge> anEdgeClass,
			EdgeDirection orientation) {
		return myGraph.getFirstEdgeOfClass(this, anEdgeClass, orientation, false);
	}

	@Override
	public Edge getFirstEdgeOfClass(EdgeClass anEdgeClass, boolean explicitType) {
		return myGraph.getFirstEdgeOfClass(this, anEdgeClass, explicitType);
	}

	@Override
	public Edge getFirstEdgeOfClass(Class<? extends Edge> anEdgeClass,
			boolean explicitType) {
		return myGraph.getFirstEdgeOfClass(this, anEdgeClass, explicitType);
	}

	@Override
	public Edge getFirstEdgeOfClass(EdgeClass anEdgeClass,
			EdgeDirection orientation, boolean explicitType) {
		return myGraph.getFirstEdgeOfClass(this, anEdgeClass, orientation,
				explicitType);
	}

	@Override
	public Edge getFirstEdgeOfClass(Class<? extends Edge> anEdgeClass,
			EdgeDirection orientation, boolean explicitType) {
		return myGraph.getFirstEdgeOfClass(this, anEdgeClass, orientation,
				explicitType);
	}

	@Override
	public void delete() {
		myGraph.deleteVertex(this);
	}

	@Override
	public void putEdgeBefore(Edge edge, Edge nextEdge) {
		if ((edge.getThis() != this) || (nextEdge.getThis() != this))
			return;
		myGraph.putEdgeBefore(edge, nextEdge);
	}

	@Override
	public void putEdgeAfter(Edge edge, Edge previousEdge) {
		if ((edge.getThis() != this) || (previousEdge.getThis() != this))
			return;
		myGraph.putEdgeAfter(edge, previousEdge);
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.uni_koblenz.jgralab.Vertex#getVertexVersion()
	 */
	@Override
	public long getIncidenceListVersion() {
		return vertexStructureVersion;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.uni_koblenz.jgralab.Vertex#isVertexModified()
	 */
	@Override
	public final boolean isIncidenceListModified(long vertexStructureVersion) {
		return (this.vertexStructureVersion != vertexStructureVersion);
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.uni_koblenz.jgralab.Vertex#vertexModified()
	 */
	@Override
	public final void incidenceListModified() {
		vertexStructureVersion++;
		getGraph().vertexListModified();
	}
	
	

	/* (non-Javadoc)
	 * @see jgralab.Vertex#getDegree(jgralab.EdgeClass)
	 */
	public int getDegree(EdgeClass ec) {
		return getDegree(ec, false);
	}
	
	/* (non-Javadoc)
	 * @see jgralab.Vertex#getDegree(Class)
	 */
	public int getDegree(Class<? extends Edge> ec) {
		return getDegree(ec, false);
	}
	
	/* (non-Javadoc)
	 * @see jgralab.Vertex#getDegree(jgralab.EdgeClass, boolean)
	 */
	public int getDegree(EdgeClass ec, boolean noSubClasses) {
		int degree = 0;
		Edge e = getFirstEdgeOfClass(ec, noSubClasses);
		while (e != null) {
			++degree;
			e = e.getNextEdgeOfClass(ec, noSubClasses);
		}
		return degree;
	}
	
	/* (non-Javadoc)
	 * @see jgralab.Vertex#getDegree(Class, boolean)
	 */
	public int getDegree(Class<? extends Edge> ec, boolean noSubClasses) {
		int degree = 0;
		Edge e = getFirstEdgeOfClass(ec, noSubClasses);
		while (e != null) {
			++degree;
			e = e.getNextEdgeOfClass(ec, noSubClasses);
		}
		return degree;
	}

	/* (non-Javadoc)
	 * @see jgralab.Vertex#getDegree(jgralab.EdgeClass, jgralab.EdgeDirection)
	 */
	public int getDegree(EdgeClass ec, EdgeDirection orientation) {
		return getDegree(ec, orientation, false);
	}
	
	/* (non-Javadoc)
	 * @see jgralab.Vertex#getDegree(Class, jgralab.EdgeDirection)
	 */
	public int getDegree(Class<? extends Edge> ec, EdgeDirection orientation) {
		return getDegree(ec, orientation, false);
	}

	/* (non-Javadoc)
	 * @see jgralab.Vertex#getDegree(jgralab.EdgeClass, jgralab.EdgeDirection, boolean)
	 */
	public int getDegree(EdgeClass ec, EdgeDirection orientation, boolean noSubClasses) {
		int degree = 0;
		Edge e = getFirstEdgeOfClass(ec, orientation, noSubClasses);
		while (e != null) {
			++degree;
			e = e.getNextEdgeOfClass(ec, orientation, noSubClasses);
		}
		return degree;
	}
	
	/* (non-Javadoc)
	 * @see jgralab.Vertex#getDegree(Class, jgralab.EdgeDirection, boolean)
	 */
	public int getDegree(Class<? extends Edge> ec, EdgeDirection orientation, boolean noSubClasses) {
		int degree = 0;
		Edge e = getFirstEdgeOfClass(ec, orientation, noSubClasses);
		while (e != null) {
			++degree;
			e = e.getNextEdgeOfClass(ec, orientation, noSubClasses);
		}
		return degree;
	}
	
	
	/* (non-Javadoc)
	 * @see jgralab.Vertex#getId()
	 */
	public int getId() {
		return id;
	}
	
	public void setId(int anId) {
		id = anId;
	}
	
	public String toString() {
		return "v" + id + ": " + getAttributedElementClass().getQualifiedName();
	}
	
	public int compareTo(AttributedElement a) {
		if (a instanceof Vertex) {
			Vertex v = (Vertex) a;
			return id - v.getId();
		}
		return -1;
	}
	
	
	public Composition getFirstComposition() {
		return (Composition)getFirstEdgeOfClass(Composition.class);
	}

	public Composition getFirstComposition(EdgeDirection orientation) {
		return (Composition)getFirstEdgeOfClass(Composition.class, orientation);
	}

	public Composition getFirstComposition(boolean noSubClasses) {
		return (Composition)getFirstEdgeOfClass(Composition.class, noSubClasses);
	}

	public Composition getFirstComposition(EdgeDirection orientation, boolean noSubClasses) {
		return (Composition)getFirstEdgeOfClass(Composition.class, orientation, noSubClasses);
	}
	
	
	public Aggregation getFirstAggregation() {
		return (Aggregation)getFirstEdgeOfClass(Aggregation.class);
	}

	public Aggregation getFirstAggregation(EdgeDirection orientation) {
		return (Aggregation)getFirstEdgeOfClass(Aggregation.class, orientation);
	}

	public Aggregation getFirstAggregation(boolean noSubClasses) {
		return (Aggregation)getFirstEdgeOfClass(Aggregation.class, noSubClasses);
	}

	public Aggregation getFirstAggregation(EdgeDirection orientation, boolean noSubClasses) {
		return (Aggregation)getFirstEdgeOfClass(Aggregation.class, orientation, noSubClasses);
	}
	
	
	public Iterable<Edge> incidences() {
		return new IncidenceIterable<Edge>(this);
	}
	

	public Iterable<Edge> incidences(EdgeDirection dir) {
		return new IncidenceIterable<Edge>(this, dir);
	}
	

	public Iterable<Edge> incidences(EdgeClass eclass, EdgeDirection dir) {
		return new IncidenceIterable<Edge>(this, eclass, dir);
	}	
	

	public Iterable<Edge> incidences(Class<? extends Edge> eclass, EdgeDirection dir) {
		return new IncidenceIterable<Edge>(this, eclass, dir);
	}
	
	public Iterable<Edge> incidences(EdgeClass eclass) {
		return new IncidenceIterable<Edge>(this, eclass);
	}
	

	public Iterable<Edge> incidences(Class<? extends Edge> eclass) {
		return new IncidenceIterable<Edge>(this, eclass);
	}
	
}
