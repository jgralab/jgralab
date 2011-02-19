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

package de.uni_koblenz.jgralab.greql2.evaluator.fa;

import java.util.logging.Logger;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.graphmarker.AbstractGraphMarker;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;

/**
 * A epsilon transition which may fire without any restrictions. Epsilon
 * transitions are used during thompson-construction of the NFAs, but they will
 * be eliminated before path search.
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class EpsilonTransition extends Transition {

	private static Logger logger = Logger.getLogger(EpsilonTransition.class
			.getName());

	/**
	 * creates a new epsilon-transition from start to end
	 * 
	 * @param start
	 *            state where this transition should start
	 * @param end
	 *            state where this transition should end
	 */
	public EpsilonTransition(State start, State end) {
		super(start, end);
	}

	/**
	 * Copy-constructor, creates a copy of the given transition
	 */
	protected EpsilonTransition(EpsilonTransition t, boolean addToStates) {
		super(t, addToStates);
	}

	/**
	 * returns a copy of this transition
	 */
	@Override
	public Transition copy(boolean addToStates) {
		return new EpsilonTransition(this, addToStates);
	}

	@Override
	public boolean equalSymbol(Transition t) {
		if (t instanceof EpsilonTransition) {
			return true;
		}
		return false;
	}

	@Override
	public boolean isEpsilon() {
		return true;
	}

	/**
	 * This method should not be called because all epsilon-transitions should
	 * be eliminated before the pathsearch strarts
	 */
	@Override
	public boolean accepts(Vertex v, Edge e,
			AbstractGraphMarker<AttributedElement> subgraph)
			throws EvaluateException {
		logger.info("Id of this epsilon transition is: " + this);
		// GreqlEvaluator.println("In Number of this transition is : " +
		// this.endState.inTransitions.indexOf(this));
		// GreqlEvaluator.println("Out Number of this transition is : " +
		// this.startState.inTransitions.indexOf(this));
		throw new EvaluateException(
				"EpsilonTransition.accepts(...) has been called. That should not happen, there should be no epsilon-transitions in the DFA used for path search. Check the DFA-Constructor");
	}

	/**
	 * returns the vertex of the datagraph which can be visited after this
	 * transition has fired. This is the vertex at the end of the edge
	 */
	@Override
	public Vertex getNextVertex(Vertex v, Edge e) {
		if (e.getAlpha() == v) {
			return e.getOmega();
		} else {
			return e.getAlpha();
		}
	}

	/**
	 * returns a string which describes the edge
	 */
	@Override
	public String edgeString() {
		String desc = "EpsilonTransition";
		return desc;
	}

	@Override
	public String prettyPrint() {
		return "epsilon";
	}

}
