/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2012 Institute for Software Technology
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
import java.util.BitSet;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.uni_koblenz.jgralab.greql.exception.ParsingException;

public class GreqlLexer {
	
	@SuppressWarnings("serial")
	protected static final Map<TokenTypes, String> fixedTokens = Collections
			.unmodifiableMap(new EnumMap<TokenTypes, String>(TokenTypes.class) {
				{
					put(TokenTypes.TRANSPOSED, "^T");
					put(TokenTypes.AND, "and");
					put(TokenTypes.FALSE, "false");
					put(TokenTypes.NOT, "not");
					put(TokenTypes.UNDEFINED, "undefined");
					put(TokenTypes.OR, "or");
					put(TokenTypes.TRUE, "true");
					put(TokenTypes.XOR, "xor");
					put(TokenTypes.AS, "as");
					put(TokenTypes.MAP, "map");
					put(TokenTypes.E, "E");
					put(TokenTypes.EXISTS_ONE, "exists!");
					put(TokenTypes.EXISTS, "exists");
					put(TokenTypes.END, "end");
					put(TokenTypes.FORALL, "forall");
					put(TokenTypes.FROM, "from");
					put(TokenTypes.IN, "in");
					put(TokenTypes.ON, "on");
					put(TokenTypes.LET, "let");
					put(TokenTypes.LIST, "list");
					put(TokenTypes.REC, "rec");
					put(TokenTypes.REPORT, "report");
					put(TokenTypes.REPORTSET, "reportSet");
					put(TokenTypes.REPORTSETN, "reportSetN");
					put(TokenTypes.REPORTLIST, "reportList");
					put(TokenTypes.REPORTLISTN, "reportListN");
					put(TokenTypes.REPORTTABLE, "reportTable");
					put(TokenTypes.REPORTMAP, "reportMap");
					put(TokenTypes.REPORTMAPN, "reportMapN");
					put(TokenTypes.STORE, "store");
					put(TokenTypes.SET, "set");
					put(TokenTypes.TUP, "tup");
					put(TokenTypes.USING, "using");
					put(TokenTypes.V, "V");
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
					put(TokenTypes.NOT_A_NUMBER, "NaN");
				}
			});

	protected static Map<String, TokenTypes> stringToTokenMap = new HashMap<String, TokenTypes>();

	{
		for (Map.Entry<TokenTypes, String> entry : fixedTokens.entrySet()) {
			stringToTokenMap.put(entry.getValue(), entry.getKey());
		}
	}

	protected static BitSet separators = new BitSet();

	{
		Character[] sepArray = { ';', '<', '>', '(', ')', '{', '}', ':', '[',
				']', ',', ' ', '\n', '\t', '.', '-', '+', '*', '/', '%', '=',
				'?', '^', '|', '!', '@' };
		for (Character sep : sepArray) {
			separators.set(sep);
		}
	}

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
		return separators.get(c);
	}

	public Token getNextToken() {
		TokenTypes recognizedTokenType = null;
		Token recognizedToken = null;
		int bml = 0; // best match length
		skipWs();
		String currentTokenString;

		int currLength = 1;
		for (int i = 0; i < 4; i++) {
			while (((position + currLength) < (query.length() - 1))
					&& !isSeparator(query.charAt(position + currLength))
					&& !isSeparator(query.charAt((position + currLength) - 1))) {
				currLength++;
			}

			if ((position + currLength) < query.length()) {
				currentTokenString = query.substring(position, position
						+ currLength);
			} else {
				currentTokenString = query.substring(position);
			}
			TokenTypes possibleToken = stringToTokenMap.get(currentTokenString);
			if (possibleToken != null) {
				recognizedTokenType = possibleToken;
				bml = currentTokenString.length();
			}
			currLength += 1;
		}
		// recognize numbers, strings and identifiers
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
				char c = query.charAt(position);
				if (isNumber(c)) {
					//match double or long value 
					Matcher m = Pattern.compile("(0[xX][0-9A-Fa-f]+)|([0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?)").matcher(query.substring(position));
					m.lookingAt();
					int end = m.end() + position;
					position+=m.end();
					String matchedString = m.group();
					if (matchedString.startsWith("0x") || matchedString.startsWith("0X")) {
						Long hexValue = Long.parseLong(matchedString.substring(2), 16);
						return new LongToken(TokenTypes.LONGLITERAL, position, end, matchedString, hexValue);
					} 
					if (matchedString.startsWith("0")) {
						//might be an octal value
						try {
							Long octValue = Long.parseLong(matchedString.substring(1), 8);
							return new LongToken(TokenTypes.LONGLITERAL, position, end, matchedString, octValue);
						} catch (Exception ex) {
							//no octal value
						}
					}
					try {
						Long decValue = Long.parseLong(matchedString);
						return new LongToken(TokenTypes.LONGLITERAL, position, end, matchedString, decValue);
					} catch (Exception ex) {
						//no decimal long value
					}
					Double doubleValue = Double.parseDouble(matchedString);
					return new DoubleToken(TokenTypes.DOUBLELITERAL, position, end, matchedString, doubleValue);
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
					} else if (tokenText.equals(fixedTokens
							.get(TokenTypes.POS_INFINITY))
							|| tokenText.equals(fixedTokens
									.get(TokenTypes.NEG_INFINITY))
							|| tokenText.equals(fixedTokens
									.get(TokenTypes.NOT_A_NUMBER))) {
						recognizedToken = matchDoubleConstantToken(start, position
								- start, tokenText);
					}  else {
						recognizedToken = new ComplexToken(TokenTypes.IDENTIFIER,
								start, position - start, tokenText);
					}
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

	
	private final Token matchDoubleConstantToken(int start, int i,
			String tokenText) {
		Double value = Double.parseDouble(tokenText);
		return new DoubleToken(TokenTypes.DOUBLELITERAL, start, i, tokenText,
				value);
	}

	private final boolean isNumber(char c) {
		return (c >= Character.valueOf('0')) && (c <= Character.valueOf('9'));
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
			if ((position < (query.length() - 2))
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
			if ((position < (query.length() - 4))
					&& (query.substring(position, position + 2).equals("/*"))) {
				position++;
				while ((position < (query.length() - 1))
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
				|| ((position < (query.length() - 2)) && (query.substring(
						position, position + 2).equals("//")))
				|| ((position < (query.length() - 4)) && (query.substring(
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
