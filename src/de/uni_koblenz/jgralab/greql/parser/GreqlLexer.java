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
package de.uni_koblenz.jgralab.greql.parser;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import de.uni_koblenz.jgralab.greql.exception.ParsingException;

public class GreqlLexer {
	private static Map<String, GreqlTokenType> stringToTokenMap = new HashMap<>();
	private static String[] nonIdTokens;

	static {
		SortedSet<String> tokens = new TreeSet<>(new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				int r = o2.length() - o1.length();
				return r == 0 ? o1.compareTo(o2) : r;
			}
		});

		for (GreqlTokenType t : GreqlTokenType.values()) {
			String text = t.getText();
			if (text != null) {
				stringToTokenMap.put(text, t);
				char c = text.charAt(0);
				if (!isIdStart(c)) {
					tokens.add(text);
				}
			}
		}
		nonIdTokens = new String[tokens.size()];
		int i = 0;
		for (String t : tokens) {
			nonIdTokens[i++] = t;
		}
	}

	protected String query;
	protected int pos;
	int ch; // current char
	int la; // lookahead-char
	int pb; // putback-char

	private final char[] queryChars;
	private final int queryLength;

	public GreqlLexer(String source) {
		this.query = source;
		if (query == null) {
			throw new NullPointerException(
					"Cannot parse nullpointer as GReQL query");
		}
		queryChars = query.toCharArray();
		queryLength = queryChars.length;
		pb = -1;
		pos = -1;
		ch = -1;
		la = pos + 1 < queryLength ? queryChars[pos + 1] : -1;
		next();
	}

	private void next() {
		if (pb >= 0) {
			ch = pb;
			pb = -1;
		} else {
			ch = la;
		}
		++pos;
		la = pos + 1 < queryLength ? queryChars[pos + 1] : -1;
	}

	@SuppressWarnings("unused")
	private void putback() {
		assert pb < 0 : "Can't put back more than one character";
		assert ch >= 0 : "Can't put back at begin of text";
		pb = ch;
		ch = la;
		--pos;
	}

	private Token getNextToken() {
		skipWhiteSpaceAndComments();
		int start = pos;
		if (ch < 0) {
			return new Token(GreqlTokenType.EOF, pos, 0);
		}
		if (isIdStart(ch)) {
			// identifier or other textual token
			while (isId(ch)) {
				next();
			}
			String id = query.substring(start, pos);
			GreqlTokenType t = stringToTokenMap.get(id);
			if (t == null) {
				return new ComplexToken(GreqlTokenType.IDENTIFIER, start, pos
						- start, id);
			} else if (t == GreqlTokenType.NOT_A_NUMBER
					|| t == GreqlTokenType.POS_INFINITY
					|| t == GreqlTokenType.NEG_INFINITY) {
				return new DoubleToken(GreqlTokenType.DOUBLELITERAL, start, pos
						- start, id, Double.parseDouble(id));
			} else if (t == GreqlTokenType.EXISTS && ch == '!') {
				// special handling for exists! token
				next();
				return new Token(GreqlTokenType.EXISTS_ONE, start, pos - start);
			} else {
				return new Token(t, start, pos - start);
			}
		} else if (isDigit(ch)) {
			// number
			if (ch == '0' && (la == 'x' || la == 'X')) {
				// hex number
				next();
				next();
				while (isHexDigit(ch)) {
					next();
				}
				String text = query.substring(start, pos);
				return new LongToken(GreqlTokenType.LONGLITERAL, start, pos
						- start, text, Long.parseLong(
						text.substring(start + 2, pos), 16));
			} else if (ch == '0' && isOctDigit(la)) {
				// octal number
				while (isOctDigit(ch)) {
					next();
				}
				String text = query.substring(start, pos);
				return new LongToken(GreqlTokenType.LONGLITERAL, start, pos
						- start, text, Long.parseLong(text, 8));
			} else {
				// decimal number
				while (isDigit(ch)) {
					next();
				}
				if (ch == '.' && la == '.') {
					// integer number followed by ..
					String text = query.substring(start, pos);
					return new LongToken(GreqlTokenType.LONGLITERAL, start, pos
							- start, text, Long.parseLong(text));
				}
				if (ch == '.' || ch == 'e' || ch == 'E') {
					// floating point number
					if (ch == '.') {
						next();
					}
					while (isDigit(ch)) {
						// fraction digits
						next();
					}
					if (ch == 'e' || ch == 'E') {
						// exponent
						next();
						if (ch == '-' || ch == '+') {
							next();
						}
						while (isDigit(ch)) {
							next();
						}
					}
					String text = query.substring(start, pos);
					try {
						double d = Double.parseDouble(text);
						return new DoubleToken(GreqlTokenType.DOUBLELITERAL,
								start, pos - start, text, d);
					} catch (NumberFormatException e) {
						throw new ParsingException(
								"Illegal floating point number", text, start,
								pos - start, query);
					}
				} else {
					// integer number
					String text = query.substring(start, pos);
					return new LongToken(GreqlTokenType.LONGLITERAL, start, pos
							- start, text, Long.parseLong(text));
				}
			}
		} else if (ch == '\'' || ch == '"') {
			// string
			int delim = ch;
			StringBuilder sb = new StringBuilder();
			next();
			while (ch >= 0 && ch != delim) {
				if (ch == '\\') {
					next();
				}
				if (ch >= 0) {
					sb.append((char) ch);
					next();
				}
			}
			if (ch == delim) {
				next();
			} else {
				throw new ParsingException("String started at position "
						+ start + " but is not closed in query",
						query.substring(start, pos), start, pos - start, query);
			}
			return new ComplexToken(GreqlTokenType.STRING, start, pos - start,
					sb.toString());
		} else {
			// other token, try tokens that dont begin with a letter in
			// order of descending length
			for (String t : nonIdTokens) {
				if (t.charAt(0) != ch) {
					continue;
				}
				int l = t.length();
				if (pos + l <= queryLength) {
					if (query.substring(pos, pos + l).equals(t)) {
						while (l > 0) {
							next();
							--l;
						}
						return new Token(stringToTokenMap.get(t), start, pos
								- start);
					}
				}
			}
		}
		throw new ParsingException("Unexpected character", "" + ((char) ch),
				pos, 1, query);
	}

	private static boolean isDigit(int ch) {
		return ch >= '0' && ch <= '9';
	}

	private static boolean isOctDigit(int ch) {
		return ch >= '0' && ch <= '7';
	}

	private static boolean isHexDigit(int ch) {
		return (ch >= '0' && ch <= '9') || (ch >= 'a' && ch <= 'f')
				|| (ch >= 'A' && ch <= 'F');
	}

	private static boolean isIdStart(int ch) {
		return (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z')
				|| (ch == '_') || (ch == '$');
	}

	private static boolean isId(int ch) {
		return isIdStart(ch) || isDigit(ch);
	}

	private final void skipWhiteSpaceAndComments() {
		while (true) {
			while (ch >= 0 && (ch == ' ') || (ch == '\n') || (ch == '\t')
					|| (ch == '\r')) {
				next();
			}
			if (ch != '/') {
				return;
			}
			if (la == '/') {
				// single line comment
				next(); // skip /
				do {
					next();
				} while (ch >= 0 && ch != '\n');
			} else if (la == '*') {
				// multi line comment
				next(); // skip *
				do {
					next();
				} while (ch >= 0 && !(ch == '*' && la == '/'));
				next(); // skip *
				next(); // skip /
			} else {
				return;
			}
		}
	}

	public static List<Token> scan(String query) {
		List<Token> list = new ArrayList<>();
		GreqlLexer lexer = new GreqlLexer(query);
		Token t;
		do {
			t = lexer.getNextToken();
			list.add(t);
		} while (t.type != GreqlTokenType.EOF);
		return list;
	}
}
