/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2009 Institute for Software Technology
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

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphException;
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

	abstract protected void setIncidentVertex(VertexImpl v);

	abstract protected VertexImpl getIncidentVertex();

	abstract protected void setNextIncidence(IncidenceImpl nextIncidence);

	abstract protected IncidenceImpl getNextIncidence();

	abstract protected void setPrevIncidence(IncidenceImpl prevIncidence);

	abstract protected IncidenceImpl getPrevIncidence();

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#getNextEdge()
	 */
	@Override
	public Edge getNextEdge() {
		return getNextIncidence();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#getPrevEdge()
	 */
	@Override
	public Edge getPrevEdge() {
		return getPrevIncidence();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.Edge#getNextEdge(de.uni_koblenz.jgralab.EdgeDirection
	 * )
	 */
	@Override
	public Edge getNextEdge(EdgeDirection orientation) {
		IncidenceImpl i = getNextIncidence();
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
	 * @see de.uni_koblenz.jgralab.Edge#getNextEdgeOfClass(java.lang.Class)
	 */
	@Override
	public Edge getNextEdgeOfClass(Class<? extends Edge> anEdgeClass) {
		return getNextEdgeOfClass(anEdgeClass, EdgeDirection.INOUT, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#getNextEdgeOfClass(java.lang.Class,
	 * boolean)
	 */
	@Override
	public Edge getNextEdgeOfClass(Class<? extends Edge> anEdgeClass,
			boolean noSubclasses) {
		return getNextEdgeOfClass(anEdgeClass, EdgeDirection.INOUT,
				noSubclasses);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#getNextEdgeOfClass(java.lang.Class,
	 * de.uni_koblenz.jgralab.EdgeDirection)
	 */
	@Override
	public Edge getNextEdgeOfClass(Class<? extends Edge> anEdgeClass,
			EdgeDirection orientation) {
		return getNextEdgeOfClass(anEdgeClass, orientation, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#getNextEdgeOfClass(java.lang.Class,
	 * de.uni_koblenz.jgralab.EdgeDirection, boolean)
	 */
	@Override
	public Edge getNextEdgeOfClass(Class<? extends Edge> anEdgeClass,
			EdgeDirection orientation, boolean noSubclasses) {
		Edge currentEdge = getNextEdge(orientation);
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
			currentEdge = currentEdge.getNextEdge(orientation);
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
	public Edge getNextEdgeOfClass(EdgeClass anEdgeClass) {
		return getNextEdgeOfClass(anEdgeClass.getM1Class(),
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
	public Edge getNextEdgeOfClass(EdgeClass anEdgeClass, boolean noSubclasses) {
		return getNextEdgeOfClass(anEdgeClass.getM1Class(),
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
	public Edge getNextEdgeOfClass(EdgeClass anEdgeClass,
			EdgeDirection orientation) {
		return getNextEdgeOfClass(anEdgeClass.getM1Class(), orientation, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.Edge#getNextEdgeOfClass(de.uni_koblenz.jgralab
	 * .schema.EdgeClass, de.uni_koblenz.jgralab.EdgeDirection, boolean)
	 */
	@Override
	public Edge getNextEdgeOfClass(EdgeClass anEdgeClass,
			EdgeDirection orientation, boolean noSubclasses) {
		return getNextEdgeOfClass(anEdgeClass.getM1Class(), orientation,
				noSubclasses);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#isBefore(de.uni_koblenz.jgralab.Edge)
	 */
	@Override
	public boolean isBefore(Edge e) {
		if (getThis() != e.getThis()) {
			throw new GraphException("this-vertex is not the same");
		}
		if (e == this) {
			return false;
		}
		IncidenceImpl i = getNextIncidence();
		while (i != null && i != e) {
			i = i.getNextIncidence();
		}
		return i != null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#isAfter(de.uni_koblenz.jgralab.Edge)
	 */
	@Override
	public boolean isAfter(Edge e) {
		if (getThis() != e.getThis()) {
			throw new GraphException("this-vertex is not the same");
		}
		if (e == this) {
			return false;
		}
		IncidenceImpl i = getPrevIncidence();
		while (i != null && i != e) {
			i = i.getPrevIncidence();
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
	public void putEdgeBefore(Edge e) {
		VertexImpl v = (VertexImpl) getThis();
		if (v != e.getThis()) {
			throw new GraphException("this-vertex is not the same");
		}
		if (this == e) {
			throw new GraphException("can't put edge before itself");
		}
		v.putIncidenceBefore((IncidenceImpl) e, this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.Edge#putEdgeAfter(de.uni_koblenz.jgralab.Edge)
	 */
	@Override
	public void putEdgeAfter(Edge e) {
		VertexImpl v = (VertexImpl) getThis();
		if (v != e.getThis()) {
			throw new GraphException("this-vertex is not the same");
		}
		if (this == e) {
			throw new GraphException("can't put edge after itself");
		}
		v.putIncidenceAfter((IncidenceImpl) e, this);
	}
}
