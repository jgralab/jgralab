/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2008 Institute for Software Technology
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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;

import de.uni_koblenz.jgralab.BooleanGraphMarker;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphMarker;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;

/**
 * Checks if the current graph or subgraph is cycle-free.
 *
 * <dl>
 * <dt><b>GReQL-signature</b></dt>
 * <dd><code>BOOLEAN isAcyclic()</code></dd>
 * <dd><code>BOOLEAN isAcyclic(graph : GRAPH)</code></dd>
 * <dd>&nbsp;</dd>
 * </dl>
 * <dl>
 * <dt></dt>
 * <dd>
 * <dl>
 * <dt><b>Parameters:</b></dt>
 * <dd><code>graph</code> - the graph to be checked (optional)</dd>
 * <dt><b>Returns:</b></dt>
 * <dd><code>true</code> if the current or given graph or subgraph is acyclic</dd>
 * <dd><code>false</code> otherwise</dd>
 * </dl>
 * </dd>
 * </dl>
 *
 * @author Daniel Bildhauer <dbildh@uni-koblenz.de> Summer 2006, Diploma Thesis
 *
 */
public class IsAcyclic implements Greql2Function {

	public JValue evaluate(Graph graph, BooleanGraphMarker subgraph,
			JValue[] arguments) throws EvaluateException {
		if (arguments.length > 0) {
			subgraph = arguments[0].toSubgraphTempAttribute();
		}

		Queue<Vertex> queue = new ArrayDeque<Vertex>();
		GraphMarker<Integer> marker = new GraphMarker<Integer>(graph);
		int vCount = 0;
		for (Vertex v : graph.vertices()) {
			if (subgraph == null || subgraph.isMarked(v)) {
				int inDegree = 0;
				for (Edge inc : v.incidences(EdgeDirection.IN)) {
					if (subgraph == null || subgraph.isMarked(inc)) {
						inDegree++;
					}
				}
				marker.mark(v, inDegree);
				if (inDegree == 0) {
					queue.add(v);
				}
				vCount++;
			}
		}

		while (!queue.isEmpty()) {
			Vertex v = queue.poll();
			vCount--;
			for (Edge inc : v.incidences(EdgeDirection.OUT)) {
				if (subgraph == null || subgraph.isMarked(inc)) {
					Vertex omega = inc.getOmega();
					assert subgraph == null || subgraph.isMarked(omega);
					int hugo = marker.getMark(omega) - 1;
					marker.mark(omega, hugo);
					if (hugo == 0) {
						queue.offer(omega);
					}
				}
			}
		}

		return new JValue(vCount == 0);

	}

	public long getEstimatedCosts(ArrayList<Long> inElements) {
		return 20;
	}

	public double getSelectivity() {
		return 0.1;
	}

	public long getEstimatedCardinality(int inElements) {
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
