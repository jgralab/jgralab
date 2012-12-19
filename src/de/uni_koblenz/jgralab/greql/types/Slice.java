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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.pcollections.PSet;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.graphmarker.SubGraphMarker;

public class Slice {

	/**
	 * This HashMap stores references from a tuple (Vertex,State) to a list of
	 * tuples(ParentVertex, ParentEdge, ParentState, DistanceToRoot)
	 */
	private final HashMap<PathSystemKey, List<PathSystemEntry>> keyToEntryMap;

	/**
	 * This HashMap stores references from a vertex to the first occurence of
	 * this vertex in the above HashMap<PathSystemKey, PathSystemEntry>
	 * keyToEntryMap
	 */
	private final HashMap<Vertex, PathSystemKey> vertexToFirstKeyMap;

	/**
	 * This is the rootvertex of the slice
	 */
	private PSet<Vertex> sliCritVertices;

	/**
	 * this set stores the keys of the leaves of this slice. It is created the
	 * first time it is needed. So the creation (which is in O(n²) ) has to be
	 * done only once.
	 */
	private ArrayList<PathSystemKey> leafKeys = null;

	/**
	 * returns the slicing criterion vertices of this slice
	 */
	public PSet<Vertex> getSlicingCriterionVertices() {
		return sliCritVertices;
	}

	/**
	 * This is a reference to the datagraph this slice is part of
	 */
	private final Graph datagraph;

	/**
	 * returns the datagraph this slice is part of
	 */
	public Graph getDataGraph() {
		return datagraph;
	}

	/**
	 * returns the hashcode of this slice
	 */
	@Override
	public int hashCode() {
		return keyToEntryMap.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if ((o == null) || !(o instanceof Slice)) {
			return false;
		}
		return keyToEntryMap.equals(((Slice) o).keyToEntryMap);
	}

	/**
	 * creates a new JValueSlice with the given rootVertex in the given
	 * datagraph
	 */
	public Slice(Graph graph) {
		datagraph = graph;
		keyToEntryMap = new HashMap<PathSystemKey, List<PathSystemEntry>>();
		vertexToFirstKeyMap = new HashMap<Vertex, PathSystemKey>();
		sliCritVertices = JGraLab.set();

	}

	private final Queue<PathSystemEntry> entriesWithoutParentEdge = new LinkedList<PathSystemEntry>();

	private boolean isCleared = true;

