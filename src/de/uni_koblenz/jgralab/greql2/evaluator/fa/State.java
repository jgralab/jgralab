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

import java.util.ArrayList;

/**
 * Models a state of the finite automaton. Is baseclass for DFAState, which
 * modells a state in the DFA.
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class State {

	/**
	 * the number of the state in the automaton. Beware, this number is not
	 * valid during the nfa-construction or the dfa construction, so use it with
	 * care. It's safe to use it after the dfa-construction has been completed
	 */
	public int number;

	/**
	 * true if the state is final. Beware, this ist not valid during nfa or dfa
	 * construction, so use it with care. It's safe to use it after the
	 * dfa-construction has been completed
	 */
	public boolean isFinal;

	/**
	 * the list of inTransitions
	 */
	public ArrayList<Transition> inTransitions;

	/**
	 * the list of outTransitions
	 */
	public ArrayList<Transition> outTransitions;

	/**
	 * constructs a new state without any transitions connected
	 * 
	 */
	public State() {
		inTransitions = new ArrayList<Transition>();
		outTransitions = new ArrayList<Transition>();
	}

	/**
	 * adds the given transition to the outTransitions
	 */
	public void addOutTransition(Transition t) {
		outTransitions.add(t);
	}

	/**
	 * adds the given Transition to the inTransitions
	 * 
	 * @param t
	 */
	public void addInTransition(Transition t) {
		inTransitions.add(t);
	}

	/**
	 * removes the given transition from the outTransitions
	 * 
	 * @param t
	 */
	public void removeOutTransition(Transition t) {
		outTransitions.remove(t);
	}

	/**
	 * removes the given transition from the inTransitions
	 * 
	 * @param t
	 */
	public void removeInTransition(Transition t) {
		inTransitions.remove(t);
	}
}
