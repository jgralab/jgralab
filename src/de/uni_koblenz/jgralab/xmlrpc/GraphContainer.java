/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2007 Institute for Software Technology
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

package de.uni_koblenz.jgralab.xmlrpc;

import java.util.Hashtable;
import java.util.Map;

import de.uni_koblenz.jgralab.Graph;

/**
 * This class holds the graphs created or loaded via the {@code JGraLabFacade},
 * mapping an integer value (the handle) to a graph.
 * 
 */
public class GraphContainer {

	/**
	 * the singleton instance of this class
	 */
	private static GraphContainer graphContainer;

	private static int keyGenerator;

	/**
	 * the {@code Map} holding the graphs created or loaded via the {@code
	 * JGraLabFacade}
	 */
	private Map<Integer, Graph> graphs;

	private GraphContainer() {
		graphContainer = null;
		keyGenerator = 0;
		graphs = new Hashtable<Integer, Graph>();
	}

	/**
	 * Returns the single instance of this class.
	 * 
	 * @return the single instance of this class
	 */
	public static GraphContainer instance() {
		if (graphContainer == null) {
			graphContainer = new GraphContainer();
		}

		return graphContainer;
	}

	/**
	 * Stores {@code graph} and returns the handle for accessing this graph.
	 * 
	 * @param graph
	 * @return handle for accessing a graph in the Map {@code graphs}
	 */
	public int addGraph(Graph graph) {
		int key = keyGenerator;
		++keyGenerator;
		graphs.put(key, graph);
		return key;
	}

	/**
	 * Returns the graph stored with the handle {@code graphNo}.
	 * 
	 * @param graphNo
	 *            the handle for the graph which shall be got
	 * @return the graph with the handle {@code graphNo}.
	 */
	public Graph getGraph(int graphNo) {
		return graphs.get(graphNo);
	}

	/**
	 * Removes the stored graph with the handle {@code graphNo}.
	 * 
	 * @param graphNo
	 *            the graph to be deleted
	 */
	public void releaseGraph(int graphNo) {
		graphs.remove(graphNo);
	}

	/**
	 * Checks whether a graph with the handle {@code graphNo} exists.
	 * 
	 * @param graphNo
	 *            the handle for which the existence of a graph shall be checked
	 * @return {@code true} if a graph with handle {@code graphNo} exists,
	 *         {@code false} otherwise
	 */
	public boolean containsGraph(int graphNo) {
		return graphs.containsKey(graphNo);
	}
}
