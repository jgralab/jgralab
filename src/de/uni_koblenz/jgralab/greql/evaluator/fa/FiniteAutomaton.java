/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2014 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 *
 * For bug reports, documentation and further information, visit
 *
 *                         https://github.com/jgralab/jgralab
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

package de.uni_koblenz.jgralab.greql.evaluator.fa;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Logger;

/**
 * This is the base class of NFA and DFA. Contains attributes and methods both
 * subclasses need.
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public abstract class FiniteAutomaton {

	private static Logger logger = Logger.getLogger(FiniteAutomaton.class
			.getPackage().getName());

	public abstract DFA getDFA();

	/*
	 * the one and only initial state of this automat
	 */
	public State initialState;

	/*
	 * the list of final states, states that are in this list are also in the
	 * state list
	 */
	public ArrayList<State> finalStates;

	/*
	 * a list of all states
	 */
	public ArrayList<State> stateList;

	/*
	 * a list of all transitions
	 */
	public ArrayList<Transition> transitionList;

	/**
	 * prints this automaton as ascii-stream
	 */
	public void printAscii() {
		logger.info("|||||||||||||||||||||||  Automaton: |||||||||||||||||||||||||");
		Iterator<State> stateIter = stateList.iterator();
		while (stateIter.hasNext()) {
			State currentState = stateIter.next();
			logger.info("[" + stateList.indexOf(currentState) + "]");
			Iterator<Transition> transitionIter = currentState.outTransitions
					.iterator();
			while (transitionIter.hasNext()) {
				Transition currentTransition = transitionIter.next();
				int stateNumber = stateList.indexOf(currentTransition.endState);
				if (finalStates.contains(currentTransition.endState)) {
					logger.info("      ----" + currentTransition.edgeString()
							+ "--->    [[" + stateNumber + "]]");
				} else {
					logger.info("      ----" + currentTransition.edgeString()
							+ "--->    [" + stateNumber + "]");
				}
			}
			logger.info("\n--------------------------");
		}
		logger.info("||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||| ");
	}

	public void printAscii2() {
		for (State currentState : stateList) {
			if (currentState.isFinal) {
				System.out.println("State: [[" + currentState.number + "]]");
			} else {
				System.out.println("State: [" + currentState.number + "]");
			}
			for (Transition currentTransition : currentState.outTransitions) {
				int stateNumber = currentTransition.endState.number;
				if (finalStates.contains(currentTransition.endState)) {
					System.out.println("      ----"
							+ currentTransition.edgeString() + "--->    [["
							+ stateNumber + "]]");
				} else {
					System.out.println("      ----"
							+ currentTransition.edgeString() + "--->    ["
							+ stateNumber + "]");
				}
			}
			System.out.println("\n--------------------------");
		}
	}

	/**
	 * returns true if the given state if final
	 */
	public boolean isFinal(State s) {
		return finalStates.contains(s);
	}

	/**
	 * creates a new instance
	 */
	public FiniteAutomaton() {
		finalStates = new ArrayList<>();
		stateList = new ArrayList<>();
		transitionList = new ArrayList<>();
	}

	/**
	 * sets the attributes "number" and "isFinal" for all states to the right
	 * values, so that each number is unique and only these state are marked as
	 * "final" which are part of the final list. This makes path-search faster
	 */
	protected void updateStateAttributes() {
		Iterator<State> iter = stateList.iterator();
		int i = 0;
		while (iter.hasNext()) {
			State s = iter.next();
			s.isFinal = false;
			s.number = i++;
		}
		iter = finalStates.iterator();
		while (iter.hasNext()) {
			iter.next().isFinal = true;
		}
	}

}
