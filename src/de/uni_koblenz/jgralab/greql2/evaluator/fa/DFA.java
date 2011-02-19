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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;

/**
 * This class models a deterministic finite automaton. The automaton is not
 * really deterministic, because there may exist more than one transition at a
 * single state, which may fire for a given edge oder vertex. For instance,
 * there may exist a tranistion which accepts all edges that are of type
 * "isExprOf" and one transition, which accepts the edge "e", which is a
 * variable and gets several values during evaluation. Now, if e is an edge of
 * type "isExprOf", both transitions may fire. So, the automaton is not
 * deterministic. But there may exists no two transitions at a state that
 * accepts the same edges, for instance, there will be never two edges that
 * accept all "isExprOf" edges.
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class DFA extends FiniteAutomaton {

	@Override
	public DFA getDFA() {
		return this;
	}

	// public static int count = 0;

	/**
	 * creates a new DFA from the given NFA. Removes all epsilon transitions and
	 * uses Myhill-Construction to create the DFA out of the NFA.
	 */
	public DFA(NFA nfa) throws EvaluateException {
		// count++;
		finalStates = new ArrayList<State>();
		transitionList = new ArrayList<Transition>();
		stateList = new ArrayList<State>();
		eleminateEpsilonTransitions(nfa);
		myhillConstruction(nfa);
		removeDuplicateTransitions();
		// now set the state numbers and also the final-attribute of the states
		updateStateAttributes();
		// printAscii2();
	}

	private void removeDuplicateTransitions() {
		Set<Transition> duplicateTransitions = new HashSet<Transition>();
		for (State s : this.stateList) {
			for (int i = 0; i < s.outTransitions.size() - 1; i++) {
				Transition t1 = s.outTransitions.get(i);
				for (int j = i + 1; j < s.outTransitions.size(); j++) {
					Transition t2 = s.outTransitions.get(j);
					if ((t1.endState == t2.endState)
							&& (t1.startState == t2.startState)
							&& (t1.equalSymbol(t2))) {
						duplicateTransitions.add(t2);
					}
				}

			}
		}
		for (Transition t : duplicateTransitions) {
			transitionList.remove(t);
			t.delete();
		}
	}

	/**
	 * removes the given epsilon transition and replace it
	 */
	private void removeEpsilonTransition(NFA nfa, Transition epsilonTransition) {
		// a epsilon-transition X->Y was found ...
		// first check if this transition starts and ends at the
		// same state, if yes, don't add new transitions
		State X = epsilonTransition.startState;
		State Y = epsilonTransition.endState;
		if (X != Y) {
			// if there are no more other incomming transitions at the end
			// state,
			// both state could be unified
			if ((Y.inTransitions.size() == 1) && (nfa.initialState != Y)) {
				Iterator<Transition> iter = Y.outTransitions.iterator();
				while (iter.hasNext()) {
					Transition t = iter.next();
					t.startState = X;
					X.outTransitions.add(t);
					iter.remove();
				}
				nfa.stateList.remove(Y);
				nfa.finalStates.remove(Y);
			} else {
				// copies all transitions Y->Z and generates transitions
				// X->Z out of them
				for (Transition currentTransition : Y.outTransitions) {
					if (!(currentTransition.isEpsilon() && currentTransition.endState == X)) {
						Transition newTransition = currentTransition
								.copy(false);
						nfa.transitionList.add(newTransition);
						newTransition.setStartState(X);
						newTransition.setEndState(newTransition.endState);
					}
				}
			}
		}
		nfa.transitionList.remove(epsilonTransition);
		epsilonTransition.delete();
		// check if Y ist final, if it is, X also gets final
		if (Y.isFinal) {
			if (!X.isFinal) {
				X.isFinal = true;
				nfa.finalStates.add(X);
			}
		}
	}

	/**
	 * eleminates all epsilon-transitions in the given NFA and replaces them
	 * with normal transitions
	 */
	private void eleminateEpsilonTransitions(NFA nfa) {
		boolean containsEpsilonTransitions = true;
		while (containsEpsilonTransitions) {
			containsEpsilonTransitions = false;
			int curTransNr = 0;
			while ((curTransNr < nfa.transitionList.size())) {
				Transition currentTransition = nfa.transitionList
						.get(curTransNr);
				if (currentTransition.isEpsilon()) {
					removeEpsilonTransition(nfa, currentTransition);
					containsEpsilonTransitions = true;
				} else {
					curTransNr++;
				}
			}

		}
	}

	/**
	 * constructs the DEA via powerset-construction (Myhill-Construction)
	 */
	private void myhillConstruction(NFA nfa) {
		initialState = new DFAState(nfa.initialState);
		if (nfa.initialState.isFinal) {
			initialState.isFinal = true;
			finalStates.add(initialState);
		}
		stateList.add(initialState);
		int i = 0;
		while (i < stateList.size()) {
			State currentState = stateList.get(i);
			for (int j = 0; j < currentState.outTransitions.size(); j++) {
				Transition firstTransition = currentState.outTransitions.get(j);
				DFAState newDFAState = new DFAState(firstTransition.endState);
				transitionList.addAll(newDFAState
						.addRepresentedState(firstTransition.endState));
				for (int k = j + 1; k < currentState.outTransitions.size(); k++) {
					Transition secondTransition = currentState.outTransitions
							.get(k);
					if (firstTransition.equalSymbol(secondTransition)) {
						// check if this two transitions end in the same state
						if (firstTransition.endState != secondTransition.endState) {
							// this two transitions accept the same symbol, but
							// have different end states,
							// so add the end state of the second one to the
							// list
							// of represented states of the first's end state
							transitionList
									.addAll(newDFAState
											.addRepresentedState(secondTransition.endState));
						}
						// delete the second transition
						secondTransition.delete();
						k--;
					}

				}
				firstTransition.setEndState(newDFAState);
				// now the newDEAStates represents a couple of NFA states, check
				// if there exist also a state in the DFA which represents the
				// same NFA states
				boolean foundSameState = false;
				for (int k = 0; k < stateList.size(); k++) {
					DFAState stateToCheck = (DFAState) stateList.get(k);
					if (stateToCheck.representSameNFAStates(newDFAState)) {
						// stateToCheck represents the same NFA states as
						// current state
						// change the endState of all inTransitions of
						// newDEAState to stateToCheck
						foundSameState = true;
						ArrayList<Transition> inTransList = new ArrayList<Transition>(
								newDFAState.inTransitions);
						Iterator<Transition> iter = inTransList.iterator();
						while (iter.hasNext()) {
							iter.next().setEndState(stateToCheck);
						}
					}
				}
				if (!foundSameState) {
					stateList.add(newDFAState);
					// maybe the newDFAState contains a final state in the nfa,
					// then the newDFAState also gets final
					if (newDFAState.containsFinalStateOfNFA(nfa)) {
						newDFAState.isFinal = true;
						finalStates.add(newDFAState);
					}
				}
			}
			i++;
		}
	}

}
