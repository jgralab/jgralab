/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2010 Institute for Software Technology
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
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.schema.AggregationKind;
import de.uni_koblenz.jgralab.schema.EdgeClass;

/**
 * TODO add comment
 * 
 * @author ist@uni-koblenz.de
 */
public abstract class ReversedEdgeBaseImpl extends IncidenceImpl implements
		Edge {
	protected final EdgeBaseImpl normalEdge;

	/**
	 * @param normalEdge
	 * @param graph
	 */
	public ReversedEdgeBaseImpl(EdgeBaseImpl normalEdge, Graph graph) {
		super(graph);
		assert normalEdge != null;
		this.normalEdge = normalEdge;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.AttributedElement#getM1Class()
	 */
	@Override
	public Class<? extends AttributedElement> getM1Class() {
		return normalEdge.getM1Class();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(AttributedElement a) {
		assert isValid();
		assert (a instanceof Edge);
		Edge e = (Edge) a;
		assert e.isValid();
		assert getGraph() == e.getGraph();
		if (e == this.getNormalEdge()) {
			return 1;
		} else {
			return Math.abs(getId()) - Math.abs(e.getId());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#delete()
	 */
	@Override
	public void delete() {
		normalEdge.delete();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#getAlpha()
	 */
	@Override
	public Vertex getAlpha() {
		return normalEdge.getIncidentVertex();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.AttributedElement#getAttribute(java.lang.String)
	 */
	@Override
	public Object getAttribute(String name) {
		return normalEdge.getAttribute(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.AttributedElement#setAttribute(java.lang.String,
	 * java.lang.Object)
	 */
	@Override
	public void setAttribute(String name, Object data) {
		normalEdge.setAttribute(name, data);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#getId()
	 */
	@Override
	public int getId() {
		return -normalEdge.getId();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#getNextEdgeInGraph()
	 */
	@Override
	public Edge getNextEdgeInGraph() {
		return normalEdge.getNextEdgeInGraph();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#getPrevEdgeInGraph()
	 */
	@Override
	public Edge getPrevEdgeInGraph() {
		return normalEdge.getPrevEdgeInGraph();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.Edge#getNextEdgeOfClassInGraph(de.uni_koblenz.
	 * jgralab.schema.EdgeClass)
	 */
	@Override
	public Edge getNextEdgeOfClassInGraph(EdgeClass anEdgeClass) {
		return normalEdge.getNextEdgeOfClassInGraph(anEdgeClass);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.Edge#getNextEdgeOfClassInGraph(java.lang.Class)
	 */
	@Override
	public Edge getNextEdgeOfClassInGraph(Class<? extends Edge> anEdgeClass) {
		return normalEdge.getNextEdgeOfClassInGraph(anEdgeClass);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.Edge#getNextEdgeOfClassInGraph(java.lang.Class,
	 * boolean)
	 */
	@Override
	public Edge getNextEdgeOfClassInGraph(Class<? extends Edge> anEdgeClass,
			boolean noSubclasses) {
		return normalEdge.getNextEdgeOfClassInGraph(anEdgeClass, noSubclasses);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.Edge#getNextEdgeOfClassInGraph(de.uni_koblenz.
	 * jgralab.schema.EdgeClass, boolean)
	 */
	@Override
	public Edge getNextEdgeOfClassInGraph(EdgeClass anEdgeClass,
			boolean noSubclasses) {
		return normalEdge.getNextEdgeOfClassInGraph(anEdgeClass, noSubclasses);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#getNormalEdge()
	 */
	@Override
	public Edge getNormalEdge() {
		return normalEdge;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#getOmega()
	 */
	@Override
	public Vertex getOmega() {
		assert isValid();
		return getIncidentVertex();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#getReversedEdge()
	 */
	@Override
	public Edge getReversedEdge() {
		return normalEdge;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#getThat()
	 */
	@Override
	public Vertex getThat() {
		return getAlpha();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#getThatRole()
	 */
	@Override
	public String getThatRole() {
		return normalEdge.getThisRole();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#getThis()
	 */
	@Override
	public Vertex getThis() {
		return getOmega();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#getThisRole()
	 */
	@Override
	public String getThisRole() {
		return normalEdge.getThatRole();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.GraphElement#graphModified()
	 */
	@Override
	public void graphModified() {
		assert isValid();
		graph.graphModified();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.Edge#isAfterInGraph(de.uni_koblenz.jgralab.Edge)
	 */
	@Override
	public boolean isAfterInGraph(Edge e) {
		return normalEdge.isAfterInGraph(e);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.Edge#isBeforeInGraph(de.uni_koblenz.jgralab.Edge)
	 */
	@Override
	public boolean isBeforeInGraph(Edge e) {
		return normalEdge.isBeforeInGraph(e);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#isNormal()
	 */
	@Override
	public boolean isNormal() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.Edge#putAfterInGraph(de.uni_koblenz.jgralab.Edge)
	 */
	@Override
	public void putAfterInGraph(Edge e) {
		normalEdge.putAfterInGraph(e);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.Edge#putBeforeInGraph(de.uni_koblenz.jgralab.Edge)
	 */
	@Override
	public void putBeforeInGraph(Edge e) {
		normalEdge.putBeforeInGraph(e);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#setAlpha(de.uni_koblenz.jgralab.Vertex)
	 */
	@Override
	public void setAlpha(Vertex alpha) {
		normalEdge.setAlpha(alpha);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#setOmega(de.uni_koblenz.jgralab.Vertex)
	 */
	@Override
	public void setOmega(Vertex omega) {
		normalEdge.setOmega(omega);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#setThat(de.uni_koblenz.jgralab.Vertex)
	 */
	@Override
	public void setThat(Vertex v) {
		normalEdge.setAlpha(v);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#setThis(de.uni_koblenz.jgralab.Vertex)
	 */
	@Override
	public void setThis(Vertex v) {
		normalEdge.setOmega(v);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "-e" + normalEdge.getId() + ": "
				+ getAttributedElementClass().getQualifiedName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#isValid()
	 */
	@Override
	public boolean isValid() {
		return graph.containsEdge(this);
	}

	@Override
	public void setId(int id) {
		normalEdge.setId(id);
	}

	@Override
	public AggregationKind getSemantics() {
		return normalEdge.getSemantics();
	}

	@Override
	public AggregationKind getAlphaSemantics() {
		return normalEdge.getAlphaSemantics();
	}

	@Override
	public AggregationKind getOmegaSemantics() {
		return normalEdge.getOmegaSemantics();
	}

	@Override
	public AggregationKind getThisSemantics() {
		return normalEdge.getOmegaSemantics();
	}

	@Override
	public AggregationKind getThatSemantics() {
		return normalEdge.getAlphaSemantics();
	}

}
