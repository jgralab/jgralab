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
import de.uni_koblenz.jgralab.EdgeClass;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.EdgeVertexPair;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.VertexClass;


/**
 * @author riediger
 *
 */
public abstract class VertexBaseImpl extends GraphElementImpl implements Vertex {
	
	/**
	 * the id of the vertex 
	 */
	protected int id;
	
	
	
	/**
	 * @param anId the id of the vertex
	 * @param aGraph its corresponding graph
	 * @param theClass the class of the vertex
	 */
	protected VertexBaseImpl(int anId, Graph theGraph, VertexClass cls) {
		super(theGraph, cls);
		id = anId;
		this.myGraph = theGraph;
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
		return "v" + id + ": " + getAttributedElementClass().getName();
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
	
	
	public Iterable<EdgeVertexPair<? extends Edge, ? extends Vertex>> incidences() {
		return new IncidenceIterable<Edge, Vertex>(this);
	}
	

	public Iterable<EdgeVertexPair<? extends Edge, ? extends Vertex>> incidences(EdgeDirection dir) {
		return new IncidenceIterable<Edge, Vertex>(this, dir);
	}
	

	public Iterable<EdgeVertexPair<? extends Edge, ? extends Vertex>> incidences(EdgeClass eclass, EdgeDirection dir) {
		return new IncidenceIterable<Edge, Vertex>(this, eclass, dir);
	}	
	

	public Iterable<EdgeVertexPair<? extends Edge, ? extends Vertex>> incidences(Class<? extends Edge> eclass, EdgeDirection dir) {
		return new IncidenceIterable<Edge, Vertex>(this, eclass, dir);
	}
	

	public Iterable<EdgeVertexPair<? extends Edge, ? extends Vertex>> incidences(EdgeClass eclass, EdgeDirection dir, boolean explicitType) {
		return new IncidenceIterable<Edge, Vertex>(this, eclass, dir, explicitType);
	}
	

	public Iterable<EdgeVertexPair<? extends Edge, ? extends Vertex>> incidences(Class<? extends Edge> eclass, EdgeDirection dir, boolean explicitType) {
		return new IncidenceIterable<Edge, Vertex>(this, eclass, dir, explicitType);
	}
	

	public Iterable<EdgeVertexPair<? extends Edge, ? extends Vertex>> incidences(EdgeClass eclass) {
		return new IncidenceIterable<Edge, Vertex>(this, eclass);
	}
	

	public Iterable<EdgeVertexPair<? extends Edge, ? extends Vertex>> incidences(Class<? extends Edge> eclass) {
		return new IncidenceIterable<Edge, Vertex>(this, eclass);
	}
	
	public Iterable<EdgeVertexPair<? extends Edge, ? extends Vertex>> incidences(EdgeClass eclass, boolean explicitType) {
		return new IncidenceIterable<Edge, Vertex>(this, eclass, explicitType);
	}
	

	public Iterable<EdgeVertexPair<? extends Edge, ? extends Vertex>> incidences(Class<? extends Edge> eclass, boolean explicitType) {
		return new IncidenceIterable<Edge, Vertex>(this, eclass, explicitType);
	}
}
