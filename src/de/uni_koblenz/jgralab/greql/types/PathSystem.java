/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2013 Institute for Software Technology
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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Logger;

import org.pcollections.PSet;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.schema.impl.DirectedAcyclicGraph;

public class PathSystem {

	public class PathSystemNode {
		public Vertex currentVertex;
		public Edge edge2parent;
		public int state = -1;

		PathSystemNode(Vertex currentVertex, Edge edge2parent, int state) {
			this.currentVertex = currentVertex;
			this.edge2parent = edge2parent;
			this.state = state;
		}

		PathSystemNode(Vertex currentVertex, int state) {
			this.currentVertex = currentVertex;
			this.state = state;
		}

		@Override
		public String toString() {
			return "(" + currentVertex + ", " + state + ") " + edge2parent;
		}

	}

	private final DirectedAcyclicGraph<PathSystemNode> dag;

	private final Map<Vertex, PathSystemNode> vertex2node;

	/**
	 * This is the rootvertex of the pathsystem
	 */
	private PathSystemNode root;

	/**
	 * stores if the pathsystem is finished
	 */
	private boolean finished = false;

	/**
	 * this set stores the keys of the leaves of this pathsystem. It is created
	 * the first time it is needed. So the creation (which is in O(n²) ) has to
	 * be done only once.
	 */
	private Set<PathSystemNode> leafNodes = null;

	private static Logger logger = JGraLab.getLogger(PathSystem.class);

	/**
	 * returns the rootVertex of this pathSystem
	 */
	public Vertex getRootVertex() {
		return root.currentVertex;
	}

	public PathSystemNode getRoot() {
		return root;
	}

	/**
	 * returns the hashcode of this PathSystem
	 */
	@Override
	public int hashCode() {
		assertFinished();
		return extractPaths().hashCode();
	}

	@Override
	public boolean equals(Object o) {
		assertFinished();
		if ((o == null) || !(o instanceof PathSystem)) {
			return false;
		}
		return extractPaths().equals(((PathSystem) o).extractPaths());
	}

	/*
	 * The following methods are used for the creation of a PathSystem
	 */

