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
import java.util.BitSet;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.graphmarker.AbstractGraphMarker;
import de.uni_koblenz.jgralab.greql2.evaluator.fa.DFA;
import de.uni_koblenz.jgralab.greql2.evaluator.fa.State;
import de.uni_koblenz.jgralab.greql2.evaluator.fa.Transition;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.WrongFunctionParameterException;
import de.uni_koblenz.jgralab.greql2.funlib.pathsearch.VertexStateQueue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueImpl;
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

	public static boolean PRINT_STOP_VERTICES = false;

	{
		JValueType[][] x = {
				{ JValueType.VERTEX, JValueType.AUTOMATON,
						JValueType.COLLECTION },
				{ JValueType.VERTEX, JValueType.AUTOMATON, JValueType.MARKER,
						JValueType.COLLECTION } };
		signatures = x;

		description = "Returns all vertices that are reachable from vertex with a path description.";

		Category[] c = { Category.GRAPH,
				Category.PATHS_AND_PATHSYSTEMS_AND_SLICES };
		categories = c;
	}

	@Override
	public JValue evaluate(Graph graph,
			AbstractGraphMarker<AttributedElement> subgraph, JValue[] arguments)
			throws EvaluateException {
		DFA dfa = null;
		switch (checkArguments(arguments)) {
		case 0:
		case 1:
			dfa = arguments[1].toAutomaton().getDFA();
			break;
		default:
			throw new WrongFunctionParameterException(this, arguments);
		}
		Vertex startVertex = arguments[0].toVertex();

		return search(startVertex, dfa, subgraph);
	}

	public static final JValueImpl search(Vertex startVertex, DFA dfa,
			AbstractGraphMarker<AttributedElement> subgraph) {
		JValueSet resultSet = new JValueSet();

		BitSet[] markedElements = new BitSet[dfa.stateList.size()];

		for (State s : dfa.stateList) {
			markedElements[s.number] = new BitSet();
		}
		VertexStateQueue queue = new VertexStateQueue();
		markedElements[dfa.initialState.number].set(startVertex.getId());
		queue.put(startVertex, dfa.initialState);
		Vertex vertex = null;
		State state = null;
		Transition currentTransition = null;
		Vertex nextVertex = null;
		while (queue.hasNext()) {
			vertex = queue.currentVertex;
			state = queue.currentState;
			if (state.isFinal) {
				resultSet.add(new JValueImpl(vertex, vertex));
			}
			Edge inc = vertex.getFirstEdge();
			while (inc != null) {
				int size = state.outTransitions.size();
				for (int i = 0; i < size; i++) {
					currentTransition = state.outTransitions.get(i);
					nextVertex = currentTransition.getNextVertex(vertex, inc);
					if (!markedElements[currentTransition.endState.number]
							.get(nextVertex.getId())) {
						if (currentTransition.accepts(vertex, inc, subgraph)) {
							markedElements[currentTransition.endState.number]
									.set(nextVertex.getId());
							queue.put(nextVertex, currentTransition.endState);
						}
					}
				}
				inc = inc.getNextEdge();
			}
			if (PRINT_STOP_VERTICES) {
				if (state.isFinal) {
					System.out.println("Vertex " + vertex
							+ " is reachable by path");
				} else {
					System.out.println("Vertex " + vertex
							+ " is not reachable by path");
					System.out.println("    Edges at vertex");
					for (Edge e : vertex.incidences()) {
						System.out.println("        "
								+ (e.isNormal() ? "-->" : "<--")
								+ e.getAttributedElementClass().getSimpleName()
								+ " " + e.getThatRole());
					}
					System.out
							.println("    Transitions that failed in accepting the pair of edge and vertex");
					for (Transition t : state.outTransitions) {
						System.out.println("        " + t.prettyPrint());
					}
				}
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
