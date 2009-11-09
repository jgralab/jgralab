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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.uni_koblenz.jgralab.GraphMarker;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.VertexEvaluator;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueTypeCollection;

/**
 * this class models a nondeterministic finite automaton. It created during
 * evaluation of the path-description via thompson-construction, but before
 * pathsearch, it will be transformed to an deterministic finite automaton
 * 
 * @see DFA
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class NFA extends FiniteAutomaton {

	/**
	 * Constructs a new NFA which is a copy of the given NFA. Depending on the
	 * parameter <code>realcopy</code> the NFA gets copied deep (that means,
	 * every transitions and every state gets copied) or flat (even the
	 * transition and stateList are the same object)
	 * 
	 * @param realcopy
	 *            : if true, a deep copy will be created
	 * 
	 */
	protected NFA(NFA nfaToCopy) {
		Map<Integer, State> oldStateToNewStateMap = new HashMap<Integer, State>();
		nfaToCopy.updateStateAttributes();
		finalStates = new ArrayList<State>();
		stateList = new ArrayList<State>();
		transitionList = new ArrayList<Transition>();
		// copy all states
		for (State currentState : nfaToCopy.stateList) {
			State newState = new State();
			newState.number = currentState.number;
			if (nfaToCopy.finalStates.contains(currentState))
				finalStates.add(newState);
			if (nfaToCopy.initialState == currentState) {
				initialState = newState;
			}
			oldStateToNewStateMap.put(currentState.number, newState);
			stateList.add(newState);
		}
		updateStateAttributes();
		// now copy all transitions
		for (Transition currentTransition : nfaToCopy.transitionList) {
			Transition newTransition = currentTransition.copy(false);
			transitionList.add(newTransition);
			State startState = oldStateToNewStateMap.get(currentTransition.getStartState().number);
			State endState = oldStateToNewStateMap.get(currentTransition.getEndState().number);
			newTransition.setStartState(startState);
			newTransition.setEndState(endState);
		}
		updateStateAttributes();
	} 


	/**
	 * creates a nfa for the given path description, calling the right
	 * create-method for this path description
	 */

	/**
	 * Constructs a NFA for the Kleene-Iteration p* of the given NFA.
	 * 
	 * @param iteratedNFA
	 *            the NFA for the path description p
	 * @param optional
	 *            if set to true, the resulting automaton will accept p*,
	 *            otherwise it will accept p+
	 * @return a NFA which accepts iteratedNFA* oder iteratedNFA+
	 */
	public static NFA createIteratedPathDescriptionNFA(NFA iteratedNFA,
			boolean optional) throws EvaluateException {
		State newFinalState;
		if (iteratedNFA.finalStates.size() > 1) {
			// there are at least to final states, there should be only one, so
			// create a new one and epsilon-transitions
			newFinalState = new State();
			iteratedNFA.constructFinalStatesEpsilonTransitions(newFinalState,
					true);
			iteratedNFA.finalStates.add(newFinalState); // this is the only
			// final
			// state
		} else {
			newFinalState = iteratedNFA.finalStates.get(0);
		}
		Transition t = new EpsilonTransition(newFinalState,
				iteratedNFA.initialState);
		iteratedNFA.transitionList.add(t);
		if (optional) {
			// this is a kleene-star iteration, so also 0 iterations are
			// allowed, create a new epsilon-transition from start to end
			Transition t2 = new EpsilonTransition(iteratedNFA.initialState,
					newFinalState);
			iteratedNFA.transitionList.add(t2);
		}
		// GreqlEvaluator.println("created IteratedPath NFA");
		iteratedNFA.updateStateAttributes();
		return iteratedNFA;
	}

	/**
	 * Constructs a NFA for the given sequential path description
	 * 
	 * @param nfaList
	 *            the list of NFAs that should be concatenated
	 * @return a NFA which accepts the concatenation of the given sequential
	 *         path description
	 */
	public static NFA createSequentialPathDescriptionNFA(List<NFA> nfaList)
			throws EvaluateException {
		NFA resultNFA = nfaList.get(0);
		for (int i = 1; i < nfaList.size(); i++) {
			NFA nextNFA = nfaList.get(i);
			resultNFA.constructFinalStatesEpsilonTransitions(
					nextNFA.initialState, true);
			resultNFA.stateList.addAll(nextNFA.stateList);
			resultNFA.finalStates.addAll(nextNFA.finalStates);
			resultNFA.transitionList.addAll(nextNFA.transitionList);
		}
		resultNFA.updateStateAttributes();
		return resultNFA;
	}

	/**
	 * Construct a NFA which accepts the given AlternativePaths
	 */
	public static NFA createAlternativePathDescriptionNFA(List<NFA> nfaList)
			throws EvaluateException {
		NFA resultNFA = nfaList.get(0);
		State finalState = new State();
		resultNFA.stateList.add(finalState);
		resultNFA.constructFinalStatesEpsilonTransitions(finalState, true);
		resultNFA.finalStates.add(finalState);
		for (int i = 1; i < nfaList.size(); i++) {
			NFA nextNFA = nfaList.get(i);
			Transition t = new EpsilonTransition(resultNFA.initialState,
					nextNFA.initialState);
			resultNFA.transitionList.add(t);
			nextNFA.constructFinalStatesEpsilonTransitions(finalState, true);
			resultNFA.stateList.addAll(nextNFA.stateList);
			resultNFA.transitionList.addAll(nextNFA.transitionList);
		}
		resultNFA.updateStateAttributes();
		return resultNFA;
	}

	/**
	 * Constructs a NFA which accepts the given OptionalPathDescription
	 */
	public static NFA createOptionalPathDescriptionNFA(NFA optionalNFA)
			throws EvaluateException {
		Transition t = new EpsilonTransition(optionalNFA.initialState,
				optionalNFA.finalStates.get(0));
		optionalNFA.transitionList.add(t);
		optionalNFA.updateStateAttributes();
		return optionalNFA;
	}

	/**
	 * Reverts the given Path Description. Method is needed by
	 * createTransposedPathDescription and for evaluation of regular
	 * BackwardVertexSets. Does not create a new NFA
	 */
	public static NFA revertNFA(NFA nfa) {
		// first reverse all transitions
		Iterator<Transition> iter = nfa.transitionList.iterator();
		while (iter.hasNext()) {
			Transition t = iter.next();
			t.reverse();
		}
		// and create a new initial state, the former initial state will become
		// the final state
		State newInitialState = null;
		if (nfa.finalStates.size() > 1) {
			newInitialState = new State();
			nfa.stateList.add(newInitialState);
			Iterator<State> finalStateIter = nfa.finalStates.iterator();
			while (finalStateIter.hasNext()) {
				State s = finalStateIter.next();
				Transition t = new EpsilonTransition(newInitialState, s);
				nfa.transitionList.add(t);
			}
		} else {
			newInitialState = nfa.finalStates.get(0);
		}
		nfa.finalStates = new ArrayList<State>();
		nfa.finalStates.add(nfa.initialState);
		nfa.initialState = newInitialState;
		nfa.updateStateAttributes();
		return nfa;
	}

	/**
	 * Constructs a NFA which accepts the given TransposedPathDescription
	 */
	public static NFA createTransposedPathDescriptionNFA(NFA transposedNFA)
			throws EvaluateException {
		return revertNFA(transposedNFA);
	}

	/**
	 * Constructs a NFA which accepts the given ExponentiatedPathDescription
	 */
	public static NFA createExponentiatedPathDescriptionNFA(
			NFA exponentiatedNFA, int exponent) throws EvaluateException {
		NFA nfaToCopy = new NFA(exponentiatedNFA);
		
		for (int i = 1; i < exponent; i++) {
			NFA nextIterationNFA = new NFA(nfaToCopy);
			exponentiatedNFA.constructFinalStatesEpsilonTransitions(
					nextIterationNFA.initialState, true);
			exponentiatedNFA.finalStates.addAll(nextIterationNFA.finalStates);
			exponentiatedNFA.stateList.addAll(nextIterationNFA.stateList);
			exponentiatedNFA.transitionList.addAll(nextIterationNFA.transitionList);
		}
		exponentiatedNFA.updateStateAttributes();
		return exponentiatedNFA;
	}

	/**
	 * Constructs a NFA which accepts the given
	 * IntermediateVertexPathDescription
	 */
	public static NFA createIntermediateVertexPathDescriptionNFA(NFA firstNFA,
			VertexEvaluator intermediateVertices, NFA secondNFA)
			throws EvaluateException {

		State newFinalState = new State();
		firstNFA.stateList.add(newFinalState);
		firstNFA.constructFinalStatesEpsilonTransitions(newFinalState, true);

		IntermediateVertexTransition t = new IntermediateVertexTransition(
				newFinalState, secondNFA.initialState, intermediateVertices);
		firstNFA.transitionList.add(t);
		firstNFA.stateList.addAll(secondNFA.stateList);
		firstNFA.finalStates.addAll(secondNFA.finalStates);
		firstNFA.transitionList.addAll(secondNFA.transitionList);
		firstNFA.updateStateAttributes();
		return firstNFA;
	}

	/**
	 * Constructs a NFA which accepts the given EdgePathDescription The
	 * EdgeRestrictions (RoleId, TypeId) are modelled in the Transition.
	 */
	public static NFA createEdgePathDescriptionNFA(
			Transition.AllowedEdgeDirection dir,
			JValueTypeCollection typeCollection, String role,
			VertexEvaluator edgeEval) {
		NFA nfa = new NFA();
		nfa.transitionList.clear();
		nfa.initialState.outTransitions.clear();
		nfa.finalStates.get(0).inTransitions.clear();
		SimpleTransition t = new EdgeTransition(nfa.initialState,
				nfa.finalStates.get(0), dir, typeCollection, role, edgeEval);
		nfa.transitionList.add(t);
		nfa.updateStateAttributes();
		return nfa;
	}

	/**
	 * Constructs a NFA which accepts the given SimplePathDescription. The
	 * EdgeRestrictions (RoleId, TypeId) are modelled in the Transition.
	 */
	public static NFA createSimplePathDescriptionNFA(
			Transition.AllowedEdgeDirection dir,
			JValueTypeCollection typeCollection, String role) {
		NFA nfa = new NFA();
		nfa.transitionList.clear();
		nfa.initialState.outTransitions.clear();
		nfa.finalStates.get(0).inTransitions.clear();
		SimpleTransition t = new SimpleTransition(nfa.initialState,
				nfa.finalStates.get(0), dir, typeCollection, role);
		nfa.transitionList.add(t);
		nfa.updateStateAttributes();
		return nfa;
	}

	/**
	 * Constructs a NFA which accepts the given AggregationPathDescription. The
	 * EdgeRestrictions (RoleId, TypeId) are modelled in the Transition.
	 */
	public static NFA createAggregationPathDescriptionNFA(
			boolean aggregateFrom, JValueTypeCollection typeCollection,
			String role) {
		NFA nfa = new NFA();
		nfa.transitionList.clear();
		nfa.initialState.outTransitions.clear();
		nfa.finalStates.get(0).inTransitions.clear();
		AggregationTransition t = new AggregationTransition(nfa.initialState,
				nfa.finalStates.get(0), aggregateFrom, typeCollection, role);
		nfa.transitionList.add(t);
		nfa.updateStateAttributes();
		return nfa;
	}

	/**
	 * Constructs a new epsilon-NFA, this is a NFA which has only two states,
	 * one initial and one final state. There is only one transition, which is a
	 * epsilon-transition from the initial to the final state
	 */
	protected NFA() {
		finalStates = new ArrayList<State>();
		transitionList = new ArrayList<Transition>();
		stateList = new ArrayList<State>();
		initialState = new State();
		State finalState = new State();
		finalStates.add(finalState);
		stateList.add(initialState);
		stateList.add(finalState);
		EpsilonTransition t = new EpsilonTransition(initialState, finalState);
		transitionList.add(t);
	}

	/**
	 * creates a new final state to the nfa that accepts a goal restriction
	 * 
	 * @param nfa
	 *            the NFA to add the goal restriction to
	 * @param typeCollection
	 *            The allowed types of the goal vertex
	 */
	public static void addGoalTypeRestriction(NFA nfa,
			JValueTypeCollection typeCollection) throws EvaluateException {
		State newEndState;
		if (nfa.finalStates.size() == 1) {
			newEndState = nfa.finalStates.get(0);
			nfa.finalStates.clear();
		} else {
			newEndState = new State();
			nfa.constructFinalStatesEpsilonTransitions(newEndState, false);
			nfa.stateList.add(newEndState);
			nfa.finalStates.clear();
		}
		State restrictedFinalState = new State();
		nfa.stateList.add(restrictedFinalState);
		nfa.finalStates.add(restrictedFinalState);
		VertexTypeRestrictionTransition trans = new VertexTypeRestrictionTransition(
				newEndState, restrictedFinalState, typeCollection);
		nfa.transitionList.add(trans);
	}

	/**
	 * Adds a boolean expression as goal restriction
	 * 
	 * @param nfa
	 *            the NFA to add the goal restriction to
	 * @param boolEval
	 *            the VertexEvaluator, which restricts this nfa
	 */
	public static void addGoalBooleanRestriction(NFA nfa,
			VertexEvaluator boolEval, GraphMarker<VertexEvaluator> marker)
			throws EvaluateException {
		State newEndState;
		if (nfa.finalStates.size() == 1) {
			newEndState = nfa.finalStates.get(0);
			nfa.finalStates.clear();
		} else {
			newEndState = new State();
			nfa.constructFinalStatesEpsilonTransitions(newEndState, false);
			nfa.stateList.add(newEndState);
			nfa.finalStates.clear();
		}
		State restrictedFinalState = new State();
		nfa.stateList.add(restrictedFinalState);
		nfa.finalStates.add(restrictedFinalState);
		BoolExpressionTransition trans = new BoolExpressionTransition(
				newEndState, restrictedFinalState, boolEval, marker);
		nfa.transitionList.add(trans);
	}

	/**
	 * Adds a boolean expression as start restriction
	 * 
	 * @param nfa
	 *            the NFA to add the start restriction to
	 * @param boolEval
	 *            the VertexEvaluator, which restricts this nfa
	 */
	public static void addStartBooleanRestriction(NFA nfa,
			VertexEvaluator boolEval, GraphMarker<VertexEvaluator> marker)
			throws EvaluateException {
		State newInitialState = new State();
		nfa.stateList.add(newInitialState);
		BoolExpressionTransition trans = new BoolExpressionTransition(
				newInitialState, nfa.initialState, boolEval, marker);
		nfa.transitionList.add(trans);
		nfa.initialState = newInitialState;
	}

	/**
	 * creates a new initial state to the nfa and a transition from the new
	 * state to the old initial state that accepts a start restriction
	 * 
	 * @param nfa
	 *            the NFA to add the start restriction to
	 * @param typeCollection
	 *            The allowed types of the start vertex
	 */
	public static void addStartTypeRestriction(NFA nfa,
			JValueTypeCollection typeCollection) throws EvaluateException {
		State newInitialState = new State();
		nfa.stateList.add(newInitialState);
		VertexTypeRestrictionTransition trans = new VertexTypeRestrictionTransition(
				newInitialState, nfa.initialState, typeCollection);
		nfa.transitionList.add(trans);
		nfa.initialState = newInitialState;
	}

	/**
	 * constructs epsilon-transition from all final states of this nfa to the
	 * given state
	 * 
	 * @param target
	 *            the target state of all transitions which will be created
	 * @param removeFinalStates
	 *            if set to true, all final states will become normal states
	 */
	protected void constructFinalStatesEpsilonTransitions(State target,
			boolean removeFinalStates) {
		Iterator<State> iter = finalStates.iterator();
		while (iter.hasNext()) {
			EpsilonTransition t = new EpsilonTransition(iter.next(), target);
			transitionList.add(t);
			if (removeFinalStates)
				iter.remove();
		}
	}

}
