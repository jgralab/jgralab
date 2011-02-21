/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2011 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 * 
 * For bug reports, documentation and further information, visit
 * 
 *                         http://jgralab.uni-koblenz.de
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

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.schema.AggregationKind;
import de.uni_koblenz.jgralab.schema.EdgeClass;

/**
 * Common base class for EdgeImpl and ReversedEdgeImpl. Implements incidence
 * list and related operations.
 * 
 * @author ist@uni-koblenz.de
 */
public abstract class IncidenceImpl extends GraphElementImpl implements Edge {
	protected IncidenceImpl(Graph graph) {
		super(graph);
	}

	protected abstract void setIncidentVertex(VertexBaseImpl v);

	protected abstract VertexBaseImpl getIncidentVertex();

	protected abstract void setNextIncidenceInternal(IncidenceImpl nextIncidence);

	protected abstract IncidenceImpl getNextIncidenceInternal();

	protected abstract void setPrevIncidenceInternal(IncidenceImpl prevIncidence);

	protected abstract IncidenceImpl getPrevIncidenceInternal();

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#getNextEdge()
	 */
	@Override
	public Edge getNextIncidence() {
		assert isValid();
		return getNextIncidenceInternal();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#getPrevEdge()
	 */
	@Override
	public Edge getPrevIncidence() {
		assert isValid();
		return getPrevIncidenceInternal();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.Edge#getNextEdge(de.uni_koblenz.jgralab.EdgeDirection
	 * )
	 */
	@Override
	public Edge getNextIncidence(EdgeDirection orientation) {
		assert isValid();
		IncidenceImpl i = getNextIncidenceInternal();
		switch (orientation) {
		case IN:
			while ((i != null) && i.isNormal()) {
				i = i.getNextIncidenceInternal();
			}
			return i;
		case OUT:
			while ((i != null) && !i.isNormal()) {
				i = i.getNextIncidenceInternal();
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
	 * @see de.uni_koblenz.jgralab.Edge#getNextEdge(boolean,
	 * de.uni_koblenz.jgralab.schema.AggregationKind[])
	 */
	@Override
	public Edge getNextIncidence(boolean thisIncidence, AggregationKind... kinds) {
		assert isValid();
		IncidenceImpl i = getNextIncidenceInternal();
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
			i = i.getNextIncidenceInternal();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#getNextEdgeOfClass(java.lang.Class)
	 */
	@Override
	public Edge getNextIncidence(Class<? extends Edge> anEdgeClass) {
		assert anEdgeClass != null;
		assert isValid();
		return getNextIncidence(anEdgeClass, EdgeDirection.INOUT, false);
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#getNextEdgeOfClass(java.lang.Class,
	 * de.uni_koblenz.jgralab.EdgeDirection)
	 */
	@Override
	public Edge getNextIncidence(Class<? extends Edge> anEdgeClass,
			EdgeDirection orientation) {
		assert anEdgeClass != null;
		assert isValid();
		return getNextIncidence(anEdgeClass, orientation, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#getNextEdgeOfClass(java.lang.Class,
	 * de.uni_koblenz.jgralab.EdgeDirection, boolean)
	 */
	@Override
	public Edge getNextIncidence(Class<? extends Edge> anEdgeClass,
			EdgeDirection orientation, boolean noSubclasses) {
		assert anEdgeClass != null;
		assert isValid();
		Edge currentEdge = getNextIncidence(orientation);
		while (currentEdge != null) {
			if (noSubclasses) {
				if (anEdgeClass == currentEdge.getM1Class()) {
					return currentEdge;
				}
			} else {
				if (anEdgeClass.isInstance(currentEdge)) {
					return currentEdge;
				}
			}
			currentEdge = currentEdge.getNextIncidence(orientation);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.Edge#getNextEdgeOfClass(de.uni_koblenz.jgralab
	 * .schema.EdgeClass)
	 */
	@Override
	public Edge getNextIncidence(EdgeClass anEdgeClass) {
		assert anEdgeClass != null;
		assert isValid();
		return getNextIncidence(anEdgeClass.getM1Class(),
				EdgeDirection.INOUT, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.Edge#getNextEdgeOfClass(de.uni_koblenz.jgralab
	 * .schema.EdgeClass, boolean)
	 */
	@Override
	public Edge getNextIncidence(EdgeClass anEdgeClass, boolean noSubclasses) {
		assert anEdgeClass != null;
		assert isValid();
		return getNextIncidence(anEdgeClass.getM1Class(),
				EdgeDirection.INOUT, noSubclasses);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.Edge#getNextEdgeOfClass(de.uni_koblenz.jgralab
	 * .schema.EdgeClass, de.uni_koblenz.jgralab.EdgeDirection)
	 */
	@Override
	public Edge getNextIncidence(EdgeClass anEdgeClass,
			EdgeDirection orientation) {
		assert anEdgeClass != null;
		assert isValid();
		return getNextIncidence(anEdgeClass.getM1Class(), orientation, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.Edge#getNextEdgeOfClass(de.uni_koblenz.jgralab
	 * .schema.EdgeClass, de.uni_koblenz.jgralab.EdgeDirection, boolean)
	 */
	@Override
	public Edge getNextIncidence(EdgeClass anEdgeClass,
			EdgeDirection orientation, boolean noSubclasses) {
		assert anEdgeClass != null;
		assert isValid();
		return getNextIncidence(anEdgeClass.getM1Class(), orientation,
				noSubclasses);
	}
	
	@Override
	public Edge getNextIncidence(Class<? extends Edge> anEdgeClass,
			boolean noSubclasses) {
		return getNextIncidence(anEdgeClass, EdgeDirection.INOUT, noSubclasses);
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#isBefore(de.uni_koblenz.jgralab.Edge)
	 */
	@Override
	public boolean isBeforeIncidence(Edge e) {
		assert e != null;
		assert isValid();
		assert e.isValid();
		assert getGraph() == e.getGraph();
		assert getThis() == e.getThis();

		if (e == this) {
			return false;
		}
		IncidenceImpl i = getNextIncidenceInternal();
		while ((i != null) && (i != e)) {
			i = i.getNextIncidenceInternal();
		}
		return i != null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#isAfter(de.uni_koblenz.jgralab.Edge)
	 */
	@Override
	public boolean isAfterIncidence(Edge e) {
		assert e != null;
		assert isValid();
		assert e.isValid();
		assert getGraph() == e.getGraph();
		assert getThis() == e.getThis();

		if (e == this) {
			return false;
		}
		IncidenceImpl i = getPrevIncidenceInternal();
		while ((i != null) && (i != e)) {
			i = i.getPrevIncidenceInternal();
		}
		return i != null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.Edge#putEdgeBefore(de.uni_koblenz.jgralab.Edge)
	 */
	@Override
	public void putIncidenceBefore(Edge e) {
		assert e != null;
		assert isValid();
		assert e.isValid();
		assert getGraph() == e.getGraph();
		assert getThis() == e.getThis();
		VertexBaseImpl v = (VertexBaseImpl) getThis();
		assert v.isValid();
		assert e != this;

		if (this != e) {
			v.putIncidenceBefore((IncidenceImpl) e, this);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.Edge#putEdgeAfter(de.uni_koblenz.jgralab.Edge)
	 */
	@Override
	public void putIncidenceAfter(Edge e) {
		assert e != null;
		assert isValid();
		assert e.isValid();
		assert getGraph() == e.getGraph();
		assert getThis() == e.getThis() : "this-vertices don't match: "
				+ getThis() + " != " + e.getThis();
		VertexBaseImpl v = (VertexBaseImpl) getThis();
		assert v.isValid();
		assert e != this;

		if (this != e) {
			v.putIncidenceAfter((IncidenceImpl) e, this);
		}
	}
}
