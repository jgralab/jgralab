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

package de.uni_koblenz.jgralab.greql2.funlib;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.graphmarker.BooleanGraphMarker;
import de.uni_koblenz.jgralab.greql2.evaluator.fa.DFA;
import de.uni_koblenz.jgralab.greql2.evaluator.fa.State;
import de.uni_koblenz.jgralab.greql2.evaluator.fa.Transition;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.WrongFunctionParameterException;
import de.uni_koblenz.jgralab.greql2.funlib.pathsearch.PathSearchQueueEntry;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueSet;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueType;

/**
 * Returns all vertices that are reachable from the given vertex with a path
 * that is accepted by the given dfa. A dfa is defined as regular path
 * expression.
 * 
 * <dl>
 * <dt><b>GReQL-signature</b></dt>
 * <dd><code>SET&lt;VERTEX&gt; reachableVertices(v:VERTEX, dfa: DFA)</code></dd>
 * <dd>&nbsp;</dd>
 * </dl>
 * <dl>
 * <dt></dt>
 * <dd>
 * <dl>
 * <dt><b>Parameters:</b></dt>
 * <dd><code>v</code> - vertex to start at</dd>
 * <dd><code>dfa</code> - a dfa that accepts regular path expressions</dd>
 * <dt><b>Returns:</b></dt>
 * <dd>a set of all vertices that are reachable from the given vertex with a
 * path that is accepted by the given dfa</dd>
 * <dd><code>Null</code> if one of the given parameters is <code>Null</code></dd>
 * </dl>
 * </dd>
 * </dl>
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class ReachableVertices extends Greql2Function {

	{
		JValueType[][] x = {
				{ JValueType.VERTEX, JValueType.DFA, JValueType.COLLECTION },
				{ JValueType.VERTEX, JValueType.DFA, JValueType.SUBGRAPH,
						JValueType.COLLECTION } };
		signatures = x;

		description = "Return all vertices that are reachable from vertex with path description.";
	}

	@Override
	public JValue evaluate(Graph graph, BooleanGraphMarker subgraph,
			JValue[] arguments) throws EvaluateException {
		if (checkArguments(arguments) == -1) {
			throw new WrongFunctionParameterException(this, arguments);
		}
		JValueSet resultSet = new JValueSet();
		DFA dfa = arguments[1].toDFA();
		Vertex startVertex = arguments[0].toVertex();
		BooleanGraphMarker[] markers = new BooleanGraphMarker[dfa.stateList
				.size()];
		for (State s : dfa.stateList) {
			markers[s.number] = new BooleanGraphMarker(graph);
		}
		Queue<PathSearchQueueEntry> queue = new LinkedList<PathSearchQueueEntry>();
		PathSearchQueueEntry currentEntry = new PathSearchQueueEntry(
				startVertex, dfa.initialState);
		markers[currentEntry.state.number].mark(currentEntry.vertex);
		queue.add(currentEntry);
		while (!queue.isEmpty()) {
			currentEntry = queue.poll();
			if (currentEntry.state.isFinal) {
				resultSet.add(new JValue(currentEntry.vertex,
						currentEntry.vertex));
			}
			// markers[currentEntry.state.number].mark(currentEntry.vertex);
			Edge inc = currentEntry.vertex.getFirstEdge();
			while (inc != null) {
				for (Transition currentTransition : currentEntry.state.outTransitions) {
					Vertex nextVertex = currentTransition.getNextVertex(
							currentEntry.vertex, inc);
					if (!markers[currentTransition.getEndState().number]
							.isMarked(nextVertex)) {
						if (currentTransition.accepts(currentEntry.vertex, inc,
								subgraph)) {
							PathSearchQueueEntry nextEntry = new PathSearchQueueEntry(
									nextVertex, currentTransition.getEndState());
							markers[nextEntry.state.number].mark(nextVertex);
							queue.add(nextEntry);
						}
					}
				}
				inc = inc.getNextEdge();
			}
		}
		return resultSet;
	}

	@Override
	public long getEstimatedCosts(ArrayList<Long> inElements) {
		return 100;
	}

	@Override
	public double getSelectivity() {
		return 1;
	}

	@Override
	public long getEstimatedCardinality(int inElements) {
		return 1;
	}

}
