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

import java.util.Comparator;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;

import de.uni_koblenz.jgralab.impl.IncidenceImpl;
import de.uni_koblenz.jgralab.impl.VertexBaseImpl;

/**
 * Vertex which can be persisted to a graph database.
 * 
 * @author ultbreit@uni-koblenz.de
 */
public abstract class VertexImpl extends VertexBaseImpl implements
		DatabasePersistableVertex {

	/**
	 * Number of vertex mapping sequence in VSeq.
	 */
	private long sequenceNumberInVSeq;

	/**
	 * Holds specific graph implementation to save some explicit type casts.
	 */
	protected GraphImpl graph;

	/**
	 * Incidence list of vertex.
	 */
	private IncidenceList incidenceList;

	/**
	 * Flag indicating persistence state of vertex.
	 */
	private boolean persistent = false;

	/**
	 * Flag indicating initialization state of vertex.
	 */
	private boolean initialized = false;

	/**
	 * Creates a new database persistent vertex.
	 * 
	 * @param vertexId
	 *            Id of vertex in graph.
	 * @param graph
	 *            Graph this vertex belongs to.
	 */
	protected VertexImpl(int vertexId, Graph graph) {
		super(vertexId, graph);
		this.incidenceList = new IncidenceList(this);
		this.graph = (GraphImpl) graph;
		this.graph.addVertex(this);
	}

	/**
	 * Gets primary key of graph this vertex is part of.
	 * 
	 * @return Primary key of graph this vertex is part of.
	 */
	public int getGId() {
		return this.graph.getGId();
	}

	/**
	 * Gets version of incidence list.
	 * 
	 * @return Version of incidence list.
	 */
	@Override
	public long getIncidenceListVersion() {
		return this.incidenceList.getVersion();
	}

	@Override
	public long getSequenceNumberInVSeq() {
		return this.sequenceNumberInVSeq;
	}

	/**
	 * Sets primary key of vertex. Should only be used after creation, as
	 * primary key is not subject to later changes.
	 * 
	 * @param primaryKey
	 *            Primary key of vertex.
	 */
	public void setPrimaryKey(int primaryKey) {
		this.setId(primaryKey);
	}

	/**
	 * Sets version of incidence list if it is different than previous version.
	 * 
	 * @param incidenceListVersion
	 *            Version of incidence list.
	 */
	@Override
	public void setIncidenceListVersion(long incidenceListVersion) {
		this.incidenceList.setVersion(incidenceListVersion);
	}

	/**
	 * Sets number of vertex mapping it's sequence in VSeq if it different than
	 * previous number.
	 * 
	 * @param sequenceNumber
	 *            Number of vertex mapping it's sequence in VSeq.
	 */
	public void setSequenceNumberInVSeq(long sequenceNumber) {
		if (this.sequenceNumberInVSeq != sequenceNumber) {
			this.updateSequenceNumber(sequenceNumber);
		}
	}

	/**
	 * Updates number of vertex mapping it's sequence in VSeq.
	 * 
	 * @param sequenceNumber
	 *            Number of vertex mapping it's sequence in VSeq.
	 */
	private void updateSequenceNumber(long sequenceNumber) {
		this.sequenceNumberInVSeq = sequenceNumber;
		if (this.isPersistent() && this.isInitialized()) {
			this.graph.updateSequenceNumberInDatabase(this);
		}
	}

	/**
	 * Adds an incidence to incidence list of vertex with incrementing incidence
	 * list version. Only used internally to fill incidence on load of a vertex.
	 * 
	 * @param eId
	 *            Identifier of incident edge.
	 * @param sequenceNumber
	 *            Number mapping incidence's sequence in incidence list.
	 */
	public void addIncidence(int eId, long sequenceNumber) {
		this.incidenceList.add(eId, sequenceNumber);
	}

	@Override
	public Vertex getPrevVertex() {
		return this.graph.getPrevVertex(this);
	}

	@Override
	public Vertex getNextVertex() {
		return this.graph.getNextVertex(this);
	}

	@Override
	protected IncidenceImpl getFirstIncidenceInternal() {
		return (IncidenceImpl) this.incidenceList.getFirst();
	}

	@Override
	protected IncidenceImpl getLastIncidenceInternal() {
		return (IncidenceImpl) this.incidenceList.getLast();
	}

	/**
	 * Notifies vertex that one of his attributes has changed. Called from
	 * generated M1 vertex classes when an attribute is changed.
	 * 
	 * @param attributeName
	 *            Name of attribute that has been changed.
	 */
	protected void attributeChanged(String attributeName) {
		if (this.isPersistent() && this.isInitialized()) {
			this.graph
					.updateVertexAttributeValueInDatabase(this, attributeName);
			this.graph.internalGraphModified();
		}
	}

	@Override
	protected void setPrevVertex(Vertex prevVertex) {
		prevVertex.putBefore(this);
	}

	@Override
	public void setNextVertex(Vertex nextVertex) {
		nextVertex.putAfter(this);
	}

	/**
	 * Precondition: - given vertex must not be null - given vertex must not be
	 * the same as this - given vertex must be created by same graph as this
	 * vertex - both vertices must be part of same graph
	 */
	@Override
	public void putBefore(Vertex v) {
		assert v != null;
		assert this.isNotTheSameVertexAs(v);
		assert this.isPartOfSameGraphAs(v);
		this.graph.putVertexBefore((VertexBaseImpl) v, this);
	}

	/**
	 * Precondition: - given vertex must not be null - given vertex must not be
	 * the same as this - given vertex must be created by same graph as this
	 * vertex - both vertices must be part of same graph
	 */
	@Override
	public void putAfter(Vertex v) {
		assert v != null;
		assert this.isNotTheSameVertexAs(v);
		assert this.isPartOfSameGraphAs(v);
		this.graph.putVertexAfter((VertexBaseImpl) v, this);
	}

	private boolean isNotTheSameVertexAs(Vertex v) {
		return !this.equals(v);
	}

	private boolean isPartOfSameGraphAs(Vertex v) {
		return this.graph == v.getGraph() && this.isValid() && v.isValid();
	}

	/**
	 * Gets previous incidence from incidence list of this vertex of given
	 * incidence.
	 * 
	 * @param edge
	 * @return Previous incidence.
	 */
	public DatabasePersistableEdge getPrevIncidence(DatabasePersistableEdge edge) {
		return this.incidenceList.getPrev(edge);
	}

	/**
	 * Gets next incidence from incidence list of this vertex of given
	 * incidence.
	 * 
	 * @param edge
	 * @return Next incidence.
	 */
	public DatabasePersistableEdge getNextIncidence(DatabasePersistableEdge edge) {
		return this.incidenceList.getNext(edge);
	}

	/**
	 * Sets first incidence in incidence list of vertex.
	 * 
	 * @param Incidence
	 *            to set as first incidence in incidence list of vertex.
	 */
	@Override
	protected void setFirstIncidence(IncidenceImpl firstIncidence) {
		assert firstIncidence != null;
		assert this.isInitialized() && this.isPersistent();
		DatabasePersistableEdge edge = (DatabasePersistableEdge) firstIncidence;
		assert edge.isPersistent();
		assert this.graph.containsEdge(edge);
		this.incidenceList.prepend(edge);
	}

	/**
	 * Sets last incidence in incidence list of vertex.
	 * 
	 * @param Incidence
	 *            to set as last incidence in incidence list of vertex.
	 */
	@Override
	protected void setLastIncidence(IncidenceImpl lastIncidence) {
		assert lastIncidence != null;
		assert this.isInitialized() && this.isPersistent();
		DatabasePersistableEdge edge = (DatabasePersistableEdge) lastIncidence;
		assert edge.isPersistent();
		assert this.graph.containsEdge(edge);
		this.appendIncidenceToLambdaSeq(lastIncidence);
	}

	/**
	 * Appends an incidence to incidence list of vertex.
	 * 
	 * @param incidence
	 *            Incidence to append to incidence list of vertex.
	 */
	@Override
	protected void appendIncidenceToLambdaSeq(IncidenceImpl incidence) {
		assert incidence != null;
		// TOOD only cast to DatabasePersitableIncidence and call
		// setIncidentVId()
		if (incidence.isNormal()) {
			((EdgeImpl) incidence).setIncidentVertex(this);
		} else {
			((ReversedEdgeImpl) incidence).setIncidentVertex(this);
		}
		this.incidenceList.append((DatabasePersistableEdge) incidence);
	}

	/**
	 * Sets id of vertex if its is different than previous one.
	 * 
	 * @param Id
	 *            of vertex.
	 */
	@Override
	protected void setId(int id) {
		assert id > 0;
		if (this.id != id) {
			this.updateId(id);
		}
	}

	private void updateId(int id) {
		int oldId = this.id;
		this.id = id;
		if (this.isPersistent() && this.isInitialized()) {
			this.graph.updateVertexIdInDatabase(oldId, this);
		}
	}

	/**
	 * Checks persistence status of vertex.
	 * 
	 * @return true if vertex is persistent, false otherwise.
	 */
	public boolean isPersistent() {
		return this.persistent;
	}

	/**
	 * Checks initialization status of vertex.
	 * 
	 * @return true if vertex is initialized, false otherwise.
	 */
	public boolean isInitialized() {
		return this.initialized;
	}

	/**
	 * Sets initialization status of vertex.
	 * 
	 * @param initialized
	 *            Initialization status to set.
	 */
	public void setInitialized(boolean initialized) {
		this.initialized = initialized;
	}

	@Override
	public boolean isBefore(Vertex vertex) {
		this.assertPreCondition(vertex);
		if (!this.equals(vertex)) {
			return this.sequenceNumberInVSeq < ((VertexImpl) vertex)
					.getSequenceNumberInVSeq();
		} else {
			return false;
		}
	}

	@Override
	public boolean isAfter(Vertex vertex) {
		this.assertPreCondition(vertex);
		if (!this.equals(vertex)) {
			return this.sequenceNumberInVSeq > ((VertexImpl) vertex)
					.getSequenceNumberInVSeq();
		} else {
			return false;
		}
	}

	private void assertPreCondition(Vertex vertex) {
		assert vertex != null;
		assert this.graph == vertex.getGraph();
		assert this.isValid() && ((VertexImpl) vertex).isValid();
	}

	@Override
	public void incidenceListModified() {
		assert isValid();
		this.incidenceList.modified();
	}

	public void incidenceListModifiedAtClient() {
		this.incidenceList.setVersion(this.getIncidenceListVersion() + 1);
	}

	@Override
	protected void removeIncidenceFromLambdaSeq(IncidenceImpl i) {
		assert i != null;
		assert this == i.getThis();
		this.incidenceList.remove((DatabasePersistableEdge) i);
	}

	@Override
	public boolean isValid() {
		if (this.graph != null) {
			return this.graph.containsVertex(this);
		} else {
			return false;
		}
	}

	@Override
	public void sortIncidences(Comparator<Edge> comp) {
		throw new UnsupportedOperationException(
				"This graph does not support sorting of incidences.");
	}

	private void assertPrecondition(IncidenceImpl target, IncidenceImpl moved) {
		assert (target != null) && (moved != null); // both incidences are
		// really given
		assert target.isValid() && moved.isValid(); // both incidence are valid
		assert target.getGraph() == moved.getGraph(); // both incidences belong
		// to same graph
		assert target.getGraph() == this.graph; // vertex and incidences belong
		// to same graph
		assert target.getThis() == moved.getThis(); // both incidences end at
		// same vertex
		assert target != moved;
	}

	@Override
	protected void putIncidenceBefore(IncidenceImpl target, IncidenceImpl moved) {
		this.assertPrecondition(target, moved);
		DatabasePersistableEdge targetEdge = (DatabasePersistableEdge) target;
		DatabasePersistableEdge movedEdge = (DatabasePersistableEdge) moved;
		if (target != moved && moved.getNextIncidence() != target) {
			this.incidenceList.putBefore(targetEdge, movedEdge);
			this.incidenceListModified();
		}
	}

	@Override
	protected void putIncidenceAfter(IncidenceImpl target, IncidenceImpl moved) {
		this.assertPrecondition(target, moved);
		DatabasePersistableEdge targetEdge = (DatabasePersistableEdge) target;
		DatabasePersistableEdge movedEdge = (DatabasePersistableEdge) moved;
		if (target != moved && target.getNextIncidence() != moved) {
			this.incidenceList.putAfter(targetEdge, movedEdge);
			this.incidenceListModified();
		}
	}

	/**
	 * Writes back incidence list version.
	 */
	protected void writeBackIncidenceListVersion() {
		this.graph.updateIncidenceListVersionInDatabase(this);
	}

	@Override
	public void setPersistent(boolean persistent) {
		this.persistent = persistent;
	}

	@Override
	public void deleted() {
		this.persistent = false;
		this.initialized = false;
		this.setId(0);
		this.graph = null;
		super.graph = null;
		this.incidenceList.clear();
	}

	@Override
	public int getDegree(EdgeDirection direction) {
		if (direction == EdgeDirection.OUT) {
			return this.incidenceList.countOutgoing();
		} else if (direction == EdgeDirection.IN) {
			return this.incidenceList.countIncoming();
		} else {
			return this.incidenceList.size();
		}
	}

	/**
	 * Deletes incident edges in memory.
	 */
	protected void deleteIncidencesInMemory() {
		for (int eId : this.incidenceList.eIds()) {
			this.getAndDeleteCachedEdge(eId);
		}
	}

	private void getAndDeleteCachedEdge(int eId) {
		if (this.graph.isEdgeCached(eId)) {
			this.getAndMarkEdgeAsDeleted(eId);
		}
	}

	private void getAndMarkEdgeAsDeleted(int eId) {
		Edge edge = this.graph.getEdge(eId);
		((DatabasePersistableEdge) edge).deleted();
	}

	/**
	 * Prints incidence list.
	 */
	public void printIncidenceList() {
		this.incidenceList.print();
	}

}