	/**
	 * creates a new PathSystem with the given rootVertex in the given datagraph
	 */
	public PathSystem() {
		dag = new DirectedAcyclicGraph<PathSystemNode>();
		leafNodes = new HashSet<PathSystemNode>();
		vertex2node = new HashMap<Vertex, PathSystemNode>();
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
	public PathSystemNode setRootVertex(Vertex vertex, int stateNumber,
			boolean finalState) {
		assertUnfinished();
		root = new PathSystemNode(vertex, null, stateNumber);
		dag.createNode(root);
		if (finalState) {
			assert root != null;
			leafNodes.add(root);
		}
		vertex2node.put(vertex, root);
		return root;
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
	public PathSystemNode addVertex(Vertex vertex, int stateNumber,
			boolean finalState) {
		assertUnfinished();
		PathSystemNode currentNode = new PathSystemNode(vertex, stateNumber);
		dag.createNode(currentNode);
		if (finalState) {
			assert currentNode != null;
			leafNodes.add(currentNode);
		}
		if (!vertex2node.containsKey(vertex)) {
			vertex2node.put(vertex, currentNode);
		}
		return currentNode;
	}

	public void addEdge(PathSystemNode child, PathSystemNode parent,
			Edge edge2Parent) {
		assertUnfinished();
		assert child.edge2parent == null || child.edge2parent == edge2Parent;
		child.edge2parent = edge2Parent;
		if (!dag.getDirectSuccessors(parent).contains(child)) {
			dag.createEdge(parent, child);
		}
	}

	public void addLeaf(PathSystemNode newLeaf) {
		assertUnfinished();
		assert newLeaf != null;
		if (!leafNodes.contains(newLeaf)) {
			leafNodes.add(newLeaf);
		}
	}

	public boolean isLeaf(PathSystemNode leaf) {
		return leafNodes.contains(leaf);
	}

	public Set<PathSystemNode> getParents(PathSystemNode pe) {
		return dag.getDirectPredecessors(pe);
	}

	/**
	 * finished the path system, after a call of this method, further changes
	 * are not possible
	 */
	public void finish() {
		dag.finish();
		finished = true;
	}

	/*
	 * The following methods are used to work with path systems
	 */

	/**
	 * Checks, wether the given element (vertex or edge) is part of this
	 * pathsystem
	 * 
	 * @return true, if the element is part of this system, false otherwise
	 */
	public boolean contains(GraphElement<?, ?> elem) {
		assertFinished();
		for (PathSystemNode node : vertex2node.values()) {
			if (node.edge2parent == elem) {
				return true;
			}
			if (node.currentVertex == elem) {
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
		for (PathSystemNode leafNode : leafNodes) {
			leaves = leaves.plus(leafNode.currentVertex);
		}
		return leaves;
	}

	public PSet<PathSystemNode> getChildren(PathSystemNode currentNode) {
		assertFinished();
		return dag.getDirectSuccessors(currentNode);
	}

	/**
	 * Extract the path which starts with the root vertex and ends with the
	 * given vertex from the PathSystem. If the given vertex exists more than
	 * one times in this pathsystem, the first occurrence is used. If the given
	 * vertex is not part of this pathsystem, null will be returned
	 * 
	 * @param vertex
	 * @return a Path from rootVertex to given vertex
	 */
	public Path extractPath(Vertex vertex) {
		assertFinished();
		PathSystemNode currentNode = vertex2node.get(vertex);
		if (currentNode == null || leafNodes.contains(vertex)) {
			return null;
		}
		return extractPath(currentNode);
	}

	/**
	 * Extract the path which starts with the root vertex and ends with the
	 * given vertex from the PathSystem.
	 * 
	 * @param currentNode
	 *            the pair (Vertex, Statenumber) which is the target of the path
	 * @return a Path from rootVertex to given vertex
	 */
	private Path extractPath(PathSystemNode currentNode) {
		assertFinished();
		Path path = Path.start(currentNode.currentVertex);
		while (currentNode != null) {
			if (currentNode.edge2parent != null) {
				PSet<PathSystemNode> predecessors = dag
						.getDirectPredecessors(currentNode);
				assert predecessors.size() == 1 : currentNode
						+ " has precessors: " + predecessors;
				path = path.append(currentNode.edge2parent.getReversedEdge());
				currentNode = predecessors.toArray(new PathSystemNode[1])[0];
			} else {
				// the root was found
				currentNode = null;
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
		for (PathSystemNode leaf : leafNodes) {
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
		Map<PathSystemNode, Integer> depth = new HashMap<PathSystemNode, Integer>();
		Queue<PathSystemNode> workingQueue = new LinkedList<PathSystemNode>();
		workingQueue.add(root);
		depth.put(root, 0);
		while (!workingQueue.isEmpty()) {
			PathSystemNode currentNode = workingQueue.poll();
			int currentDepth = depth.get(currentNode);
			if (currentDepth > maxdepth) {
				maxdepth = currentDepth;
			}
			for (PathSystemNode child : dag.getDirectSuccessors(currentNode)) {
				depth.put(child, currentDepth + 1);
				workingQueue.add(child);
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
		assertFinished();
		PathSystemNode node = vertex2node.get(vertex);
		if (node == null) {
			return -1;
		}
		int distance = 0;
		while (node != null) {
			if (node.edge2parent == null) {
				// the root is reached
				break;
			}
			PSet<PathSystemNode> parents = dag.getDirectPredecessors(node);
			assert parents.size() == 1;
			node = parents.toArray(new PathSystemNode[1])[0];
			distance++;
		}
		return distance;
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
		return returnSet.plusAll(vertex2node.keySet());
	}

	public PSet<Edge> getEdges() {
		assertFinished();
		PSet<Edge> resultSet = JGraLab.set();
		for (PathSystemNode node : dag.getNodesInTopologicalOrder()) {
			if (node.edge2parent != null) {
				resultSet = resultSet.plus(node.edge2parent);
			}
		}
		return resultSet;
	}
}
