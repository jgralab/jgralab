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

	/**
	 * creates a new DFA from the given NFA. Removes all epsilon transitions and
	 * uses Myhill-Construction to create the DFA out of the NFA.
	 */
	public DFA(NFA nfa) throws EvaluateException {
		finalStates = new ArrayList<State>();
		transitionList = new ArrayList<Transition>();
		stateList = new ArrayList<State>();
		eleminateEpsilonTransitions(nfa);
		myhillConstruction(nfa);
		// now set the state numbers and also the final-attribute of the states
		updateStateAttributes();
	}

	/**
	 * removes the given epsilon transition and replace it
	 */
	private void removeEpsilonTransition(NFA nfa, Transition epsilonTransition) {
		// a epsilon-transition X->Y was found ...
		// first check if this transition starts and ends at the
		// same state, if yes, don't add new transitions
		State X = epsilonTransition.getStartState();
		State Y = epsilonTransition.getEndState();
		if (X != Y) {
			// copies all transitions Y->Z and generates transitions
			// X->Z out of them
			Iterator<Transition> outTransitionIter = Y.outTransitions
					.iterator();
			while (outTransitionIter.hasNext()) {
				Transition newTransition = outTransitionIter.next().copy(false);
				nfa.transitionList.add(newTransition);
				newTransition.setStartState(X);
				newTransition.setEndState(newTransition.getEndState());
			}

		}
		nfa.transitionList.remove(epsilonTransition);
		epsilonTransition.delete();
		// check if Y ist final, if it is, X also gets final
		if (nfa.finalStates.contains(Y)) {
			if (!nfa.finalStates.contains(X))
				nfa.finalStates.add(X);
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
		stateList.add(initialState);
		int i = 0;
		while (i < stateList.size()) {
			State currentState = stateList.get(i);
			for (int j = 0; j < currentState.outTransitions.size(); j++) {
				Transition firstTransition = currentState.outTransitions.get(j);
				DFAState newDFAState = new DFAState(firstTransition
						.getEndState());
				transitionList.addAll(newDFAState
						.addRepresentedState(firstTransition.getEndState()));
				for (int k = j + 1; k < currentState.outTransitions.size(); k++) {
					Transition secondTransition = currentState.outTransitions
							.get(k);
					if (firstTransition.equalSymbol(secondTransition)) {
						// check if this two transitions end in the same state
						if (firstTransition.getEndState() != secondTransition
								.getEndState()) {
							// this two transitions accept the same symbol, but
							// have different end states,
							// so add the end state of the second one to the list
							// of represented states of the first's end state
							transitionList.addAll(newDFAState
									.addRepresentedState(secondTransition
											.getEndState()));
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
					if (newDFAState.containsFinalStateOfNFA(nfa))
						finalStates.add(newDFAState);
				}
			}
			i++;
		}
	}

}
