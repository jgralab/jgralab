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

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.graphmarker.AbstractGraphMarker;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;

/**
 * Is baseclass of all transitions. Has one start and one endstate.
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public abstract class Transition {

	public enum AllowedEdgeDirection {
		IN, OUT, ANY
	}

	/**
	 * The state where this transition starts
	 */
	protected State startState;

	/**
	 * @return the start state
	 */
	public final State getStartState() {
		return startState;
	}

	public void setStartState(State state) {
		startState.removeOutTransition(this);
		startState = state;
		startState.addOutTransition(this);
	}

	/**
	 * The state where this transition ends
	 */
	public State endState;

	/**
	 * Sets the new endstate of this transition
	 * 
	 * @param state
	 */
	public void setEndState(State state) {
		endState.removeInTransition(this);
		endState = state;
		endState.addInTransition(this);
	}

	/**
	 * creates a new transition from start to end
	 * 
	 * @param start
	 *            state where this transition should start
	 * @param end
	 *            state where this transition should end
	 */
	public Transition(State start, State end) {
		startState = start;
		endState = end;
		startState.addOutTransition(this);
		endState.addInTransition(this);
	}

	/**
	 * Copy-constructor, creates a copy of the given transition.
	 * 
	 * @param addToStates
	 *            if true, the transition gets added to the in/out
	 *            transitionlist of start and end state. Beware, if you use one
	 *            of these lists in an iterator and create copies for all
	 *            transitions, it will result in a concurntlyModifiedException
	 */
	protected Transition(Transition t, boolean addToStates) {
		startState = t.startState;
		endState = t.endState;
		if (addToStates) {
			startState.addOutTransition(this);
			endState.addInTransition(this);
		}
	}

	/**
	 * returns a string which describes the edge
	 */
	public abstract String edgeString();

	/** a pretty-printed string for this tranistion */
	public abstract String prettyPrint();

	/**
	 * returns a copy of this transition
	 * 
	 * @param addToStates
	 *            if this parameter is set to true, the transition will be added
	 *            to the start and endstate of the original transition Beware,
	 *            this may lead to an ConcurrentModificatonException if used in
	 *            an iterator over the transitions of a state
	 */
	public abstract Transition copy(boolean addToStates);

	/**
	 * deletes this transitions, that means, removes them from the outList of
	 * the startState and from the inList of the endState
	 */
	public void delete() {
		startState.removeOutTransition(this);
		endState.removeInTransition(this);
	}

	/**
	 * @return true if this transition and the given one accept exactly the same
	 *         symbols
	 */
	public abstract boolean equalSymbol(Transition t);

	/**
	 * reverses this transition, that means, the former end state gets the new
	 * start state and vice versa,
	 */
	public void reverse() {
		State s = startState;
		startState.removeOutTransition(this);
		endState.removeInTransition(this);
		startState = endState;
		endState = s;
		endState.addInTransition(this);
		startState.addOutTransition(this);
	}

	/**
	 * returns true if this transition is an epsilon transition
	 */
	public abstract boolean isEpsilon();

	/**
	 * returns true if this transition accepts the given combination of Vertex
	 * and Edge and if both are part of the given subgraph
	 */
	public abstract boolean accepts(Vertex v, Edge e,
			AbstractGraphMarker<AttributedElement> subgraph)
			throws EvaluateException;

	/**
	 * returns the vertex of the datagraph which can be visited after this
	 * transition has fired. This can be either the vertex itself or the vertex
	 * at the end of the edge
	 */
	public abstract Vertex getNextVertex(Vertex v, Edge e);

}
