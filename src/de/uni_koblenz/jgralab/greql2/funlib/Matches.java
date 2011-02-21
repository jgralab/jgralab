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

package de.uni_koblenz.jgralab.greql2.funlib;

import java.util.ArrayList;
import java.util.Iterator;
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
import de.uni_koblenz.jgralab.greql2.funlib.pathsearch.PathSystemQueueEntry;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueBoolean;
import de.uni_koblenz.jgralab.greql2.jvalue.JValuePath;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueType;

/**
 * Checks if the given dfa matches the given path. A dfa is defined as regular
 * path expression.
 * 
 * <dl>
 * <dt><b>GReQL-signature</b></dt>
 * <dd><code>BOOL matches(p1:PATH, dfa:DFA)</code></dd>
 * <dd>&nbsp;</dd>
 * </dl>
 * <dl>
 * <dt></dt>
 * <dd>
 * <dl>
 * <dt><b>Parameters:</b></dt>
 * <dd><code>p1</code> - path to be checked against</dd>
 * <dd><code>dfa</code> - a dfa that accepts regular path expressions</dd>
 * <dt><b>Returns:</b></dt>
 * <dd><code>true</code> if the given regular path expression matches the given
 * path</dd>
 * <dd><code>Null</code> if one of the given parameters is <code>Null</code></dd>
 * <dd><code>false</code> otherwise</dd>
 * </dl>
 * </dd>
 * </dl>
 * 
 * @author ist@uni-koblenz.de
 * 
 */

public class Matches extends Greql2Function {

	{
		JValueType[][] x = { { JValueType.PATH, JValueType.AUTOMATON,
				JValueType.BOOL } };
		signatures = x;

		description = "Returns true iff the given path description matches the given path.";

		Category[] c = { Category.PATHS_AND_PATHSYSTEMS_AND_SLICES };
		categories = c;
	}

	@Override
	public JValue evaluate(Graph graph,
			AbstractGraphMarker<AttributedElement> subgraph, JValue[] arguments)
			throws EvaluateException {
		DFA dfa = null;
		switch (checkArguments(arguments)) {
		case 0:
			dfa = arguments[1].toAutomaton().getDFA();
			break;
		default:
			throw new WrongFunctionParameterException(this, arguments);
		}

		JValuePath path = arguments[0].toPath();

		Queue<PathSystemQueueEntry> queue = new LinkedList<PathSystemQueueEntry>();
		PathSystemQueueEntry currentEntry = new PathSystemQueueEntry(path
				.getStartVertex(), dfa.initialState, null, null, 0);
		BooleanGraphMarker[] markers = new BooleanGraphMarker[dfa.stateList
				.size()];
		for (State s : dfa.stateList) {
			markers[s.number] = new BooleanGraphMarker(graph);
		}
		while (currentEntry != null) {
			Iterator<Transition> transitionIter = currentEntry.state.outTransitions
					.iterator();
			// get the one and only edge
			Edge edge = path.edgeTrace().get(currentEntry.distanceToRoot);
			while (transitionIter.hasNext()) {
				Transition currentTransition = transitionIter.next();
				Vertex nextVertex = currentTransition.getNextVertex(
						currentEntry.vertex, edge);
				if (!markers[currentTransition.endState.number]
						.isMarked(nextVertex)) {
					if (currentTransition.accepts(currentEntry.vertex, edge,
							null)) {
						markers[currentTransition.endState.number]
								.mark(nextVertex);
						PathSystemQueueEntry nextEntry = new PathSystemQueueEntry(
								nextVertex, currentTransition.endState, edge,
								currentEntry.state,
								currentEntry.distanceToRoot + 1);
						queue.add(nextEntry);
					}
				}
			}
			currentEntry = queue.poll();
		}
		return JValueBoolean.getValue(false);
	}

	@Override
	public long getEstimatedCosts(ArrayList<Long> inElements) {
		return 40;
	}

	@Override
	public double getSelectivity() {
		return 0.1;
	}

	@Override
	public long getEstimatedCardinality(int inElements) {
		return 1;
	}

}
