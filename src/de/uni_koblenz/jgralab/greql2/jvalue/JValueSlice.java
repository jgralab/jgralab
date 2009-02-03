/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2009 Institute for Software Technology
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

package de.uni_koblenz.jgralab.greql2.jvalue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.exception.JValueInvalidTypeException;

public class JValueSlice extends JValue {

	private static Logger logger = Logger.getLogger(JValuePathSystem.class
			.getName());

	/**
	 * This HashMap stores references from a tuple (Vertex,State) to a list of
	 * tuples(ParentVertex, ParentEdge, ParentState, DistanceToRoot)
	 */
	private HashMap<PathSystemKey, List<PathSystemEntry>> keyToEntryMap;

	/**
	 * This HashMap stores references from a vertex to the first occurence of
	 * this vertex in the above HashMap<PathSystemKey, PathSystemEntry>
	 * keyToEntryMap
	 */
	private HashMap<Vertex, PathSystemKey> vertexToFirstKeyMap;

	/**
	 * This is the rootvertex of the slice
	 */
	private Set<Vertex> sliCritVertices;

	/**
	 * this set stores the keys of the leaves of this slice. It is created the
	 * first time it is needed. So the creation (which is in O(n²) ) has to be
	 * done only once.
	 */
	private ArrayList<PathSystemKey> leafKeys = null;

	/**
	 * returns the slicing criterion vertices of this slice
	 */
	public JValueSet getSlicingCriterionVertices() {
		JValueSet resultSet = new JValueSet();

		for (Vertex v : sliCritVertices) {
			resultSet.add(new JValue(v));
		}
		return resultSet;
	}

	/**
	 * This is a reference to the datagraph this slice is part of
	 */
	private Graph datagraph;

	/**
	 * stores the hashcode of this slice so it must be calculated only if the
	 * slice changes
	 */
	private int hashvalue = 0;

	/**
	 * returns the datagraph this slice is part of
	 */
	public Graph getDataGraph() {
		return datagraph;
	}

	/**
	 * returns a JValueSlice-Reference to this JValue object
	 */
	public JValueSlice toSlice() throws JValueInvalidTypeException {
		return this;
	}

