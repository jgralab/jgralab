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

package de.uni_koblenz.jgralab.greql.types;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.logging.Logger;

import org.pcollections.PSet;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.Vertex;

public class PathSystem {

	/**
	 * This HashMap stores references from a tuple (Vertex,StateNumber) to a
	 * tuple(ParentVertex, ParentEdge, ParentStateNumber, DistanceToRoot)
	 */
	private final HashMap<PathSystemKey, PathSystemEntry> keyToEntryMap;

	/**
	 * This HashMap stores references from a vertex which is a leaf is the path
	 * system to the first occurence of this vertex as a leaf in the above
	 * HashMap<PathSystemKey, PathSystemEntry> keyToEntryMap
	 */
	private final HashMap<Vertex, PathSystemKey> leafVertexToLeafKeyMap;

	/**
	 * This HashMap stores references from a vertex in the path system to the
	 * first occurence of this vertex in the above HashMap<PathSystemKey,
	 * PathSystemEntry> keyToEntryMap
	 */
	private final HashMap<Vertex, PathSystemKey> vertexToFirstKeyMap;

	/**
	 * This is the rootvertex of the pathsystem
	 */
	private Vertex rootVertex;

	/**
	 * stores if the pathsystem is finished
	 */
	private boolean finished = false;

	/**
	 * this set stores the keys of the leaves of this pathsystem. It is created
	 * the first time it is needed. So the creation (which is in O(n²) ) has to
	 * be done only once.
	 */
	private List<PathSystemKey> leafKeys = null;

	private static Logger logger = JGraLab.getLogger(PathSystem.class);

	/**
	 * returns the rootVertex of this pathSystem
	 */
	public Vertex getRootVertex() {
		return rootVertex;
	}

	/**
	 * This is a reference to the datagraph this pathsystem is part of
	 */
	private final Graph datagraph;

	/**
	 * returns the datagraph this PathSystem is part of
	 */
	public Graph getDataGraph() {
		return datagraph;
	}

	/**
	 * returns the hashcode of this PathSystem
	 */
	@Override
	public int hashCode() {
		assertFinished();
		return keyToEntryMap.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		assertFinished();
		if ((o == null) || !(o instanceof PathSystem)) {
			return false;
		}
		return keyToEntryMap.equals(((PathSystem) o).keyToEntryMap);
	}

	/**
	 * finished the path system, after a call of this method, further changes
	 * are not possible
	 */
	public void finish() {
		completePathSystem();
		createLeafKeys();
		finished = true;
	}

	/**
	 * creates a new JValuePathSystem with the given rootVertex in the given
	 * datagraph
	 */
	public PathSystem(Graph graph) {
		datagraph = graph;
		keyToEntryMap = new HashMap<PathSystemKey, PathSystemEntry>();
		leafVertexToLeafKeyMap = new HashMap<Vertex, PathSystemKey>();
		vertexToFirstKeyMap = new HashMap<Vertex, PathSystemKey>();
	}

	private final Queue<PathSystemEntry> entriesWithoutParentEdge = new LinkedList<PathSystemEntry>();

	/**
	 * to some vertices there is a path with an vertex restriction on the end
	 * and thus the last transition in the dfa does not accept an edge - hence,
	 * the parent edge is not set. This method finds those vertices and set the
	 * edge information
	 */
	private void completePathSystem() {
		assertUnfinished();
		while (!entriesWithoutParentEdge.isEmpty()) {
			PathSystemEntry te = entriesWithoutParentEdge.poll();
			PathSystemEntry pe = null;
			if (te.getParentVertex() != null) {
				pe = keyToEntryMap.get(new PathSystemKey(te.getParentVertex(),
						te.getParentStateNumber()));
			} else {
				PathSystemKey key = new PathSystemKey(rootVertex,
						te.getParentStateNumber());
				pe = keyToEntryMap.get(key);
			}
			// if pe is null, te is the entry of the root vertex
			if (pe != null) {
				te.setParentEdge(pe.getParentEdge());
				te.setDistanceToRoot(pe.getDistanceToRoot());
				te.setParentStateNumber(pe.getParentStateNumber());
				te.setParentVertex(pe.getParentVertex());
			}
		}
	}

	/**
	 * adds a vertex of the PathSystem which is described by the parameters to
	 * the PathSystem
	 * 
	 * @param vertex
	 *            the vertex to add
	 * @param stateNumber
	 *            the number of the DFAState the DFA was in when this vertex was
	 *            visited
	 * @param finalState
	 *            true if the rootvertex is visited by the dfa in a final state
	 */
	public void setRootVertex(Vertex vertex, int stateNumber, boolean finalState) {
		assertUnfinished();
		PathSystemKey key = new PathSystemKey(vertex, stateNumber);
		PathSystemEntry entry = new PathSystemEntry(null, null, -1, 0,
				finalState);
		keyToEntryMap.put(key, entry);
		if (finalState && !leafVertexToLeafKeyMap.containsKey(vertex)) {
			leafVertexToLeafKeyMap.put(vertex, key);
		}
		vertexToFirstKeyMap.put(vertex, key);
		leafKeys = null;
		rootVertex = vertex;
	}

