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
		InternalEdge {

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

	@Override
	public Class<? extends Edge> getSchemaClass() {
		return normalEdge.getSchemaClass();
	}

	@Override
	public int compareTo(AttributedElement<EdgeClass, Edge> a) {
		assert isValid();
		assert (a instanceof Edge);
		Edge e = (Edge) a;
		assert e.isValid();
		assert getGraph() == e.getGraph();
		if (e == getNormalEdge()) {
			return 1;
		} else {
			return Math.abs(getId()) - Math.abs(e.getId());
		}
	}

	@Override
	public void delete() {
		normalEdge.delete();
	}

	@Override
	public Vertex getAlpha() {
		return normalEdge.getIncidentVertex();
	}

	@Override
	public <T> T getAttribute(String name) {
		return normalEdge.<T> getAttribute(name);
	}

	@Override
	public <T> void setAttribute(String name, T data) {
		normalEdge.setAttribute(name, data);
	}

	@Override
	public int getId() {
		return -normalEdge.getId();
	}

	@Override
	public Edge getNextEdge() {
		return normalEdge.getNextEdge();
	}

	@Override
	public InternalEdge getNextEdgeInESeq() {
		return normalEdge.getNextEdgeInESeq();
	}

	@Override
	public Edge getPrevEdge() {
		return normalEdge.getPrevEdge();
	}

	@Override
	public InternalEdge getPrevEdgeInESeq() {
		return normalEdge.getPrevEdgeInESeq();
	}

	@Override
	public Edge getNextEdge(EdgeClass anEdgeClass) {
		return normalEdge.getNextEdge(anEdgeClass);
	}

	@Override
	public Edge getNextEdge(Class<? extends Edge> anEdgeClass) {
		return normalEdge.getNextEdge(anEdgeClass);
	}

	@Override
	public Edge getNormalEdge() {
		return normalEdge;
	}

	@Override
	public Vertex getOmega() {
		assert isValid();
		return getIncidentVertex();
	}

	@Override
	public Edge getReversedEdge() {
		return normalEdge;
	}

	@Override
	public Vertex getThat() {
		return getAlpha();
	}

	@Override
	public String getThatRole() {
		return normalEdge.getThisRole();
	}

	@Override
	public Vertex getThis() {
		return getOmega();
	}

	@Override
	public String getThisRole() {
		return normalEdge.getThatRole();
	}

	@Override
	public void graphModified() {
		assert isValid();
		graph.graphModified();
	}

	@Override
	public boolean isAfterEdge(Edge e) {
		return normalEdge.isAfterEdge(e);
	}

	@Override
	public boolean isBeforeEdge(Edge e) {
		return normalEdge.isBeforeEdge(e);
	}

	@Override
	public boolean isNormal() {
		return false;
	}

	@Override
	public void putAfterEdge(Edge e) {
		normalEdge.putAfterEdge(e);
	}

	@Override
	public void putBeforeEdge(Edge e) {
		normalEdge.putBeforeEdge(e);
	}

	@Override
	public void setAlpha(Vertex alpha) {
		normalEdge.setAlpha(alpha);
	}

	@Override
	public void setOmega(Vertex omega) {
		normalEdge.setOmega(omega);
	}

	@Override
	public void setThat(Vertex v) {
		normalEdge.setAlpha(v);
	}

	@Override
	public void setThis(Vertex v) {
		normalEdge.setOmega(v);
	}

	@Override
	public String toString() {
		assert isValid();
		return "-e" + normalEdge.getId() + ": "
				+ getAttributedElementClass().getQualifiedName();
	}

	@Override
	public boolean isValid() {
		return graph.eSeqContainsEdge(this);
	}

	@Override
	public void setId(int id) {
		normalEdge.setId(id);
	}

	@Override
	public AggregationKind getAggregationKind() {
		return normalEdge.getAggregationKind();
	}

	@Override
	public AggregationKind getAlphaAggregationKind() {
		return normalEdge.getAlphaAggregationKind();
	}

	@Override
	public AggregationKind getOmegaAggregationKind() {
		return normalEdge.getOmegaAggregationKind();
	}

	@Override
	public AggregationKind getThisAggregationKind() {
		return normalEdge.getOmegaAggregationKind();
	}

	@Override
	public AggregationKind getThatAggregationKind() {
		return normalEdge.getAlphaAggregationKind();
	}

	@Override
	public void setNextEdgeInGraph(Edge nextEdge) {
		normalEdge.setNextEdgeInGraph(nextEdge);
	}

	@Override
	public void setPrevEdgeInGraph(Edge prevEdge) {
		normalEdge.setPrevEdgeInGraph(prevEdge);
	}

}
