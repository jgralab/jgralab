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
 
package de.uni_koblenz.jgralab.greql2.funlib;

import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.funlib.pathsearch.PathSearch;
import de.uni_koblenz.jgralab.greql2.funlib.pathsearch.PathSearchQueueEntry;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import de.uni_koblenz.jgralab.BooleanGraphMarker;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;


/**
 * Checks if the current graph or subgraph is cycle-free.
 *
 * <dl>
 * <dt><b>GReQL-signature</b></dt>
 * <dd><code>BOOLEAN isAcyclic()</code></dd>
 * <dd>&nbsp;</dd>
 * </dl>
 * <dl><dt></dt>
 * <dd>
 * <dl>
 * <dt><b>Parameters:</b></dt>
 * <dt><b>Returns:</b></dt>
 * <dd><code>true</code> if the current graph or subgraph is acyclic</dd>
 * <dd><code>false</code> otherwise</dd>
 * </dl>
 * </dd>
 * </dl>
 * @author Daniel Bildhauer <dbildh@uni-koblenz.de> Summer 2006, Diploma Thesis
 * 
 */

/*
 * Checks if the given graph or subgraph contains no cycles
 * Only parameter is : SubgraphTempAttribute
 * 
 * @return true if the graph contains no cycles, false if at least one cycle is
 *         found
 */

public class IsAcyclic extends PathSearch implements Greql2Function {

	public JValue evaluate(Graph graph, BooleanGraphMarker subgraph, JValue[] arguments) throws EvaluateException {
		Queue<PathSearchQueueEntry> queue = new LinkedList<PathSearchQueueEntry>();
		boolean cycleFound = false;
		Vertex firstVertex = graph.getFirstVertex();
		Vertex currentVertex = firstVertex;
		do {
			PathSearchQueueEntry currentEntry = new PathSearchQueueEntry(
					currentVertex, null);
			while (currentEntry != null) {
				//markVertex(currentEntry.vertex, state);
				Edge inc = currentEntry.vertex.getFirstEdge(EdgeDirection.OUT);
				while (inc != null) {
					Vertex nextVertex = inc.getOmega();
					if (nextVertex == currentVertex) {
						cycleFound = true;
						break;
					}
					/*if ((subgraph.marksGraphElement(nextVertex)) && (!isMarked(nextVertex, state))) {
						PathSearchQueueEntry nextEntry = new PathSearchQueueEntry(
								nextVertex, state);
						queue.add(nextEntry);
					}*/
					inc = inc.getNextEdge(EdgeDirection.OUT);
				}
				currentEntry = queue.poll();
			}
			currentVertex = currentVertex.getNextVertex();
		} while ((!cycleFound) && (firstVertex != currentVertex));
		//removeVertexMarks();
		return new JValue(cycleFound);
	}

	public int getEstimatedCosts(ArrayList<Integer> inElements) {
		// TODO Auto-generated method stub
		return 0;
	}

	public double getSelectivity() {
		return 0.1;
	}

	public int getEstimatedCardinality(int inElements) {
		return 1;
	}

	public String getExpectedParameters() {
		return "([SubgraphTempAttribute])";
	}

	@Override
	public boolean isPredicate() {
		return true;
	}

}
