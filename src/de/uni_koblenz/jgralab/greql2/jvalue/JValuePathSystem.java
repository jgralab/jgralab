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

package de.uni_koblenz.jgralab.greql2.jvalue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.logging.Logger;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.exception.JValueInvalidTypeException;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;

public class JValuePathSystem extends JValueImpl {

	private static Logger logger = Logger.getLogger(JValuePathSystem.class
			.getName());

	/**
	 * This HashMap stores references from a tuple (Vertex,State) to a
	 * tuple(ParentVertex, ParentEdge, ParentState, DistanceToRoot)
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
	 * this set stores the keys of the leaves of this pathsystem. It is created
	 * the first time it is needed. So the creation (which is in O(n²) ) has to
	 * be done only once.
	 */
	private ArrayList<PathSystemKey> leafKeys = null;

	/**
	 * returns the rootVertex of this pathSystem
	 */
	public JValue getRootVertex() {
		return new JValueImpl(rootVertex);
	}

	/**
	 * This is a reference to the datagraph this pathsystem is part of
	 */
	private final Graph datagraph;

	/**
	 * stores the hashcode of this pathsystem so it must be calculated only if
	 * the pathsystem changes
	 */
	private int hashvalue = 0;

	/**
	 * returns the datagraph this PathSystem is part of
	 */
	public Graph getDataGraph() {
		return datagraph;
	}

	private boolean isCleared = true;

	/**
	 * returns a JValuePathSystem-Reference to this JValue object
	 */
	@Override
	public JValuePathSystem toPathSystem() throws JValueInvalidTypeException {
		return this;
	}

