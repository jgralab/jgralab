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

package de.uni_koblenz.jgralab.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Set;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.PathElement;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.schema.AggregationKind;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.VertexClass;
import de.uni_koblenz.jgralab.schema.impl.DirectedM1EdgeClass;

/**
 * TODO add comment
 * 
 * @author ist@uni-koblenz.de
 */
public abstract class VertexBaseImpl extends GraphElementImpl implements Vertex {
	abstract protected void setIncidenceListVersion(long incidenceListVersion);

	/**
	 * @param id
	 *            the id of the vertex
	 * @param graph
	 *            its corresponding graph
	 */
	protected VertexBaseImpl(int id, Graph graph) {
		super(graph);
		this.id = id;
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
	 * @see
	 * de.uni_koblenz.jgralab.Vertex#getDegree(de.uni_koblenz.jgralab.EdgeDirection
	 * )
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
	abstract public Vertex getNextVertex();

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Vertex#getNextVertexOfClass(java.lang.Class)
	 */
	@Override
	public Vertex getNextVertexOfClass(Class<? extends Vertex> vertexClass) {
		assert vertexClass != null;
		assert isValid();
		return getNextVertexOfClass(vertexClass, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Vertex#getNextVertexOfClass(java.lang.Class,
	 * boolean)
	 */
	@Override
	public Vertex getNextVertexOfClass(Class<? extends Vertex> m1VertexClass,
			boolean noSubclasses) {
		assert m1VertexClass != null;
		assert isValid();
		VertexBaseImpl v = (VertexBaseImpl) getNextVertex();
		while (v != null) {
			if (noSubclasses) {
				if (m1VertexClass == v.getM1Class()) {
					return v;
				}
			} else {
				if (m1VertexClass.isInstance(v)) {
					return v;
				}
			}
			v = (VertexBaseImpl) v.getNextVertex();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.Vertex#getNextVertexOfClass(de.uni_koblenz.jgralab
	 * .schema.VertexClass)
	 */
	@Override
	public Vertex getNextVertexOfClass(VertexClass vertexClass) {
		assert vertexClass != null;
		assert isValid();
		return getNextVertexOfClass(vertexClass.getM1Class(), false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.Vertex#getNextVertexOfClass(de.uni_koblenz.jgralab
	 * .schema.VertexClass, boolean)
	 */
	@Override
	public Vertex getNextVertexOfClass(VertexClass vertexClass,
			boolean noSubclasses) {
		assert vertexClass != null;
		assert isValid();
		return getNextVertexOfClass(vertexClass.getM1Class(), noSubclasses);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.Vertex#isBefore(de.uni_koblenz.jgralab.Vertex)
	 */
	@Override
	public boolean isBefore(Vertex v) {
		assert v != null;
		assert getGraph() == v.getGraph();
		assert isValid() && v.isValid();
		if (this == v) {
			return false;
		}
		Vertex prev = ((VertexBaseImpl) v).getPrevVertex();
		while ((prev != null) && (prev != this)) {
			prev = ((VertexBaseImpl) prev).getPrevVertex();
		}
		return prev != null;
	}

	@Override
	public boolean isValid() {
		return graph.containsVertex(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.Vertex#putBefore(de.uni_koblenz.jgralab.Vertex)
	 */
	@Override
	public void putBefore(Vertex v) {
		assert v != null;
		assert v != this;
		assert getGraph() == v.getGraph();
		assert isValid() && v.isValid();
		graph.putVertexBefore((VertexBaseImpl) v, this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Vertex#isAfter(de.uni_koblenz.jgralab.Vertex)
	 */
	@Override
	public boolean isAfter(Vertex v) {
		assert v != null;
		assert getGraph() == v.getGraph();
		assert isValid() && v.isValid();
		if (this == v) {
			return false;
		}
		VertexBaseImpl next = (VertexBaseImpl) v.getNextVertex();
		while ((next != null) && (next != this)) {
			next = (VertexBaseImpl) next.getNextVertex();
		}
		return next != null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.Vertex#putAfter(de.uni_koblenz.jgralab.Vertex)
	 */
	@Override
	public void putAfter(Vertex v) {
		assert v != null;
		assert v != this;
		assert getGraph() == v.getGraph();
		assert isValid() && v.isValid();
		graph.putVertexAfter((VertexBaseImpl) v, this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Vertex#getFirstEdge()
	 */
	@Override
	public Edge getFirstEdge() {
		assert isValid();
		return getFirstIncidence();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Vertex#getLastEdge()
	 */
	@Override
	public Edge getLastEdge() {
		assert isValid();
		return getLastIncidence();
	}

	abstract protected IncidenceImpl getFirstIncidence();

	abstract protected IncidenceImpl getLastIncidence();

	/*
	 * (non-Javadoc)
	 * 
	 * @seede.uni_koblenz.jgralab.Vertex#getFirstEdge(de.uni_koblenz.jgralab.
	 * EdgeDirection)
	 */
	@Override
	public Edge getFirstEdge(EdgeDirection orientation) {
		assert isValid();
		IncidenceImpl i = getFirstIncidence();
		switch (orientation) {
		case IN:
			while ((i != null) && i.isNormal()) {
				i = i.getNextIncidence();
			}
			return i;
		case OUT:
			while ((i != null) && !i.isNormal()) {
				i = i.getNextIncidence();
			}
			return i;
		case INOUT:
			return i;
		default:
			throw new RuntimeException("FIXME!");
		}
	}

	@Override
	public Edge getFirstEdge(boolean thisIncidence, AggregationKind... kinds) {
		assert isValid();
		IncidenceImpl i = getFirstIncidence();
		if (kinds.length == 0) {
			return i;
		}
		while (i != null) {
			for (AggregationKind element : kinds) {
				if ((thisIncidence ? i.getThisSemantics() : i
						.getThatSemantics()) == element) {
					return i;
				}
			}
			i = i.getNextIncidence();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.Vertex#getFirstEdgeOfClass(de.uni_koblenz.jgralab
	 * .schema.EdgeClass)
	 */
	@Override
	public Edge getFirstEdgeOfClass(EdgeClass anEdgeClass) {
		assert anEdgeClass != null;
		assert isValid();
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
		assert anEdgeClass != null;
		assert isValid();
		return getFirstEdgeOfClass(anEdgeClass, EdgeDirection.INOUT, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.Vertex#getFirstEdgeOfClass(de.uni_koblenz.jgralab
	 * .schema.EdgeClass, de.uni_koblenz.jgralab.EdgeDirection)
	 */
	@Override
	public Edge getFirstEdgeOfClass(EdgeClass anEdgeClass,
			EdgeDirection orientation) {
		assert anEdgeClass != null;
		assert isValid();
		return getFirstEdgeOfClass(anEdgeClass.getM1Class(), orientation, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Vertex#getFirstEdgeOfClass(java.lang.Class,
	 * de.uni_koblenz.jgralab.EdgeDirection)
	 */
	@Override
	public Edge getFirstEdgeOfClass(Class<? extends Edge> anEdgeClass,
			EdgeDirection orientation) {
		assert anEdgeClass != null;
		assert isValid();
		return getFirstEdgeOfClass(anEdgeClass, orientation, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.Vertex#getFirstEdgeOfClass(de.uni_koblenz.jgralab
	 * .schema.EdgeClass, boolean)
	 */
	@Override
	public Edge getFirstEdgeOfClass(EdgeClass anEdgeClass, boolean noSubclasses) {
		assert anEdgeClass != null;
		assert isValid();
		return getFirstEdgeOfClass(anEdgeClass.getM1Class(),
				EdgeDirection.INOUT, noSubclasses);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Vertex#getFirstEdgeOfClass(java.lang.Class,
	 * boolean)
	 */
	@Override
	public Edge getFirstEdgeOfClass(Class<? extends Edge> anEdgeClass,
			boolean noSubclasses) {
		assert anEdgeClass != null;
		assert isValid();
		return getFirstEdgeOfClass(anEdgeClass, EdgeDirection.INOUT,
				noSubclasses);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.Vertex#getFirstEdgeOfClass(de.uni_koblenz.jgralab
	 * .schema.EdgeClass, de.uni_koblenz.jgralab.EdgeDirection, boolean)
	 */
	@Override
	public Edge getFirstEdgeOfClass(EdgeClass anEdgeClass,
			EdgeDirection orientation, boolean noSubclasses) {
		assert anEdgeClass != null;
		assert isValid();
		return getFirstEdgeOfClass(anEdgeClass.getM1Class(), orientation,
				noSubclasses);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Vertex#getFirstEdgeOfClass(java.lang.Class,
	 * de.uni_koblenz.jgralab.EdgeDirection, boolean)
	 */
	@Override
	public Edge getFirstEdgeOfClass(Class<? extends Edge> anEdgeClass,
			EdgeDirection orientation, boolean noSubclasses) {
		assert anEdgeClass != null;
		assert isValid();
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
		assert isValid() : this + " is not valid!";
		graph.deleteVertex(this);
	}

	protected void putIncidenceAfter(IncidenceImpl target, IncidenceImpl moved) {
		assert (target != null) && (moved != null);
		assert target.isValid() && moved.isValid();
		assert target.getGraph() == moved.getGraph();
		assert target.getGraph() == getGraph();
		assert target.getThis() == moved.getThis();
		assert target != moved;

		if ((target == moved) || (target.getNextIncidence() == moved)) {
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

	protected void putIncidenceBefore(IncidenceImpl target, IncidenceImpl moved) {
		assert (target != null) && (moved != null);
		assert target.isValid() && moved.isValid();
		assert target.getGraph() == moved.getGraph();
		assert target.getGraph() == getGraph();
		assert target.getThis() == moved.getThis();
		assert target != moved;

		if ((target == moved) || (target.getPrevIncidence() == moved)) {
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
			IncidenceImpl previousIncidence = target.getPrevIncidence();
			previousIncidence.setNextIncidence(moved);
			moved.setPrevIncidence(previousIncidence);
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
	abstract public long getIncidenceListVersion();

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Vertex#isVertexModified()
	 */
	@Override
	public boolean isIncidenceListModified(long vertexStructureVersion) {
		assert isValid();
		return (this.getIncidenceListVersion() != vertexStructureVersion);
	}

	/**
	 * Must be called by all methods which manipulate the incidence list of this
	 * Vertex.
	 */
	public void incidenceListModified() {
		assert isValid();
		setIncidenceListVersion(getIncidenceListVersion() + 1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Vertex#getDegree(jgralab.EdgeClass)
	 */
	@Override
	public int getDegree(EdgeClass ec) {
		assert ec != null;
		assert isValid();
		return getDegree(ec, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Vertex#getDegree(Class)
	 */
	@Override
	public int getDegree(Class<? extends Edge> ec) {
		assert ec != null;
		assert isValid();
		return getDegree(ec, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Vertex#getDegree(jgralab.EdgeClass, boolean)
	 */
	@Override
	public int getDegree(EdgeClass ec, boolean noSubClasses) {
		assert ec != null;
		assert isValid();
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
		assert ec != null;
		assert isValid();
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
		assert ec != null;
		assert isValid();
		return getDegree(ec, orientation, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Vertex#getDegree(Class, jgralab.EdgeDirection)
	 */
	@Override
	public int getDegree(Class<? extends Edge> ec, EdgeDirection orientation) {
		assert ec != null;
		assert isValid();
		return getDegree(ec, orientation, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Vertex#getDegree(jgralab.EdgeClass, jgralab.EdgeDirection,
	 * boolean)
	 */
	@Override
	public int getDegree(EdgeClass ec, EdgeDirection orientation,
			boolean noSubClasses) {
		assert ec != null;
		assert isValid();
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
		assert ec != null;
		assert isValid();
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
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		assert isValid();
		return "v" + id + ": " + getAttributedElementClass().getQualifiedName();
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
		assert isValid() && v.isValid();
		assert getGraph() == v.getGraph();
		return getId() - v.getId();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Vertex#incidences()
	 */
	@Override
	public Iterable<Edge> incidences() {
		assert isValid();
		return new IncidenceIterable<Edge>(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.Vertex#incidences(de.uni_koblenz.jgralab.EdgeDirection
	 * )
	 */
	@Override
	public Iterable<Edge> incidences(EdgeDirection dir) {
		assert isValid();
		return new IncidenceIterable<Edge>(this, dir);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.Vertex#incidences(de.uni_koblenz.jgralab.schema
	 * .EdgeClass, de.uni_koblenz.jgralab.EdgeDirection)
	 */
	@Override
	public Iterable<Edge> incidences(EdgeClass eclass, EdgeDirection dir) {
		assert eclass != null;
		assert isValid();
		return new IncidenceIterable<Edge>(this, eclass.getM1Class(), dir);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Vertex#incidences(java.lang.Class,
	 * de.uni_koblenz.jgralab.EdgeDirection)
	 */
	@Override
	public Iterable<Edge> incidences(Class<? extends Edge> eclass,
			EdgeDirection dir) {
		assert eclass != null;
		assert isValid();
		return new IncidenceIterable<Edge>(this, eclass, dir);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.Vertex#incidences(de.uni_koblenz.jgralab.schema
	 * .EdgeClass)
	 */
	@Override
	public Iterable<Edge> incidences(EdgeClass eclass) {
		assert eclass != null;
		assert isValid();
		return new IncidenceIterable<Edge>(this, eclass.getM1Class());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Vertex#incidences(java.lang.Class)
	 */
	@Override
	public Iterable<Edge> incidences(Class<? extends Edge> eclass) {
		assert eclass != null;
		assert isValid();
		return new IncidenceIterable<Edge>(this, eclass);
	}

	abstract protected void setNextVertex(Vertex nextVertex);

	abstract protected void setPrevVertex(Vertex prevVertex);

	abstract public Vertex getPrevVertex();

	protected void appendIncidenceToLambdaSeq(IncidenceImpl i) {
		assert i != null;
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

	protected void removeIncidenceFromLambdaSeq(IncidenceImpl i) {
		assert i != null;
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

	abstract protected void setFirstIncidence(IncidenceImpl firstIncidence);

	abstract protected void setLastIncidence(IncidenceImpl lastIncidence);

	public void sortIncidences(Comparator<Edge> comp) {
		assert isValid();

		if (getFirstIncidence() == null) {
			// no sorting required for empty incidence lists
			return;
		}
		class IncidenceList {
			IncidenceImpl first;
			IncidenceImpl last;

			public void add(IncidenceImpl e) {
				if (first == null) {
					first = e;
					assert (last == null);
					last = e;
				} else {
					e.setPrevIncidence(last);
					last.setNextIncidence(e);
					last = e;
				}
				e.setNextIncidence(null);
			}

			public IncidenceImpl remove() {
				if (first == null) {
					throw new NoSuchElementException();
				}
				IncidenceImpl out;
				if (first == last) {
					out = first;
					first = null;
					last = null;
					return out;
				}
				out = first;
				first = out.getNextIncidence();
				first.setPrevIncidence(null);
				return out;
			}

			public boolean isEmpty() {
				assert ((first == null) == (last == null));
				return first == null;
			}

		}

		IncidenceList a = new IncidenceList();
		IncidenceList b = new IncidenceList();
		IncidenceList out = a;

		// split
		IncidenceImpl last;
		IncidenceList l = new IncidenceList();
		l.first = getFirstIncidence();
		l.last = getLastIncidence();

		out.add(last = l.remove());
		while (!l.isEmpty()) {
			IncidenceImpl current = l.remove();
			if (comp.compare(current, last) < 0) {
				out = (out == a) ? b : a;
			}
			out.add(current);
			last = current;
		}
		if (a.isEmpty() || b.isEmpty()) {
			out = a.isEmpty() ? b : a;
			setFirstIncidence(out.first);
			setLastIncidence(out.last);
			return;
		}

		while (true) {
			if (a.isEmpty() || b.isEmpty()) {
				out = a.isEmpty() ? b : a;
				setFirstIncidence(out.first);
				setLastIncidence(out.last);
				incidenceListModified();
				return;
			}

			IncidenceList c = new IncidenceList();
			IncidenceList d = new IncidenceList();
			out = c;

			last = null;
			while (!a.isEmpty() && !b.isEmpty()) {
				int compareAToLast = last != null ? comp.compare(a.first, last)
						: 0;
				int compareBToLast = last != null ? comp.compare(b.first, last)
						: 0;

				if ((compareAToLast >= 0) && (compareBToLast >= 0)) {
					if (comp.compare(a.first, b.first) <= 0) {
						out.add(last = a.remove());
					} else {
						out.add(last = b.remove());
					}
				} else if ((compareAToLast < 0) && (compareBToLast < 0)) {
					out = (out == c) ? d : c;
					last = null;
				} else if ((compareAToLast < 0) && (compareBToLast >= 0)) {
					out.add(last = b.remove());
				} else {
					out.add(last = a.remove());
				}
			}

			// copy rest of A
			while (!a.isEmpty()) {
				IncidenceImpl current = a.remove();
				if (comp.compare(current, last) < 0) {
					out = (out == c) ? d : c;
				}
				out.add(current);
				last = current;
			}

			// copy rest of B
			while (!b.isEmpty()) {
				IncidenceImpl current = b.remove();
				if (comp.compare(current, last) < 0) {
					out = (out == c) ? d : c;
				}
				out.add(current);
				last = current;
			}

			a = c;
			b = d;
		}

	}

	public List<? extends Vertex> adjacences(String role) {
		assert (role != null) && (role.length() > 0);
		assert isValid();
		DirectedM1EdgeClass entry = getEdgeForRolename(role);
		List<Vertex> adjacences = new ArrayList<Vertex>();
		Class<? extends Edge> ec = entry.getM1Class();
		EdgeDirection dir = entry.getDirection();
		for (Edge e : incidences(ec, dir)) {
			adjacences.add(e.getThat());
		}
		return adjacences;
	}

	public Edge addAdjacence(String role, Vertex other) {
		assert (role != null) && (role.length() > 0);
		assert isValid();
		assert other.isValid();
		assert getGraph() == other.getGraph();

		DirectedM1EdgeClass entry = getEdgeForRolename(role);
		Class<? extends Edge> ec = entry.getM1Class();
		EdgeDirection dir = entry.getDirection();
		Vertex from = null;
		Vertex to = null;
		if (dir == EdgeDirection.IN) {
			from = other;
			to = this;
		} else {
			to = other;
			from = this;
		}
		Edge e = getGraph().createEdge(ec, from, to);
		return e;
	}

	public List<Vertex> removeAdjacences(String role) {
		assert (role != null) && (role.length() > 0);
		assert isValid();

		DirectedM1EdgeClass entry = getEdgeForRolename(role);
		Class<? extends Edge> ec = entry.getM1Class();
		List<Vertex> adjacences = new ArrayList<Vertex>();
		List<Edge> deleteList = new ArrayList<Edge>();
		EdgeDirection dir = entry.getDirection();
		for (Edge e : incidences(ec, dir)) {
			deleteList.add(e);
			adjacences.add(e.getThat());
		}
		for (Edge e : deleteList) {
			e.delete();
		}
		return adjacences;
	}

	public void removeAdjacence(String role, Vertex other) {
		assert (role != null) && (role.length() > 0);
		assert isValid();
		assert other.isValid();
		assert getGraph() == other.getGraph();

		DirectedM1EdgeClass entry = getEdgeForRolename(role);
		Class<? extends Edge> ec = entry.getM1Class();
		List<Edge> deleteList = new ArrayList<Edge>();
		EdgeDirection dir = entry.getDirection();
		for (Edge e : incidences(ec, dir)) {
			if (e.getThat() == other) {
				deleteList.add(e);
			}
		}
		for (Edge e : deleteList) {
			e.delete();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Vertex#reachableVertices(java.lang.String,
	 * java.lang.Class)
	 */
	@Override
	public synchronized <T extends Vertex> List<T> reachableVertices(
			String pathDescription, Class<T> vertexType) {
		return graph.reachableVertices(this, pathDescription, vertexType);
	}

	@SuppressWarnings("unchecked")
	public <T extends Vertex> Set<T> reachableVertices(Class<T> returnType,
			PathElement... pathElements) {
		Set<T> result = new LinkedHashSet<T>();
		Queue<Vertex> q = new LinkedList<Vertex>();
		q.add(this);

		for (int i = 0; i < pathElements.length; i++) {
			PathElement t = pathElements[i];
			// the null marks the end of the iteration with PathElement t
			q.add(null);
			Vertex vx = q.poll();
			while (vx != null) {
				for (Edge e : vx.incidences(t.edgeClass, t.edgeDirection)) {
					if (!t.strictType
							|| (t.strictType && (t.edgeClass == e.getM1Class()))) {
						if (i == pathElements.length - 1) {
							Vertex r = e.getThat();
							if (returnType.isInstance(r)) {
								result.add((T) r);
							}
						} else {
							q.add(e.getThat());
						}
					}
				}
				vx = q.poll();
			}
		}
		return result;
	}

}
