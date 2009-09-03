package de.uni_koblenz.jgralab.greql2.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ManualGreqlLexer {


	protected static Map<TokenTypes, String> fixedTokens;

	{
		fixedTokens = new HashMap<TokenTypes, String>();
		fixedTokens.put(TokenTypes.T, "T");
		fixedTokens.put(TokenTypes.AND, "and");
		fixedTokens.put(TokenTypes.FALSE, "false");
		fixedTokens.put(TokenTypes.NOT, "not");
		fixedTokens.put(TokenTypes.NULL_VALUE, "null");
		fixedTokens.put(TokenTypes.OR, "or");
		fixedTokens.put(TokenTypes.TRUE, "true");
		fixedTokens.put(TokenTypes.XOR, "xor");
		fixedTokens.put(TokenTypes.AS, "as");
		fixedTokens.put(TokenTypes.BAG, "bag");
		fixedTokens.put(TokenTypes.MAP, "map");
		fixedTokens.put(TokenTypes.E, "E");
		fixedTokens.put(TokenTypes.ESUBGRAPH, "eSubgraph");
		fixedTokens.put(TokenTypes.EXISTS_ONE, "exists!");
		fixedTokens.put(TokenTypes.EXISTS, "exists");
		fixedTokens.put(TokenTypes.END, "end");
		fixedTokens.put(TokenTypes.FORALL, "forall");
		fixedTokens.put(TokenTypes.FROM, "from");
		fixedTokens.put(TokenTypes.IN, "in");
		fixedTokens.put(TokenTypes.LET, "let");
		fixedTokens.put(TokenTypes.LIST, "list");
		fixedTokens.put(TokenTypes.PATH, "path");
		fixedTokens.put(TokenTypes.PATHSYSTEM, "pathsystem");
		fixedTokens.put(TokenTypes.REC, "rec");
		fixedTokens.put(TokenTypes.REPORT, "report");
		fixedTokens.put(TokenTypes.REPORTSET, "reportSet");
		fixedTokens.put(TokenTypes.REPORTBAG, "reportBag");
		fixedTokens.put(TokenTypes.REPORTTABLE, "reportTable");
		fixedTokens.put(TokenTypes.REPORTMAP, "reportMap");
		fixedTokens.put(TokenTypes.STORE, "store");
		fixedTokens.put(TokenTypes.SET, "set");
		fixedTokens.put(TokenTypes.TUP, "tup");
		fixedTokens.put(TokenTypes.USING, "using");
		fixedTokens.put(TokenTypes.V, "V");
		fixedTokens.put(TokenTypes.VSUBGRAPH, "vSubgraph");
		fixedTokens.put(TokenTypes.WHERE, "where");
		fixedTokens.put(TokenTypes.WITH, "with");
		fixedTokens.put(TokenTypes.QUESTION, "?");
		fixedTokens.put(TokenTypes.EXCL, "!");
		fixedTokens.put(TokenTypes.COLON, ":");
		fixedTokens.put(TokenTypes.COMMA, ",");
		fixedTokens.put(TokenTypes.DOT, ".");
		fixedTokens.put(TokenTypes.DOTDOT, "..");
		fixedTokens.put(TokenTypes.AT, "@");
		fixedTokens.put(TokenTypes.LPAREN, "(");
		fixedTokens.put(TokenTypes.RPAREN, ")");
		fixedTokens.put(TokenTypes.LBRACK, "[");
		fixedTokens.put(TokenTypes.RBRACK, "]");
		fixedTokens.put(TokenTypes.LCURLY, "{");
		fixedTokens.put(TokenTypes.RCURLY, "}");
		fixedTokens.put(TokenTypes.EDGESTART, "<-");
		fixedTokens.put(TokenTypes.EDGEEND, "->");
		fixedTokens.put(TokenTypes.EDGE, "--");
		fixedTokens.put(TokenTypes.RARROW, "-->");
		fixedTokens.put(TokenTypes.LARROW, "<--");
		fixedTokens.put(TokenTypes.ARROW, "<->");
		fixedTokens.put(TokenTypes.ASSIGN, ":=");
		fixedTokens.put(TokenTypes.EQUAL, "=");
		fixedTokens.put(TokenTypes.MATCH, "=~");
		fixedTokens.put(TokenTypes.NOT_EQUAL, "<>");
		fixedTokens.put(TokenTypes.LE, "<=");
		fixedTokens.put(TokenTypes.GE, ">=");
		fixedTokens.put(TokenTypes.L_T, "<");
		fixedTokens.put(TokenTypes.G_T, ">");
		fixedTokens.put(TokenTypes.DIV, "/");
		fixedTokens.put(TokenTypes.PLUS, "+");
		fixedTokens.put(TokenTypes.MINUS, "-");
		fixedTokens.put(TokenTypes.STAR, "*");
		fixedTokens.put(TokenTypes.MOD, "%");
		fixedTokens.put(TokenTypes.SEMI, ";");
		fixedTokens.put(TokenTypes.CARET, "^");
		fixedTokens.put(TokenTypes.BOR, "|");
		fixedTokens.put(TokenTypes.AMP, "&");
		fixedTokens.put(TokenTypes.SMILEY, ":-)");
		fixedTokens.put(TokenTypes.HASH, "#");
		fixedTokens.put(TokenTypes.OUTAGGREGATION, "<>--");
		fixedTokens.put(TokenTypes.INAGGREGATION, "--<>");
		fixedTokens.put(TokenTypes.PATHSYSTEMSTART, "-<");
		fixedTokens.put(TokenTypes.IMPORT, "import");
	}
	


	

	protected String query = null;

	protected int position = 0;

	public ManualGreqlLexer(String source) {
		this.query = source;
		if (query == null)
			throw new NullPointerException("Cannot parse nullpointer as GReQL query");
	}

	public static String getTokenString(TokenTypes token) {
		return fixedTokens.get(token);
	}
	
	private final static boolean isSeparator(int c) {
		return     c == ';' || c == '<' || c == '>' || c == '(' || c == ')'
				|| c == '{' || c == '}' || c == ':' || c == '[' || c == ']'
				|| c == ',' || c == ' ' || c == '.' || c == '-' || c == '+' 
				|| c == '*' || c == '/' || c == '%' || c == '~' || c == '='
				|| c == '?'	|| c == '^' || c == '|' || c == '!';
	}
	
	
	public Token getNextToken() {
		TokenTypes recognizedTokenType = null;
		Token recognizedToken = null;
		int bml = 0; // best match length
		skipWs();
		//recognize fixed tokens
		for (Entry<TokenTypes, String> currentEntry : fixedTokens.entrySet()) {
			String currentString = currentEntry.getValue();
			int currLen = currentString.length();
			if (bml > currLen)
				continue;
			if (query.regionMatches(position, currentString, 0, currLen)) {
				if (((position+currLen) == query.length()) || isSeparator(query.charAt(position + currLen - 1))
						|| isSeparator(query.charAt(position + currLen))) {
					bml = currLen;
					recognizedTokenType = currentEntry.getKey();
				}
			}
		}
		//recognize strings and identifiers
		if (recognizedTokenType == null) {
			if (query.charAt(position) == '\"') { //String
				position++;
				int start = position;
				StringBuilder sb = new StringBuilder();
				while (position < query.length() && query.charAt(position) != '\"') {
					if (query.charAt(position) == '\\') {
						if (position == query.length())
							throw new ParsingException("String started at position " + start + " but is not closed in query", query.substring(start, position), start, position-start);
						if ((query.charAt(position+1) == '"') || (query.charAt(position+1) == '\\')) {
							position++;
						}	
					} 
					sb.append(query.charAt(position));
					position++;
				}
				if (query.charAt(position) != '\"')
					throw new ParsingException("String started at position " + start + " but is not closed in query", sb.toString(), start, position-start);
				recognizedTokenType = TokenTypes.STRING;
				recognizedToken = new ComplexToken(TokenTypes.STRING, start, position, sb.toString());
				position++;
			} else {
			//identifier and literals
				StringBuffer nextPossibleToken = new StringBuffer();
				int start = position;
				while ((query.length() > position)
						&& (!isSeparator(query.charAt(position))))
					nextPossibleToken.append(query.charAt(position++));
				String tokenText = nextPossibleToken.toString(); 
				if (tokenText.equals("thisVertex")) {
					recognizedToken = new ComplexToken(TokenTypes.THISVERTEX, start, position-start, tokenText);
				} else if (tokenText.equals("thisEdge")) {
					recognizedToken = new ComplexToken(TokenTypes.THISEDGE, start, position-start, tokenText);
				} else if (startsWithNumber(tokenText)) {
					recognizedToken = matchNumericToken(start, position-start, tokenText);
				} else {	
					recognizedToken = new ComplexToken(TokenTypes.IDENTIFIER, start, position-start, tokenText);
				}	
		
			}	
		} else {
			recognizedToken = new SimpleToken(recognizedTokenType, position, bml);
			position += bml;
		}
		if (recognizedToken == null)
			throw new ParsingException("Error while scanning query at position", null, position, position);
		return recognizedToken;
	} 

	private final boolean startsWithNumber(String text) {
		char c = text.charAt(0);
		return (c >= Character.valueOf('0')) && (c <=Character.valueOf('9'));
	}

	//TODO: Exponenten
	private final Token matchNumericToken(int start, int end, String text) {
		int value = 0;
		int decValue = 0;
		TokenTypes type = null;
		if ( (text.charAt(0) == '0') && (text.charAt(text.length()-1) != 'f') && (text.charAt(text.length()-1) != 'F') && (text.charAt(text.length()-1) != 'd') && (text.charAt(text.length()-1) != 'D'))  {
			if (text.length() == 1) {
				type = TokenTypes.INTLITERAL;
				value = 0;
			} else if ((text.charAt(1) == 'x') || (text.charAt(1) == 'X')) {
				type = TokenTypes.HEXLITERAL;
				try {
					value = Integer.parseInt(text.substring(2), 16);
				} catch (NumberFormatException ex) {
					throw new ParsingException("Not a valid hex number", text, start, end-start);
				}	
			} else {
				type = TokenTypes.OCTLITERAL;
				try {
					value = Integer.parseInt(text.substring(1), 8);
					decValue = Integer.parseInt(text);
				} catch (NumberFormatException ex) {
					throw new ParsingException("Not a valid octal number", text, start, end-start);
				}		
			}
		} else {
			switch (text.charAt(text.length()-1)) {
				case 'h':
					type = TokenTypes.HEXLITERAL;
					try {
						value = Integer.parseInt(text.substring(0, text.length()-1), 16);
					} catch (NumberFormatException ex) {
						throw new ParsingException("Not a valid hex number", text, start, end-start);
					}	
					break;
				case 'd':
				case 'D':
				case 'f':
				case 'F':	
					type = TokenTypes.REALLITERAL;
					try {
						String tokenString = text.substring(0, text.length()-1);
						System.out.println("TokenString: " + tokenString);
						return new RealToken(type, start, end-start,  Double.parseDouble(tokenString));
					} catch (NumberFormatException ex) {
						throw new ParsingException("Not a valid float number", text, start, end-start);
					}
				default:
					type = TokenTypes.INTLITERAL;
					try {
						value = Integer.parseInt(text);
					} catch (NumberFormatException ex) {
						throw new ParsingException("Not a valid integer number", text, start, end-start);
					}	
			}
		}
		if (type != TokenTypes.OCTLITERAL)
			decValue = value;
		return new IntegerToken(type, start, end-start, value, decValue);
		
	}

	public boolean hasNextToken() {
		skipWs();
		return (position < query.length());
	}

	private final static boolean isWs(int c) {
		return c == ' ' || c == '\n' || c == '\t' || c == '\r';
	}

	private final void skipWs() {
		// skip whitespace and consecutive single line comments
		do {
			// skip whitespace
			while ((position < query.length()) && isWs(query.charAt(position)))
				position++;
			// skip single line comments
			if (query.regionMatches(position, "//", 0, 2)) {
				while ((position < query.length())
						&& (query.charAt(position) != '\n'))
					position++;
			}
		} while ((position < query.length()) && isWs(query.charAt(position)));
	}
	
	
	public static List<Token> scan(String query) {
		List<Token> list = new ArrayList<Token>();
		ManualGreqlLexer lexer = new ManualGreqlLexer(query);
		while(lexer.hasNextToken()) {
			Token nextToken = lexer.getNextToken();
			list.add(nextToken);
		}
		return list;
	}
	
	

}
