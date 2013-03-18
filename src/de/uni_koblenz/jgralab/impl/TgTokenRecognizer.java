/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2013 Institute for Software Technology
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
package de.uni_koblenz.jgralab.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import de.uni_koblenz.jgralab.impl.TgLexer.Token;

/**
 * {@link TgTokenRecognizer} basically is a DFA that accepts all {@link Token}s
 * of the TG format. The constructor creates the DFA states from the token
 * lexemes.
 * 
 * {@link #reset()} initializes the DFA. The a call to {@link #next(int)} feeds
 * the next input symbol. At any time, {@link #getToken()} returns the accepted
 * {@link Token}, or <code><code>null</code> when the DFA is not accepting
 * current input.
 * 
 * @author ist@uni-koblenz.de
 */
final class TgTokenRecognizer {

	private State[] states; // the DFA states
	private int minChar; // min character code of all tokens
	private int maxChar; // max character code of all tokens
	private int maxLen; // length of longest lexeme
	private int pos; // number of characters in current token
	private State currentState;

	private boolean neg; // found a '-' sign
	private long val; // integer value of token

	/**
	 * represents a DFA state, next array contains transitions, index is
	 * character code; token != null <=> state is accepting token
	 */
	private static final class State {
		public int[] next;
		public Token token;

		State(int l) {
			next = new int[l];
			Arrays.fill(next, -1);
		}
	}

	/**
	 * Creates a TgTokenRecognizer and constructs DFA states from {@link Token}
	 * lexemes.
	 * 
	 * To process integer and long values, characters +, -, and 0..9 are added.
	 */
	TgTokenRecognizer() {
		minChar = Math.min('-', Math.min('+', '0'));
		maxChar = Math.max('-', Math.max('+', '9'));
		maxLen = Integer.MIN_VALUE;
		for (Token t : Token.values()) {
			String s = t.lexeme;
			if (s == null) {
				continue;
			}
			if (s.length() > maxLen) {
				maxLen = s.length();
			}
			for (char c : s.toCharArray()) {
				if (c < minChar) {
					minChar = c;
				}
				if (c > maxChar) {
					maxChar = c;
				}
			}
		}

		// number of entries in State's next arrays
		int stateLen = maxChar - minChar + 1;

		// temporary list of states, copied into the states array when DFA is
		// complete
		ArrayList<State> dfaStates = new ArrayList<State>();

		// Maps states and input characters to next state index
		// key = "s" + input state + character
		Map<String, Integer> cm = new HashMap<String, Integer>();

		// create initial state s0
		State cr = new State(stateLen);
		dfaStates.add(cr);

		// initially, create 3 states to accept long/int values of the form
		// [+-]?[0-9]+
		int intStateNumber;
		{
			// transition from s0 upon [0..9] into s1
			// s1 is the accepting state for Token INT
			// and from s1 upon [0..9] into s1 again
			State nx = new State(stateLen);
			dfaStates.add(nx);
			int n = dfaStates.size() - 1;
			intStateNumber = n;
			nx.token = Token.INT;
			for (char c = '0'; c <= '9'; ++c) {
				cr.next[c - minChar] = n;
				nx.next[c - minChar] = n;
				cm.put("s0" + c, n);
			}
		}

		{
			// transition from s0 upon [-] into s2
			// and from s2 upon [0..9] into s1
			char c = '-';
			State nx = new State(stateLen);
			dfaStates.add(nx);
			int n = dfaStates.size() - 1;
			cr.next[c - minChar] = n;
			cm.put("s0-", n);
			for (char d = '0'; d <= '9'; ++d) {
				nx.next[d - minChar] = intStateNumber;
				cm.put("s" + n + c, intStateNumber);
			}
		}

		{
			// transition from s0 upon [+] into s3
			// and from s3 upon [0..9] into s1
			char c = '+';
			State nx = new State(stateLen);
			dfaStates.add(nx);
			int n = dfaStates.size() - 1;
			cr.next[c - minChar] = n;
			cm.put("s0+", n);
			for (char d = '0'; d <= '9'; ++d) {
				nx.next[d - minChar] = intStateNumber;
				cm.put("s" + n + c, intStateNumber);
			}
		}

		// maps Tokens to the corresponding state index during construction;
		// after processing each character of the token, the map is updated.
		Map<Token, Integer> tm = new HashMap<Token, Integer>();
		// initially, all tokens map to the start state (index 0)
		for (Token t : Token.values()) {
			if (t.lexeme == null) {
				continue;
			}
			tm.put(t, 0);
		}
		// process character in position p of all tokens
		for (int p = 0; p < maxLen; ++p) {
			for (Token t : Token.values()) {
				if (t.lexeme == null || p >= t.lexeme.length()) {
					// either token has no lexeme or is shorter than index p
					continue;
				}
				// cn is the index of the current state for token
				// cr is current state
				int cn = tm.get(t);
				cr = dfaStates.get(cn);
				char c = t.lexeme.charAt(p);
				String k = ("s" + cn) + c; // key for cm map
				int n; // next state index
				State nx; // next state
				if (cm.containsKey(k)) {
					// current state already has a transition on c
					// reuse it
					n = cm.get(k);
					nx = dfaStates.get(n);
				} else {
					// current state has no transition on c
					// create a new state
					nx = new State(stateLen);
					dfaStates.add(nx);
					n = dfaStates.size() - 1;
					cm.put(k, n);
					cr.next[c - minChar] = n;
				}
				// remember next state for token
				tm.put(t, n);
				if (p == t.lexeme.length() - 1) {
					// state nx is accepting state for token t
					nx.token = t;
				}

			}
		}
		// convert temporary state list into state array
		states = new State[dfaStates.size()];
		dfaStates.toArray(states);
		dfaStates = null;
	}

	/**
	 * reset recognizer to match a new token
	 */
	final void reset() {
		pos = 0;
		neg = false;
		val = 0;
		currentState = states[0];
	}

	/**
	 * Feed character code <code>ch</code> into DFA.
	 * 
	 * @param ch
	 */
	final void next(int ch) {
		if (currentState == null) {
			return;
		}
		if (pos == 0 && ch == '-') {
			neg = true;
		}
		if (ch >= '0' && ch <= '9') {
			val = 10 * val + (ch - '0');
		} else if (pos >= maxLen || ch < minChar || ch > maxChar) {
			currentState = null;
			return;
		}
		int n = currentState.next[ch - minChar];
		if (n > 0) {
			currentState = states[n];
			++pos;
		} else {
			currentState = null;
		}
	}

	/**
	 * @return the token recognized, or {@link Token#TEXT} if no other token
	 *         matched
	 */
	final Token getToken() {
		return currentState == null || currentState.token == null ? Token.TEXT
				: currentState.token;
	}

	/**
	 * @return the integer value of the token
	 */
	final long getValue() {
		assert getToken() == Token.INT;
		return neg ? -val : val;
	}
}