	public void clearPathSystem() {
		if (!isCleared) {
			while (!entriesWithoutParentEdge.isEmpty()) {
				PathSystemEntry te = entriesWithoutParentEdge.poll();
				Vertex p = te.getParentVertex();
				if (p == null) {
					// root vertex
				} else {
					List<PathSystemEntry> pel = keyToEntryMap
							.get(new PathSystemKey(p, te.getParentStateNumber()));
					PathSystemEntry pe = pel.get(0);
					te.setParentEdge(pe.getParentEdge());
					te.setDistanceToRoot(pe.getDistanceToRoot());
					te.setParentStateNumber(pe.getParentStateNumber());
					te.setParentVertex(pe.getParentVertex());
					if (te.getParentEdge() == null) {
						entriesWithoutParentEdge.add(te);
					}
				}
			}
			isCleared = true;
		}
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
		// System.out.println("Adding vertex " + vertex +
		// " as slicing criterion");
		PathSystemKey key = new PathSystemKey(vertex, stateNumber);
		PathSystemEntry entry = new PathSystemEntry(null, null, -1, 0,
				finalState);
		List<PathSystemEntry> entryList = new ArrayList<PathSystemEntry>();
		entryList.add(entry);
		keyToEntryMap.put(key, entryList);
		if (!vertexToFirstKeyMap.containsKey(vertex)) {
			vertexToFirstKeyMap.put(vertex, key);
		}
		leafKeys = null;
		sliCritVertices = sliCritVertices.plus(vertex);
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
			if (!vertexToFirstKeyMap.containsKey(vertex)) {
				vertexToFirstKeyMap.put(vertex, key);
			}
			leafKeys = null;
		}
		PathSystemEntry entry = new PathSystemEntry(parentVertex, parentEdge,
				parentStateNumber, 0, finalState);
		if (!entryList.contains(entry)) {
			entryList.add(entry);
		}
		if (parentEdge == null) {
			entriesWithoutParentEdge.add(entry);
			isCleared = false;
		}
	}

	/**
	 * Calculates the set of edges in this slice.
	 */
	public PSet<Edge> getEdges() {
		clearPathSystem();
		PSet<Edge> resultSet = JGraLab.set();
		for (Map.Entry<PathSystemKey, List<PathSystemEntry>> mapEntry : keyToEntryMap
				.entrySet()) {
			for (PathSystemEntry thisEntry : mapEntry.getValue()) {
				if (thisEntry.getParentEdge() != null) {
					resultSet = resultSet.plus(thisEntry.getParentEdge());
				}
			}
		}
		return resultSet;
	}

	/**
	 * Calculates the set of nodes which are part of this slice.
	 */
	public PSet<Vertex> getVertices() {
		clearPathSystem();
		PSet<Vertex> resultSet = JGraLab.set();
		for (PathSystemKey mapKey : keyToEntryMap.keySet()) {
			resultSet = resultSet.plus(mapKey.getVertex());
		}

		return resultSet;
	}

	/**
	 * Calculates the set of leaves in this slice. Costs: O(n²) where n is the
	 * number of vertices in the slice. The created set is stored as private
	 * field <code>leaves</code>, so the creation has to be done only once.
	 */
	public PSet<Vertex> getLeaves() {
		clearPathSystem();
		PSet<Vertex> leaves = JGraLab.set();
		if (leafKeys == null) {
			createLeafKeys();
		}
		// create the set of leaves out of the key set
		for (PathSystemKey key : leafKeys) {
			leaves = leaves.plus(key.getVertex());
		}
		return leaves;
	}

	/**
	 * create the set of leaf keys if it is not already created
	 */
	private void createLeafKeys() {
		clearPathSystem();
		if (leafKeys != null) {
			return;
		}
		leafKeys = new ArrayList<PathSystemKey>();
		for (Map.Entry<PathSystemKey, List<PathSystemEntry>> mapEntry : keyToEntryMap
				.entrySet()) {
			boolean isFinal = false;
			for (PathSystemEntry entry : mapEntry.getValue()) {
				if (entry.getStateIsFinal()) {
					isFinal = true;
				}
			}
			if (isFinal) {
				leafKeys.add(mapEntry.getKey());
			}
		}
	}

	/**
	 * returns a string representation of this slice
	 */
	@Override
	public String toString() {
		clearPathSystem();
		Set<Vertex> vset = new HashSet<Vertex>();
		vset.addAll(vertexToFirstKeyMap.keySet());
		Set<Edge> eset = new HashSet<Edge>();
		for (List<PathSystemEntry> pl : keyToEntryMap.values()) {
			for (PathSystemEntry pe : pl) {
				eset.add(pe.getParentEdge());
			}
		}

		StringBuilder returnString = new StringBuilder("Slice: ");
		returnString.append("Vertices: ");
		boolean first = true;
		for (Vertex v : vset) {
			if (first) {
				first = false;
				returnString.append(v);
			} else {
				returnString.append(", ");
				returnString.append(v);
			}
		}
		returnString.append(", Edges: ");
		first = true;
		for (Edge e : eset) {
			if (first) {
				first = false;
				returnString.append(e);
			} else {
				returnString.append(", ");
				returnString.append(e);
			}
		}
		return returnString.toString();
	}

	public SubGraphMarker toSubGraphMarker() {
		SubGraphMarker r = new SubGraphMarker(getDataGraph());
		for (Vertex v : getVertices()) {
			r.mark(v);
		}
		for (Edge e : getEdges()) {
			r.mark(e);
		}
		return r;
	}
}
