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

package de.uni_koblenz.jgralab.greql2.evaluator;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.Map.Entry;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueSet;

/**
 * This class contains the computed index for a graph
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class GraphIndex {

	private static class VertexIndexEntry {

		public long modificationTime = 0;

		public JValueSet vertexSet = null;

		public VertexIndexEntry(JValueSet v) {
			vertexSet = v;
			modificationTime = System.currentTimeMillis();
		}

		public void modified() {
			modificationTime = System.currentTimeMillis();
		}

	}

	/**
	 * the size of this graphindex, counted in vertices/edges. So, if the index
	 * contains 1 million vertices and two million edges, the size of the index
	 * is three million.
	 */
	private int indexSize;

	/**
	 * The id of the graph this index belongs to
	 */
	private String graphId;

	/**
	 * The original size of that graph
	 */
	private int graphSize;

	/**
	 * The version number of the graph this index belongs to
	 */
	private long graphVersion;

	/**
	 * Holds the index of VertexSets
	 */
	private HashMap<String, VertexIndexEntry> vertexIndex;

	/**
	 * Creates a new GraphIndex for the graph
	 * 
	 * @param graph
	 *            the graph to create the Index for
	 */
	public GraphIndex(Graph graph) {
		graphId = graph.getId();
		graphVersion = graph.getGraphVersion();
		graphSize = graph.getECount() + graph.getVCount();
		vertexIndex = new HashMap<String, VertexIndexEntry>();
	}

	/**
	 * @return true iff this index is still valid for the given graph
	 */
	public boolean isValid(Graph g) {
		return ((g.getId().equals(graphId)) && (!g
				.isGraphModified(graphVersion)));
	}

	/**
	 * @return the ID of the graph this index belongs to
	 */
	public String getGraphId() {
		return graphId;
	}

	/**
	 * adds a indexed vertex set
	 * 
	 * @param query
	 *            the query which constructs this set, e.g. "V{Identifier}"
	 */
	public void addVertexSet(String query, JValueSet vertexSet) {
		vertexIndex.put(query, new VertexIndexEntry(vertexSet));
		indexSize += vertexSet.size();
		reduceIndexSize(graphSize);
	}

	/**
	 * returns the indexed vertex set for the given query part or
	 * <code>null</code> if no indexed vertex set exists
	 */
	public JValueSet getVertexSet(String query) {
		VertexIndexEntry entry = vertexIndex.get(query);
		if (entry != null) {
			entry.modified();
			return entry.vertexSet;
		}
		return null;
	}

	/**
	 * removes as much entrys as are needed to push the index size below the
	 * given value in graph elements
	 * 
	 * @param maxIndexSize
	 *            the method will remove as much indix entrys as needed to get
	 *            the index size lower than maxIndexSize, measured in
	 *            GraphElements
	 */
	public void reduceIndexSize(long maxIndexSize) {
		if (indexSize <= maxIndexSize) {
			return;
		}
		TreeSet<Entry<String, VertexIndexEntry>> entrySet = new TreeSet<Entry<String, VertexIndexEntry>>(
				new Comparator<Entry<String, VertexIndexEntry>>() {
					public int compare(Entry<String, VertexIndexEntry> o1,
							Entry<String, VertexIndexEntry> o2) {
						return (int) (o1.getValue().modificationTime - o2
								.getValue().modificationTime);
					}
				});
		entrySet.addAll(vertexIndex.entrySet());
		Iterator<Entry<String, VertexIndexEntry>> iter = entrySet.iterator();
		while (iter.hasNext() && (indexSize > maxIndexSize)) {
			Entry<String, VertexIndexEntry> currentEntry = iter.next();
			indexSize -= currentEntry.getValue().vertexSet.size();
			vertexIndex.remove(currentEntry.getKey());
		}
	}

}
