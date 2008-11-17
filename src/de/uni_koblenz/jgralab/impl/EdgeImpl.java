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
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphException;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.schema.EdgeClass;

/**
 * TODO add comment
 * 
 * @author ist@uni-koblenz.de
 */
public abstract class EdgeImpl extends IncidenceImpl implements Edge {
	private int id;

	// global egde sequence
	private Edge nextEdge;
	private Edge prevEdge;

	protected ReversedEdgeImpl reversedEdge;

	/**
	 * @param anId
	 * @param graph
	 */
	public EdgeImpl(int anId, Graph graph) {
		super(graph);
		setId(anId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(AttributedElement a) {
		assert a instanceof Edge;
		Edge e = (Edge) a;
		return getId() - e.getId();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#delete()
	 */
	@Override
	public void delete() {
		myGraph.deleteEdge(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#getAlpha()
	 */
	@Override
	public Vertex getAlpha() {
		return getIncidentVertex();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#getId()
	 */
	public int getId() {
		return id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#getNextEdgeInGraph()
	 */
	@Override
	public Edge getNextEdgeInGraph() {
		return nextEdge;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#getNextEdgeOfClassInGraph(java.lang.Class)
	 */
	@Override
	public Edge getNextEdgeOfClassInGraph(Class<? extends Edge> anEdgeClass) {
		return getNextEdgeOfClassInGraph(anEdgeClass, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#getNextEdgeOfClassInGraph(de.uni_koblenz.jgralab.schema.EdgeClass)
	 */
	@Override
	public Edge getNextEdgeOfClassInGraph(EdgeClass anEdgeClass) {
		return getNextEdgeOfClassInGraph(anEdgeClass.getM1Class(), false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#getNextEdgeOfClassInGraph(de.uni_koblenz.jgralab.schema.EdgeClass,
	 *      boolean)
	 */
	@Override
	public Edge getNextEdgeOfClassInGraph(EdgeClass anEdgeClass,
			boolean noSubclasses) {
		return getNextEdgeOfClassInGraph(anEdgeClass.getM1Class(), noSubclasses);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#getNextEdgeOfClassInGraph(java.lang.Class,
	 *      boolean)
	 */
	@Override
	public Edge getNextEdgeOfClassInGraph(Class<? extends Edge> anEdgeClass,
			boolean noSubclasses) {
		Edge currentEdge = getNextEdgeInGraph();
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
			currentEdge = currentEdge.getNextEdgeInGraph();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#getNormalEdge()
	 */
	@Override
	public Edge getNormalEdge() {
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#getOmega()
	 */
	@Override
	public Vertex getOmega() {
		return reversedEdge.getIncidentVertex();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#getReversedEdge()
	 */
	@Override
	public Edge getReversedEdge() {
		return reversedEdge;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#getThat()
	 */
	@Override
	public Vertex getThat() {
		return getOmega();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#getThatRole()
	 */
	@Override
	public String getThatRole() {
		return ((EdgeClass) this.getAttributedElementClass()).getToRolename();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#getThis()
	 */
	@Override
	public Vertex getThis() {
		return getAlpha();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#getThisRole()
	 */
	@Override
	public String getThisRole() {
		return ((EdgeClass) this.getAttributedElementClass()).getFromRolename();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#isAfterInGraph(de.uni_koblenz.jgralab.Edge)
	 */
	@Override
	public boolean isAfterInGraph(Edge e) {
		e = e.getNormalEdge();
		if (e == this) {
			return false;
		}
		Edge p = getPrevEdgeInGraph();
		while (p != null && p != e) {
			p = p.getPrevEdgeInGraph();
		}
		return p != null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#isBeforeInGraph(de.uni_koblenz.jgralab.Edge)
	 */
	@Override
	public boolean isBeforeInGraph(Edge e) {
		e = e.getNormalEdge();
		if (e == this) {
			return false;
		}
		Edge n = getNextEdgeInGraph();
		while (n != null && n != e) {
			n = n.getNextEdgeInGraph();
		}
		return n != null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#isNormal()
	 */
	@Override
	public boolean isNormal() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#putAfterInGraph(de.uni_koblenz.jgralab.Edge)
	 */
	@Override
	public void putAfterInGraph(Edge e) {
		myGraph.putEdgeAfterInGraph((EdgeImpl) e.getNormalEdge(), this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#putBeforeInGraph(de.uni_koblenz.jgralab.Edge)
	 */
	@Override
	public void putBeforeInGraph(Edge e) {
		myGraph.putEdgeBeforeInGraph((EdgeImpl) e.getNormalEdge(), this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#setAlpha(de.uni_koblenz.jgralab.Vertex)
	 */
	@Override
	public void setAlpha(Vertex alpha) {
		VertexImpl oldAlpha = getIncidentVertex();
		if (alpha == oldAlpha) {
			// nothing to change
			return;
		}

		if (!alpha.isValidAlpha(this)) {
			throw new GraphException("Edges of class "
					+ getAttributedElementClass().getUniqueName()
					+ " may not start at vertices of class "
					+ alpha.getAttributedElementClass().getUniqueName());
		}

		oldAlpha.removeIncidenceFromLambaSeq(this);
		oldAlpha.incidenceListModified();

		VertexImpl newAlpha = (VertexImpl) alpha;
		newAlpha.appendIncidenceToLambaSeq(this);
		newAlpha.incidenceListModified();
		setIncidentVertex(newAlpha);
	}

	/**
	 * sets the id field of this edge
	 * 
	 * @param id
	 */
	void setId(int id) {
		assert id >= 0;
		this.id = id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#setOmega(de.uni_koblenz.jgralab.Vertex)
	 */
	@Override
	public void setOmega(Vertex omega) {
		VertexImpl oldOmgea = reversedEdge.getIncidentVertex();
		if (omega == oldOmgea) {
			// nothing to change
			return;
		}

		if (!omega.isValidOmega(this)) {
			throw new GraphException("Edges of class "
					+ getAttributedElementClass().getUniqueName()
					+ " may not end at at vertices of class "
					+ omega.getAttributedElementClass().getUniqueName());
		}

		oldOmgea.removeIncidenceFromLambaSeq(reversedEdge);
		oldOmgea.incidenceListModified();

		VertexImpl newOmega = (VertexImpl) omega;
		newOmega.appendIncidenceToLambaSeq(reversedEdge);
		newOmega.incidenceListModified();
		reversedEdge.setIncidentVertex(newOmega);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#setThat(de.uni_koblenz.jgralab.Vertex)
	 */
	@Override
	public void setThat(Vertex v) {
		setOmega(v);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#setThis(de.uni_koblenz.jgralab.Vertex)
	 */
	@Override
	public void setThis(Vertex v) {
		setAlpha(v);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "+e" + getId() + ": "
				+ getAttributedElementClass().getQualifiedName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#isValid()
	 */
	@Override
	public boolean isValid() {
		return myGraph.containsEdge(this);
	}

	/**
	 * @param nextEdge
	 */
	public void setNextEdgeInGraph(Edge nextEdge) {
		this.nextEdge = nextEdge;
	}

	/**
	 * @param prevEdge
	 */
	public void setPrevEdgeInGraph(Edge prevEdge) {
		this.prevEdge = prevEdge;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#getPrevEdgeInGraph()
	 */
	public Edge getPrevEdgeInGraph() {
		return prevEdge;
	}

}