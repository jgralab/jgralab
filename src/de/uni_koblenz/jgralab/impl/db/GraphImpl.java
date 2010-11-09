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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.GraphException;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.JGraLabList;
import de.uni_koblenz.jgralab.JGraLabMap;
import de.uni_koblenz.jgralab.JGraLabSet;
import de.uni_koblenz.jgralab.Record;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.impl.EdgeBaseImpl;
import de.uni_koblenz.jgralab.impl.FreeIndexList;
import de.uni_koblenz.jgralab.impl.GraphBaseImpl;
import de.uni_koblenz.jgralab.impl.IncidenceImpl;
import de.uni_koblenz.jgralab.impl.ReversedEdgeBaseImpl;
import de.uni_koblenz.jgralab.impl.VertexBaseImpl;
import de.uni_koblenz.jgralab.impl.std.JGraLabListImpl;
import de.uni_koblenz.jgralab.impl.std.JGraLabMapImpl;
import de.uni_koblenz.jgralab.impl.std.JGraLabSetImpl;
import de.uni_koblenz.jgralab.schema.GraphClass;
import de.uni_koblenz.jgralab.trans.CommitFailedException;
import de.uni_koblenz.jgralab.trans.InvalidSavepointException;
import de.uni_koblenz.jgralab.trans.Savepoint;
import de.uni_koblenz.jgralab.trans.Transaction;

/**
 * Graph which can be persisted into a graph database.
 * 
 * @author ultbreit@uni-koblenz.de
 */