	/**
	 * returns the hashcode of this slice
	 */
	public int hashCode() {
		if (hashvalue == 0) {
			Iterator<Map.Entry<PathSystemKey, List<PathSystemEntry>>> iter = keyToEntryMap
					.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry<PathSystemKey, List<PathSystemEntry>> mapEntry = iter
						.next();
				PathSystemKey key = mapEntry.getKey();
				for (PathSystemEntry thisEntry : mapEntry.getValue())
					hashvalue += key.hashCode() * 11 + thisEntry.hashCode() * 7;
			}
			if (hashvalue < 0)
				hashvalue = -hashvalue;
		}
		return hashvalue;
	}

	/**
	 * creates a new JValueSlice with the given rootVertex in the given
	 * datagraph
	 */
	public JValueSlice(Graph graph) {
		datagraph = graph;
		keyToEntryMap = new HashMap<PathSystemKey, List<PathSystemEntry>>();
		vertexToFirstKeyMap = new HashMap<Vertex, PathSystemKey>();
		type = JValueType.PATHSYSTEM;
		sliCritVertices = new HashSet<Vertex>();

	}

	/**
	 * adds a vertex of the slice which is described by the parameters to the
	 * slicing criterion
	 * 
	 * @param vertex
	 *            the vertex to add
	 * @param stateNumber
	 *            the number of the DFAState the DFA was in when this vertex was
	 *            visited
	 * @param finalState
	 *            true if the vertex is visited by the dfa in a final state
	 */
	public void addSlicingCriterionVertex(Vertex vertex, int stateNumber,
			boolean finalState) {
		PathSystemKey key = new PathSystemKey(vertex, stateNumber);
		PathSystemEntry entry = new PathSystemEntry(null, null, -1, 0,
				finalState);
		List<PathSystemEntry> entryList = new ArrayList<PathSystemEntry>();
		entryList.add(entry);
		keyToEntryMap.put(key, entryList);
		if (!vertexToFirstKeyMap.containsKey(vertex))
			vertexToFirstKeyMap.put(vertex, key);
		leafKeys = null;
		hashvalue = 0;
		sliCritVertices.add(vertex);
	}

	/**
	 * adds a vertex of the slice which is described by the parameters to the
	 * slice
	 * 
	 * @param vertex
	 *            the vertex to add
	 * @param stateNumber
	 *            the number of the DFAState the DFA was in when this vertex was
	 *            visited
	 * @param parentEdge
	 *            the edge which leads from vertex to parentVertex
	 * @param parentVertex
	 *            the parentVertex of the vertex in the slice
	 * @param parentStateNumber
	 *            the number of the DFAState the DFA was in when the
	 *            parentVertex was visited
	 */
	public void addVertex(Vertex vertex, int stateNumber, Edge parentEdge,
			Vertex parentVertex, int parentStateNumber, boolean finalState) {
		PathSystemKey key = new PathSystemKey(vertex, stateNumber);
		List<PathSystemEntry> entryList = keyToEntryMap.get(key);
		if (entryList == null) {
			entryList = new ArrayList<PathSystemEntry>();
			keyToEntryMap.put(key, entryList);
			if (!vertexToFirstKeyMap.containsKey(vertex))
				vertexToFirstKeyMap.put(vertex, key);
			leafKeys = null;
		}
		PathSystemEntry entry = new PathSystemEntry(parentVertex, parentEdge,
				parentStateNumber, 0, finalState);
		if (!entryList.contains(entry)) {
			entryList.add(entry);
		}
	}

	// /**
	// * Calculates the set of children the given vertex has in this PathSystem.
	// * If the given vertex exists more than one times in this pathsystem, the
	// * first occurence if used.
	// */
	// public JValueSet children(Vertex vertex) {
	// PathSystemKey key = vertexToFirstKeyMap.get(vertex);
	// return children(key);
	// }
	//
	// /**
	// * Calculates the set of child the given key has in this PathSystem
	// */
	// public JValueSet children(PathSystemKey key) {
	// JValueSet returnSet = new JValueSet();
	// for (Map.Entry<PathSystemKey, List<PathSystemEntry>> mapEntry:
	// keyToEntryMap.entrySet()) {
	// for (PathSystemEntry thisEntry : mapEntry.getValue()) {
	// if ((thisEntry.getParentVertex() == key.getVertex())
	// && (thisEntry.getParentStateNumber() == key
	// .getStateNumber())) {
	// Vertex v = mapEntry.getKey().getVertex();
	// returnSet.add(new JValue(v, v));
	// }
	// }
	// }
	// return returnSet;
	// }

	/**
	 * Calculates the parent vertices of the given vertex in this slice. If the
	 * given vertex exists more than one times in this slice, the first
	 * occurence if used. If the given vertex is not part of this slice, an
	 * invalid JValue will be returned
	 */
	public JValue parents(Vertex vertex) {
		PathSystemKey key = vertexToFirstKeyMap.get(vertex);
		return parents(key);
	}

	/**
	 * Calculates the parent vertices of the given key in this slice.
	 */
	public JValueSet parents(PathSystemKey key) {
		JValueSet resultSet = new JValueSet();

		for (PathSystemEntry entry : keyToEntryMap.get(key)) {
			resultSet.add(new JValue(entry.getParentVertex(), entry
					.getParentVertex()));
		}

		return resultSet;
	}

	// /**
	// * Calculates the set of types this pathsystem contains
	// */
	// public JValueSet types() {
	// JValueSet returnSet = new JValueSet();
	// Iterator<Map.Entry<PathSystemKey, PathSystemEntry>> iter = keyToEntryMap
	// .entrySet().iterator();
	// while (iter.hasNext()) {
	// Map.Entry<PathSystemKey, PathSystemEntry> entry = iter.next();
	// returnSet.add(new JValue((GraphElementClass) entry.getKey()
	// .getVertex().getAttributedElementClass(), entry.getKey()
	// .getVertex()));
	// Edge e = entry.getValue().getParentEdge();
	// if (e != null)
	// returnSet.add(new JValue((GraphElementClass) e
	// .getAttributedElementClass(), e));
	// }
	// return returnSet;
	// }
	//
	// /**
	// * Calculates the set of vertextypes this pathsystem contains
	// */
	// public JValueSet vertexTypes() {
	// JValueSet returnSet = new JValueSet();
	// Iterator<Map.Entry<PathSystemKey, PathSystemEntry>> iter = keyToEntryMap
	// .entrySet().iterator();
	// while (iter.hasNext()) {
	// Map.Entry<PathSystemKey, PathSystemEntry> entry = iter.next();
	// returnSet.add(new JValue((GraphElementClass) entry.getKey()
	// .getVertex().getAttributedElementClass(), entry.getKey()
	// .getVertex()));
	// }
	// return returnSet;
	// }
	//
	// /**
	// * Calculates the set of edgetypes this pathsystem contains
	// */
	// public JValueSet edgeTypes() {
	// JValueSet returnSet = new JValueSet();
	// Iterator<Map.Entry<PathSystemKey, PathSystemEntry>> iter = keyToEntryMap
	// .entrySet().iterator();
	// while (iter.hasNext()) {
	// Map.Entry<PathSystemKey, PathSystemEntry> entry = iter.next();
	// Edge e = entry.getValue().getParentEdge();
	// if (e != null)
	// returnSet.add(new JValue((GraphElementClass) e
	// .getAttributedElementClass(), e));
	// }
	// return returnSet;
	// }
	//
	// /**
	// * Checks, wether the given element (vertex or edge) is part of this
	// * pathsystem
	// *
	// * @return true, if the element is part of this system, false otherwise
	// */
	// public boolean contains(GraphElement elem) {
	// Iterator<Map.Entry<PathSystemKey, PathSystemEntry>> iter = keyToEntryMap
	// .entrySet().iterator();
	// while (iter.hasNext()) {
	// Map.Entry<PathSystemKey, PathSystemEntry> entry = iter.next();
	// if (entry.getValue().getParentEdge() == elem)
	// return true;
	// if (entry.getKey().getVertex() == elem)
	// return true;
	// }
	// return false;
	// }

	// /**
	// * Checks, wether the pathsystem contains an element which has the given
	// * type
	// *
	// * @return true, if the element is part of this system, false otherwise
	// */
	// public boolean contains(AttributedElementClass type) {
	// Iterator<Map.Entry<PathSystemKey, PathSystemEntry>> iter = keyToEntryMap
	// .entrySet().iterator();
	// while (iter.hasNext()) {
	// Map.Entry<PathSystemKey, PathSystemEntry> entry = iter.next();
	// if (entry.getValue().getParentEdge().getAttributedElementClass() == type)
	// return true;
	// if (entry.getKey().getVertex().getAttributedElementClass() == type)
	// return true;
	// }
	// return false;
	// }
	//
	// /**
	// * Calculates the number of incomming or outgoing edges of the given
	// vertex
	// * which are part of this PathSystem
	// *
	// * @param vertex
	// * the vertex for which the number of edges gets counted
	// * @param orientation
	// * if set to true, the incomming edges will be counted,
	// * otherwise, the outgoing ones will be counted
	// * @param typeCol
	// * the JValueTypeCollection which toggles wether a type is
	// * accepted or not
	// * @return the number of edges with the given orientation connected to the
	// * given vertex or -1 if the given vertex is not part of this
	// * pathsystem
	// */
	// public int degree(Vertex vertex, boolean orientation,
	// JValueTypeCollection typeCol) {
	// if (vertex == null)
	// return -1;
	// int degree = 0;
	// Iterator<Map.Entry<PathSystemKey, PathSystemEntry>> iter = keyToEntryMap
	// .entrySet().iterator();
	// while (iter.hasNext()) {
	// Map.Entry<PathSystemKey, PathSystemEntry> entry = iter.next();
	// if (orientation) {
	// if ((entry.getValue().getParentVertex() == vertex)
	// && ((typeCol == null) || (typeCol.acceptsType(vertex
	// .getAttributedElementClass()))))
	// degree++;
	// } else {
	// if ((entry.getKey().getVertex() == vertex)
	// && ((typeCol == null) || (typeCol.acceptsType(vertex
	// .getAttributedElementClass()))))
	// degree++;
	// }
	// }
	// return degree;
	// }
	//
	// /**
	// * Calculates the number of incomming and outgoing edges of the given
	// vertex
	// * which are part of this PathSystem
	// *
	// * @param vertex
	// * the vertex for which the number of edges gets counted
	// * @param typeCol
	// * the JValueTypeCollection which toggles wether a type is
	// * accepted or not
	// * @return the number of edges connected to the given vertex or -1 if the
	// * given vertex is not part of this pathsystem
	// */
	// public int degree(Vertex vertex, JValueTypeCollection typeCol) {
	// if (vertex == null)
	// return -1;
	// int degree = 0;
	// Iterator<Map.Entry<PathSystemKey, PathSystemEntry>> iter = keyToEntryMap
	// .entrySet().iterator();
	// while (iter.hasNext()) {
	// Map.Entry<PathSystemKey, PathSystemEntry> entry = iter.next();
	// if (((entry.getValue().getParentVertex() == vertex) || (entry
	// .getKey().getVertex() == vertex))
	// && ((typeCol == null) || (typeCol.acceptsType(vertex
	// .getAttributedElementClass()))))
	// degree++;
	// }
	// return degree;
	// }
	//
	// /**
	// * Calculates the set of incomming or outgoing edges of the given vertex,
	// * which are also part of this pathsystem
	// *
	// * @param vertex
	// * the vertex for which the edgeset will be created
	// * @param orientation
	// * if set to true, the set of incomming edges will, be created,
	// * otherwise, the set of outgoing ones will be created
	// * @return a set of edges with the given orientation connected to the
	// given
	// * vertex or an empty set, if the vertex is not part of this
	// * pathsystem
	// */
	// public JValueSet edgesConnected(Vertex vertex, boolean orientation) {
	// JValueSet resultSet = new JValueSet();
	// if (vertex == null)
	// return resultSet;
	// Iterator<Map.Entry<PathSystemKey, PathSystemEntry>> iter = keyToEntryMap
	// .entrySet().iterator();
	// while (iter.hasNext()) {
	// Map.Entry<PathSystemKey, PathSystemEntry> entry = iter.next();
	// if (orientation) {
	// if (entry.getValue().getParentVertex() == vertex)
	// resultSet.add(new JValue(entry.getValue().getParentEdge()));
	// } else {
	// if (entry.getKey().getVertex() == vertex)
	// resultSet.add(new JValue(entry.getValue().getParentEdge()));
	// }
	// }
	// return resultSet;
	// }
	//
	// /**
	// * Calculates the set of edges which are connected to the given vertex,
	// and
	// * which are also part of this pathsystem
	// *
	// * @param vertex
	// * the vertex for which the edgeset will be created
	// * @return a set of edges connected to the given vertex or an empty set,
	// if
	// * the vertex is not part of this pathsystem
	// */
	// public JValueSet edgesConnected(Vertex vertex) {
	// JValueSet resultSet = new JValueSet();
	// if (vertex == null)
	// return resultSet;
	// Iterator<Map.Entry<PathSystemKey, PathSystemEntry>> iter = keyToEntryMap
	// .entrySet().iterator();
	// while (iter.hasNext()) {
	// Map.Entry<PathSystemKey, PathSystemEntry> entry = iter.next();
	// if (entry.getValue().getParentVertex() == vertex)
	// resultSet.add(new JValue(entry.getValue().getParentEdge()));
	// if (entry.getKey().getVertex() == vertex)
	// resultSet.add(new JValue(entry.getValue().getParentEdge()));
	// }
	// return resultSet;
	// }

	/**
	 * Calculates the set of edges nodes in this slice.
	 */
	public JValueSet edges() {
		JValueSet resultSet = new JValueSet();
		for (Map.Entry<PathSystemKey, List<PathSystemEntry>> mapEntry : keyToEntryMap
				.entrySet()) {
			for (PathSystemEntry thisEntry : mapEntry.getValue()) {
				if (thisEntry.getParentEdge() != null)
					resultSet.add(new JValue(thisEntry.getParentEdge()));
			}
		}
		return resultSet;
	}

	/**
	 * Calculates the set of nodes which are part of this slice.
	 */
	public JValueSet nodes() {
		JValueSet resultSet = new JValueSet();
		for (PathSystemKey mapKey : keyToEntryMap.keySet()) {
			resultSet.add(new JValue(mapKey.getVertex()));
		}

		return resultSet;
	}

	/**
	 * Calculates the set of inner nodes in this slice. Inner nodes are these
	 * nodes, which are neither root nor leave Costs: O(n) where n is the number
	 * of vertices in the slice
	 */
	public JValueSet innerNodes() {
		JValueSet resultSet = new JValueSet();
		for (Map.Entry<PathSystemKey, List<PathSystemEntry>> mapEntry : keyToEntryMap
				.entrySet()) {
			for (PathSystemEntry entry : mapEntry.getValue()) {
				if ((!entry.isStateIsFinal())
						&& (entry.getParentVertex() != null)) {
					resultSet.add(new JValue(mapEntry.getKey().getVertex()));
				}
			}
		}
		return resultSet;
	}

	/**
	 * Calculates the set of leaves in this slice. Costs: O(n²) where n is the
	 * number of vertices in the slice. The created set is stored as private
	 * field <code>leaves</code>, so the creation has to be done only once.
	 */
	public JValueSet leaves() {
		JValueSet leaves = new JValueSet();
		if (leafKeys == null) {
			createLeafKeys();
		}
		// create the set of leaves out of the key set
		for (PathSystemKey key : leafKeys) {
			leaves.add(new JValue(key.getVertex()));
		}
		return leaves;
	}

	/**
	 * create the set of leaf keys if it is not already created
	 */
	private void createLeafKeys() {
		if (leafKeys != null) {
			return;
		}
		leafKeys = new ArrayList<PathSystemKey>();
		for (Map.Entry<PathSystemKey, List<PathSystemEntry>> mapEntry : keyToEntryMap
				.entrySet()) {
			boolean isFinal = false;
			for (PathSystemEntry entry : mapEntry.getValue()) {
				if (entry.isStateIsFinal())
					isFinal = true;
			}
			if (isFinal) {
				leafKeys.add(mapEntry.getKey());
			}
		}
	}

	// /**
	// * Extracts the paths which start at one of the vertices in the slicing
	// * criterion and end with the given vertex from the slice. If the given
	// * vertex exists more than one times in this slice, the first occurrence
	// if
	// * used. If the given vertex is not part of this slice, null will be
	// * returned.
	// *
	// * @param vertex
	// * @return a set containing the paths from the vertices in the slicing
	// * criterion to the given vertex
	// */
	// public JValueSet extractPaths(Vertex vertex) throws JValuePathException {
	// PathSystemKey key = vertexToFirstKeyMap.get(vertex);
	// if (key == null)
	// return new JValueSet();
	// return extractPaths(key);
	// }
	//
	// /**
	// * Extract the paths which start at one of the vertices in the slicing
	// * criterion and end with the vertex in the given key.
	// *
	// * @param key
	// * the pair (Vertex, Statenumber) which is the target of the paths
	// * @return a Path from rootVertex to given vertex
	// */
	// public JValuePath extractPath(PathSystemKey key) throws
	// JValuePathException {
	// JValuePath path = new JValuePath(key.getVertex());
	// while (key != null) {
	// PathSystemEntry entry = keyToEntryMap.get(key);
	// if (entry.getParentEdge() != null) {
	// path.addEdge(entry.getParentEdge().getReversedEdge());
	// key = new PathSystemKey(entry.getParentVertex(), entry
	// .getParentStateNumber());
	// } else {
	// key = null;
	// }
	// }
	// return path.reverse();
	// }

	// /**
	// * Extract the set of paths which are part of this path system. These
	// paths
	// * start with the root vertex and ends with a leave.
	// *
	// * @return a set of Paths from rootVertex to leaves
	// */
	// public JValueSet extractPath() throws JValuePathException {
	// JValueSet pathSet = new JValueSet();
	// if (leafKeys == null)
	// createLeafKeys();
	// Iterator<PathSystemKey> iter = leafKeys.iterator();
	// while (iter.hasNext()) {
	// JValuePath path = extractPath(iter.next());
	// pathSet.add(path);
	// }
	// return pathSet;
	// }

	// /**
	// * Extracts all paths which length equal to <code>len</code>
	// *
	// * @return a set of Paths from rootVertex to leaves
	// */
	// public JValueSet extractPath(int len) throws JValuePathException {
	// JValueSet pathSet = new JValueSet();
	// if (leafKeys == null)
	// createLeafKeys();
	// Iterator<PathSystemKey> iter = leafKeys.iterator();
	// while (iter.hasNext()) {
	// JValuePath path = extractPath(iter.next());
	// if (path.pathLength() == len)
	// pathSet.add(path);
	// }
	// return pathSet;
	// }

	/**
	 * calculate the number of vertices this slice has. If a vertex is part of
	 * this slice n times, it is counted n times
	 */
	public int weight() {
		return keyToEntryMap.size();
	}

	/**
	 * @return true if the given first vertex is a neighbour of the given second
	 *         vertex, that means, if there is a edge from v1 to v2. If one or
	 *         both of the given vertices are part of the slice more than once,
	 *         the first occurence is used. If one of the vertices is not part
	 *         of this slice, false is returned
	 */
	public boolean isNeighbour(Vertex v1, Vertex v2) {
		PathSystemKey key1 = vertexToFirstKeyMap.get(v1);
		PathSystemKey key2 = vertexToFirstKeyMap.get(v2);
		return isNeighbour(key1, key2);
	}

	/**
	 * @return true if the given first key is a neighbour of the given second
	 *         key, that means, if there is a edge from key1.vertex to
	 *         key2.vertex and the states matches. If one of the keys is not
	 *         part of this slice, false is returned
	 */
	public boolean isNeighbour(PathSystemKey key1, PathSystemKey key2) {
		if ((key1 == null) || (key2 == null)) {
			return false;
		}
		for (PathSystemEntry entry1 : keyToEntryMap.get(key1)) {
			for (PathSystemEntry entry2 : keyToEntryMap.get(key2)) {
				if ((entry1.getParentVertex() == key2.getVertex())
						&& (entry1.getParentStateNumber() == key2
								.getStateNumber()))
					return true;
				if ((entry2.getParentVertex() == key1.getVertex())
						&& (entry2.getParentStateNumber() == key1
								.getStateNumber()))
					return true;
			}
		}
		return false;
	}

	// /**
	// * @return true, if the given path is part of this path tree, false
	// * otherwise
	// */
	// public boolean containsPath(JValuePath path) {
	// if (path.getStartVertex() != rootVertex)
	// return false;
	// if (leafKeys == null)
	// createLeafKeys();
	// Iterator<PathSystemKey> iter = leafKeys.iterator();
	// while (iter.hasNext()) {
	// PathSystemKey key = iter.next();
	// PathSystemEntry entry = keyToEntryMap.get(key);
	// if ((entry.getDistanceToRoot() == path.pathLength())
	// && (key.getVertex() == path.getEndVertex())) {
	// try {
	// JValuePath entryPath = extractPath(path.getEndVertex());
	// if (entryPath.isSubPathOf(path))
	// return true;
	// } catch (JValuePathException ex) {
	// ex.printStackTrace();
	// }
	// }
	// }
	// return false;
	// }

	// /**
	// * Prints this pathsystem as ascii-art
	// */
	// public void printAscii() {
	// try {
	// JValueSet pathSet = extractPath();
	// Iterator<JValue> iter = pathSet.iterator();
	// while (iter.hasNext()) {
	// JValuePath path = (JValuePath) iter.next();
	// logger.info(path.toString());
	// }
	// } catch (JValuePathException ex) {
	// logger.severe("Caught " + ex);
	// }
	// }

	// /**
	// * returns a string representation of this path system
	// */
	// public String toString() {
	// StringBuffer returnString = new StringBuffer("PathSystem: \n");
	// try {
	// JValueSet pathSet = extractPath();
	// Iterator<JValue> iter = pathSet.iterator();
	// while (iter.hasNext()) {
	// JValuePath path = (JValuePath) iter.next();
	// returnString.append(path.toString());
	// }
	// } catch (JValuePathException ex) {
	// return ex.toString();
	// }
	// return returnString.toString();
	// }

	/**
	 * Prints the <key, List<entry>> map as single <key, entry> entries, i.e. a
	 * key may occur multiple times.
	 */
	public void printEntryMap() {
		logger.info("<Key, Entry> set of slice is:");
		for (Map.Entry<PathSystemKey, List<PathSystemEntry>> mapEntry : keyToEntryMap
				.entrySet()) {
			for (PathSystemEntry entry : mapEntry.getValue()) {
				logger.info(mapEntry.getKey().toString() + " maps to "
						+ entry.toString());
			}
		}
	}

	/**
	 * Prints the <vertex, key map>.
	 */
	public void printKeyMap() {
		Iterator<Map.Entry<Vertex, PathSystemKey>> iter = vertexToFirstKeyMap
				.entrySet().iterator();
		logger.info("<Vertex, FirstKey> set of slice is:");
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
	public void accept(JValueVisitor v) {
		v.visitSlice(this);
	}

}