	/**
	 * adds a vertex of the PathSystem which is described by the parameters to
	 * the PathSystem
	 * 
	 * @param vertex
	 *            the vertex to add
	 * @param stateNumber
	 *            the number of the DFAState the DFA was in when this vertex was
	 *            visited
	 * @param parentEdge
	 *            the edge which leads from vertex to parentVertex
	 * @param parentVertex
	 *            the parentVertex of the vertex in the PathSystem
	 * @param parentStateNumber
	 *            the number of the DFAState the DFA was in when the
	 *            parentVertex was visited
	 * @param distance
	 *            the distance to the rootvertex of the PathSystem
	 */
	public void addVertex(Vertex vertex, int stateNumber, Edge parentEdge,
			Vertex parentVertex, int parentStateNumber, int distance,
			boolean finalState) {
		assertUnfinished();
		PathSystemKey key = new PathSystemKey(vertex, stateNumber);
		PathSystemEntry entry = keyToEntryMap.get(key);
		if ((entry == null)
				|| ((entry.getDistanceToRoot() > distance) && (!entry
						.getStateIsFinal() || finalState))) {
			entry = new PathSystemEntry(parentVertex, parentEdge,
					parentStateNumber, distance, finalState);
			keyToEntryMap.put(key, entry);
			// add vertex to leaves
			if (finalState) {
				PathSystemKey existingLeafkey = leafVertexToLeafKeyMap
						.get(vertex);
				if ((existingLeafkey == null)
						|| (keyToEntryMap.get(existingLeafkey)
								.getDistanceToRoot() > distance)) {
					leafVertexToLeafKeyMap.put(vertex, key);
				}
			}
			if (parentEdge != null) {
				PathSystemKey firstKey = vertexToFirstKeyMap.get(vertex);
				if ((firstKey == null)
						|| (keyToEntryMap.get(firstKey).getDistanceToRoot() > distance)) {
					vertexToFirstKeyMap.put(vertex, key);
				}
			} else {
				if (!((vertex == rootVertex) && (distance == 0))) {
					entriesWithoutParentEdge.add(entry);
				}
			}
		}
	}

