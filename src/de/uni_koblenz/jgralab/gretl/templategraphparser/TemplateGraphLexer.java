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
package de.uni_koblenz.jgralab.gretl.templategraphparser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class TemplateGraphLexer {
	protected static Map<TokenType, String> fixedTokens;

	static {
		fixedTokens = new HashMap<>();
		fixedTokens.put(TokenType.L_CURLY, "{");
		fixedTokens.put(TokenType.R_CURLY, "}");
		fixedTokens.put(TokenType.PIPE, "|");
		fixedTokens.put(TokenType.ASSIGN, "=");
		fixedTokens.put(TokenType.L_ARROW, "<--");
		fixedTokens.put(TokenType.R_ARROW, "-->");
		fixedTokens.put(TokenType.L_PAREN, "(");
		fixedTokens.put(TokenType.R_PAREN, ")");
		fixedTokens.put(TokenType.COMMA, ",");
		fixedTokens.put(TokenType.TRIPLE_DOT, "...");
		fixedTokens.put(TokenType.HASH, "#");
	}

	private String text;
	private int position = 0;

	public TemplateGraphLexer(String ics) {
		text = ics;
	}

	public static List<Token> scan(String query) {
		List<Token> list = new ArrayList<>();
		TemplateGraphLexer lexer = new TemplateGraphLexer(query);
		while (lexer.hasNextToken()) {
			Token nextToken = lexer.getNextToken();
			list.add(nextToken);
		}
		// System.out.println(list);
		return list;
	}

	private Token getNextToken() {
		TokenType recognizedTokenType = null;
		skipWhitespaces();

		// fixed tokens
		for (Entry<TokenType, String> currentEntry : fixedTokens.entrySet()) {
			String tokStr = currentEntry.getValue();
			int tokLen = tokStr.length();
			if (text.regionMatches(position, tokStr, 0, tokLen)) {
				recognizedTokenType = currentEntry.getKey();
				Token tok = new SimpleToken(recognizedTokenType, position,
						tokLen);
				position += tokLen;
				return tok;
			}
		}

		// strings
		if (text.charAt(position) == '\'') {
			recognizedTokenType = TokenType.STRING;
			// match till the next double quote
			position++;
			int start = position;
			StringBuilder sb = new StringBuilder();
			while ((position < text.length())
					&& (text.charAt(position) != '\'')) {
				if (text.charAt(position) == '\\') {
					if (position == text.length()) {
						throw new TemplateGraphParserException(
								"String started at position " + start
										+ " but is not closed in query: '"
										+ text.substring(start, position)
										+ "'.");
					}
					if ((text.charAt(position + 1) == '\'')
							|| (text.charAt(position + 1) == '\\')) {
						position++;
					}
				}
				sb.append(text.charAt(position));
				position++;
			}
			if ((position >= text.length()) || (text.charAt(position) != '\'')) {
				throw new TemplateGraphParserException(
						"String started at position " + start
								+ " but is not closed in query: '"
								+ sb.toString() + "'.");
			}
			Token tok = new ComplexToken(recognizedTokenType, start, position,
					sb.toString());
			position++;
			return tok;
		} else {
			// this must me an identifier
			StringBuilder sb = new StringBuilder();
			int start = position;
			while ((text.length() > position)
					&& (!isSeparator(text.charAt(position)))) {
				sb.append(text.charAt(position++));
			}
			if (start == position) {
				throw new TemplateGraphParserException(
						"Zero-length IDENTIFIER at position " + position + ".");
			}
			Token tok = new ComplexToken(TokenType.IDENT, start, position
					- start, sb.toString());
			return tok;
		}
	}

	private boolean isSeparator(char c) {
		return fixedTokens.values().contains(String.valueOf(c))
				|| isWhitespace(c) || (c == '-') || (c == '<') || (c == ';');
	}

	private boolean hasNextToken() {
		skipWhitespaces();
		return (position < text.length());
	}

	private final static boolean isWhitespace(int c) {
		return (c == ' ') || (c == '\n') || (c == '\t') || (c == '\r');
	}

	private void skipWhitespaces() {
		while ((position < text.length())
				&& isWhitespace(text.charAt(position))) {
			position++;
		}
	}

	public static void main(String[] args) {
		System.out
				.println(TemplateGraphLexer
						.scan("v1(foo.bar.Bar 'tup(\"x\", $[1])' | x='\"foo\"', y= '$[0].y')"
								+ "-->{edge.Bar '$[17]'|e = '$[18].name'} v2(Baz '$[11]')"));

		System.out
				.println(TemplateGraphLexer
						.scan("v1(a.A 'tup($[1], \"aArch\")' | a1 = '\"A\"', a2='$[2] + 1') "
								+ "-->{a.E 'tup($[1], $[2])' | e1 = '$[17]'} v1"));

		System.out
				.println(TemplateGraphLexer
						.scan("v1(a.A 'tup($[1], \"aArch\")' | a1 = '\"A\"', a2='$[2] + 1') "
								+ "-->{a.E 'tup($[1], $[2])' | e1 = '$[17]'} v1"
								+ "<--{a.E2 '$[3]'} v3(a.B '$[4]'),"
								+ "v4('$[6]') <--{a.F '1'| e3='17'} v1"));
		System.out
				.println(TemplateGraphLexer
						.scan("v1(,'\"a.A\" ++ Foo' 'tup($[1], \"aArch\")' | a1 = '\"A\"', a2='$[2] + 1', ...) "));
	}
}
