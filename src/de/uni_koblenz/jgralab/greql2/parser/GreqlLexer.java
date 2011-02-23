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
package de.uni_koblenz.jgralab.greql2.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.uni_koblenz.jgralab.greql2.exception.ParsingException;

public class GreqlLexer {

	@SuppressWarnings("serial")
	protected static final Map<TokenTypes, String> fixedTokens = Collections
			.unmodifiableMap(new HashMap<TokenTypes, String>() {
				{
					put(TokenTypes.T, "T");
					put(TokenTypes.AND, "and");
					put(TokenTypes.FALSE, "false");
					put(TokenTypes.NOT, "not");
					put(TokenTypes.NULL_VALUE, "null");
					put(TokenTypes.OR, "or");
					put(TokenTypes.TRUE, "true");
					put(TokenTypes.XOR, "xor");
					put(TokenTypes.AS, "as");
					put(TokenTypes.BAG, "bag");
					put(TokenTypes.MAP, "map");
					put(TokenTypes.E, "E");
					put(TokenTypes.ESUBGRAPH, "eSubgraph");
					put(TokenTypes.EXISTS_ONE, "exists!");
					put(TokenTypes.EXISTS, "exists");
					put(TokenTypes.END, "end");
					put(TokenTypes.FORALL, "forall");
					put(TokenTypes.FROM, "from");
					put(TokenTypes.IN, "in");
					put(TokenTypes.LET, "let");
					put(TokenTypes.LIST, "list");
					put(TokenTypes.REC, "rec");
					put(TokenTypes.REPORT, "report");
					put(TokenTypes.REPORTSET, "reportSet");
					put(TokenTypes.REPORTBAG, "reportBag");
					put(TokenTypes.REPORTTABLE, "reportTable");
					put(TokenTypes.REPORTMAP, "reportMap");
					put(TokenTypes.STORE, "store");
					put(TokenTypes.SET, "set");
					put(TokenTypes.TUP, "tup");
					put(TokenTypes.USING, "using");
					put(TokenTypes.V, "V");
					put(TokenTypes.VSUBGRAPH, "vSubgraph");
					put(TokenTypes.WHERE, "where");
					put(TokenTypes.WITH, "with");
					put(TokenTypes.QUESTION, "?");
					put(TokenTypes.EXCL, "!");
					put(TokenTypes.COLON, ":");
					put(TokenTypes.COMMA, ",");
					put(TokenTypes.DOT, ".");
					put(TokenTypes.DOTDOT, "..");
					put(TokenTypes.AT, "@");
					put(TokenTypes.LPAREN, "(");
					put(TokenTypes.RPAREN, ")");
					put(TokenTypes.LBRACK, "[");
					put(TokenTypes.RBRACK, "]");
					put(TokenTypes.LCURLY, "{");
					put(TokenTypes.RCURLY, "}");
					put(TokenTypes.EDGESTART, "<-");
					put(TokenTypes.EDGEEND, "->");
					put(TokenTypes.EDGE, "--");
					put(TokenTypes.RARROW, "-->");
					put(TokenTypes.LARROW, "<--");
					put(TokenTypes.ARROW, "<->");
					put(TokenTypes.ASSIGN, ":=");
					put(TokenTypes.EQUAL, "=");
					put(TokenTypes.MATCH, "=~");
					put(TokenTypes.NOT_EQUAL, "<>");
					put(TokenTypes.LE, "<=");
					put(TokenTypes.GE, ">=");
					put(TokenTypes.L_T, "<");
					put(TokenTypes.G_T, ">");
					put(TokenTypes.DIV, "/");
					put(TokenTypes.PLUS, "+");
					put(TokenTypes.MINUS, "-");
					put(TokenTypes.STAR, "*");
					put(TokenTypes.MOD, "%");
					put(TokenTypes.PLUSPLUS, "++");
					put(TokenTypes.SEMI, ";");
					put(TokenTypes.CARET, "^");
					put(TokenTypes.BOR, "|");
					put(TokenTypes.AMP, "&");
					put(TokenTypes.SMILEY, ":-)");
					put(TokenTypes.HASH, "#");
					put(TokenTypes.OUTAGGREGATION, "<>--");
					put(TokenTypes.INAGGREGATION, "--<>");
					put(TokenTypes.PATHSYSTEMSTART, "-<");
					put(TokenTypes.IMPORT, "import");
					put(TokenTypes.POS_INFINITY, "POSITIVE_INFINITY");
					put(TokenTypes.NEG_INFINITY, "NEGATIVE_INFINITY");
					put(TokenTypes.NaN, "NaN");
				}
			});