	/**
	 * returns the hashcode of this PathSystem
	 */
	@Override
	public int hashCode() {
		if (hashvalue == 0) {
			Iterator<Map.Entry<PathSystemKey, PathSystemEntry>> iter = keyToEntryMap
					.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry<PathSystemKey, PathSystemEntry> mapEntry = iter
						.next();
				PathSystemEntry thisEntry = mapEntry.getValue();
				PathSystemKey key = mapEntry.getKey();
				hashvalue += key.hashCode() * 11 + thisEntry.hashCode() * 7;
			}
			if (hashvalue < 0) {
				hashvalue = -hashvalue;
			}
		}
		return hashvalue;
	}

	/**
	 * creates a new JValuePathSystem with the given rootVertex in the given
	 * datagraph
	 */
	public JValuePathSystem(Graph graph) {
		datagraph = graph;
		keyToEntryMap = new HashMap<PathSystemKey, PathSystemEntry>();
		leafVertexToLeafKeyMap = new HashMap<Vertex, PathSystemKey>();
		vertexToFirstKeyMap = new HashMap<Vertex, PathSystemKey>();
		type = JValueType.PATHSYSTEM;
	}

	private final Queue<PathSystemEntry> entriesWithoutParentEdge = new LinkedList<PathSystemEntry>();

	public void clearPathSystem() {
		if (!isCleared) {
			while (!entriesWithoutParentEdge.isEmpty()) {
				PathSystemEntry te = entriesWithoutParentEdge.poll();
				PathSystemEntry pe = null;
				if (te.getParentVertex() != null) {
					pe = keyToEntryMap.get(new PathSystemKey(te
							.getParentVertex(), te.getParentStateNumber()));
				} else {
					pe = keyToEntryMap.get(new PathSystemKey(rootVertex, te
							.getParentStateNumber()));
				}
				te.setParentEdge(pe.getParentEdge());
				te.setDistanceToRoot(pe.getDistanceToRoot());
				te.setParentStateNumber(pe.getParentStateNumber());
				te.setParentVertex(pe.getParentVertex());
				if (te.getParentEdge() == null) {
					entriesWithoutParentEdge.add(te);
				}
			}
			isCleared = true;
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
		PathSystemKey key = new PathSystemKey(vertex, stateNumber);
		PathSystemEntry entry = new PathSystemEntry(null, null, -1, 0,
				finalState);
		keyToEntryMap.put(key, entry);
		if (finalState && !leafVertexToLeafKeyMap.containsKey(vertex)) {
			leafVertexToLeafKeyMap.put(vertex, key);
		}
		vertexToFirstKeyMap.put(vertex, key);
		leafKeys = null;
		hashvalue = 0;
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
		PathSystemKey key = new PathSystemKey(vertex, stateNumber);
		if (!keyToEntryMap.containsKey(key)) {
			PathSystemEntry entry = new PathSystemEntry(parentVertex,
					parentEdge, parentStateNumber, distance, finalState);
			keyToEntryMap.put(key, entry);
			if (finalState && !leafVertexToLeafKeyMap.containsKey(vertex)) {
				leafVertexToLeafKeyMap.put(vertex, key);
				leafKeys = null;
			}
			if (parentEdge != null) {
				vertexToFirstKeyMap.put(vertex, key);
			} else {
				entriesWithoutParentEdge.add(entry);
				isCleared = false;
			}
		}
	}

	/**
	 * Calculates the set of children the given vertex has in this PathSystem.
	 * If the given vertex exists more than one times in this slice, the first
	 * occurrence if used.
	 */
	public JValueSet children(Vertex vertex) {
		clearPathSystem();
		PathSystemKey key = vertexToFirstKeyMap.get(vertex);
		return children(key);
	}

	/**
	 * Calculates the set of child the given key has in this PathSystem
	 */
	public JValueSet children(PathSystemKey key) {
		clearPathSystem();
		JValueSet returnSet = new JValueSet();
		Iterator<Map.Entry<PathSystemKey, PathSystemEntry>> iter = keyToEntryMap
				.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<PathSystemKey, PathSystemEntry> mapEntry = iter.next();
			PathSystemEntry thisEntry = mapEntry.getValue();
			if ((thisEntry.getParentVertex() == key.getVertex())
					&& (thisEntry.getParentStateNumber() == key
							.getStateNumber())) {
				Vertex v = mapEntry.getKey().getVertex();
				returnSet.add(new JValueImpl(v, v));
			}
		}
		return returnSet;
	}

	/**
	 * Calculates the set of siblings of the given vertex in this PathSystem. If
	 * the given vertex exists more than one times in this pathsystem, the first
	 * occurence if used.
	 */
	public JValueSet siblings(Vertex vertex) {
		clearPathSystem();
		PathSystemKey key = vertexToFirstKeyMap.get(vertex);
		return siblings(key);
	}

	/**
	 * Calculates the set of children the given key has in this PathSystem
	 */
	public JValueSet siblings(PathSystemKey key) {
		clearPathSystem();
		PathSystemEntry entry = keyToEntryMap.get(key);

		if (entry == null) {
			return null;
		}

		JValueSet returnSet = new JValueSet();

		for (Map.Entry<PathSystemKey, PathSystemEntry> mapEntry : keyToEntryMap
				.entrySet()) {
			PathSystemEntry value = mapEntry.getValue();

			if ((value.getParentVertex() == entry.getParentVertex())
					&& (value.getParentStateNumber() == entry
							.getParentStateNumber())
					&& (mapEntry.getKey().getVertex() != key.getVertex())) {
				Vertex v = mapEntry.getKey().getVertex();
				returnSet.add(new JValueImpl(v, v));
			}
		}
		return returnSet;
	}

	/**
	 * Calculates the parent vertex of the given vertex in this PathSystem. If
	 * the given vertex exists more than one times in this pathsystem, the first
	 * occurrence if used. If the given vertex is not part of this pathsystem, a
	 * invalid JValue will be returned
	 */
	public JValueImpl parent(Vertex vertex) {
		clearPathSystem();
		PathSystemKey key = vertexToFirstKeyMap.get(vertex);
		return parent(key);
	}

	/**
	 * Calculates the parent vertex of the given key in this PathSystem.
	 */
	public JValueImpl parent(PathSystemKey key) {
		clearPathSystem();
		if (key == null) {
			return new JValueImpl();
		}
		PathSystemEntry entry = keyToEntryMap.get(key);
		return new JValueImpl(entry.getParentVertex(), entry.getParentVertex());
	}

	/**
	 * Calculates the set of types this pathsystem contains
	 */
	public JValueSet types() {
		clearPathSystem();
		JValueSet returnSet = new JValueSet();
		Iterator<Map.Entry<PathSystemKey, PathSystemEntry>> iter = keyToEntryMap
				.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<PathSystemKey, PathSystemEntry> entry = iter.next();
			returnSet.add(new JValueImpl(entry.getKey().getVertex()
					.getAttributedElementClass(), entry.getKey().getVertex()));
			Edge e = entry.getValue().getParentEdge();
			if (e != null) {
				returnSet.add(new JValueImpl(e.getAttributedElementClass(), e));
			}
		}
		return returnSet;
	}

	/**
	 * Calculates the set of vertextypes this pathsystem contains
	 */
	public JValueSet vertexTypes() {
		clearPathSystem();
		JValueSet returnSet = new JValueSet();
		Iterator<Map.Entry<PathSystemKey, PathSystemEntry>> iter = keyToEntryMap
				.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<PathSystemKey, PathSystemEntry> entry = iter.next();
			returnSet.add(new JValueImpl(entry.getKey().getVertex()
					.getAttributedElementClass(), entry.getKey().getVertex()));
		}
		return returnSet;
	}

	/**
	 * Calculates the set of edgetypes this pathsystem contains
	 */
	public JValueSet edgeTypes() {
		clearPathSystem();
		JValueSet returnSet = new JValueSet();
		Iterator<Map.Entry<PathSystemKey, PathSystemEntry>> iter = keyToEntryMap
				.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<PathSystemKey, PathSystemEntry> entry = iter.next();
			Edge e = entry.getValue().getParentEdge();
			if (e != null) {
				returnSet.add(new JValueImpl(e.getAttributedElementClass(), e));
			}
		}
		return returnSet;
	}

	/**
	 * Checks, wether the given element (vertex or edge) is part of this
	 * pathsystem
	 * 
	 * @return true, if the element is part of this system, false otherwise
	 */
	public boolean contains(GraphElement elem) {
		clearPathSystem();
		Iterator<Map.Entry<PathSystemKey, PathSystemEntry>> iter = keyToEntryMap
				.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<PathSystemKey, PathSystemEntry> entry = iter.next();
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
	 * Checks, wether the pathsystem contains an element which has the given
	 * type
	 * 
	 * @return true, if the element is part of this system, false otherwise
	 */
	public boolean contains(AttributedElementClass type) {
		clearPathSystem();
		Iterator<Map.Entry<PathSystemKey, PathSystemEntry>> iter = keyToEntryMap
				.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<PathSystemKey, PathSystemEntry> entry = iter.next();
			if (entry.getValue().getParentEdge().getAttributedElementClass() == type) {
				return true;
			}
			// TODO: Don't we need to check parentVertex, too?? Or is that
			// the key?
			if (entry.getKey().getVertex().getAttributedElementClass() == type) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Calculates the number of incomming or outgoing edges of the given vertex
	 * which are part of this PathSystem
	 * 
	 * @param vertex
	 *            the vertex for which the number of edges gets counted
	 * @param orientation
	 *            if set to true, the incomming edges will be counted,
	 *            otherwise, the outgoing ones will be counted
	 * @param typeCol
	 *            the JValueTypeCollection which toggles wether a type is
	 *            accepted or not
	 * @return the number of edges with the given orientation connected to the
	 *         given vertex or -1 if the given vertex is not part of this
	 *         pathsystem
	 */
	public int degree(Vertex vertex, EdgeDirection direction,
			JValueTypeCollection typeCol) {
		clearPathSystem();
		if (vertex == null) {
			return -1;
		}
		int degree = 0;
		switch (direction) {
		case IN:
			for (Map.Entry<PathSystemKey, PathSystemEntry> entry : keyToEntryMap
					.entrySet()) {
				if ((entry.getKey().getVertex() == vertex)
						&& ((typeCol == null) || (typeCol.acceptsType(entry
								.getValue().getParentEdge()
								.getAttributedElementClass())))) {
					degree++;
				}
			}
			break;
		case OUT:
			for (Map.Entry<PathSystemKey, PathSystemEntry> entry : keyToEntryMap
					.entrySet()) {
				if ((entry.getValue().getParentVertex() == vertex)
						&& ((typeCol == null) || (typeCol.acceptsType(entry
								.getValue().getParentEdge()
								.getAttributedElementClass())))) {
					degree++;
				}
			}
			break;
		case INOUT:
			for (Map.Entry<PathSystemKey, PathSystemEntry> entry : keyToEntryMap
					.entrySet()) {
				PathSystemEntry pe = entry.getValue();
				if ((typeCol == null)
						|| typeCol.acceptsType(pe.getParentEdge()
								.getAttributedElementClass())) {
					if (pe.getParentVertex() == vertex) {
						degree++;
					}
					// cannot transform two if statements to one if with an or,
					// because an edge may be a loop
					if (entry.getKey().getVertex() == vertex) {
						degree++;
					}
				}
			}
			break;
		default:
			throw new JValuePathException(
					"Incomplete switch statement in JValuePathSystem");
		}
		return degree;
	}

	/**
	 * Calculates the number of incomming and outgoing edges of the given vertex
	 * which are part of this PathSystem
	 * 
	 * @param vertex
	 *            the vertex for which the number of edges gets counted
	 * @param typeCol
	 *            the JValueTypeCollection which toggles wether a type is
	 *            accepted or not
	 * @return the number of edges connected to the given vertex or -1 if the
	 *         given vertex is not part of this pathsystem
	 */
	public int degree(Vertex vertex, JValueTypeCollection typeCol) {
		clearPathSystem();
		if (vertex == null) {
			return -1;
		}
		int degree = 0;
		for (Map.Entry<PathSystemKey, PathSystemEntry> entry : keyToEntryMap
				.entrySet()) {
			PathSystemEntry pe = entry.getValue();
			if ((typeCol == null)
					|| typeCol.acceptsType(pe.getParentEdge()
							.getAttributedElementClass())) {
				if (pe.getParentVertex() == vertex) {
					degree++;
				}
				// cannot transform two if statements to one if with an or,
				// because an edge may be a loop
				if (entry.getKey().getVertex() == vertex) {
					degree++;
				}
			}
		}
		return degree;
	}

	/**
	 * Calculates the set of incomming or outgoing edges of the given vertex,
	 * which are also part of this pathsystem
	 * 
	 * @param vertex
	 *            the vertex for which the edgeset will be created
	 * @param orientation
	 *            if set to true, the set of incomming edges will, be created,
	 *            otherwise, the set of outgoing ones will be created
	 * @return a set of edges with the given orientation connected to the given
	 *         vertex or an empty set, if the vertex is not part of this
	 *         pathsystem
	 */
	public JValueSet edgesConnected(Vertex vertex, EdgeDirection direction) {
		clearPathSystem();
		JValueSet resultSet = new JValueSet();
		if (vertex == null) {
			return resultSet;
		}
		for (Map.Entry<PathSystemKey, PathSystemEntry> entry : keyToEntryMap
				.entrySet()) {
			if (entry.getKey().getVertex() == vertex) {
				Edge edge = entry.getValue().getParentEdge();
				if (edge == null) {
					continue;
				}
				switch (direction) {
				case IN:
					if (edge.isNormal()) {
						resultSet.add(new JValueImpl(edge));
					}
					break;
				case OUT:
					if (!edge.isNormal()) {
						resultSet.add(new JValueImpl(edge));
					}
					break;
				case INOUT:
					resultSet.add(new JValueImpl(edge));
					break;
				default:
					throw new JValuePathException(
							"Incomplete switch statement in JValuePathSystem");
				}
			}
			if (entry.getValue().getParentVertex() == vertex) {
				Edge edge = entry.getValue().getParentEdge();
				if (edge == null) {
					continue;
				}
				switch (direction) {
				case IN:
					if (!edge.isNormal()) {
						resultSet.add(new JValueImpl(edge));
					}
					break;
				case OUT:
					if (edge.isNormal()) {
						resultSet.add(new JValueImpl(edge));
					}
					break;
				case INOUT:
					resultSet.add(new JValueImpl(edge));
					break;
				default:
					throw new JValuePathException(
							"Incomplete switch statement in JValuePathSystem");
				}
			}
		}
		return resultSet;
	}

	/**
	 * Calculates the set of edges which are connected to the given vertex, and
	 * which are also part of this pathsystem
	 * 
	 * @param vertex
	 *            the vertex for which the edgeset will be created
	 * @return a set of edges connected to the given vertex or an empty set, if
	 *         the vertex is not part of this pathsystem
	 */
	public JValueSet edgesConnected(Vertex vertex) {
		clearPathSystem();
		JValueSet resultSet = new JValueSet();
		if (vertex == null) {
			return resultSet;
		}
		Iterator<Map.Entry<PathSystemKey, PathSystemEntry>> iter = keyToEntryMap
				.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<PathSystemKey, PathSystemEntry> entry = iter.next();
			if (entry.getValue().getParentVertex() == vertex) {
				resultSet.add(new JValueImpl(entry.getValue().getParentEdge()));
			}
			if (entry.getKey().getVertex() == vertex) {
				resultSet.add(new JValueImpl(entry.getValue().getParentEdge()));
			}
		}
		return resultSet;
	}

	/**
	 * Calculates the set of edges nodes in this PathSystem.
	 */
	public JValueSet edges() {
		clearPathSystem();
		JValueSet resultSet = new JValueSet();
		for (Map.Entry<PathSystemKey, PathSystemEntry> mapEntry : keyToEntryMap
				.entrySet()) {
			PathSystemEntry thisEntry = mapEntry.getValue();
			if (thisEntry.getParentEdge() != null) {
				resultSet.add(new JValueImpl(thisEntry.getParentEdge()));
			}
		}
		return resultSet;
	}

	/**
	 * Calculates the set of nodes which are part of this pathsystem.
	 */
	public JValueSet nodes() {
		clearPathSystem();
		JValueSet returnSet = new JValueSet();
		Iterator<PathSystemKey> iter = keyToEntryMap.keySet().iterator();
		while (iter.hasNext()) {
			returnSet.add(new JValueImpl(iter.next().getVertex()));
		}
		return returnSet;
	}

	/**
	 * Calculates the set of inner nodes in this PathSystem. Inner nodes are
	 * these nodes, which are neither root nor leave Costs: O(n) where n is the
	 * number of vertices in the path system
	 */
	public JValueSet innerNodes() {
		clearPathSystem();
		JValueSet resultSet = new JValueSet();
		Iterator<Map.Entry<PathSystemKey, PathSystemEntry>> iter = keyToEntryMap
				.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<PathSystemKey, PathSystemEntry> entry = iter.next();
			if ((!entry.getValue().isStateIsFinal())
					&& (entry.getValue().getParentVertex() != null)) {
				resultSet.add(new JValueImpl(entry.getKey().getVertex()));
			}
		}
		return resultSet;
	}

	/**
	 * Calculates the set of leaves in this PathSystem. Costs: O(n²) where n is
	 * the number of vertices in the path system. The created set is stored as
	 * private field <code>leaves</code>, so the creating has to be done only
	 * once.
	 */
	public JValueSet leaves() {
		clearPathSystem();
		JValueSet leaves = new JValueSet();
		if (leafKeys != null) {
			// create the set of leaves out of the key set
			Iterator<PathSystemKey> iter = leafKeys.iterator();
			while (iter.hasNext()) {
				leaves.add(new JValueImpl(iter.next().getVertex()));
			}
			return leaves;
		} else {
			leafKeys = new ArrayList<PathSystemKey>();
			Iterator<Map.Entry<PathSystemKey, PathSystemEntry>> iter = keyToEntryMap
					.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry<PathSystemKey, PathSystemEntry> entry = iter.next();
				if (entry.getValue().isStateIsFinal()) {
					leafKeys.add(entry.getKey());
					leaves.add(new JValueImpl(entry.getKey().getVertex()));
				}
			}
			return leaves;
		}
	}

	/**
	 * create the set of leave keys
	 */
	private void createLeafKeys() {
		clearPathSystem();
		if (leafKeys != null) {
			return;
		}
		leafKeys = new ArrayList<PathSystemKey>();
		Iterator<Map.Entry<PathSystemKey, PathSystemEntry>> iter = keyToEntryMap
				.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<PathSystemKey, PathSystemEntry> entry = iter.next();
			if (entry.getValue().isStateIsFinal()) {
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
	public JValuePath extractPath(Vertex vertex) throws JValuePathException {
		clearPathSystem();
		PathSystemKey key = leafVertexToLeafKeyMap.get(vertex);
		if (key == null) {
			return new JValuePath((Vertex) null);
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
	public JValuePath extractPath(PathSystemKey key) throws JValuePathException {
		clearPathSystem();
		JValuePath path = new JValuePath(key.getVertex());
		while (key != null) {
			PathSystemEntry entry = keyToEntryMap.get(key);
			if (entry.getParentEdge() != null) {
				path.addEdge(entry.getParentEdge().getReversedEdge());
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
	public JValueSet extractPaths() throws JValuePathException {
		clearPathSystem();
		JValueSet pathSet = new JValueSet();
		if (leafKeys == null) {
			createLeafKeys();
		}
		Iterator<PathSystemKey> iter = leafKeys.iterator();
		while (iter.hasNext()) {
			JValuePath path = extractPath(iter.next());
			pathSet.add(path);
		}
		return pathSet;
	}

	/**
	 * Extracts all paths which length equal to <code>len</code>
	 * 
	 * @return a set of Paths from rootVertex to leaves
	 */
	public JValueSet extractPaths(int len) throws JValuePathException {
		clearPathSystem();
		JValueSet pathSet = new JValueSet();
		if (leafKeys == null) {
			createLeafKeys();
		}
		Iterator<PathSystemKey> iter = leafKeys.iterator();
		while (iter.hasNext()) {
			JValuePath path = extractPath(iter.next());
			if (path.pathLength() == len) {
				pathSet.add(path);
			}
		}
		return pathSet;
	}

	/**
	 * calculate the number of vertices this pathsystem has. If a vertex is part
	 * of this PathSystem n times, it is counted n times
	 */
	public int weight() {
		clearPathSystem();
		return keyToEntryMap.size();
	}

	/**
	 * calculates the depth of this pathtree
	 */
	public int depth() {
		clearPathSystem();
		int maxdepth = 0;
		Iterator<Map.Entry<PathSystemKey, PathSystemEntry>> iter = keyToEntryMap
				.entrySet().iterator();
		while (iter.hasNext()) {
			PathSystemEntry thisEntry = iter.next().getValue();
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
		clearPathSystem();
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
	public int distance(PathSystemKey key) {
		clearPathSystem();
		if (key == null) {
			return -1;
		}
		PathSystemEntry entry = keyToEntryMap.get(key);
		return entry.getDistanceToRoot();
	}

	/**
	 * Calculates the shortest distance between a leaf of this pathsystem and
	 * the root vertex, this is the length of the shortest path in this
	 * pathsystem
	 */
	public int minPathLength() {
		clearPathSystem();
		if (leafKeys == null) {
			createLeafKeys();
		}
		int minDistance = Integer.MAX_VALUE;
		Iterator<PathSystemKey> iter = leafKeys.iterator();
		while (iter.hasNext()) {
			PathSystemEntry entry = keyToEntryMap.get(iter.next());
			if (entry.getDistanceToRoot() < minDistance) {
				minDistance = entry.getDistanceToRoot();
			}
		}
		return minDistance;
	}

	/**
	 * Calculates the longest distance between a leaf of this pathsystem and the
	 * root vertex, this is the length of the longest path in this pathsystem
	 */
	public int maxPathLength() {
		clearPathSystem();
		if (leafKeys == null) {
			createLeafKeys();
		}
		int maxDistance = 0;
		Iterator<PathSystemKey> iter = leafKeys.iterator();
		while (iter.hasNext()) {
			PathSystemEntry entry = keyToEntryMap.get(iter.next());
			if (entry.getDistanceToRoot() > maxDistance) {
				maxDistance = entry.getDistanceToRoot();
			}
		}
		return maxDistance;
	}

	/**
	 * @return true if the given first vertex is a neighbour of the given second
	 *         vertex, that means, if there is a edge in the pathtree from v1 to
	 *         v2. If one or both of the given vertices are part of the
	 *         pathsystem more than one times, the first occurence is used. If
	 *         one of the vertices is not part of this pathsystem, false is
	 *         returned
	 */
	public boolean isNeighbour(Vertex v1, Vertex v2) {
		clearPathSystem();
		PathSystemKey key1 = vertexToFirstKeyMap.get(v1);
		PathSystemKey key2 = vertexToFirstKeyMap.get(v2);
		return isNeighbour(key1, key2);
	}

	/**
	 * @return true if the given first key is a neighbour of the given second
	 *         key, that means, if there is a edge in the pathtree from
	 *         key1.vertex to key2.vertex and the states matches. If one of the
	 *         keys is not part of this pathsystem, false is returned
	 */
	public boolean isNeighbour(PathSystemKey key1, PathSystemKey key2) {
		clearPathSystem();
		if ((key1 == null) || (key2 == null)) {
			return false;
		}
		PathSystemEntry entry1 = keyToEntryMap.get(key1);
		PathSystemEntry entry2 = keyToEntryMap.get(key2);
		if ((entry1.getParentVertex() == key2.getVertex())
				&& (entry1.getParentStateNumber() == key2.getStateNumber())) {
			return true;
		}
		if ((entry2.getParentVertex() == key1.getVertex())
				&& (entry2.getParentStateNumber() == key1.getStateNumber())) {
			return true;
		}
		return false;
	}

	/**
	 * @return true if the given first vertex is a brother of the given second
	 *         vertex, that means, if they have the same father. If one or both
	 *         of the given vertices are part of the pathsystem more than one
	 *         times, the first occurence is used. If one of the vertices is not
	 *         part of this pathsystem, false is returned
	 */
	public boolean isSibling(Vertex v1, Vertex v2) {
		clearPathSystem();
		PathSystemKey key1 = vertexToFirstKeyMap.get(v1);
		PathSystemKey key2 = vertexToFirstKeyMap.get(v2);
		return isSibling(key1, key2);
	}

	/**
	 * @return true if the given first key is a brother of the given second key,
	 *         that means, if thy have the same father in the pathtree from
	 *         key1.vertex to key2.vertex and the states matches. If one of the
	 *         keys is not part of this pathsystem, false is returned
	 */
	public boolean isSibling(PathSystemKey key1, PathSystemKey key2) {
		clearPathSystem();
		if ((key1 == null) || (key2 == null)) {
			return false;
		}
		PathSystemEntry entry1 = keyToEntryMap.get(key1);
		PathSystemEntry entry2 = keyToEntryMap.get(key2);
		if ((entry1.getParentVertex() == entry2.getParentVertex())
				&& (entry1.getParentStateNumber() == entry2
						.getParentStateNumber())) {
			return true;
		}
		return false;
	}

	/**
	 * @return true, if the given path is part of this path tree, false
	 *         otherwise
	 */
	public boolean containsPath(JValuePath path) {
		clearPathSystem();
		if (path.getStartVertex() != rootVertex) {
			return false;
		}
		if (leafKeys == null) {
			createLeafKeys();
		}
		Iterator<PathSystemKey> iter = leafKeys.iterator();
		while (iter.hasNext()) {
			PathSystemKey key = iter.next();
			PathSystemEntry entry = keyToEntryMap.get(key);
			if ((entry.getDistanceToRoot() == path.pathLength())
					&& (key.getVertex() == path.getEndVertex())) {
				try {
					JValuePath entryPath = extractPath(path.getEndVertex());
					if (entryPath.isSubPathOf(path)) {
						return true;
					}
				} catch (JValuePathException ex) {
					ex.printStackTrace();
				}
			}
		}
		return false;
	}

	/**
	 * Prints this pathsystem as ascii-art
	 */
	public void printAscii() {
		clearPathSystem();
		try {
			JValueSet pathSet = extractPaths();
			Iterator<JValue> iter = pathSet.iterator();
			while (iter.hasNext()) {
				JValuePath path = (JValuePath) iter.next();
				logger.info(path.toString());
			}
		} catch (JValuePathException ex) {
			logger.severe("Caught " + ex);
		}
	}

	/**
	 * returns a string representation of this path system
	 */
	@Override
	public String toString() {
		clearPathSystem();
		StringBuffer returnString = new StringBuffer("PathSystem: \n");
		try {
			JValueSet pathSet = extractPaths();
			Iterator<JValue> iter = pathSet.iterator();
			while (iter.hasNext()) {
				JValuePath path = (JValuePath) iter.next();
				returnString.append(path.toString());
			}
		} catch (JValuePathException ex) {
			return ex.toString();
		}
		return returnString.toString();
	}

	/**
	 * prints the <key, entry> map
	 */
	public void printEntryMap() {
		clearPathSystem();
		Iterator<Map.Entry<PathSystemKey, PathSystemEntry>> iter = keyToEntryMap
				.entrySet().iterator();
		logger.info("<Key, Entry> Set of PathSystem is:");
		while (iter.hasNext()) {
			Map.Entry<PathSystemKey, PathSystemEntry> mapEntry = iter.next();
			PathSystemEntry thisEntry = mapEntry.getValue();
			PathSystemKey thisKey = mapEntry.getKey();
			logger.info(thisKey.toString() + " maps to " + thisEntry.toString());
		}
	}

	/**
	 * prints the <vertex, key map
	 */
	public void printKeyMap() {
		clearPathSystem();
		Iterator<Map.Entry<Vertex, PathSystemKey>> iter = vertexToFirstKeyMap
				.entrySet().iterator();
		logger.info("<Vertex, FirstKey> Set of PathSystem is:");
		while (iter.hasNext()) {
			Map.Entry<Vertex, PathSystemKey> mapEntry = iter.next();
			PathSystemKey thisKey = mapEntry.getValue();
			Vertex vertex = mapEntry.getKey();
			logger.info(vertex + " maps to " + thisKey.toString());
		}
	}

	/**
	 * accepts te given visitor to visit this jvalue
	 */
	@Override
	public void accept(JValueVisitor v) {
		clearPathSystem();
		v.visitPathSystem(this);
	}

	/**
	 * Converts this pathsystem to a set containing all vertices and edges
	 * contained in this slice.
	 */
	@Override
	public JValueSet toJValueSet() throws JValueInvalidTypeException {
		JValueSet edges = edges();
		JValueSet nodes = nodes();
		JValueSet edgesAndNodes = new JValueSet(edges.size() + nodes.size());
		edgesAndNodes.addAll(edges);
		edgesAndNodes.addAll(nodes);
		return edgesAndNodes;
	}

	/**
	 * Converts this pathsystem to a set containing all vertices and edges
	 * contained in this slice.
	 */
	@Override
	public JValueSet toCollection() throws JValueInvalidTypeException {
		return toJValueSet();
	}

}
