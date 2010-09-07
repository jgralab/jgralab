/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2010 Institute for Software Technology
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

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.graphmarker.AbstractGraphMarker;
import de.uni_koblenz.jgralab.graphmarker.BooleanGraphMarker;
import de.uni_koblenz.jgralab.greql2.evaluator.fa.DFA;
import de.uni_koblenz.jgralab.greql2.evaluator.fa.State;
import de.uni_koblenz.jgralab.greql2.evaluator.fa.Transition;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.WrongFunctionParameterException;
import de.uni_koblenz.jgralab.greql2.funlib.pathsearch.PathSearchQueueEntry;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueImpl;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueType;

/**
 * Checks if there exists a path from the first given vertex to the second given
 * vertex in the graph, that is accepted by the given dfa. A dfa is defined as
 * regular path expression.
 * 
 * <dl>
 * <dt><b>GReQL-signature</b></dt>
 * <dd>
 * <code>BOOL isReachable(startVertex:VERTEX, targetVertex:VERTEX, dfa: DFA)</code>
 * </dd>
 * <dd>&nbsp;</dd>
 * </dl>
 * <dl>
 * <dt></dt>
 * <dd>
 * <dl>
 * <dt><b>Parameters:</b></dt>
 * <dd><code>startVertex</code> - first vertex of a potential path</dd>
 * <dd><code>targetVertex</code> - last vertex of a potential path</dd>
 * <dd><code>dfa</code> - a dfa that accepts regular path expressions</dd>
 * <dt><b>Returns:</b></dt>
 * <dd><code>true</code> if at least one path exists from the first given vertex
 * to the second given vertex and that is accepted by the given dfa</dd>
 * <dd><code>Null</code> if one of the given parameters is <code>Null</code></dd>
 * <dd><code>false</code> otherwise</dd>
 * </dl>
 * </dd>
 * </dl>
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class IsReachable extends Greql2Function {

	{
		JValueType[][] x = {
				{ JValueType.VERTEX, JValueType.VERTEX, JValueType.AUTOMATON,
						JValueType.BOOL } };
		signatures = x;

		description = "Returns true iff there is a path from the first to the second vertex.\n"
				+ "The third parameter is a path description, describing the structure\n"
				+ "of the path.  If you are interested in only somehow connections,\n"
				+ "use <->* here.";

		Category[] c = { Category.GRAPH,
				Category.PATHS_AND_PATHSYSTEMS_AND_SLICES };
		categories = c;
	}

	@Override
	public JValue evaluate(Graph graph, AbstractGraphMarker<AttributedElement> subgraph,
			JValue[] arguments) throws EvaluateException {

		if (checkArguments(arguments) == -1) {
			throw new WrongFunctionParameterException(this, arguments);
		}
		Vertex startVertex = arguments[0].toVertex();
		Vertex endVertex = arguments[1].toVertex();
		DFA dfa = arguments[2].toAutomaton().getDFA();

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
			if (currentEntry.vertex == endVertex && currentEntry.state.isFinal) {
				return new JValueImpl(true, startVertex);
			}
			Edge inc = currentEntry.vertex.getFirstEdge();
			while (inc != null) {
				for (Transition currentTransition : currentEntry.state.outTransitions) {
					Vertex nextVertex = currentTransition.getNextVertex(
							currentEntry.vertex, inc);
					if (!markers[currentTransition.endState.number]
							.isMarked(nextVertex)) {
						if (currentTransition.accepts(currentEntry.vertex, inc,
								subgraph)) {
							PathSearchQueueEntry nextEntry = new PathSearchQueueEntry(
									nextVertex, currentTransition.endState);
							markers[nextEntry.state.number].mark(nextVertex);
							queue.add(nextEntry);
						}
					}
				}
				inc = inc.getNextEdge();
			}
		}
		return new JValueImpl(false, startVertex);
	}

	@Override
	public long getEstimatedCosts(ArrayList<Long> inElements) {
		return 50;
	}

	@Override
	public double getSelectivity() {
		return 0.01;
	}

	@Override
	public long getEstimatedCardinality(int inElements) {
		return 1;
	}

}