	protected String query = null;

	protected int position = 0;

	public GreqlLexer(String source) {
		this.query = source;
		if (query == null) {
			throw new NullPointerException(
					"Cannot parse nullpointer as GReQL query");
		}
	}

	public static String getTokenString(TokenTypes token) {
		return fixedTokens.get(token);
	}

	private final static boolean isSeparator(int c) {
		return (c == ';') || (c == '<') || (c == '>') || (c == '(')
				|| (c == ')') || (c == '{') || (c == '}') || (c == ':')
				|| (c == '[') || (c == ']') || (c == ',') || (c == ' ')
				|| (c == '\n') || (c == '\t') || (c == '.') || (c == '-')
				|| (c == '+') || (c == '*') || (c == '/') || (c == '%')
				|| (c == '=') || (c == '?') || (c == '^') || (c == '|')
				|| (c == '!') || (c == '@');
	}

	public Token getNextToken() {
		TokenTypes recognizedTokenType = null;
		Token recognizedToken = null;
		int bml = 0; // best match length
		skipWs();
		// recognize fixed tokens
		for (Entry<TokenTypes, String> currentEntry : fixedTokens.entrySet()) {
			String currentString = currentEntry.getValue();
			int currLen = currentString.length();
			if (bml > currLen) {
				continue;
			}
			if (query.regionMatches(position, currentString, 0, currLen)) {
				if (((position + currLen) == query.length())
						|| isSeparator(query.charAt(position + currLen - 1))
						|| isSeparator(query.charAt(position + currLen))) {
					bml = currLen;
					recognizedTokenType = currentEntry.getKey();
				}
			}
		}
		// recognize strings and identifiers
		if (recognizedTokenType == null) {
			char separator = query.charAt(position);
			if ((separator == '\"') || (separator == '\'')) { // String
				position++;
				int start = position;
				StringBuilder sb = new StringBuilder();
				while ((position < query.length())
						&& (query.charAt(position) != separator)) {
					if (query.charAt(position) == '\\') {
						if (position == query.length()) {
							throw new ParsingException(
									"String started at position " + start
											+ " but is not closed in query",
									query.substring(start, position), start,
									position - start, query);
						}
						if ((query.charAt(position + 1) == separator)
								|| (query.charAt(position + 1) == '\\')) {
							position++;
						}
					}
					sb.append(query.charAt(position));
					position++;
				}
				if ((position >= query.length())
						|| (query.charAt(position) != separator)) {
					throw new ParsingException("String started at position "
							+ start + " but is not closed in query",
							sb.toString(), start, position - start, query);
				}
				recognizedTokenType = TokenTypes.STRING;
				recognizedToken = new ComplexToken(recognizedTokenType, start,
						position, sb.toString());
				position++;
			} else {
				// identifier and literals
				StringBuilder nextPossibleToken = new StringBuilder();
				int start = position;
				while ((query.length() > position)
						&& (!isSeparator(query.charAt(position)))) {
					nextPossibleToken.append(query.charAt(position++));
				}
				if (query.length() < position) {
					return new SimpleToken(TokenTypes.EOF, start, 0);
				}
				String tokenText = nextPossibleToken.toString();
				if (tokenText.equals("thisVertex")) {
					recognizedToken = new ComplexToken(TokenTypes.THISVERTEX,
							start, position - start, tokenText);
				} else if (tokenText.equals("thisEdge")) {
					recognizedToken = new ComplexToken(TokenTypes.THISEDGE,
							start, position - start, tokenText);
				} else if (tokenText.equals(fixedTokens.get(TokenTypes.POS_INFINITY)) || tokenText.equals(fixedTokens.get(TokenTypes.NEG_INFINITY))  || tokenText.equals(fixedTokens.get(TokenTypes.NaN)) ) {
					recognizedToken = matchDoubleConstantToken(start,
							position - start, tokenText);
				} else if (startsWithNumber(tokenText)) {
					recognizedToken = matchNumericToken(start,
							position - start, tokenText);
				} else {
					recognizedToken = new ComplexToken(TokenTypes.IDENTIFIER,
							start, position - start, tokenText);
				}

			}
		} else {
			recognizedToken = new SimpleToken(recognizedTokenType, position,
					bml);
			position += bml;
		}
		if (recognizedToken == null) {
			throw new ParsingException(
					"Error while scanning query at position", null, position,
					position, query);
		}
		return recognizedToken;
	}

