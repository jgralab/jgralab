package de.uni_koblenz.jgralab.gretl.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import de.uni_koblenz.jgralab.gretl.CreateSubgraph;
import de.uni_koblenz.jgralab.gretl.MatchReplace;

public class GReTLLexer {
	private String transformText = null;
	private int position = 0;

	private List<Token> tokens = null;

	/**
	 * Names of transformation ops that have their own parser.
	 */
	private static Set<String> transformsWithCustomDSL = new HashSet<String>();

	static {
		transformsWithCustomDSL.add(CreateSubgraph.class.getSimpleName());
		transformsWithCustomDSL.add(MatchReplace.class.getSimpleName());
	}

	private GReTLLexer(String source) {
		transformText = source;
		position = 0;
		tokens = new ArrayList<Token>();
		if (transformText == null) {
			throw new NullPointerException(
					"Cannot parse nullpointer as GReTL transformation.");
		}
	}

	protected static Map<TokenTypes, String> fixedTokens = new LinkedHashMap<TokenTypes, String>();

	private static final class StringLengthComparator implements
			Comparator<String> {
		@Override
		public int compare(String o1, String o2) {
			int o1l = o1.length();
			int o2l = o1.length();
			int diff = o1l - o2l;
			if (diff != 0) {
				return diff;
			}
			return o1.compareTo(o2);
		}
	}

	static {
		fixedTokens.put(TokenTypes.TRANSFORMATION, "transformation");
		fixedTokens.put(TokenTypes.AGGREGATION, "aggregation");
		fixedTokens.put(TokenTypes.FROM, "from");
		fixedTokens.put(TokenTypes.ROLE, "role");
		fixedTokens.put(TokenTypes.TO, "to");
		fixedTokens.put(TokenTypes.TRANSFORM_ARROW, "<==");
		fixedTokens.put(TokenTypes.DEFINES, ":=");
		fixedTokens.put(TokenTypes.PAREN_OPEN, "(");
		fixedTokens.put(TokenTypes.PAREN_CLOSE, ")");
		fixedTokens.put(TokenTypes.COLON, ":");
		fixedTokens.put(TokenTypes.ASSIGN, "=");
		fixedTokens.put(TokenTypes.SEMICOLON, ";");
		fixedTokens.put(TokenTypes.COMMA, ",");
		fixedTokens.put(TokenTypes.GREQL_IMPORT, "import");
	}

	private final static String[] semanticExpSeparators = new String[] {
			fixedTokens.get(TokenTypes.DEFINES),
			fixedTokens.get(TokenTypes.TRANSFORM_ARROW) };

	static {
		Arrays.sort(semanticExpSeparators, new StringLengthComparator());
	}

	private final static boolean isSeparator(int c) {
		return (c == ';') || (c == '(') || (c == ')') || (c == '{')
				|| (c == '}') || (c == ':') || (c == ',')
				|| Character.isWhitespace(c) || (c == '-');
	}

	private final static boolean isStringQuote(int c) {
		return (c == '"') || (c == '\'');
	}

	private Token nextIdentifier() {
		StringBuilder nextPossibleToken = new StringBuilder();
		int start = position;
		while ((position < transformText.length())
				&& (!isSeparator(transformText.charAt(position)))) {
			nextPossibleToken.append(transformText.charAt(position++));
		}
		String tokenText = nextPossibleToken.toString();
		// System.out.println(tokenText);
		return new Token(TokenTypes.IDENT, tokenText, start, position);
	}

	/**
	 * Skip
	 *
	 * @param separator
	 * @return
	 */
	private int skipString() {
		char separator = transformText.charAt(position);
		int start = position;
		position++;
		while ((position < transformText.length())
				&& (transformText.charAt(position) != separator)) {
			if (transformText.charAt(position) == '\\') {
				if (position == transformText.length()) {
					throw new RuntimeException("String started at position "
							+ start + " but is not closed in query: "
							+ transformText.substring(start, position));
				}
				if ((transformText.charAt(position + 1) == separator)
						|| (transformText.charAt(position + 1) == '\\')) {
					position++;
				}
			}
			position++;
		}
		// skip the trailing " or '
		position++;

		if (position >= transformText.length()) {
			throw new RuntimeException("String started at position " + start
					+ " but is not closed in query: "
					+ transformText.substring(start));
		}

		return start;
	}

