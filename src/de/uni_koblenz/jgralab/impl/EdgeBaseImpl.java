/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2013 Institute for Software Technology
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

import java.util.BitSet;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.TraversalContext;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.exception.GraphException;
import de.uni_koblenz.jgralab.schema.AggregationKind;
import de.uni_koblenz.jgralab.schema.EdgeClass;

/**
 * TODO add comment
 * 
 * @author ist@uni-koblenz.de
 */
public abstract class EdgeBaseImpl extends IncidenceImpl implements
		InternalEdge {

	protected final ReversedEdgeBaseImpl reversedEdge;

	/**
	 * @param anId
	 * @param graph
	 */
	protected EdgeBaseImpl(int anId, Graph graph, Vertex alpha, Vertex omega) {
		super(graph);
		setId(anId);
		reversedEdge = createReversedEdge();
	}

	private BitSet setAttributes;

	@Override
	public void internalInitializeSetAttributesBitSet() {
		setAttributes = new BitSet(getAttributedElementClass()
				.getAttributeCount());
	}

	@Override
	public BitSet internalGetSetAttributesBitSet() {
		return setAttributes;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public final int compareTo(AttributedElement<EdgeClass, Edge> a) {
		assert a != null;
		assert a instanceof Edge;
		if (this == a) {
			return 0;
		}
		Edge e = (Edge) a;
		assert isValid();
		assert e.isValid();
		assert getGraph() == e.getGraph();

		if (e == getReversedEdge()) {
			return -1;
		} else {
			int x = Math.abs(getId()) - Math.abs(e.getId());
			if (x != 0) {
				return x;
			}
			return getGraph().compareTo(e.getGraph());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#delete()
	 */
	@Override
	public final void delete() {
		assert isValid();
		graph.deleteEdge(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#getAlpha()
	 */
	@Override
	public Vertex getAlpha() {
		assert isValid();
		return getIncidentVertex();
	}

	@Override
	public final Edge getNextEdge() {
		TraversalContext tc = graph.getTraversalContext();
		InternalEdge nextEdge = getNextEdgeInESeq();
		if (!((tc == null) || (nextEdge == null) || tc.containsEdge(nextEdge))) {
			while (!((nextEdge == null) || tc.containsEdge(nextEdge))) {
				nextEdge = nextEdge.getNextEdgeInESeq();
			}
		}
		return nextEdge;
	}

	@Override
	public final Edge getPrevEdge() {
		TraversalContext tc = graph.getTraversalContext();
		InternalEdge prevEdge = getPrevEdgeInESeq();
		if (!((tc == null) || (prevEdge == null) || tc.containsEdge(prevEdge))) {
			while (!((prevEdge == null) || tc.containsEdge(prevEdge))) {
				prevEdge = prevEdge.getPrevEdgeInESeq();
			}
		}
		return prevEdge;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.Edge#getNextEdgeOfClassInGraph(de.uni_koblenz.
	 * jgralab.schema.EdgeClass)
	 */
	@Override
	public final Edge getNextEdge(EdgeClass anEdgeClass) {
		assert anEdgeClass != null;
		assert isValid();
		Edge currentEdge = getNextEdge();
		while (currentEdge != null) {
			if (currentEdge.isInstanceOf(anEdgeClass)) {
				return currentEdge;
			}
			currentEdge = currentEdge.getNextEdge();
		}
		return null;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#getNormalEdge()
	 */
	@Override
	public final Edge getNormalEdge() {
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#getOmega()
	 */
	@Override
	public Vertex getOmega() {
		assert isValid();
		return reversedEdge.getIncidentVertex();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#getReversedEdge()
	 */
	@Override
	public final Edge getReversedEdge() {
		return reversedEdge;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#getThat()
	 */
	@Override
	public final Vertex getThat() {
		assert isValid();
		return getOmega();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#getThatRole()
	 */
	@Override
	public final String getThatRole() {
		assert isValid();
		return getAttributedElementClass().getTo().getRolename();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#getThis()
	 */
	@Override
	public final Vertex getThis() {
		assert isValid();
		return getAlpha();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#getThisRole()
	 */
	@Override
	public final String getThisRole() {
		assert isValid();
		return getAttributedElementClass().getFrom().getRolename();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.Edge#isAfterInGraph(de.uni_koblenz.jgralab.Edge)
	 */
	@Override
	public final boolean isAfterEdge(Edge e) {
		assert e != null;
		assert isValid();
		assert e.isValid();
		assert getGraph() == e.getGraph();
		e = e.getNormalEdge();
		if (e == this) {
			return false;
		}
		InternalEdge p = getPrevEdgeInESeq();
		while ((p != null) && (p != e)) {
			p = p.getPrevEdgeInESeq();
		}
		return p != null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.Edge#isBeforeInGraph(de.uni_koblenz.jgralab.Edge)
	 */
	@Override
	public final boolean isBeforeEdge(Edge e) {
		assert e != null;
		assert isValid();
		assert e.isValid();
		assert getGraph() == e.getGraph();

		e = e.getNormalEdge();
		if (e == this) {
			return false;
		}
		InternalEdge n = getNextEdgeInESeq();
		while ((n != null) && (n != e)) {
			n = n.getNextEdgeInESeq();
		}
		return n != null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#isNormal()
	 */
	@Override
	public final boolean isNormal() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.Edge#putAfterInGraph(de.uni_koblenz.jgralab.Edge)
	 */
	@Override
	public final void putAfterEdge(Edge e) {
		assert e != null;
		assert isValid();
		assert e.isValid();
		assert getGraph() == e.getGraph();
		assert e != this;
		assert e != reversedEdge;
		graph.putEdgeAfterInGraph((InternalEdge) e.getNormalEdge(), this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.Edge#putBeforeInGraph(de.uni_koblenz.jgralab.Edge)
	 */
	@Override
	public final void putBeforeEdge(Edge e) {
		assert e != null;
		assert isValid();
		assert e.isValid();
		assert getGraph() == e.getGraph();
		assert e != this;
		assert e != reversedEdge;
		graph.putEdgeBeforeInGraph((InternalEdge) e.getNormalEdge(), this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#setAlpha(de.uni_koblenz.jgralab.Vertex)
	 */
	@Override
	public final void setAlpha(Vertex alpha) {
		InternalVertex alphaBase = (InternalVertex) alpha;
		assert isValid();
		assert alphaBase != null;
		assert alphaBase.isValid();
		assert getGraph() == alphaBase.getGraph();

		InternalVertex oldAlpha = getIncidentVertex();

		graph.fireBeforeChangeAlpha(this, oldAlpha, alphaBase);

		if (alphaBase == oldAlpha) {
			return; // nothing to change
		}
		if (!(alphaBase.getAttributedElementClass())
				.isValidFromFor(getAttributedElementClass())) {
			throw new GraphException("Edges of class "
					+ getAttributedElementClass().getUniqueName()
					+ " may not start at vertices of class "
					+ alphaBase.getAttributedElementClass().getUniqueName());
		}

		oldAlpha.removeIncidenceFromISeq(this);
		oldAlpha.incidenceListModified();

		InternalVertex newAlpha = alphaBase;
		newAlpha.appendIncidenceToISeq(this);
		newAlpha.incidenceListModified();
		setIncidentVertex(newAlpha);

		graph.fireAfterChangeAlpha(this, oldAlpha, alphaBase);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#setOmega(de.uni_koblenz.jgralab.Vertex)
	 */
	@Override
	public final void setOmega(Vertex omega) {
		InternalVertex omegaBase = (InternalVertex) omega;
		assert isValid();
		assert omegaBase != null;
		assert omegaBase.isValid();
		assert getGraph() == omegaBase.getGraph();

		InternalVertex oldOmgea = reversedEdge.getIncidentVertex();
		graph.fireBeforeChangeOmega(this, oldOmgea, omegaBase);

		if (omegaBase == oldOmgea) {
			return; // nothing to change
		}

		if (!(omegaBase.getAttributedElementClass())
				.isValidToFor(getAttributedElementClass())) {
			throw new GraphException("Edges of class "
					+ getAttributedElementClass().getUniqueName()
					+ " may not end at at vertices of class "
					+ omegaBase.getAttributedElementClass().getUniqueName());
		}

		oldOmgea.removeIncidenceFromISeq(reversedEdge);
		oldOmgea.incidenceListModified();

		InternalVertex newOmega = omegaBase;
		newOmega.appendIncidenceToISeq(reversedEdge);
		newOmega.incidenceListModified();
		// TODO Check if this is really needed as
		// appenIncidenceToLambdaSeq called it before.
		reversedEdge.setIncidentVertex(newOmega);

		graph.fireAfterChangeOmega(this, oldOmgea, omegaBase);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#setThat(de.uni_koblenz.jgralab.Vertex)
	 */
	@Override
	public final void setThat(Vertex v) {
		assert isValid();
		assert v != null;
		assert v.isValid();
		assert getGraph() == v.getGraph();

		setOmega(v);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#setThis(de.uni_koblenz.jgralab.Vertex)
	 */
	@Override
	public final void setThis(Vertex v) {
		assert isValid();
		assert v != null;
		assert v.isValid();
		assert getGraph() == v.getGraph();

		setAlpha(v);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "+e" + id + ": "
				+ getAttributedElementClass().getQualifiedName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#isValid()
	 */
	@Override
	public final boolean isValid() {
		return graph.eSeqContainsEdge(this);
	}

	@Override
	public final AggregationKind getThisAggregationKind() {
		assert isValid();
		return getAlphaAggregationKind();
	}

	@Override
	public final AggregationKind getThatAggregationKind() {
		assert isValid();
		return getOmegaAggregationKind();
	}

	/**
	 * Creates the reversed edge for this edge. Should be implemented by the
	 * generated edge classes.
	 */
	abstract protected ReversedEdgeBaseImpl createReversedEdge();
}
