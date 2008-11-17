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

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphException;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.VertexClass;

/**
 * TODO add comment
 * 
 * @author ist@uni-koblenz.de
 */
public abstract class VertexImpl extends GraphElementImpl implements Vertex {
	private int id;

	// global vertex sequence
	private VertexImpl nextVertex;
	private VertexImpl prevVertex;

	// lambda sequence
	private IncidenceImpl firstIncidence;
	private IncidenceImpl lastIncidence;

	/**
	 * holds the version of the vertex strcutre, for every modification of the
	 * structure (e.g. adding or deleting an incident edge or changing the
	 * incidence sequence) this version number is increased by one. It is set to
	 * 0 when the vertex is created or the graph is loaded.
	 */
	protected long incidenceListVersion = 0;

	/**
	 * @param id
	 *            the id of the vertex
	 * @param graph
	 *            its corresponding graph
	 */
	protected VertexImpl(int id, Graph graph) {
		super(graph);
		setId(id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Vertex#getDegree()
	 */
	@Override
	public int getDegree() {
		return getDegree(EdgeDirection.INOUT);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Vertex#getDegree(de.uni_koblenz.jgralab.EdgeDirection)
	 */
	@Override
	public int getDegree(EdgeDirection orientation) {
		int d = 0;
		IncidenceImpl i = getFirstIncidence();
		switch (orientation) {
		case IN:
			while (i != null) {
				if (!i.isNormal()) {
					++d;
				}
				i = i.getNextIncidence();
			}
			return d;
		case OUT:
			while (i != null) {
				if (i.isNormal()) {
					++d;
				}
				i = i.getNextIncidence();
			}
			return d;
		case INOUT:
			while (i != null) {
				++d;
				i = i.getNextIncidence();
			}
			return d;
		default:
			throw new RuntimeException("FIXME!");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Vertex#getNextVertex()
	 */
	@Override
	public Vertex getNextVertex() {
		assert isValid();
		return nextVertex;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Vertex#getNextVertexOfClass(java.lang.Class)
	 */
	@Override
	public Vertex getNextVertexOfClass(Class<? extends Vertex> vertexClass) {
		return getNextVertexOfClass(vertexClass, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Vertex#getNextVertexOfClass(java.lang.Class,
	 *      boolean)
	 */
	@Override
	public Vertex getNextVertexOfClass(Class<? extends Vertex> aM1VertexClass,
			boolean noSubclasses) {
		assert isValid();
		VertexImpl v = (VertexImpl) getNextVertex();
		while (v != null) {
			if (noSubclasses) {
				if (aM1VertexClass == v.getM1Class()) {
					return v;
				}
			} else {
				if (aM1VertexClass.isInstance(v)) {
					return v;
				}
			}
			v = (VertexImpl) v.getNextVertex();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Vertex#getNextVertexOfClass(de.uni_koblenz.jgralab.schema.VertexClass)
	 */
	@Override
	public Vertex getNextVertexOfClass(VertexClass vertexClass) {
		return getNextVertexOfClass(vertexClass.getM1Class(), false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Vertex#getNextVertexOfClass(de.uni_koblenz.jgralab.schema.VertexClass,
	 *      boolean)
	 */
	@Override
	public Vertex getNextVertexOfClass(VertexClass vertexClass,
			boolean noSubclasses) {
		return getNextVertexOfClass(vertexClass.getM1Class(), noSubclasses);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Vertex#isBefore(de.uni_koblenz.jgralab.Vertex)
	 */
	@Override
	public boolean isBefore(Vertex v) {
		assert (isValid() && v.isValid());
		if (this == v) {
			return false;
		}
		Vertex prev = ((VertexImpl) v).getPrevVertex();
		while ((prev != null) && (prev != this)) {
			prev = ((VertexImpl) prev).getPrevVertex();
		}
		return prev != null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Vertex#putBefore(de.uni_koblenz.jgralab.Vertex)
	 */
	@Override
	public void putBefore(Vertex v) {
		myGraph.putVertexBefore((VertexImpl) v, this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Vertex#isAfter(de.uni_koblenz.jgralab.Vertex)
	 */
	@Override
	public boolean isAfter(Vertex v) {
		assert (isValid() && v.isValid());
		if (this == v) {
			return false;
		}
		VertexImpl next = (VertexImpl) v.getNextVertex();
		while ((next != null) && (next != this)) {
			next = (VertexImpl) next.getNextVertex();
		}
		return next != null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Vertex#putAfter(de.uni_koblenz.jgralab.Vertex)
	 */
	@Override
	public void putAfter(Vertex v) {
		myGraph.putVertexAfter((VertexImpl) v, this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Vertex#getFirstEdge()
	 */
	@Override
	public Edge getFirstEdge() {
		return getFirstIncidence();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Vertex#getLastEdge()
	 */
	@Override
	public Edge getLastEdge() {
		return getLastIncidence();
	}

	public IncidenceImpl getFirstIncidence() {
		return firstIncidence;
	}

	public IncidenceImpl getLastIncidence() {
		return lastIncidence;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Vertex#getFirstEdge(de.uni_koblenz.jgralab.EdgeDirection)
	 */
	@Override
	public Edge getFirstEdge(EdgeDirection orientation) {
		IncidenceImpl i = getFirstIncidence();
		switch (orientation) {
		case IN:
			while (i != null && i.isNormal()) {
				i = i.getNextIncidence();
			}
			return i;
		case OUT:
			while (i != null && !i.isNormal()) {
				i = i.getNextIncidence();
			}
			return i;
		case INOUT:
			return i;
		default:
			throw new RuntimeException("FIXME!");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Vertex#getFirstEdgeOfClass(de.uni_koblenz.jgralab.schema.EdgeClass)
	 */
	@Override
	public Edge getFirstEdgeOfClass(EdgeClass anEdgeClass) {
		return getFirstEdgeOfClass(anEdgeClass.getM1Class(),
				EdgeDirection.INOUT, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Vertex#getFirstEdgeOfClass(java.lang.Class)
	 */
	@Override
	public Edge getFirstEdgeOfClass(Class<? extends Edge> anEdgeClass) {
		return getFirstEdgeOfClass(anEdgeClass, EdgeDirection.INOUT, false);
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
		return getFirstEdgeOfClass(anEdgeClass.getM1Class(), orientation, false);
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
		return getFirstEdgeOfClass(anEdgeClass, orientation, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Vertex#getFirstEdgeOfClass(de.uni_koblenz.jgralab.schema.EdgeClass,
	 *      boolean)
	 */
	@Override
	public Edge getFirstEdgeOfClass(EdgeClass anEdgeClass, boolean noSubclasses) {
		return getFirstEdgeOfClass(anEdgeClass.getM1Class(),
				EdgeDirection.INOUT, noSubclasses);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Vertex#getFirstEdgeOfClass(java.lang.Class,
	 *      boolean)
	 */
	@Override
	public Edge getFirstEdgeOfClass(Class<? extends Edge> anEdgeClass,
			boolean noSubclasses) {
		return getFirstEdgeOfClass(anEdgeClass, EdgeDirection.INOUT,
				noSubclasses);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Vertex#getFirstEdgeOfClass(de.uni_koblenz.jgralab.schema.EdgeClass,
	 *      de.uni_koblenz.jgralab.EdgeDirection, boolean)
	 */
	@Override
	public Edge getFirstEdgeOfClass(EdgeClass anEdgeClass,
			EdgeDirection orientation, boolean noSubclasses) {
		return getFirstEdgeOfClass(anEdgeClass.getM1Class(), orientation,
				noSubclasses);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Vertex#getFirstEdgeOfClass(java.lang.Class,
	 *      de.uni_koblenz.jgralab.EdgeDirection, boolean)
	 */
	@Override
	public Edge getFirstEdgeOfClass(Class<? extends Edge> anEdgeClass,
			EdgeDirection orientation, boolean noSubclasses) {
		Edge currentEdge = getFirstEdge(orientation);
		while (currentEdge != null) {
			if (noSubclasses) {
				if (anEdgeClass == currentEdge.getM1Class()) {
					return currentEdge;
				}
			} else {
				if (anEdgeClass.isInstance(currentEdge.getNormalEdge())) {
					return currentEdge;
				}
			}
			currentEdge = currentEdge.getNextEdge(orientation);
		}
		return null;
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

	public void putIncidenceAfter(IncidenceImpl target, IncidenceImpl moved) {
		assert (target.isValid() && moved.isValid());

		if (target == moved) {
			throw new GraphException("an edge can't be put after itself");
		}

		if (target.getNextIncidence() == moved) {
			return;
		}

		// there are at least 2 incidences in the incidence list
		// such that firstIncidence != lastIncidence
		assert getFirstIncidence() != getLastIncidence();

		// remove moved incidence from lambdaSeq
		if (moved == getFirstIncidence()) {
			setFirstIncidence(moved.getNextIncidence());
			moved.getNextIncidence().setPrevIncidence(null);
		} else if (moved == getLastIncidence()) {
			setLastIncidence(moved.getPrevIncidence());
			moved.getPrevIncidence().setNextIncidence(null);
		} else {
			moved.getPrevIncidence().setNextIncidence(moved.getNextIncidence());
			moved.getNextIncidence().setPrevIncidence(moved.getPrevIncidence());
		}

		// insert moved incidence in lambdaSeq immediately after target
		if (target == getLastIncidence()) {
			setLastIncidence(moved);
			moved.setNextIncidence(null);
		} else {
			target.getNextIncidence().setPrevIncidence(moved);
			moved.setNextIncidence(target.getNextIncidence());
		}
		moved.setPrevIncidence(target);
		target.setNextIncidence(moved);
		incidenceListModified();
	}

	public void putIncidenceBefore(IncidenceImpl target, IncidenceImpl moved) {
		assert (target.isValid() && moved.isValid());

		if (target == moved) {
			throw new GraphException("an edge can't be put beore itself");
		}

		if (target.getPrevIncidence() == moved) {
			return;
		}

		// there are at least 2 incidences in the incidence list
		// such that firstIncidence != lastIncidence
		assert getFirstIncidence() != getLastIncidence();

		// remove moved incidence from lambdaSeq
		if (moved == getFirstIncidence()) {
			setFirstIncidence(moved.getNextIncidence());
			moved.getNextIncidence().setPrevIncidence(null);
		} else if (moved == getLastIncidence()) {
			setLastIncidence(moved.getPrevIncidence());
			moved.getPrevIncidence().setNextIncidence(null);
		} else {
			moved.getPrevIncidence().setNextIncidence(moved.getNextIncidence());
			moved.getNextIncidence().setPrevIncidence(moved.getPrevIncidence());
		}

		// insert moved incidence in lambdaSeq immediately before target
		if (target == getFirstIncidence()) {
			setFirstIncidence(moved);
			moved.setPrevIncidence(null);
		} else {
			target.getPrevIncidence().setNextIncidence(moved);
			moved.setPrevIncidence(target.getPrevIncidence());
		}
		moved.setNextIncidence(target);
		target.setPrevIncidence(moved);
		incidenceListModified();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Vertex#getVertexVersion()
	 */
	@Override
	public long getIncidenceListVersion() {
		return incidenceListVersion;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Vertex#isVertexModified()
	 */
	@Override
	public boolean isIncidenceListModified(long vertexStructureVersion) {
		return (this.incidenceListVersion != vertexStructureVersion);
	}

	/**
	 * Must be called by all methods which manipulate the incidence list of this
	 * Vertex.
	 */
	public void incidenceListModified() {
		++incidenceListVersion;
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
	public int getId() {
		return id;
	}

	/**
	 * sets the id field of this vertex
	 * 
	 * @param id
	 *            an id
	 */
	void setId(int id) {
		assert id >= 0;
		this.id = id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "v" + getId() + ": "
				+ getAttributedElementClass().getQualifiedName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(AttributedElement a) {
		assert a instanceof Vertex;
		Vertex v = (Vertex) a;
		return getId() - v.getId();
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
		return new IncidenceIterable<Edge>(this, eclass.getM1Class(), dir);
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
		return new IncidenceIterable<Edge>(this, eclass.getM1Class());
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
	public boolean isValid() {
		return myGraph.containsVertex(this);
	}

	protected void setNextVertex(Vertex nextVertex) {
		this.nextVertex = (VertexImpl) nextVertex;
	}

	public void setPrevVertex(Vertex prevVertex) {
		this.prevVertex = (VertexImpl) prevVertex;
	}

	public Vertex getPrevVertex() {
		return prevVertex;
	}

	public void appendIncidenceToLambaSeq(IncidenceImpl i) {
		assert i.getIncidentVertex() != this;
		i.setIncidentVertex(this);
		if (getFirstIncidence() == null) {
			setFirstIncidence(i);
		}
		if (getLastIncidence() != null) {
			getLastIncidence().setNextIncidence(i);
			i.setPrevIncidence(getLastIncidence());
		}
		setLastIncidence(i);
	}

	public void removeIncidenceFromLambaSeq(IncidenceImpl i) {
		assert i.getIncidentVertex() == this;
		if (i == getFirstIncidence()) {
			// delete at head of incidence list
			setFirstIncidence(i.getNextIncidence());
			if (getFirstIncidence() != null) {
				getFirstIncidence().setPrevIncidence(null);
			}
			if (i == getLastIncidence()) {
				// this incidence was the only one...
				setLastIncidence(null);
			}
		} else if (i == getLastIncidence()) {
			// delete at tail of incidence list
			setLastIncidence(i.getPrevIncidence());
			if (getLastIncidence() != null) {
				getLastIncidence().setNextIncidence(null);
			}
		} else {
			// delete somewhere in the middle
			i.getPrevIncidence().setNextIncidence(i.getNextIncidence());
			i.getNextIncidence().setPrevIncidence(i.getPrevIncidence());
		}
		// delete incidence
		i.setIncidentVertex(null);
		i.setNextIncidence(null);
		i.setPrevIncidence(null);
	}

	protected void setFirstIncidence(IncidenceImpl firstIncidence) {
		this.firstIncidence = firstIncidence;
	}

	protected void setLastIncidence(IncidenceImpl lastIncidence) {
		this.lastIncidence = lastIncidence;
	}
}