	private final void skipWhitespacesAndComments() {
		do {
			// skip whitespace
			while ((position < transformText.length())
					&& Character.isWhitespace(transformText.charAt(position))) {
				position++;
			}
			// skip single line comments
			if ((position < (transformText.length() - 2))
					&& (transformText.substring(position, position + 2)
							.equals("//"))) {
				position++;
				while ((position < transformText.length())
						&& (transformText.charAt(position) != '\n')) {
					position++;
				}
				if ((position < transformText.length())
						&& (transformText.charAt(position) == '\n')) {
					position++;
				}
			}
			// skip multiline comments
			if ((position < (transformText.length() - 4))
					&& (transformText.substring(position, position + 2)
							.equals("/*"))) {
				position++;
				while ((position < (transformText.length() - 1))
						&& (transformText.substring(position, position + 2)
								.equals("*/"))) {
					position++;
				}
				if ((position < transformText.length())
						&& (transformText.substring(position, position + 2)
								.equals("*/"))) {
					position += 2;
				}
			}
		} while (((position < transformText.length()) && (Character
				.isWhitespace(transformText.charAt(position))))
				|| ((position < (transformText.length() - 2)) && (transformText
						.substring(position, position + 2).equals("//")))
				|| ((position < (transformText.length() - 4)) && (transformText
						.substring(position, position + 2).equals("/*"))));
	}

	public static List<Token> scan(String query) {
		GReTLLexer lexer = new GReTLLexer(query);
		List<Token> tokens = lexer.scan();
		// System.out.println(tokens);
		return tokens;
	}

	public List<Token> scan() {
		boolean lexingGReQL = false;
		boolean lexingDSL = false;

		nextToken: do {
			skipWhitespacesAndComments();
			if (position == transformText.length()) {
				tokens.add(new Token(TokenTypes.EOF, "", position, position));
			} else if (lexingGReQL) {
				// We need to lex everything up to the trailing ; into a GReQL
				// token.
				int start = skipToSemicolonOrArrow();
				lexingGReQL = false;
				Token t = new Token(TokenTypes.GREQL, transformText.substring(
						start, position).trim(), start, position);
				tokens.add(t);
				continue nextToken;
			} else if (lexingDSL) {
				// We need to lex everything up to the next <== into a DSL
				// token.
				int start = skipToArrow();
				lexingDSL = false;
				Token t = new Token(TokenTypes.DOMAIN_SPECIFIC, transformText
						.substring(start, position).trim(), start, position);
				tokens.add(t);
				continue nextToken;
			} else {
				// First, check if it is a string
				if ((transformText.charAt(position) == '\'')
						|| (transformText.charAt(position) == '"')) {
					int start = skipString();
					Token t = new Token(TokenTypes.STRING,
							transformText.substring(start + 1, position - 1),
							start, position);
					tokens.add(t);
					continue nextToken;
				}

				// Ok, this is some real GReTL syntax
				int start = position;
				for (Entry<TokenTypes, String> e : fixedTokens.entrySet()) {
					if (transformText.regionMatches(start, e.getValue(), 0, e
							.getValue().length())) {
						char c = transformText.charAt(position
								+ e.getValue().length());
						// ensure that we don't match "to" in "town"...
						if (e.getValue().matches("\\p{Alnum}+")
								&& !isSeparator(c)) {
							// We matched a keyword, so the next has to be a
							// separator...
							continue;
						}
						position = start + e.getValue().length();
						Token t = new Token(e.getKey(), e.getValue(), start,
								position);
						tokens.add(t);

						// Does now follow GReQL?
						if ((e.getKey() == TokenTypes.TRANSFORM_ARROW)
								|| (e.getKey() == TokenTypes.DEFINES)) {
							lexingGReQL = true;
						}
						continue nextToken;

					}
				}

				// Ok, no fixed token was at the current position...
				Token t = nextIdentifier();
				tokens.add(t);

				// Does it be a transformation operation with own attribute
				// parsing/lexing capabilities?
				if (transformsWithCustomDSL.contains(t.value)) {
					lexingDSL = true;
				}

				continue nextToken;
			}
		} while (position < transformText.length());

		return tokens;
	}

	private int skipToArrow() {
		int start = position;
		while (!transformText.regionMatches(position,
				fixedTokens.get(TokenTypes.TRANSFORM_ARROW), 0, 3)) {
			position++;
		}
		return start;
	}

	private int skipToSemicolonOrArrow() {
		// TODO: we also need to handle comments in here...
		int start = position;
		char c = transformText.charAt(position);
		do {
			if (isStringQuote(c)) {
				skipString();
			}
			c = transformText.charAt(++position);
		} while ((c != ';')
				&& (!transformText.substring(position, position + 3).equals(
						fixedTokens.get(TokenTypes.TRANSFORM_ARROW))));
		return start;
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		BufferedReader r = new BufferedReader(new FileReader(new File(
				"testit/transforms/F2G_ICMT2011.gretl")));
		StringBuilder sb = new StringBuilder();
		String line = null;
		while ((line = r.readLine()) != null) {
			sb.append(line);
			sb.append('\n');
		}

		List<Token> tokens = GReTLLexer.scan(sb.toString());
		for (Token t : tokens) {
			System.out.println(t);
		}
	}

}
