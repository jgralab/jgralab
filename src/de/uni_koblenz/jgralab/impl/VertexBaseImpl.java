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

package de.uni_koblenz.jgralab.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;

import org.pcollections.POrderedSet;
import org.pcollections.PSet;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.PathElement;
import de.uni_koblenz.jgralab.TraversalContext;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.schema.AggregationKind;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.VertexClass;
import de.uni_koblenz.jgralab.schema.impl.DirectedSchemaEdgeClass;

/**
 * TODO add comment
 *
 * @author ist@uni-koblenz.de
 */
public abstract class VertexBaseImpl extends
		GraphElementImpl<VertexClass, Vertex> implements Vertex, InternalVertex {

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
		Edge i = getFirstIncidence();
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
		TraversalContext tc = graph.getTraversalContext();
		InternalVertex nextVertex = getNextVertexInVSeq();
		if (!((tc == null) || (nextVertex == null) || tc
				.containsVertex(nextVertex))) {
			while (!((nextVertex == null) || tc.containsVertex(nextVertex))) {
				nextVertex = nextVertex.getNextVertexInVSeq();
			}
		}
		return nextVertex;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.uni_koblenz.jgralab.Vertex#getNextVertexOfClass(java.lang.Class)
	 */
	@Override
	public Vertex getNextVertex(Class<? extends Vertex> vertexClass) {
		assert vertexClass != null;
		assert isValid();
		InternalVertex v = (InternalVertex) getNextVertex();
		while (v != null) {
			if (vertexClass.isInstance(v)) {
				return v;
			}
			v = (InternalVertex) v.getNextVertex();
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
	public Vertex getNextVertex(VertexClass vertexClass) {
		assert vertexClass != null;
		assert isValid();
		Vertex v = getNextVertex();
		while (v != null) {
			if (v.isInstanceOf(vertexClass)) {
				return v;
			}
			v = v.getNextVertex();
		}
		return null;
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
		Vertex prev = ((InternalVertex) v).getPrevVertexInVSeq();
		while ((prev != null) && (prev != this)) {
			prev = ((InternalVertex) prev).getPrevVertexInVSeq();
		}
		return prev != null;
	}

	@Override
	public boolean isValid() {
		return graph.vSeqContainsVertex(this);
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
		graph.putVertexBefore((InternalVertex) v, this);
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
		InternalVertex next = ((InternalVertex) v).getNextVertexInVSeq();
		while ((next != null) && (next != this)) {
			next = next.getNextVertexInVSeq();
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
		graph.putVertexAfter((InternalVertex) v, this);
	}

	@Override
	public Edge getFirstIncidence() {
		TraversalContext tc = graph.getTraversalContext();
		Edge firstIncidence = getFirstIncidenceInISeq();
		if (!((tc == null) || (firstIncidence == null) || tc
				.containsEdge(firstIncidence))) {
			firstIncidence = firstIncidence.getNextIncidence();
		}
		return firstIncidence;
	}

	@Override
	public Edge getLastIncidence() {
		TraversalContext tc = graph.getTraversalContext();
		Edge lastIncidence = getLastIncidenceInISeq();
		if (!((tc == null) || (lastIncidence == null) || tc
				.containsEdge(lastIncidence))) {
			lastIncidence = lastIncidence.getPrevIncidence();
		}
		return lastIncidence;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @seede.uni_koblenz.jgralab.Vertex#getFirstEdge(de.uni_koblenz.jgralab.
	 * EdgeDirection)
	 */
	@Override
	public Edge getFirstIncidence(EdgeDirection orientation) {
		assert isValid();
		Edge i = getFirstIncidence();
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
	public Edge getFirstIncidence(boolean thisIncidence,
			AggregationKind... kinds) {
		assert isValid();
		Edge i = getFirstIncidence();
		if (kinds.length == 0) {
			return i;
		}
		while (i != null) {
			for (AggregationKind element : kinds) {
				if ((thisIncidence ? i.getThisAggregationKind() : i
						.getThatAggregationKind()) == element) {
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
	public Edge getFirstIncidence(EdgeClass anEdgeClass) {
		assert anEdgeClass != null;
		assert isValid();
		return getFirstIncidence(anEdgeClass, EdgeDirection.INOUT);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.uni_koblenz.jgralab.Vertex#getFirstEdgeOfClass(java.lang.Class)
	 */
	@Override
	public Edge getFirstIncidence(Class<? extends Edge> anEdgeClass) {
		assert anEdgeClass != null;
		assert isValid();
		return getFirstIncidence(anEdgeClass, EdgeDirection.INOUT);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * de.uni_koblenz.jgralab.Vertex#getFirstEdgeOfClass(de.uni_koblenz.jgralab
	 * .schema.EdgeClass, de.uni_koblenz.jgralab.EdgeDirection)
	 */
	@Override
	public Edge getFirstIncidence(EdgeClass anEdgeClass,
			EdgeDirection orientation) {
		assert anEdgeClass != null;
		assert isValid();
		Edge currentEdge = getFirstIncidence(orientation);
		while (currentEdge != null) {
			if (currentEdge.isInstanceOf(anEdgeClass)) {
				return currentEdge;
			}
			currentEdge = currentEdge.getNextIncidence(orientation);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.uni_koblenz.jgralab.Vertex#getFirstEdgeOfClass(java.lang.Class,
	 * de.uni_koblenz.jgralab.EdgeDirection)
	 */
	@Override
	public Edge getFirstIncidence(Class<? extends Edge> anEdgeClass,
			EdgeDirection orientation) {
		assert anEdgeClass != null;
		assert isValid();
		Edge currentEdge = getFirstIncidence(orientation);
		while (currentEdge != null) {
			if (anEdgeClass.isInstance(currentEdge.getNormalEdge())) {
				return currentEdge;
			}
			currentEdge = currentEdge.getNextIncidence(orientation);
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

	@Override
	public void putIncidenceAfter(InternalEdge target, InternalEdge moved) {
		assert (target != null) && (moved != null);
		assert target.isValid() && moved.isValid();
		assert target.getGraph() == moved.getGraph();
		assert target.getGraph() == getGraph();
		assert target.getThis() == moved.getThis();
		assert target != moved;

		if ((target == moved) || (target.getNextIncidenceInISeq() == moved)) {
			return;
		}

		// there are at least 2 incidences in the incidence list
		// such that firstIncidence != lastIncidence
		assert getFirstIncidenceInISeq() != getLastIncidenceInISeq();

		// remove moved incidence from lambdaSeq
		if (moved == getFirstIncidenceInISeq()) {
			setFirstIncidence(moved.getNextIncidenceInISeq());
			(moved.getNextIncidenceInISeq()).setPrevIncidenceInternal(null);

		} else if (moved == getLastIncidenceInISeq()) {
			setLastIncidence(moved.getPrevIncidenceInISeq());
			(moved.getPrevIncidenceInISeq()).setNextIncidenceInternal(null);
		} else {
			(moved.getPrevIncidenceInISeq()).setNextIncidenceInternal(moved
					.getNextIncidenceInISeq());
			(moved.getNextIncidenceInISeq()).setPrevIncidenceInternal(moved
					.getPrevIncidenceInISeq());
		}

		// insert moved incidence in lambdaSeq immediately after target
		if (target == getLastIncidenceInISeq()) {
			setLastIncidence(moved);
			moved.setNextIncidenceInternal(null);
		} else {
			(target.getNextIncidenceInISeq()).setPrevIncidenceInternal(moved);
			moved.setNextIncidenceInternal(target.getNextIncidenceInISeq());
		}
		moved.setPrevIncidenceInternal(target);

		target.setNextIncidenceInternal(moved);
		incidenceListModified();
	}

	@Override
	public void putIncidenceBefore(InternalEdge target, InternalEdge moved) {
		assert (target != null) && (moved != null);
		assert target.isValid() && moved.isValid();
		assert target.getGraph() == moved.getGraph();
		assert target.getGraph() == getGraph();
		assert target.getThis() == moved.getThis();
		assert target != moved;

		if ((target == moved) || (target.getPrevIncidenceInISeq() == moved)) {
			return;
		}

		// there are at least 2 incidences in the incidence list
		// such that firstIncidence != lastIncidence
		assert getFirstIncidenceInISeq() != getLastIncidenceInISeq();

		// remove moved incidence from lambdaSeq
		if (moved == getFirstIncidenceInISeq()) {
			setFirstIncidence(moved.getNextIncidenceInISeq());
			(moved.getNextIncidenceInISeq()).setPrevIncidenceInternal(null);
		} else if (moved == getLastIncidenceInISeq()) {
			setLastIncidence(moved.getPrevIncidenceInISeq());
			(moved.getPrevIncidenceInISeq()).setNextIncidenceInternal(null);
		} else {
			(moved.getPrevIncidenceInISeq()).setNextIncidenceInternal(moved
					.getNextIncidenceInISeq());
			(moved.getNextIncidenceInISeq()).setPrevIncidenceInternal(moved
					.getPrevIncidenceInISeq());
		}

		// insert moved incidence in lambdaSeq immediately before target
		if (target == getFirstIncidenceInISeq()) {
			setFirstIncidence(moved);
			moved.setPrevIncidenceInternal(null);
		} else {
			InternalEdge previousIncidence = target.getPrevIncidenceInISeq();
			previousIncidence.setNextIncidenceInternal(moved);
			moved.setPrevIncidenceInternal(previousIncidence);
		}
		moved.setNextIncidenceInternal(target);
		target.setPrevIncidenceInternal(moved);
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
		return (getIncidenceListVersion() != vertexStructureVersion);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.uni_koblenz.jgralab.impl.InternalVertex#incidenceListModified()
	 */
	@Override
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
		return getDegree(ec, EdgeDirection.INOUT);
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
		return getDegree(ec, EdgeDirection.INOUT);
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
		int degree = 0;
		for (Edge e = getFirstIncidence(ec, orientation); e != null; e = e
				.getNextIncidence(ec, orientation)) {
			++degree;
		}
		return degree;
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
		int degree = 0;
		for (Edge e = getFirstIncidence(ec, orientation); e != null; e = e
				.getNextIncidence(ec, orientation)) {
			++degree;
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
		return "v" + id + ": " + getAttributedElementClass().getQualifiedName();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(AttributedElement<VertexClass, Vertex> a) {
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
		return new IncidenceIterable<Edge>(this, eclass, dir);
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
		return new IncidenceIterable<Edge>(this, eclass);
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

	@Override
	public Vertex getPrevVertex() {
		TraversalContext tc = graph.getTraversalContext();
		InternalVertex prevVertex = getPrevVertexInVSeq();
		if (!((tc == null) || (prevVertex == null) || tc
				.containsVertex(prevVertex))) {
			while (!((prevVertex == null) || tc.containsVertex(prevVertex))) {
				prevVertex = prevVertex.getPrevVertexInVSeq();
			}
		}
		return prevVertex;
	}

	@Override
	public void appendIncidenceToISeq(InternalEdge i) {
		assert i != null;
		assert i.getIncidentVertex() != this;
		i.setIncidentVertex(this);
		if (getFirstIncidenceInISeq() == null) {
			setFirstIncidence(i);
		}
		if (getLastIncidenceInISeq() != null) {
			getLastIncidenceInISeq().setNextIncidenceInternal(i);
			i.setPrevIncidenceInternal(getLastIncidenceInISeq());
		}
		setLastIncidence(i);
	}

	@Override
	public void removeIncidenceFromISeq(InternalEdge i) {
		assert i != null;
		assert i.getIncidentVertex() == this;
		if (i == getFirstIncidenceInISeq()) {
			// delete at head of incidence list
			setFirstIncidence(i.getNextIncidenceInISeq());
			if (getFirstIncidenceInISeq() != null) {
				getFirstIncidenceInISeq().setPrevIncidenceInternal(null);
			}
			if (i == getLastIncidenceInISeq()) {
				// this incidence was the only one...
				setLastIncidence(null);
			}
		} else if (i == getLastIncidenceInISeq()) {
			// delete at tail of incidence list
			setLastIncidence(i.getPrevIncidenceInISeq());
			if (getLastIncidenceInISeq() != null) {
				getLastIncidenceInISeq().setNextIncidenceInternal(null);
			}
		} else {
			// delete somewhere in the middle
			(i.getPrevIncidenceInISeq()).setNextIncidenceInternal(i
					.getNextIncidenceInISeq());
			(i.getNextIncidenceInISeq()).setPrevIncidenceInternal(i
					.getPrevIncidenceInISeq());
		}
		// delete incidence
		i.setIncidentVertex(null);
		i.setNextIncidenceInternal(null);
		i.setPrevIncidenceInternal(null);
	}

	@Override
	public void sortIncidences(Comparator<Edge> comp) {
		assert isValid();

		if (getFirstIncidenceInISeq() == null) {
			// no sorting required for empty incidence lists
			return;
		}
		class IncidenceList {
			InternalEdge first;
			InternalEdge last;

			public void add(InternalEdge e) {
				if (first == null) {
					first = e;
					assert (last == null);
					last = e;
				} else {
					e.setPrevIncidenceInternal(last);
					last.setNextIncidenceInternal(e);
					last = e;
				}
				e.setNextIncidenceInternal(null);
			}

			public InternalEdge remove() {
				if (first == null) {
					throw new NoSuchElementException();
				}
				InternalEdge out;
				if (first == last) {
					out = first;
					first = null;
					last = null;
					return out;
				}
				out = first;
				first = out.getNextIncidenceInISeq();
				first.setPrevIncidenceInternal(null);
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
		InternalEdge last;
		IncidenceList l = new IncidenceList();
		l.first = getFirstIncidenceInISeq();
		l.last = getLastIncidenceInISeq();

		out.add(last = l.remove());
		while (!l.isEmpty()) {
			InternalEdge current = l.remove();
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
				InternalEdge current = a.remove();
				if (comp.compare(current, last) < 0) {
					out = (out == c) ? d : c;
				}
				out.add(current);
				last = current;
			}

			// copy rest of B
			while (!b.isEmpty()) {
				InternalEdge current = b.remove();
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

	@Override
	public List<? extends Vertex> adjacences(String role) {
		assert (role != null) && (role.length() > 0);
		assert isValid();
		DirectedSchemaEdgeClass entry = getEdgeForRolename(role);
		List<Vertex> adjacences = new ArrayList<Vertex>();
		EdgeDirection dir = entry.getDirection();
		for (Edge e : incidences(entry.getEdgeClass(), dir)) {
			adjacences.add(e.getThat());
		}
		return adjacences;
	}

	@Override
	public Edge addAdjacence(String role, Vertex other) {
		assert (role != null) && (role.length() > 0);
		assert isValid();
		assert other.isValid();
		assert getGraph() == other.getGraph();

		DirectedSchemaEdgeClass entry = getEdgeForRolename(role);
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
		return getGraph().createEdge(entry.getEdgeClass(), from, to);
	}

	@Override
	public List<Vertex> removeAdjacences(String role) {
		assert (role != null) && (role.length() > 0);
		assert isValid();
		TraversalContext oldTC = getGraph().setTraversalContext(null);
		try {
			DirectedSchemaEdgeClass entry = getEdgeForRolename(role);
			List<Vertex> adjacences = new ArrayList<Vertex>();
			List<Edge> deleteList = new ArrayList<Edge>();
			EdgeDirection dir = entry.getDirection();
			for (Edge e : incidences(entry.getEdgeClass(), dir)) {
				deleteList.add(e);
				adjacences.add(e.getThat());
			}
			for (Edge e : deleteList) {
				e.delete();
			}
			return adjacences;
		} finally {
			getGraph().setTraversalContext(oldTC);
		}
	}

	@Override
	public void removeAdjacence(String role, Vertex other) {
		assert (role != null) && (role.length() > 0);
		assert isValid();
		assert other.isValid();
		assert getGraph() == other.getGraph();
		TraversalContext oldTC = getGraph().setTraversalContext(null);
		try {
			DirectedSchemaEdgeClass entry = getEdgeForRolename(role);
			List<Edge> deleteList = new ArrayList<Edge>();
			EdgeDirection dir = entry.getDirection();
			for (Edge e : incidences(entry.getEdgeClass(), dir)) {
				if (e.getThat() == other) {
					deleteList.add(e);
				}
			}
			for (Edge e : deleteList) {
				e.delete();
			}
		} finally {
			getGraph().setTraversalContext(oldTC);
		}
	}

	@Override
	public DirectedSchemaEdgeClass getEdgeForRolename(String rolename) {
		return getAttributedElementClass().getDirectedEdgeClassForFarEndRole(
				rolename);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends Vertex> POrderedSet<T> reachableVertices(
			PathElement... pathElements) {
		PSet<T> result = JGraLab.set();
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
							|| (t.strictType && (t.edgeClass.getSchemaClass() == e
									.getAttributedElementClass()))) {
						if (i == (pathElements.length - 1)) {
							Vertex r = e.getThat();
							result = result.plus((T) r);
						} else {
							q.add(e.getThat());
						}
					}
				}
				vx = q.poll();
			}
		}
		return (POrderedSet<T>) result;
	}
}
