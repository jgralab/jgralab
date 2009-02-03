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

package de.uni_koblenz.jgralab.greql2.evaluator.fa;

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

	private static Logger logger = Logger.getLogger(EpsilonTransition.class
			.getName());

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
		logger
				.info("|||||||||||||||||||||||  Automaton: |||||||||||||||||||||||||");
		Iterator<State> stateIter = stateList.iterator();
		while (stateIter.hasNext()) {
			State currentState = stateIter.next();
			logger.info("[" + stateList.indexOf(currentState) + "]");
			Iterator<Transition> transitionIter = currentState.outTransitions
					.iterator();
			while (transitionIter.hasNext()) {
				Transition currentTransition = transitionIter.next();
				int stateNumber = stateList.indexOf(currentTransition
						.getEndState());
				if (finalStates.contains(currentTransition.getEndState())) {
					logger.info("      ----" + currentTransition.edgeString()
							+ "--->    [[" + stateNumber + "]]");
				} else {
					logger.info("      ----" + currentTransition.edgeString()
							+ "--->    [" + stateNumber + "]");
				}
			}
			logger.info("\n--------------------------");
		}
		logger
				.info("||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||| ");
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
		finalStates = new ArrayList<State>();
		stateList = new ArrayList<State>();
		transitionList = new ArrayList<Transition>();
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
