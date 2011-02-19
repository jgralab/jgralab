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
 package de.uni_koblenz.jgralab.impl.db;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.Hashtable;

import de.uni_koblenz.jgralab.Graph;

/**
 * Global cache for graphs using weak references.
 * 
 * @author ultbreit@uni-koblenz.de
 */
public class SoftCache implements GraphCache {

	private Hashtable<Graph, Hashtable<Integer, SoftReference<DatabasePersistableVertex>>> verticesOfGraphs;

	private ReferenceQueue<DatabasePersistableVertex> vertexReferenceQueue;

	private Hashtable<Graph, Hashtable<Integer, SoftReference<DatabasePersistableEdge>>> edgesOfGraphs;

	private ReferenceQueue<DatabasePersistableEdge> edgeReferenceQueue;

	private int THRESHOLD = 10000;

	private int accessCounter = 0;

	/**
	 * Creates and initializes a new <code>WeakCache</graph>.
	 */
	public SoftCache() {
		verticesOfGraphs = new Hashtable<Graph, Hashtable<Integer, SoftReference<DatabasePersistableVertex>>>();
		vertexReferenceQueue = new ReferenceQueue<DatabasePersistableVertex>();
		edgesOfGraphs = new Hashtable<Graph, Hashtable<Integer, SoftReference<DatabasePersistableEdge>>>();
		edgeReferenceQueue = new ReferenceQueue<DatabasePersistableEdge>();
	}

	private void checkThreshold() {
		accessCounter++;
		if (accessCounter > THRESHOLD) {
			clearReferenceQueues();
			accessCounter = 0;
		}
	}

	@Override
	public void addVertex(DatabasePersistableVertex vertex) {
		checkThreshold();
		Hashtable<Integer, SoftReference<DatabasePersistableVertex>> vertices;
		if (verticesOfGraphs.containsKey(vertex.getGraph())) {
			vertices = verticesOfGraphs.get(vertex.getGraph());
		} else {
			vertices = new Hashtable<Integer, SoftReference<DatabasePersistableVertex>>();
			verticesOfGraphs.put(vertex.getGraph(), vertices);
		}
		vertices.put(vertex.getId(),
				new SoftReference<DatabasePersistableVertex>(vertex,
						vertexReferenceQueue));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.impl.db.GraphCache#containsVertex(de.uni_koblenz
	 * .jgralab.Graph, int)
	 */
	@Override
	public boolean containsVertex(Graph graph, int vId) {
		checkThreshold();
		if (verticesOfGraphs.containsKey(graph)) {
			Hashtable<Integer, SoftReference<DatabasePersistableVertex>> vertices = verticesOfGraphs
					.get(graph);
			return vertices.containsKey(vId) && vertices.get(vId).get() != null;
		} else {
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.impl.db.GraphCache#getVertex(de.uni_koblenz.jgralab
	 * .Graph, int)
	 */
	@Override
	public DatabasePersistableVertex getVertex(Graph graph, int vId) {
		checkThreshold();
		if (verticesOfGraphs.containsKey(graph)) {
			Hashtable<Integer, SoftReference<DatabasePersistableVertex>> vertices = verticesOfGraphs
					.get(graph);
			return vertices.get(vId).get();
		} else {
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.impl.db.GraphCache#removeVertex(de.uni_koblenz
	 * .jgralab.impl.db.DatabasePersistableGraph, int)
	 */
	@Override
	public void removeVertex(DatabasePersistableGraph graph, int vId) {
		checkThreshold();
		Hashtable<Integer, SoftReference<DatabasePersistableVertex>> vertices = verticesOfGraphs
				.get(graph);
		vertices.remove(vId);
	}

	@Override
	public void addEdge(DatabasePersistableEdge edge) {
		checkThreshold();
		Hashtable<Integer, SoftReference<DatabasePersistableEdge>> edges;
		if (edgesOfGraphs.containsKey(edge.getGraph())) {
			edges = edgesOfGraphs.get(edge.getGraph());
		} else {
			edges = new Hashtable<Integer, SoftReference<DatabasePersistableEdge>>();
			edgesOfGraphs.put(edge.getGraph(), edges);
		}
		edges.put(edge.getId(), new SoftReference<DatabasePersistableEdge>(
				edge, edgeReferenceQueue));
	}

	@Override
	public boolean containsEdge(Graph graph, int eId) {
		checkThreshold();
		if (edgesOfGraphs.containsKey(graph)) {
			Hashtable<Integer, SoftReference<DatabasePersistableEdge>> edges = edgesOfGraphs
					.get(graph);
			return edges.containsKey(Math.abs(eId))
					&& edges.get(Math.abs(eId)).get() != null;
		} else {
			return false;
		}
	}

	@Override
	public DatabasePersistableEdge getEdge(Graph graph, int eId) {
		checkThreshold();
		if (edgesOfGraphs.containsKey(graph)) {
			Hashtable<Integer, SoftReference<DatabasePersistableEdge>> edges = edgesOfGraphs
					.get(graph);
			return edges.get(eId).get();
		} else {
			return null;
		}
	}

	@Override
	public void removeEdge(DatabasePersistableGraph graph, int eId) {
		checkThreshold();
		Hashtable<Integer, SoftReference<DatabasePersistableEdge>> edges = edgesOfGraphs
				.get(graph);
		edges.remove(eId);
	}

	@Override
	public void clear() {
		edgesOfGraphs.clear();
		clearReferenceQueues();
		verticesOfGraphs.clear();
	}

	/**
	 * Clears reference queue from dead WeakReferences by polling it.
	 */
	private void clearReferenceQueues() {
		while (vertexReferenceQueue.poll() != null) {
			;
		}
		while (edgeReferenceQueue.poll() != null) {
			;
		}
	}
}
