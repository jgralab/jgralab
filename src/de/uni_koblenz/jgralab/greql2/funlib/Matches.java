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

import de.uni_koblenz.jgralab.greql2.evaluator.fa.DFA;
import de.uni_koblenz.jgralab.greql2.evaluator.fa.NFA;
import de.uni_koblenz.jgralab.greql2.evaluator.fa.State;
import de.uni_koblenz.jgralab.greql2.evaluator.fa.Transition;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.WrongFunctionParameterException;
import de.uni_koblenz.jgralab.greql2.funlib.pathsearch.PathSearch;
import de.uni_koblenz.jgralab.greql2.funlib.pathsearch.PathSystemQueueEntry;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValuePath;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import de.uni_koblenz.jgralab.BooleanGraphMarker;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;

/**
 * Checks if the given dfa matches the given path. A dfa is defined as regular
 * path expression.
 * 
 * <dl>
 * <dt><b>GReQL-signature</b></dt>
 * <dd><code>BOOLEAN matches(p1:PATH, dfa:DFA)</code></dd>
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
 * <dd><code>true</code> if the given regular path expression matches the
 * given path</dd>
 * <dd><code>Null</code> if one of the given parameters is <code>Null</code></dd>
 * <dd><code>false</code> otherwise</dd>
 * </dl>
 * </dd>
 * </dl>
 * 
 * @author Daniel Bildhauer <dbildh@uni-koblenz.de> Summer 2006, Diploma Thesis
 * 
 */

public class Matches extends PathSearch implements Greql2Function {

	public JValue evaluate(Graph graph, BooleanGraphMarker subgraph,
			JValue[] arguments) throws EvaluateException {
		JValuePath path;
		DFA dfa;
		try {
			path = arguments[0].toPath();
			if (arguments[1].isNFA()) {
				NFA nfa = arguments[1].toNFA();
				dfa = new DFA(nfa);
			} else
				dfa = arguments[1].toDFA();
		} catch (Exception ex) {
			throw new WrongFunctionParameterException(this, null, arguments);
		}
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
				if (!markers[currentTransition.getEndState().number]
						.isMarked(nextVertex)) {
					if (currentTransition.accepts(currentEntry.vertex, edge,
							null)) {
						markers[currentTransition.getEndState().number]
								.mark(nextVertex);
						PathSystemQueueEntry nextEntry = new PathSystemQueueEntry(
								nextVertex, currentTransition.getEndState(),
								edge, currentEntry.state,
								currentEntry.distanceToRoot + 1);
						queue.add(nextEntry);
					}
				}
			}
			currentEntry = queue.poll();
		}
		return new JValue(false);
	}

	public int getEstimatedCosts(ArrayList<Integer> inElements) {
		return 40;
	}

	public double getSelectivity() {
		return 0.1;
	}

	public int getEstimatedCardinality(int inElements) {
		return 1;
	}

	public String getExpectedParameters() {
		return "(JValuePath, DFA or NFA)";
	}

	@Override
	public boolean isPredicate() {
		return true;
	}
}