public abstract class GraphImpl extends GraphBaseImpl implements
		DatabasePersistableGraph {

	/**
	 * Flag indicating persistence state of graph.
	 */
	private boolean persistent = false;

	/**
	 * Primary key of graph in database.
	 */
	private int gId = -1;

	/**
	 * Global vertex sequence.
	 */
	private VertexList vSeq;

	/**
	 * Global edge sequence.
	 */
	private EdgeList eSeq;

	/**
	 * Cache holding primary keys of elements in database to speed up access to
	 * them.
	 */
	private GraphCache graphCache;

	/**
	 * Database containing this graph.
	 */
	private GraphDatabase containingDatabase;

	/**
	 * Creates a new <code>GraphImpl</code> persistent in database.
	 * 
	 * @param id
	 *            Identifier of graph.
	 * @param graphClass
	 *            Type of graph.
	 * @param graphDatabase
	 *            Database in which graph is persistent.
	 */
	protected GraphImpl(String id, GraphClass graphClass,
			GraphDatabase graphDatabase) {
		this(id, 1000, 1000, graphClass, graphDatabase);
	}

	/**
	 * Creates a new <code>GraphImpl</code> persistent in database.
	 * 
	 * @param id
	 *            Identifier of graph.
	 * @param graphClass
	 *            Type of graph.
	 * @param graphDatabase
	 *            Database in which graph is persistent.
	 */
	protected GraphImpl(String id, int vMax, int eMax, GraphClass graphClass,
			GraphDatabase graphDatabase) {
		this(id, graphClass, vMax, eMax);
		if (graphDatabase != null) {
			this.containingDatabase = graphDatabase;
		} else {
			throw new GraphException(
					"Cannot create a graph with database support with no database given.");
		}
	}

	/**
	 * Creates a new <code>GraphImpl</code> not persistent in database.
	 * 
	 * @param id
	 *            Identifier of graph.
	 * @param graphClass
	 *            Type of graph.
	 * @param vMax
	 *            Maximum count of vertices in graph.
	 * @param eMax
	 *            Maximum count of edges in graph.
	 */
	private GraphImpl(String id, GraphClass graphClass, int vMax, int eMax) {
		super(id, graphClass, vMax, eMax);
		this.vSeq = new VertexList(this);
		this.eSeq = new EdgeList(this);
		this.graphCache = new GraphCache();
	}

	@Override
	public boolean isPersistent() {
		return this.persistent;
	}

	@Override
	public void setPersistent(boolean persistent) {
		this.persistent = persistent;
	}

	@Override
	public boolean isInitialized() {
		return !this.isLoading();
	}

	@Override
	public void setInitialized(boolean initialized) {
		super.setLoading(!initialized);
	}

	@Override
	public int getGId() {
		return this.gId;
	}

	@Override
	public void setGId(int gId) {
		this.gId = gId;
	}

	@Override
	public void setGraphVersion(long graphVersion) {
		if (super.getGraphVersion() != graphVersion) {
			this.updateGraphVersionInMemory(graphVersion);
		}
	}

	private void updateGraphVersionInMemory(long graphVersion) {
		super.setGraphVersion(graphVersion);
	}

	/**
	 * Notifies graph that it has been modified.
	 */
	protected void internalGraphModified() {
		this.updateGraphVersionInMemory(super.getGraphVersion() + 1);
	}

	@Override
	public void addVertex(int vId, long sequenceNumber) {
		this.vSeq.add(sequenceNumber, vId);
	}

	@Override
	protected void addVertex(Vertex newVertex) {
		if (newVertex.getId() == 0) {
			this.addFreshlyCreatedVertex(newVertex);
		} else {
			this.testValidityOfLoadedVertex(newVertex);
		}
	}

	private void testValidityOfLoadedVertex(Vertex vertex) {
		int vId = vertex.getId();
		if (vId <= 0) {
			throw new GraphException("Cannot load a vertex with id <= 0.");
		// else if (vId > vMax)
		// throw new GraphException("Vertex id " + vId +
		// " is bigger than vSize.");
		}
	}

	private void addFreshlyCreatedVertex(Vertex vertex) {
		this.allocateValidIdTo(vertex);
		this.vSeq.append((DatabasePersistableVertex) vertex);
		this.vSeq.modified();
		this.insertVertexIntoDatabase((DatabasePersistableVertex) vertex);
		this.graphCache.addVertex((DatabasePersistableVertex) vertex);
		internalGraphModified();

		this.internalVertexAdded((VertexBaseImpl) vertex);
	}

	private void allocateValidIdTo(Vertex vertex) {
		int vId = allocateVertexIndex(vertex.getId());
		assert vId != 0;
		((VertexImpl) vertex).setId(vId);
	}

	@Override
	protected void appendVertexToVSeq(VertexBaseImpl v) {
		this.vSeq.append((DatabasePersistableVertex) v);
		this.vertexListModified();
	}

	@Override
	public void addEdge(int eId, long sequenceNumber) {
		this.eSeq.add(sequenceNumber, eId);
	}

	/**
	 * Only use it to add an edge internally to graph. It means that edge has
	 * just been created by this graph with createEdge or is loaded from
	 * storage. Not meant to add an edge from another graph.
	 */
	@Override
	protected void addEdge(Edge newEdge, Vertex alpha, Vertex omega) {
		this.assertPreConditionOfAddEdge(newEdge, alpha, omega);
		this.testEdgeSuitingVertices(newEdge, alpha, omega);
		this.proceedWithAdditionOf(newEdge, alpha, omega);
	}

	private void assertPreConditionOfAddEdge(Edge newEdge, Vertex alpha,
			Vertex omega) {
		assert newEdge != null;
		assert this.isVertexValid(alpha) : "Alpha vertex is invalid";
		assert this.isVertexValid(omega) : "Omega vertex is invalid";
		assert newEdge.isNormal() : "Cannot add reversed edge";
		assert this.isSameSchemaAsGraph(newEdge, alpha, omega) : "Schemas of alpha, omega, edge and the graph do not match!";
		assert this.isGraphMatching(newEdge, alpha, omega) : "Graphs of alpha, omega, edge and this graph do not match!";
	}

	private boolean isVertexValid(Vertex vertex) {
		return (vertex != null) && vertex.isValid() && containsVertex(vertex);
	}

	private boolean isSameSchemaAsGraph(Edge edge, Vertex alpha, Vertex omega) {
		return alpha.getSchema() == omega.getSchema()
				&& alpha.getSchema() == this.getSchema()
				&& edge.getSchema() == this.getSchema();
	}

	private boolean isGraphMatching(Edge edge, Vertex alpha, Vertex omega) {
		return alpha.getGraph() == omega.getGraph() && alpha.getGraph() == this
				&& edge.getGraph() == this;
	}

	private void testEdgeSuitingVertices(Edge edge, Vertex alpha, Vertex omega) {
		if (!alpha.isValidAlpha(edge)) {
			throw new GraphException("Edges of class "
					+ edge.getAttributedElementClass().getQualifiedName()
					+ " may not start at vertices of class "
					+ alpha.getAttributedElementClass().getQualifiedName());
		}
		if (!omega.isValidOmega(edge)) {
			throw new GraphException("Edges of class "
					+ edge.getAttributedElementClass().getQualifiedName()
					+ " may not end at vertices of class "
					+ omega.getAttributedElementClass().getQualifiedName());
		}
	}

	private void proceedWithAdditionOf(Edge edge, Vertex alpha, Vertex omega) {
		assert edge.getId() >= 0;
		if (edge.getId() == 0) {
			this.addFreshlyCreatedEdge(edge, alpha, omega);
		} else {
			this.testValidityOfLoadedEdge(edge);
		}
	}

	private void testValidityOfLoadedEdge(Edge edge) {
		int eId = edge.getId();
		if (eId <= 0) {
			throw new GraphException("Cannot load an edge with id <= 0");
		} else if (eId > eMax) {
			throw new GraphException("Edge's id " + eId
					+ " is bigger than maximum capacity of edges allowed.");
		}
	}

	private void addFreshlyCreatedEdge(Edge edge, Vertex alpha, Vertex omega) {

		EdgeImpl edgeImpl = (EdgeImpl) edge;
		this.allocateValidIdTo(edgeImpl);

		VertexImpl alphaImpl = (VertexImpl) alpha;
		VertexImpl omegaImpl = (VertexImpl) omega;

		this.addEdgeToIncidenceLists(edgeImpl, alphaImpl, omegaImpl);

		this.insertEdgeIntoDatabase(edgeImpl, alphaImpl, omegaImpl);

		this.appendEdgeToESeq(edgeImpl);

		this.graphCache.addEdge(edgeImpl);

		internalGraphModified();
		this.internalEdgeAdded(edgeImpl);
	}

	private void allocateValidIdTo(EdgeImpl edge) {
		int eId = allocateEdgeIndex(edge.getId());
		assert eId != 0;
		edge.setId(eId);
	}

	private void addEdgeToIncidenceLists(EdgeImpl edge, VertexImpl alpha,
			VertexImpl omega) {
		alpha.appendIncidenceToLambdaSeq(edge);
		alpha.incidenceListModifiedAtClient();
		omega
				.appendIncidenceToLambdaSeq((IncidenceImpl) edge
						.getReversedEdge());
		omega.incidenceListModifiedAtClient();
	}

	@Override
	protected void appendEdgeToESeq(EdgeBaseImpl edge) {
		this.eSeq.append((DatabasePersistableEdge) edge);
		this.eSeq.modified();
	}

	private void insertVertexIntoDatabase(DatabasePersistableVertex vertex) {
		try {
			this.containingDatabase.insert(vertex);
		} catch (GraphDatabaseException exception) {
			exception.printStackTrace();
			throw new GraphException("Cannot persist vertex.", exception);
		}
	}

	@Override
	protected Edge internalCreateEdge(Class<? extends Edge> cls, Vertex alpha,
			Vertex omega) {
		Edge edge = graphFactory.createEdgeWithDatabaseSupport(cls, 0, this,
				alpha, omega);
		edge.initializeAttributesWithDefaultValues();
		this.graphCache.addEdge((DatabasePersistableEdge) edge);
		return edge;
	}

	@Override
	protected Vertex internalCreateVertex(Class<? extends Vertex> cls) {
		Vertex vertex = (DatabasePersistableVertex) graphFactory
				.createVertexWithDatabaseSupport(cls, 0, this);
		vertex.initializeAttributesWithDefaultValues();
		this.graphCache.addVertex((DatabasePersistableVertex) vertex);
		return vertex;
	}

	private void insertEdgeIntoDatabase(DatabasePersistableEdge edge,
			DatabasePersistableVertex alpha, DatabasePersistableVertex omega) {
		try {
			this.containingDatabase.insert(edge, alpha, omega);
		} catch (GraphDatabaseException exception) {
			exception.printStackTrace();
			throw new GraphException("Cannot persist edge.", exception);
		}
	}

	/**
	 * Notifies graph that one of his attributes has changed. Called from
	 * generated M1 graph classes when an attribute is changed.
	 * 
	 * @param attributeName
	 *            Name of attribute that has been changed.
	 */
	protected void attributeChanged(String attributeName) {
		if (this.isPersistent() && this.isInitialized()) {
			this.writeBackGraphAttribute(attributeName);
			this.internalGraphModified();
		}
	}

	private void writeBackGraphAttribute(String attributeName) {
		try {
			this.containingDatabase.updateAttributeValueOf(this, attributeName);
		} catch (GraphDatabaseException exception) {
			exception.printStackTrace();
			throw new GraphException(
					"Cannot write back graph attribute value.", exception);
		}
	}

	@Override
	public long getVertexListVersion() {
		return this.vSeq.getVersion();
	}

	@Override
	public long getEdgeListVersion() {
		return this.eSeq.getVersion();
	}

	@Override
	protected void setVertexListVersion(long vertexListVersion) {
		this.vSeq.setVersion(vertexListVersion);
	}

	@Override
	public void setEdgeListVersion(long edgeListVersion) {
		this.eSeq.setVersion(edgeListVersion);
	}

	@Override
	public int getVCount() {
		return this.vSeq.size();
	}

	@Override
	public int getECount() {
		return this.eSeq.size();
	}

	@Override
	public void setVCount(int count) {
		if (count > super.vMax) {
			expandVertexArray(count);
		}
	}

	@Override
	protected void setECount(int count) {
		if (count > super.eMax) {
			expandEdgeArray(count);
		}
	}

	@Override
	public Vertex getFirstVertex() {
		return this.vSeq.getFirst();
	}

	@Override
	public Edge getFirstEdge() {
		return this.eSeq.getFirst();
	}

	@Override
	public Vertex getLastVertex() {
		return this.vSeq.getLast();
	}

	@Override
	public Edge getLastEdge() {
		return this.eSeq.getLast();
	}

	private DatabasePersistableVertex getVertexFromDatabase(int vId) {
		try {
			this.setLoading(true);
			DatabasePersistableVertex vertex = this.containingDatabase
					.getVertex(vId, this);
			this.setLoading(false);
			return vertex;
		} catch (Exception exception) {
			exception.printStackTrace();
			throw new GraphException("Cannot get vertex.", exception);
		}
	}

	@Override
	protected void setFirstVertex(VertexBaseImpl vertex) {
		if (vertex != null) { // because of super constructor
			DatabasePersistableVertex persistentVertex = (DatabasePersistableVertex) vertex;
			this.vSeq.prepend(persistentVertex);
		}
	}

	@Override
	protected void setFirstEdgeInGraph(EdgeBaseImpl edge) {
		// assert edge != null;
		if (edge != null) { // because of super constructor
			DatabasePersistableEdge persistentEdge = (DatabasePersistableEdge) edge;
			this.eSeq.prepend(persistentEdge);
		}
	}

	@Override
	protected void setLastVertex(VertexBaseImpl vertex) {
		if (vertex != null) { // because of super constructor
			DatabasePersistableVertex persistentVertex = (DatabasePersistableVertex) vertex;
			this.vSeq.append(persistentVertex);
		}
	}

	@Override
	protected void setLastEdgeInGraph(EdgeBaseImpl edge) {
		if (edge != null) { // because of super constructor
			DatabasePersistableEdge persistentEdge = (DatabasePersistableEdge) edge;
			this.eSeq.append(persistentEdge);
		}
	}

	@Override
	public void setId(String id) {
		if (super.getId() != id) {
			this.updateId(id);
		}
	}

	private void updateId(String id) {
		super.setId(id);
		if (this.isPersistent() && this.isInitialized()) {
			this.writeBackGraphId();
		}
	}

	private void writeBackGraphId() {
		try {
			this.containingDatabase.updateIdOf(this);
		} catch (GraphDatabaseException exception) {
			exception.printStackTrace();
			throw new GraphException("Cannot write back id of graph.",
					exception);
		}
	}

	@Override
	public Vertex getVertex(int vId) {
		if (this.isValidVertexId(vId)) {
			return this.internalGetVertexFromCacheOrDatabase(vId);
		} else {
			throw new GraphException("Id of vertex must be > 0.");
		}
	}

	private boolean isValidVertexId(int vId) {
		return vId > 0;
	}

	private Vertex internalGetVertexFromCacheOrDatabase(int vId) {
		if (this.vSeq.containsVertex(vId)) {
			return this.getVertexFromCacheOrDatabase(vId);
		} else {
			return null;
		}
	}

	private DatabasePersistableVertex getVertexFromCacheOrDatabase(int vId) {
		if (this.graphCache.containsVertex(this, vId)) {
			return this.graphCache.getVertex(this, vId);
		} else {
			return this.getAndCacheVertexFromDatabase(vId);
		}
	}

	private DatabasePersistableVertex getAndCacheVertexFromDatabase(int vId) {
		DatabasePersistableVertex vertex = this.getVertexFromDatabase(vId);
		this.graphCache.addVertex(vertex);
		return vertex;
	}

	@Override
	public Edge getEdge(int eId) {
		if (this.isValidEdgeId(eId)) {
			return this.internalGetOrientedEdge(eId);
		} else {
			throw new GraphException("Edge id must be != 0.");
		}
	}

	private boolean isValidEdgeId(int eId) {
		return eId != 0;
	}

	private Edge internalGetOrientedEdge(int eId) {
		if (this.eSeq.containsEdge(eId)) {
			return this.getOrientedEdge(eId);
		} else {
			return null;
		}
	}

	private Edge getOrientedEdge(int eId) {
		if (eId > 0) {
			return this.getEdgeFromCacheOrDatabase(eId);
		} else {
			return this.getEdgeFromCacheOrDatabase(Math.abs(eId))
					.getReversedEdge();
		}
	}

	private DatabasePersistableEdge getEdgeFromCacheOrDatabase(int eId) {
		if (this.graphCache.containsEdge(this, eId)) {
			return this.graphCache.getEdge(this, eId);
		} else {
			return this.getAndCacheEdgeFromDatabase(eId);
		}
	}

	private DatabasePersistableEdge getAndCacheEdgeFromDatabase(int eId) {
		DatabasePersistableEdge edge = this.getEdgeFromDatabase(eId);
		this.graphCache.addEdge(edge);
		return edge;
	}

	private DatabasePersistableEdge getEdgeFromDatabase(int eId) {
		try {
			this.setLoading(true);
			DatabasePersistableEdge edge = this.containingDatabase.getEdge(eId,
					this);
			this.setLoading(false);
			return edge;
		} catch (Exception exception) {
			exception.printStackTrace();
			throw new GraphException("Cannot get edge.", exception);
		}
	}

	@Override
	protected void removeVertexFromVSeq(VertexBaseImpl v) {
		assert v != null;
		this.vSeq.remove((DatabasePersistableVertex) v);
		freeVertexIndex(v.getId());
	}

	/**
	 * Deletes vertex from database, removes it from cache and sets it to
	 * deleted.
	 * 
	 * @param vertexToBeDeleted
	 *            The vertex to delete from database.
	 * 
	 *            Precondition: Vertex is not part of VSeq and persistent in
	 *            database.
	 * 
	 *            Postcondition: Vertex is not in cache, no longer persistent in
	 *            database and set to deleted.
	 */
	@Override
	protected void vertexAfterDeleted(Vertex vertexToBeDeleted) {
		assert vertexToBeDeleted != null;
		DatabasePersistableVertex vertex = (DatabasePersistableVertex) vertexToBeDeleted;
		this.graphCache.removeVertex(this, vertex.getId());
		if (vertex.isPersistent()) {
			this.deleteVertexAndIncidentEdgesFromDatabase(vertex);
		}
		vertex.deleted();
	}

	private void deleteVertexAndIncidentEdgesFromDatabase(
			DatabasePersistableVertex vertex) {
		try {
			this.containingDatabase.delete(vertex);
		} catch (GraphDatabaseException exception) {
			exception.printStackTrace();
			throw new GraphException("Cannot delete vertex.", exception);
		}
	}

	@Override
	protected void removeEdgeFromESeq(EdgeBaseImpl e) {
		assert e != null;
		this.eSeq.remove((DatabasePersistableEdge) e);
		this.freeEdgeIndex(e.getId());
	}

	@Override
	protected void edgeAfterDeleted(Edge e, Vertex oldAlpha, Vertex oldOmega) {
		assert e != null;
		DatabasePersistableEdge edge = (DatabasePersistableEdge) e;
		if (edge.isPersistent()) {
			this.deleteEdgeFromDatabase(edge);
		}
		this.graphCache.removeEdge(this, edge.getId());
		edge.deleted();
	}

	private void deleteEdgeFromDatabase(DatabasePersistableEdge edge) {
		try {
			this.containingDatabase.delete(edge);
		} catch (Exception exception) {
			exception.printStackTrace();
			throw new GraphException("Cannot delete edge " + edge.getId()
					+ " in graph " + this.getId(), exception);
		}
	}

	@Override
	protected void vertexListModified() {
		if (!this.isLoading()) {
			this.vSeq.modified();
			graphModified();
		}
	}

	@Override
	protected void edgeListModified() {
		if (!this.isLoading()) {
			this.eSeq.modified();
			graphModified();
		}
	}

	@Override
	protected void putVertexAfter(VertexBaseImpl targetVertex,
			VertexBaseImpl movedVertex) {
		DatabasePersistableVertex targetVertexImpl = (DatabasePersistableVertex) targetVertex;
		DatabasePersistableVertex movedVertexImpl = (DatabasePersistableVertex) movedVertex;
		this.vSeq.putAfter(targetVertexImpl, movedVertexImpl);
		// this.vertexListModified();
	}

	@Override
	protected void putEdgeAfterInGraph(EdgeBaseImpl targetEdge,
			EdgeBaseImpl movedEdge) {
		this.assertEdges(movedEdge, targetEdge);
		DatabasePersistableEdge dbTargetEdge = (DatabasePersistableEdge) targetEdge;
		DatabasePersistableEdge dbMovedEdge = (DatabasePersistableEdge) movedEdge;
		this.eSeq.putAfter(dbTargetEdge, dbMovedEdge);
	}

	private void assertEdges(EdgeBaseImpl movedEdge, EdgeBaseImpl targetEdge) {
		assert (targetEdge != null) && targetEdge.isValid()
				&& this.containsEdge(targetEdge);
		assert (movedEdge != null) && movedEdge.isValid()
				&& this.containsEdge(movedEdge);
		assert !((DatabasePersistableEdge) targetEdge).equals(movedEdge);
	}

	@Override
	protected void putVertexBefore(VertexBaseImpl targetVertex,
			VertexBaseImpl movedVertex) {
		DatabasePersistableVertex targetVertexImpl = (DatabasePersistableVertex) targetVertex;
		DatabasePersistableVertex movedVertexImpl = (DatabasePersistableVertex) movedVertex;
		this.vSeq.putBefore(targetVertexImpl, movedVertexImpl);
	}

	@Override
	protected void putEdgeBeforeInGraph(EdgeBaseImpl targetEdge,
			EdgeBaseImpl movedEdge) {
		this.assertEdges(movedEdge, targetEdge);
		DatabasePersistableEdge dbTargetEdge = (DatabasePersistableEdge) targetEdge;
		DatabasePersistableEdge dbMovedEdge = (DatabasePersistableEdge) movedEdge;
		this.eSeq.putBefore(dbTargetEdge, dbMovedEdge);
	}

	@Override
	public boolean containsVertex(Vertex v) {
		return v != null && v.getGraph() == this
				&& this.vSeq.contains((DatabasePersistableVertex) v);
	}

	@Override
	public boolean containsEdge(Edge e) {
		return (e != null) && e.getGraph() == this
				&& this.eSeq.contains((DatabasePersistableEdge) e);
	}

	@Override
	protected void expandVertexArray(int newSize) {
		if (newSize > vMax) {
			expandFreeVertexList(newSize);
		} else {
			throw new GraphException("newSize must > vSize: vSize=" + vMax
					+ ", newSize=" + newSize);
		}
	}

	private void expandFreeVertexList(int newSize) {
		if (getFreeVertexList() == null) {
			setFreeVertexList(new FreeIndexList(newSize));
		} else {
			getFreeVertexList().expandBy(newSize - vMax);
		}
		vMax = newSize;
		notifyMaxVertexCountIncreased(newSize);
	}

	@Override
	protected void expandEdgeArray(int newSize) {
		if (newSize > eMax) {
			this.expandFreeEdgeList(newSize);
		} else {
			throw new GraphException("newSize must be > eSize: eSize=" + eMax
					+ ", newSize=" + newSize);
		}
	}

	private void expandFreeEdgeList(int newSize) {
		if (getFreeEdgeList() == null) {
			setFreeEdgeList(new FreeIndexList(newSize));
		} else {
			getFreeEdgeList().expandBy(newSize - eMax);
		}
		eMax = newSize;
		super.notifyMaxEdgeCountIncreased(newSize);
	}

	private void writeBackVertexListVersion() {
		try {
			this.containingDatabase.updateVertexListVersionOf(this);
		} catch (GraphDatabaseException exception) {
			exception.printStackTrace();
			throw new GraphException(
					"Could not write back vertex list version.", exception);
		}
	}

	private void writeBackEdgeListVersion() {
		try {
			this.containingDatabase.updateEdgeListVersionOf(this);
		} catch (GraphDatabaseException exception) {
			exception.printStackTrace();
			throw new GraphException("Could not write back edge list version.",
					exception);
		}
	}

	/**
	 * Updates a vertex's attribute value in database.
	 * 
	 * @param edge
	 *            Vertex with attribute value to update.
	 * @param attributeName
	 *            Name of attribute.
	 */
	protected void updateVertexAttributeValueInDatabase(
			DatabasePersistableVertex vertex, String attributeName) {
		try {
			this.containingDatabase.updateAttributeValueOf(vertex,
					attributeName);
		} catch (GraphDatabaseException exception) {
			exception.printStackTrace();
			throw new GraphException(
					"Could write back vertex attribute value.", exception);
		}
	}

	/**
	 * Updates an edge's attribute value in database.
	 * 
	 * @param edge
	 *            Edge with attribute value to update.
	 * @param attributeName
	 *            Name of attribute.
	 */
	protected void updateEdgeAttributeValueInDatabase(
			DatabasePersistableEdge edge, String attributeName) {
		try {
			this.containingDatabase.updateAttributeValueOf(edge, attributeName);
		} catch (GraphDatabaseException exception) {
			exception.printStackTrace();
			throw new GraphException("Could write back edge attribute value.",
					exception);
		}
	}

	/**
	 * Updates incidence list of a vertex in database.
	 * 
	 * @param vertex
	 *            Vertex with incidence list to update.
	 */
	protected void updateIncidenceListVersionInDatabase(
			DatabasePersistableVertex vertex) {
		try {
			this.containingDatabase.updateIncidenceListVersionOf(vertex);
		} catch (GraphDatabaseException exception) {
			exception.printStackTrace();
			throw new GraphException(
					"Could not write back incidence list of vertex.", exception);
		}
	}

	/**
	 * Updates sequence number of vertex in database.
	 * 
	 * @param vertex
	 *            Vertex with sequence number to update.
	 */
	protected void updateSequenceNumberInDatabase(
			DatabasePersistableVertex vertex) {
		try {
			this.containingDatabase.updateSequenceNumberInVSeqOf(vertex);
		} catch (GraphDatabaseException exception) {
			exception.printStackTrace();
			throw new GraphException("", exception);
		}
	}

	/**
	 * Updates id of a vertex in database.
	 * 
	 * @param oldVId
	 *            Old id of vertex.
	 * @param vertex
	 *            Vertex with new id.
	 */
	protected void updateVertexIdInDatabase(int oldVId,
			DatabasePersistableVertex vertex) {
		try {
			this.containingDatabase.updateIdOf(oldVId, vertex);
		} catch (GraphDatabaseException exception) {
			exception.printStackTrace();
			throw new GraphException("Could not write back new id of vertex.",
					exception);
		}
	}

	/**
	 * Write id of incident vertex back.
	 * 
	 * @param incidence
	 *            Incidence to write back id of it's incident vertex.
	 */
	protected void writeIncidentVIdBack(DatabasePersistableIncidence incidence) {
		try {
			this.containingDatabase.updateIncidentVIdOf(incidence);
		} catch (GraphDatabaseException exception) {
			exception.printStackTrace();
			throw new GraphException(
					"Cannot write id of incident vertex back.", exception);
		}
	}

	/**
	 * Write edge number mapping it's sequence in ESeq back.
	 * 
	 * @param incidence
	 *            Edge to write back it's number mapping it's sequence in ESeq.
	 */
	protected void writeSequenceNumberInESeqBack(DatabasePersistableEdge edge) {
		try {
			this.containingDatabase.updateSequenceNumberInESeqOf(edge);
		} catch (GraphDatabaseException exception) {
			exception.printStackTrace();
			throw new GraphException(
					"Cannot write number mapping edge's sequence in ESeq back.",
					exception);
		}
	}

	/**
	 * Write incidence's number mapping it's sequence in LambdaSeq back.
	 * 
	 * @param incidence
	 *            Incidence to write back it's number mapping it's sequence in
	 *            LambdaSeq.
	 */
	protected void writeSequenceNumberInLambdaSeqBack(
			DatabasePersistableIncidence incidence) {
		try {
			this.containingDatabase
					.updateSequenceNumberInLambdaSeqOf(incidence);
		} catch (GraphDatabaseException exception) {
			exception.printStackTrace();
			throw new GraphException(
					"Cannot write incidence's number mapping it's sequence in LambdaSeq back.",
					exception);
		}
	}

	/**
	 * Write edge's new id back to database.
	 * 
	 * @param edge
	 *            Edge to write back id.
	 * @param newEId
	 *            The new edge id.
	 */
	protected void writeBackEdgeId(DatabasePersistableEdge edge, int newEId) {
		try {
			this.containingDatabase.updateIdOf(edge, newEId);
		} catch (GraphDatabaseException exception) {
			exception.printStackTrace();
			throw new GraphException("Cannot write edge id back.", exception);
		}
	}

	/**
	 * Gets previous vertex in VSeq before given one.
	 * 
	 * @param vertex
	 *            Vertex to get it's predecessor in VSeq.
	 * @return Predecessor in VSeq of edge or null if given one is first vertex
	 *         in VSeq.
	 */
	protected Vertex getPrevVertex(DatabasePersistableVertex vertex) {
		assert vertex.isValid();
		assert this == vertex.getGraph();
		return this.vSeq.getPrev(vertex);
	}

	/**
	 * Gets previous vertex in VSeq before given one.
	 * 
	 * @param vertex
	 *            Vertex to get it's successor in VSeq.
	 * @return Successor in VSeq of edge or null if given one is last vertex in
	 *         VSeq.
	 */
	protected Vertex getNextVertex(DatabasePersistableVertex vertex) {
		assert vertex.isValid();
		assert this == vertex.getGraph();
		return this.vSeq.getNext(vertex);
	}

	/**
	 * Gets previous edge in ESeq before given one.
	 * 
	 * @param edge
	 *            Edge to get it's predecessor in ESeq.
	 * @return Predecessor in ESeq of edge or null if given one is first edge in
	 *         ESeq.
	 */
	protected Edge getPrevEdge(DatabasePersistableEdge edge) {
		assert edge.isValid();
		assert this == edge.getGraph();
		return this.eSeq.getPrev(edge);
	}

	/**
	 * Gets next edge in ESeq after given one.
	 * 
	 * @param edge
	 *            Edge to get it's successor in ESeq.
	 * @return Successor in ESeq of edge or null if given one is last edge in
	 *         ESeq.
	 */
	protected Edge getNextEdge(DatabasePersistableEdge edge) {
		assert edge.isValid();
		assert this == edge.getGraph();
		return this.eSeq.getNext(edge);
	}

	@Override
	public void deleted() {
		this.setGId(-1);
		this.setPersistent(false);
		this.setLoading(false);
		this.containingDatabase = null;
		this.vSeq.clear();
		this.vSeq = null;
		this.eSeq.clear();
		this.eSeq = null;
	}

	@Override
	public void loadingCompleted() {
		this.setLoading(false);
	}

	/**
	 * Checks by it's id whether a vertex is cached or not.
	 * 
	 * @param vId
	 *            Id of vertex.
	 * @return true if vertex is cached, false otherwise.
	 */
	protected boolean isVertexCached(int vId) {
		return this.graphCache.containsVertex(this, vId);
	}

	/**
	 * Checks by it's id whether an edge is cached or not.
	 * 
	 * @param eId
	 *            Id of edge.
	 * @return true if edge is cached, false otherwise.
	 */
	protected boolean isEdgeCached(int eId) {
		return this.graphCache.containsEdge(this, eId);
	}

	/**
	 * Reorganizes sequence numbers in vertex list at database only.
	 * 
	 * Precondition: Vertex list must have been reorganized in memory just
	 * before.
	 */
	public void reorganizeVertexListInGraphDatabase() {
		try {
			long start = this.vSeq.getFirst().getSequenceNumberInVSeq();
			this.containingDatabase.reorganizeVertexList(this, start);
		} catch (GraphDatabaseException exception) {
			exception.printStackTrace();
			throw new GraphException("VSeq of graph " + this.getId()
					+ " could not be reorganized in database.", exception);
		}
	}

	/**
	 * Reorganizes sequence numbers in edge list at database only.
	 * 
	 * Precondition: Edge list must have been reorganized in memory just before.
	 */
	public void reorganizeEdgeListInGraphDatabase() {
		try {
			long start = this.eSeq.getFirst().getSequenceNumberInESeq();
			this.containingDatabase.reorganizeEdgeList(this, start);
		} catch (GraphDatabaseException exception) {
			exception.printStackTrace();
			throw new GraphException("ESeq of graph " + this.getId()
					+ " could not be reorganized in database.", exception);
		}
	}

	/**
	 * Reorganizes sequence numbers in incidence list of a vertex at database
	 * only.
	 * 
	 * @param vertex
	 *            Vertex with incidence list to reorganize.
	 * 
	 *            Precondition: Incidence list must have been reorganized in
	 *            memory just before.
	 */
	public void reorganizeIncidenceListInDatabaseOf(
			DatabasePersistableVertex vertex) {
		try {
			Edge firstIncidence = vertex.getFirstIncidence();
			long start = ((DatabasePersistableIncidence) firstIncidence)
					.getSequenceNumberInLambdaSeq();
			this.containingDatabase.reorganizeIncidenceList(vertex, start);
		} catch (GraphDatabaseException exception) {
			exception.printStackTrace();
			throw new GraphException("Incidence list of vertex "
					+ vertex.getId() + " in graph " + vertex.getGraph().getId()
					+ " could not be reorganized in database.", exception);
		}
	}

	@Override
	public void delete() {
		try {
			this.containingDatabase.delete(this);
		} catch (GraphDatabaseException exception) {
			throw new GraphException(
					"Graph could not be deleted from database.", exception);
		}
	}

	/**
	 * Writes version of graph and global sequences back to database.
	 */
	protected void writeBackVersions() {
		if (this.isPersistent()) {
			this.writeBackGraphVersion(this.getGraphVersion());
			this.writeBackEdgeListVersion();
			this.writeBackVertexListVersion();
		}
	}

	/**
	 * Writes version of graph back to database.
	 */
	private void writeBackGraphVersion(long graphVersion) {
		try {
			this.containingDatabase.updateVersionOf(this);
		} catch (GraphDatabaseException e) {
			e.printStackTrace();
		}
	}

	// --------------- copied from impl.std.GraphImpl --------------

	/**
	 * List of vertices to be deleted by a cascading delete caused by deletion
	 * of a composition "parent".
	 */
	private List<VertexBaseImpl> deleteVertexList;

	@Override
	protected FreeIndexList getFreeEdgeList() {
		return super.freeEdgeList;
	}

	@Override
	protected FreeIndexList getFreeVertexList() {
		return super.freeVertexList;
	}

	@Override
	protected void freeEdgeIndex(int index) {
		super.freeEdgeList.freeIndex(index);
	}

	@Override
	protected void freeVertexIndex(int index) {
		super.freeVertexList.freeIndex(index);
	}

	@Override
	protected int allocateEdgeIndex(int currentId) {
		int eId = freeEdgeList.allocateIndex();
		if (eId == 0) {
			expandEdgeArray(getExpandedEdgeCount());
			eId = freeEdgeList.allocateIndex();
		}
		return eId;
	}

	@Override
	protected int allocateVertexIndex(int currentId) {
		int vId = freeVertexList.allocateIndex();
		if (vId == 0) {
			expandVertexArray(getExpandedVertexCount());
			vId = freeVertexList.allocateIndex();
		}
		return vId;
	}

	@Override
	protected List<VertexBaseImpl> getDeleteVertexList() {
		return this.deleteVertexList;
	}

	@Override
	protected void setDeleteVertexList(List<VertexBaseImpl> deleteVertexList) {
		this.deleteVertexList = deleteVertexList;
	}

	@Override
	public <T> JGraLabList<T> createList() {
		return new JGraLabListImpl<T>();
	}

	@Override
	public <T> JGraLabList<T> createList(Collection<? extends T> collection) {
		return new JGraLabListImpl<T>(collection);
	}

	@Override
	public <T> JGraLabList<T> createList(int initialCapacity) {
		return new JGraLabListImpl<T>(initialCapacity);
	}

	@Override
	public <K, V> JGraLabMap<K, V> createMap() {
		return new JGraLabMapImpl<K, V>();
	}

	@Override
	public <K, V> JGraLabMap<K, V> createMap(Map<? extends K, ? extends V> map) {
		return new JGraLabMapImpl<K, V>(map);
	}

	@Override
	public <K, V> JGraLabMap<K, V> createMap(int initialCapacity) {
		return new JGraLabMapImpl<K, V>(initialCapacity);
	}

	@Override
	public <K, V> JGraLabMap<K, V> createMap(int initialCapacity,
			float loadFactor) {
		return new JGraLabMapImpl<K, V>(initialCapacity, loadFactor);
	}

	@Override
	public <T> JGraLabSet<T> createSet() {
		return new JGraLabSetImpl<T>();
	}

	@Override
	public <T> JGraLabSet<T> createSet(Collection<? extends T> collection) {
		return new JGraLabSetImpl<T>(collection);
	}

	@Override
	public <T> JGraLabSet<T> createSet(int initialCapacity) {
		return new JGraLabSetImpl<T>(initialCapacity);
	}

	@Override
	public <T> JGraLabSet<T> createSet(int initialCapacity, float loadFactor) {
		return new JGraLabSetImpl<T>(initialCapacity, loadFactor);
	}

	@Override
	public final boolean hasTransactionSupport() {
		return false;
	}

	@Override
	public <T extends Record> T createRecord(Class<T> recordClass, GraphIO io) {
		T record = graphFactory.createRecord(recordClass, this);
		try {
			record.readComponentValues(io);
		} catch (GraphIOException e) {
			e.printStackTrace();
		}
		return record;
	}

	@Override
	public <T extends Record> T createRecord(Class<T> recordClass,
			Map<String, Object> fields) {
		T record = graphFactory.createRecord(recordClass, this);
		record.setComponentValues(fields);
		return record;
	}

	@Override
	public <T extends Record> T createRecord(Class<T> recordClass,
			Object... components) {
		T record = graphFactory.createRecord(recordClass, this);
		record.setComponentValues(components);
		return record;
	}

	@Override
	public final boolean hasSavememSupport() {
		return false;
	}

	@Override
	public final boolean hasStandardSupport() {
		return false;
	}

	// --------- Unsupported operations ------------------------------------

	@Override
	public boolean isInConflict() {
		throw new UnsupportedOperationException(
				"Transactions are not supported for this graph.");
	}

	@Override
	public Transaction newReadOnlyTransaction() {
		throw new UnsupportedOperationException(
				"Creation of read-only-transactions is not supported for this graph.");
	}

	@Override
	public Transaction newTransaction() {
		throw new UnsupportedOperationException(
				"Creation of read-write-transactions is not supported for this graph.");
	}

	@Override
	public void restoreSavepoint(Savepoint savepoint)
			throws InvalidSavepointException {
		throw new UnsupportedOperationException(
				"Definition of save-points is not supported for this graph.");
	}

	@Override
	public void setCurrentTransaction(Transaction transaction) {
		throw new UnsupportedOperationException(
				"Transactions are not supported for this graph.");
	}

	@Override
	public Savepoint defineSavepoint() {
		throw new UnsupportedOperationException(
				"Definition of save-points is not supported for this graph.");
	}

	@Override
	public Transaction getCurrentTransaction() {
		throw new UnsupportedOperationException(
				"Transactions are not supported for this graph.");
	}

	@Override
	protected VertexBaseImpl[] getVertex() {
		throw new UnsupportedOperationException(
				"Operation not supported by this graph.");
	}

	@Override
	protected EdgeBaseImpl[] getEdge() {
		throw new UnsupportedOperationException(
				"Operation not supported by this graph.");
	}

	@Override
	protected ReversedEdgeBaseImpl[] getRevEdge() {
		throw new UnsupportedOperationException(
				"Operation not supported by this graph.");
	}

	@Override
	protected void setVertex(VertexBaseImpl[] vertex) {
		throw new UnsupportedOperationException(
				"Operation not supported by this graph.");
	}

	@Override
	protected void setEdge(EdgeBaseImpl[] edge) {
		throw new UnsupportedOperationException(
				"Operation not supported by this graph.");
	}

	@Override
	protected void setRevEdge(ReversedEdgeBaseImpl[] revEdge) {
		throw new UnsupportedOperationException(
				"Operation not supported by this graph.");
	}

	@Override
	public void abort() {
		throw new UnsupportedOperationException(
				"Abort is not supported for this graph.");
	}

	@Override
	public void commit() throws CommitFailedException {
		throw new UnsupportedOperationException(
				"Commit is not supported for this graph.");
	}

	@Override
	public void defragment() {
		throw new UnsupportedOperationException(
				"Defragment is not supported by this graph.");
	}

	@Override
	public void internalLoadingCompleted(int[] firstIncidence,
			int[] nextIncidence) {
		throw new UnsupportedOperationException(
				"Operation is not supported by this graph.");
	}
}