	/**
	 * Checks, wether the given element (vertex or edge) is part of this
	 * pathsystem
	 * 
	 * @return true, if the element is part of this system, false otherwise
	 */
	public boolean contains(GraphElement<?, ?> elem) {
		assertFinished();
		for (Map.Entry<PathSystemKey, PathSystemEntry> entry : keyToEntryMap
				.entrySet()) {
			if (entry.getValue().getParentEdge() == elem) {
				return true;
			}
			if (entry.getKey().getVertex() == elem) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Calculates the set of leaves in this PathSystem. Costs: O(n) where n is
	 * the number of vertices in the path system. The created set is stored as
	 * private field <code>leaves</code>, so the creating has to be done only
	 * once.
	 */
	public PSet<Vertex> getLeaves() {
		assertFinished();

		PSet<Vertex> leaves = JGraLab.set();
		// create the set of leaves out of the key set
		for (PathSystemKey key : leafKeys) {
			leaves = leaves.plus(key.getVertex());
		}
		return leaves;
	}

	/**
	 * create the set of leave keys
	 */
	private void createLeafKeys() {
		assertUnfinished();

		if (leafKeys != null) {
			return;
		}
		leafKeys = new LinkedList<PathSystemKey>();
		for (Map.Entry<PathSystemKey, PathSystemEntry> entry : keyToEntryMap
				.entrySet()) {
			if (entry.getValue().getStateIsFinal()) {
				leafKeys.add(entry.getKey());
			}
		}
	}

	/**
	 * Extract the path which starts with the root vertex and ends with the
	 * given vertex from the PathSystem. If the given vertex exists more than
	 * one times in this pathsystem, the first occurrence if used. If the given
	 * vertex is not part of this pathsystem, null will be returned
	 * 
	 * @param vertex
	 * @return a Path from rootVertex to given vertex
	 */
	public Path extractPath(Vertex vertex) {
		assertFinished();
		PathSystemKey key = leafVertexToLeafKeyMap.get(vertex);
		if (key == null) {
			return null;
		}
		return extractPath(key);
	}

	/**
	 * Extract the path which starts with the root vertex and ends with the
	 * given vertex from the PathSystem.
	 * 
	 * @param key
	 *            the pair (Vertex, Statenumber) which is the target of the path
	 * @return a Path from rootVertex to given vertex
	 */
	private Path extractPath(PathSystemKey key) {
		assertFinished();
		Path path = Path.start(key.getVertex());
		while (key != null) {
			PathSystemEntry entry = keyToEntryMap.get(key);
			if (entry.getParentEdge() != null) {
				path = path.append(entry.getParentEdge().getReversedEdge());
				key = new PathSystemKey(entry.getParentVertex(),
						entry.getParentStateNumber());
			} else {
				key = null;
			}
		}
		return path.reverse();
	}

	/**
	 * Extract the set of paths which are part of this path system. These paths
	 * start with the root vertex and ends with a leave.
	 * 
	 * @return a set of Paths from rootVertex to leaves
	 */
	public PSet<Path> extractPaths() {
		assertFinished();
		PSet<Path> pathSet = JGraLab.set();
		for (PathSystemKey leaf : leafKeys) {
			pathSet = pathSet.plus(extractPath(leaf));
		}
		return pathSet;
	}

	/**
	 * calculates the depth of this pathtree
	 */
	public int getDepth() {
		assertFinished();
		int maxdepth = 0;
		for (Map.Entry<PathSystemKey, PathSystemEntry> entry : keyToEntryMap
				.entrySet()) {
			PathSystemEntry thisEntry = entry.getValue();
			if (thisEntry.getDistanceToRoot() > maxdepth) {
				maxdepth = thisEntry.getDistanceToRoot();
			}
		}
		return maxdepth;
	}

	/**
	 * Calculates the distance between the root vertex of this path system and
	 * the given vertex If the given vertices is part of the pathsystem more
	 * than one times, the first occurence is used
	 * 
	 * @return the distance or -1 if the given vertex is not part of this path
	 *         system
	 */
	public int distance(Vertex vertex) {
		PathSystemKey key = vertexToFirstKeyMap.get(vertex);
		return distance(key);
	}

	/**
	 * Calculates the distance between the root vertex of this path system and
	 * the given key
	 * 
	 * @return the distance or -1 if the given vertex is not part of this path
	 *         system.
	 */
	private int distance(PathSystemKey key) {
		assertFinished();

		if (key == null) {
			return -1;
		}
		PathSystemEntry entry = keyToEntryMap.get(key);
		return entry.getDistanceToRoot();
	}

	/**
	 * Prints this pathsystem as ascii-art
	 */
	public void printAscii() {
		assertFinished();
		PSet<Path> pathSet = extractPaths();
		for (Path path : pathSet) {
			logger.info(path.toString());
		}
	}

	/**
	 * returns a string representation of this path system
	 */
	@Override
	public String toString() {
		StringBuilder returnString = new StringBuilder("PathSystem:\n");
		PSet<Path> pathSet = extractPaths();
		for (Path path : pathSet) {
			returnString.append(path.toString());
			returnString.append('\n');
		}
		return returnString.toString();
	}

	/**
	 * prints the <key, entry> map
	 */
	public void printEntryMap() {
		assertFinished();
		logger.info("<Key, Entry> Set of PathSystem is:");
		for (Map.Entry<PathSystemKey, PathSystemEntry> entry : keyToEntryMap
				.entrySet()) {
			PathSystemEntry thisEntry = entry.getValue();
			PathSystemKey thisKey = entry.getKey();
			logger.info(thisKey.toString() + " maps to " + thisEntry.toString());
		}
	}

	/**
	 * prints the <vertex, key map
	 */
	public void printKeyMap() {
		assertFinished();
		logger.info("<Vertex, FirstKey> Set of PathSystem is:");
		for (Map.Entry<Vertex, PathSystemKey> entry : vertexToFirstKeyMap
				.entrySet()) {
			PathSystemKey thisKey = entry.getValue();
			Vertex vertex = entry.getKey();
			logger.info(vertex + " maps to " + thisKey.toString());
		}
	}

	private void assertUnfinished() {
		if (finished) {
			throw new IllegalStateException(
					"Cannot modify a finished path system");
		}
	}

	private void assertFinished() {
		if (!finished) {
			throw new IllegalStateException(
					"Path System needs to be finished before it can be used. Use PathSystem.finish()");
		}
	}

	public PSet<Vertex> getVertices() {
		assertFinished();
		PSet<Vertex> returnSet = JGraLab.set();
		for (PathSystemKey key : keyToEntryMap.keySet()) {
			returnSet = returnSet.plus(key.getVertex());
		}
		return returnSet;
	}

	public PSet<Edge> getEdges() {
		assertFinished();
		PSet<Edge> resultSet = JGraLab.set();
		for (Map.Entry<PathSystemKey, PathSystemEntry> mapEntry : keyToEntryMap
				.entrySet()) {
			PathSystemEntry thisEntry = mapEntry.getValue();
			if (thisEntry.getParentEdge() != null) {
				resultSet = resultSet.plus(thisEntry.getParentEdge());
			}
		}
		return resultSet;
	}
}
