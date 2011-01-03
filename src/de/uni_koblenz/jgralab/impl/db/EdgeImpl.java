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
package de.uni_koblenz.jgralab.impl.db;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.impl.EdgeBaseImpl;
import de.uni_koblenz.jgralab.impl.IncidenceImpl;
import de.uni_koblenz.jgralab.impl.VertexBaseImpl;

/**
 * Implements a database persistable edge.
 * 
 * @author ultbreit@uni-koblenz.de
 */
public abstract class EdgeImpl extends EdgeBaseImpl implements
		DatabasePersistableEdge, DatabasePersistableIncidence {

	/**
	 * Primary key of incident vertex.
	 */
	private int alphaVId = 0;

	/**
	 * Number mapping incidence's sequence in LambdaSeq of incident vertex
	 * (alpha).
	 */
	private long sequenceNumberInLambdaSeq;

	/**
	 * Number mapping edge's sequence in ESeq of graph.
	 */
	private long sequenceNumberInESeq;

	/**
	 * Flag indicating if an edge has been fully initialized, so it can be used
	 * without unwanted effects.
	 */
	private boolean initialized;

	/**
	 * Flag indicating whether an edge is persistent in database.
	 */
	private boolean persistent = false;

	/**
	 * Creates and initializes a new <code>EdgeImpl</code>.
	 * 
	 * @param anId
	 *            Id edge will have.
	 * @param graph
	 *            Graph created edge will belong to.
	 * @param alpha
	 *            Start vertex of edge.
	 * @param omega
	 *            End vertex of edge.
	 */
	protected EdgeImpl(int anId, Graph graph, Vertex alpha, Vertex omega) {
		super(anId, graph);
		this.getGraphImpl().addEdge(this, alpha, omega);
	}

	/**
	 * Creates and initializes a new <code>EdgeImpl</code>
	 * 
	 * @param anId
	 *            Id edge will have.
	 * @param graph
	 *            Graph created edge will belong to.
	 */
	protected EdgeImpl(int anId, Graph graph) {
		super(anId, graph);
	}

	private GraphImpl getGraphImpl() {
		return (GraphImpl) super.graph;
	}

	@Override
	public int getGId() {
		return this.getGraphImpl().getGId();
	}

	@Override
	public long getSequenceNumberInLambdaSeq() {
		return this.sequenceNumberInLambdaSeq;
	}

	@Override
	public long getSequenceNumberInESeq() {
		return this.sequenceNumberInESeq;
	}

	@Override
	public int getIncidentVId() {
		return this.alphaVId;
	}

	@Override
	public void setIncidentVId(int incidentVId) {
		if (this.alphaVId != incidentVId) {
			this.updateIncidentVId(incidentVId);
		}
	}

	@Override
	public void setSequenceNumberInLambdaSeq(long sequenceNumber) {
		if (this.sequenceNumberInLambdaSeq != sequenceNumber) {
			this.updateSequenceNumberInLambdaSeq(sequenceNumber);
		}
	}

	@Override
	public void setSequenceNumberInESeq(long sequenceNumberInESeq) {
		if (this.sequenceNumberInESeq != sequenceNumberInESeq) {
			this.updateSequenceNumberInEseq(sequenceNumberInESeq);
		}
	}

	/**
	 * Updates id of incident vertex.
	 * 
	 * @param incidentVId
	 *            Id of incident vertex.
	 */
	private void updateIncidentVId(int incidentVId) {
		this.alphaVId = incidentVId;
		if (this.isPersistent() && this.isInitialized() && incidentVId > 0) {
			; // TODO check
		}
		this.getGraphImpl().writeIncidentVIdBack(this);
	}

	/**
	 * Updates number mapping edge's sequence LambdaSeq of incident vertex.
	 * 
	 * @param sequenceNumber
	 *            Number of vertex mapping it's sequence LambdaSeq of incident
	 *            vertex.
	 */
	private void updateSequenceNumberInLambdaSeq(long sequenceNumber) {
		this.sequenceNumberInLambdaSeq = sequenceNumber;
		if (this.isPersistent() && this.isInitialized()) {
			this.getGraphImpl().writeSequenceNumberInLambdaSeqBack(this);
		}
	}

	/**
	 * Updates number mapping edge's sequence in VSeq.
	 * 
	 * @param sequenceNumber
	 *            Number of vertex mapping it's sequence in VSeq.
	 */
	private void updateSequenceNumberInEseq(long sequenceNumberInESeq) {
		this.sequenceNumberInESeq = sequenceNumberInESeq;
		if (this.isPersistent() && this.isInitialized()) {
			this.getGraphImpl().writeSequenceNumberInESeqBack(this);
		}
	}

	@Override
	public boolean isPersistent() {
		return this.persistent;
	}

	@Override
	public boolean isInitialized() {
		return this.initialized;
	}

	@Override
	public void setInitialized(boolean initialized) {
		this.initialized = true;
	}

	@Override
	public void setPersistent(boolean persistent) {
		this.persistent = persistent;
	}

	@Override
	public boolean isBeforeIncidence(Edge e) {
		this.assertPreCondition(e);
		if (!this.equals(e)) {
			return this.sequenceNumberInLambdaSeq < ((DatabasePersistableEdge) e)
					.getSequenceNumberInLambdaSeq();
		} else {
			return false;
		}
	}

	@Override
	public boolean isAfterIncidence(Edge e) {
		this.assertPreCondition(e);
		if (!this.equals(e)) {
			return this.sequenceNumberInLambdaSeq > ((DatabasePersistableEdge) e)
					.getSequenceNumberInLambdaSeq();
		} else {
			return false;
		}
	}

	@Override
	public boolean isBeforeEdge(Edge e) {
		this.assertGraphPreCondition(e);
		if (this != e.getNormalEdge()) {
			e = e.getNormalEdge();
			return this.sequenceNumberInESeq < ((DatabasePersistableEdge) e)
					.getSequenceNumberInESeq();
		} else {
			return false;
		}
	}

	@Override
	public boolean isAfterEdge(Edge e) {
		this.assertGraphPreCondition(e);
		if (this != e.getNormalEdge()) {
			e = e.getNormalEdge();
			return this.sequenceNumberInESeq > ((DatabasePersistableEdge) e)
					.getSequenceNumberInESeq();
		} else {
			return false;
		}
	}

	private void assertPreCondition(Edge e) {
		this.assertGraphPreCondition(e);
		assert ((VertexImpl) this.getThis()).equals(e.getThis());
	}

	private void assertGraphPreCondition(Edge e) {
		assert e != null;
		assert this.isValid();
		assert e.isValid();
		assert this.graph == e.getGraph();
	}

	@Override
	public Edge getPrevEdge() {
		return this.getGraphImpl().getPrevEdge(this);
	}

	@Override
	public Edge getNextEdge() {
		return this.getGraphImpl().getNextEdge(this);
	}

	@Override
	protected IncidenceImpl getPrevIncidenceInternal() {
		VertexImpl vertex = (VertexImpl) this.getIncidentVertex();
		return (IncidenceImpl) vertex.getPrevIncidence(this);
	}

	@Override
	protected IncidenceImpl getNextIncidenceInternal() {
		VertexImpl vertex = (VertexImpl) this.getIncidentVertex();
		return (IncidenceImpl) vertex.getNextIncidence(this);
	}

	@Override
	protected void setPrevEdgeInGraph(Edge prevEdge) {
		prevEdge.putBeforeEdge(this);
	}

	@Override
	protected void setNextEdgeInGraph(Edge nextEdge) {
		nextEdge.putAfterEdge(this);
	}

	@Override
	protected void setIncidentVertex(VertexBaseImpl v) {
		// does not add this edge to incidence list of vertex as it is taken
		// care of elsewhere
		this.setIncidentVId(v.getId());
	}

	@Override
	protected void setNextIncidenceInternal(IncidenceImpl nextIncidence) {
		nextIncidence.putIncidenceAfter(this);
	}

	private boolean isNotTheSameEdgeAs(Edge e) {
		return !this.equals(e);
	}

	private boolean isPartOfSameGraphAs(Edge e) {
		return this.graph == e.getGraph() && this.isValid() && e.isValid();
	}

	@Override
	public void putIncidenceBefore(Edge e) {
		assert e != null;
		assert this.isPartOfSameGraphAs(e);
		assert getThis() == e.getThis();
		VertexImpl v = (VertexImpl) this.getThis();
		assert v.isValid();
		if (this != e && this.getNextIncidence() != e) {
			v.putIncidenceBefore((IncidenceImpl) e, this);
			v.incidenceListModified();
		}
	}

	@Override
	public void putIncidenceAfter(Edge e) {
		assert e != null;
		assert this.isPartOfSameGraphAs(e);
		assert getThis() == e.getThis();
		VertexImpl v = (VertexImpl) this.getThis();
		assert v.isValid();
		// if (this.isNotTheSameEdgeAs(e)){
		if (this != e && this != e.getNextIncidence()) {
			// System.out.println("putEdgeAfter calls v.putIncidenceAfter");
			v.putIncidenceAfter((IncidenceImpl) e, this);
			v.incidenceListModified();
		}
	}

	@Override
	public void putAfterEdge(Edge e) {
		assert e != null;
		assert this.isPartOfSameGraphAs(e);
		assert this.isNotTheSameEdgeAs(e);
		assert e != reversedEdge;
		if (this != e && this != e.getNextEdge()) {
			this.getGraphImpl().putEdgeAfterInGraph(
					(EdgeBaseImpl) e.getNormalEdge(), this);
			this.getGraphImpl().edgeListModified();
		}
	}

	@Override
	public void putBeforeEdge(Edge e) {
		assert e != null;
		assert this.isPartOfSameGraphAs(e);
		assert this != e;
		assert e != reversedEdge;
		if (this != e && this.getNextEdge() != e) {
			this.getGraphImpl().putEdgeBeforeInGraph(
					(EdgeBaseImpl) e.getNormalEdge(), this);
			this.getGraphImpl().edgeListModified();
		}
	}

	@Override
	protected void setPrevIncidenceInternal(IncidenceImpl prevIncidence) {
		prevIncidence.putIncidenceBefore(this);
	}

	/**
	 * Sets id of edge if it is different than previous one.
	 * 
	 * @param id
	 *            Id of edge.
	 */
	@Override
	protected void setId(int id) {
		assert id > 0;
		if (super.getId() != id) {
			this.updateId(id);
		}
	}

	/**
	 * Updates id of edge.
	 * 
	 * @param id
	 *            Id of edge.
	 */
	private void updateId(int newEId) {
		if (this.isPersistent() && this.isInitialized()) {
			this.getGraphImpl().writeBackEdgeId(this, newEId);
		}
		this.id = newEId;
	}

	@Override
	protected VertexBaseImpl getIncidentVertex() {
		if (this.alphaVId > 0) {
			return (VertexBaseImpl) this.graph.getVertex(this.alphaVId);
		} else {
			return null;
		}
	}

	/**
	 * Notifies edge that one of his attributes has changed. Called from
	 * generated M1 edge classes when an attribute is changed.
	 * 
	 * @param attributeName
	 *            Name of attribute that has been changed.
	 */
	protected void attributeChanged(String attributeName) {
		if (this.isPersistent() && this.isInitialized()) {
			this.getGraphImpl().updateEdgeAttributeValueInDatabase(this,
					attributeName);
			this.getGraphImpl().internalGraphModified();
		}
	}

	@Override
	public boolean isValid() {
		if (this.graph != null) {
			return this.graph.containsEdge(this);
		} else {
			return false;
		}
	}

	@Override
	public void deleted() {
		this.persistent = false;
		this.initialized = false;
		this.id = 0;
		this.graph = null;
	}

	@Override
	public int getIncidentEId() {
		return this.getId();
	}
}