	private final Token matchDoubleConstantToken(int start, int i, String tokenText) {
		Double value = Double.parseDouble(tokenText);
		return new RealToken(TokenTypes.REALLITERAL, start, i, tokenText, value);
	}

	private final boolean startsWithNumber(String text) {
		char c = text.charAt(0);
		return (c >= Character.valueOf('0')) && (c <= Character.valueOf('9'));
	}

	// TODO: Exponenten
	private final Token matchNumericToken(int start, int end, String text) {
		long value = 0;
		long decValue = 0;
		String stringValue = "0";
		TokenTypes type = null;
		stringValue = text;
		if (text.charAt(0) == '0') {
			if (text.length() == 1) {
				type = TokenTypes.INTLITERAL;
				value = 0;
			} else if ((text.charAt(1) == 'x') || (text.charAt(1) == 'X')) {
				type = TokenTypes.HEXLITERAL;
				try {
					value = Integer.parseInt(text.substring(2), 16);
				} catch (NumberFormatException ex) {
					throw new ParsingException("Not a valid hex number", text,
							start, end - start, query);
				}
			} else {
				type = TokenTypes.OCTLITERAL;
				try {
					value = Integer.parseInt(text.substring(1), 8);
					decValue = Integer.parseInt(text);
				} catch (NumberFormatException ex) {
					throw new ParsingException("Not a valid octal number",
							text, start, end - start, query);
				}
			}
		} else {
			switch (text.charAt(text.length() - 1)) {
			case 'h':
				type = TokenTypes.HEXLITERAL;
				try {
					value = Integer.parseInt(
							text.substring(0, text.length() - 1), 16);
				} catch (NumberFormatException ex) {
					throw new ParsingException("Not a valid hex number", text,
							start, end - start, query);
				}
				break;
			default:
				type = TokenTypes.INTLITERAL;
				try {
					value = Integer.parseInt(text);
				} catch (NumberFormatException ex) {
					throw new ParsingException("Not a valid integer number",
							text, start, end - start, query);
				}
			}
		}
		if (type != TokenTypes.OCTLITERAL) {
			decValue = value;
		}
		return new IntegerToken(type, start, end - start, stringValue, value,
				decValue);

	}

	public boolean hasNextToken() {
		skipWs();
		return (position < query.length());
	}

	private final static boolean isWs(int c) {
		return (c == ' ') || (c == '\n') || (c == '\t') || (c == '\r');
	}

	private final void skipWs() {
		// skip whitespace and consecutive single line comments
		do {
			// skip whitespace
			while ((position < query.length()) && isWs(query.charAt(position))) {
				position++;
			}
			// skip single line comments
			if ((position < query.length() - 2)
					&& (query.substring(position, position + 2).equals("//"))) {
				position++;
				while ((position < query.length())
						&& (query.charAt(position) != '\n')) {
					position++;
				}
				if ((position < query.length())
						&& (query.charAt(position) == '\n')) {
					position++;
				}
			}
			// skip multiline comments
			if ((position < query.length() - 4)
					&& (query.substring(position, position + 2).equals("/*"))) {
				position++;
				while ((position < query.length() - 1)
						&& (query.substring(position, position + 2)
								.equals("*/"))) {
					position++;
				}
				if ((position < query.length())
						&& (query.substring(position, position + 2)
								.equals("*/"))) {
					position += 2;
				}
			}
		} while (((position < query.length()) && (isWs(query.charAt(position))))
				|| ((position < query.length() - 2) && (query.substring(
						position, position + 2).equals("//")))
				|| ((position < query.length() - 4) && (query.substring(
						position, position + 2).equals("/*"))));
	}

	public static List<Token> scan(String query) {
		List<Token> list = new ArrayList<Token>();
		GreqlLexer lexer = new GreqlLexer(query);
		while (lexer.hasNextToken()) {
			Token nextToken = lexer.getNextToken();
			list.add(nextToken);
		}
		if (list.isEmpty()
				|| (list.get(list.size() - 1).type != TokenTypes.EOF)) {
			list.add(new SimpleToken(TokenTypes.EOF, lexer.position, 0));
		}
		return list;
	}

}
