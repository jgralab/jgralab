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

import java.lang.ref.WeakReference;
import java.util.Hashtable;

import de.uni_koblenz.jgralab.Graph;

/**
 * Cache for a graph. Only one graph per cache as cached data is more easy to
 * manage this way.
 * 
 * @author ultbreit@uni-koblenz.de
 * 
 *         TODO Put something into removed vertex and edges. TODO Add reference
 *         quees to clear unneeded weak references.
 */
public class WeakCache implements GraphCache {

	/**
	 * Contains cached primary keys of attributes mapped by their name.
	 */
	private Hashtable<String, Integer> attributePrimaryKeyCache;

	/**
	 * Contains cached primary keys of types mapped by their name.
	 */
	private Hashtable<String, Integer> typePrimaryKeyCache;

	/**
	 * Contains cached vertices mapped by their vId.
	 */
	private Hashtable<Integer, DatabasePersistableVertex> cachedVertices;

	/**
	 * Contains cached edges mapped by their eId.
	 */
	private Hashtable<Integer, DatabasePersistableEdge> cachedEdges;

	/**
	 * Contains weak references to removed vertices so they are still reachable
	 * as long as they have not been destroyed by garbage collector.
	 */
	private Hashtable<Integer, WeakReference<DatabasePersistableVertex>> removedVertices;

	/**
	 * Contains weak references to removed edges so they are still reachable as
	 * long as they have not been destroyed by garbage collector.
	 */
	private Hashtable<Integer, WeakReference<DatabasePersistableEdge>> removedEdges;

	// TODO Remove when pk of vertex has been removed from schema.
	private Hashtable<Integer, Integer> vertexPrimaryKeyCache;

	// TODO Remove when pk of vertex has been removed from schema.
	private Hashtable<Integer, Integer> edgePrimaryKeyCache;

	/**
	 * Creates and initializes a new <code>WeakCache</graph>.
	 */
	public WeakCache() {
		typePrimaryKeyCache = new Hashtable<String, Integer>();
		vertexPrimaryKeyCache = new Hashtable<Integer, Integer>();
		edgePrimaryKeyCache = new Hashtable<Integer, Integer>();
		cachedVertices = new Hashtable<Integer, DatabasePersistableVertex>();
		cachedEdges = new Hashtable<Integer, DatabasePersistableEdge>();
		removedVertices = new Hashtable<Integer, WeakReference<DatabasePersistableVertex>>();
		removedEdges = new Hashtable<Integer, WeakReference<DatabasePersistableEdge>>();
	}

	public boolean hasPrimaryKeyOfType(String qualifiedName) {
		return typePrimaryKeyCache.containsKey(qualifiedName);
	}

	public int getPrimaryKeyOfType(String qualifiedTypeName) {
		return typePrimaryKeyCache.get(qualifiedTypeName);
	}

	public void addPrimaryKeyOfType(String qualifiedTypeName, int primaryKey) {
		typePrimaryKeyCache.put(qualifiedTypeName, primaryKey);
	}

	// TODO Remove when pk of vertex has been removed from schema.
	public int getPrimaryKeyOfVertex(int vId) {
		return vertexPrimaryKeyCache.get(vId);
	}

	// TODO Remove when pk of vertex has been removed from schema.
	public void addPrimaryKeyOfVertex(int vId, int primaryKey) {
		vertexPrimaryKeyCache.put(vId, primaryKey);
	}

	// TODO Remove when pk of edge has been removed from schema.
	public int getPrimaryKeyOfEdge(int eId) {
		return edgePrimaryKeyCache.get(eId);
	}

	// TODO Remove when pk of edge has been removed from schema.
	public void addPrimaryKeyOfEdge(int eId, int primaryKey) {
		edgePrimaryKeyCache.put(eId, primaryKey);
	}

	// TODO Remove when pk of edge has been removed from schema.
	public boolean hasPrimaryKeyOfEdge(int eId) {
		return edgePrimaryKeyCache.containsKey(eId);
	}

	// TODO Remove when pk of vertex has been removed from schema.
	public boolean hasPrimaryKeyOfVertex(int vId) {
		return vertexPrimaryKeyCache.containsKey(vId);
	}

	public boolean hasPrimaryKeyOfAttribute(String attributeName) {
		return attributePrimaryKeyCache.containsKey(attributeName);
	}

	public int getPrimaryKeyOfAttribute(String attributeName) {
		return attributePrimaryKeyCache.get(attributeName);
	}

	public void addPrimaryKeyOfAttribute(String attributeName,
			int primaryKeyOfAttribute) {
		attributePrimaryKeyCache.put(attributeName, primaryKeyOfAttribute);
	}

	public void addVertex(DatabasePersistableVertex vertex) {
		cachedVertices.put(vertex.getId(), vertex);
	}

	public boolean containsVertex(int vId) {
		return cachedVertices.containsKey(vId) || isVertexWeaklyReachable(vId);
	}

	private boolean isVertexWeaklyReachable(int vId) {
		return removedVertices.containsKey(vId)
				&& (removedVertices.get(vId).get() != null);
	}

	public DatabasePersistableVertex getVertex(int vId) {
		DatabasePersistableVertex vertex = cachedVertices.get(vId);
		if (vertex == null) {
			vertex = getWeakVertex(vId);
		}
		return vertex;
	}

	private DatabasePersistableVertex getWeakVertex(int vId) {
		if (removedVertices.containsKey(vId)) {
			return removedVertices.get(vId).get();
		} else {
			return null;
		}
	}

	public void addEdge(DatabasePersistableEdge edge) {
		cachedEdges.put(edge.getId(), edge);
	}

	public boolean containsEdge(int eId) {
		return cachedEdges.containsKey(eId) || isEdgeWeaklyReachable(eId);
	}

	private boolean isEdgeWeaklyReachable(int eId) {
		return removedEdges.containsKey(eId)
				&& (removedEdges.get(eId).get() != null);
	}

	public DatabasePersistableEdge getEdge(int eId) {
		DatabasePersistableEdge edge = cachedEdges.get(eId);
		if (edge == null) {
			edge = getWeakEdge(eId);
		}
		return edge;
	}

	private DatabasePersistableEdge getWeakEdge(int eId) {
		if (removedEdges.containsKey(eId)) {
			return removedEdges.get(eId).get();
		} else {
			return null;
		}
	}

	@Override
	public boolean containsVertex(Graph graph, int vId) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public DatabasePersistableVertex getVertex(Graph graph, int vId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeVertex(DatabasePersistableGraph graph, int vId) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean containsEdge(Graph graph, int eId) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public DatabasePersistableEdge getEdge(Graph graph, int eId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeEdge(DatabasePersistableGraph graph, int eId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub

	}
}
