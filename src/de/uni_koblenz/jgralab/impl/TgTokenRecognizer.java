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

final class TgTokenRecognizer {

	private State[] states;
	private int minChar;
	private int maxChar;
	private int maxLen;
	private int pos;
	private State curr;

	private boolean neg;
	private long val;

	TgTokenRecognizer() {
		minChar = Math.min('-', Math.min('+', '0'));
		maxChar = Math.max('-', Math.max('+', '9'));
		maxLen = Integer.MIN_VALUE;
		for (Token t : Token.values()) {
			String s = t.text;
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
		// System.out.println("minChar " + (char) minChar);
		// System.out.println("maxChar " + (char) maxChar);

		int stateLen = maxChar - minChar + 1; // number of entries in arrays

		ArrayList<State> tmpStates = new ArrayList<State>();
		Map<Token, Integer> tm = new HashMap<Token, Integer>();
		State cr = new State(stateLen);
		tmpStates.add(cr);
		for (Token t : Token.values()) {
			if (t.text == null) {
				continue;
			}
			tm.put(t, 0);
		}
		Map<String, Integer> cm = new HashMap<String, Integer>();
		int intStateNumber;
		{
			State nx = new State(stateLen);
			tmpStates.add(nx);
			int n = tmpStates.size() - 1;
			intStateNumber = n;
			nx.token = Token.INT;
			// System.out.println("s" + n + " final for " + nx.token);
			for (char c = '0'; c <= '9'; ++c) {
				cr.next[c - minChar] = n;
				nx.next[c - minChar] = n;
				cm.put("s0" + c, n);
			}
		}

		{
			char c = '-';
			State nx = new State(stateLen);
			tmpStates.add(nx);
			int n = tmpStates.size() - 1;
			cr.next[c - minChar] = n;
			cm.put("s0-", n);
			for (char d = '0'; d <= '9'; ++d) {
				nx.next[d - minChar] = intStateNumber;
				cm.put("s" + n + c, intStateNumber);
			}
		}

		{
			char c = '+';
			State nx = new State(stateLen);
			tmpStates.add(nx);
			int n = tmpStates.size() - 1;
			cr.next[c - minChar] = n;
			cm.put("s0+", n);
			for (char d = '0'; d <= '9'; ++d) {
				nx.next[d - minChar] = intStateNumber;
				cm.put("s" + n + c, intStateNumber);
			}
		}
		for (int p = 0; p < maxLen; ++p) {
			for (Token t : Token.values()) {
				if (t.text == null) {
					continue;
				}
				int cn = tm.get(t);
				cr = tmpStates.get(cn);
				if (p < t.text.length()) {
					char c = t.text.charAt(p);
					String k = ("s" + cn) + c;
					State nx;
					int n;
					if (cm.containsKey(k)) {
						n = cm.get(k);
						nx = tmpStates.get(n);
					} else {
						nx = new State(stateLen);
						tmpStates.add(nx);
						n = tmpStates.size() - 1;
						cm.put(k, n);
						cr.next[c - minChar] = n;
					}
					tm.put(t, n);
					// System.err.println("s" + cn + " " + c + " -> s" + n +
					// " ("
					// + t.name() + ")");
					if (p == t.text.length() - 1) {
						nx.token = t;
						// System.err.println("s" + n + " final for " + t);
					}
				}
			}
		}
		states = new State[tmpStates.size()];
		tmpStates.toArray(states);
		tmpStates = null;
	}

	private static final class State {
		public int[] next;
		public Token token;

		State(int l) {
			next = new int[l];
			Arrays.fill(next, -1);
		}
	}

	final void reset() {
		pos = 0;
		neg = false;
		val = 0;
		curr = states[0];
	}

	final void next(int ch) {
		if (curr == null) {
			return;
		}
		if (pos == 0 && ch == '-') {
			neg = true;
		}
		if (ch >= '0' && ch <= '9') {
			val = 10 * val + (ch - '0');
		} else if (pos >= maxLen || ch < minChar || ch > maxChar) {
			curr = null;
			return;
		}
		int n = curr.next[ch - minChar];
		if (n > 0) {
			curr = states[n];
			++pos;
		} else {
			curr = null;
		}
	}

	final Token getToken() {
		return curr == null || curr.token == null ? Token.TEXT : curr.token;
	}

	final long getValue() {
		return neg ? -val : val;
	}
}