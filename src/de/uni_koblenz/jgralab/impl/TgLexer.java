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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import de.uni_koblenz.jgralab.exception.GraphIOException;

public class TgLexer {
	public enum Token {
		NULL_LITERAL("n"), TRUE_LITERAL("t"), FALSE_LITERAL("f"), COMMA(","), LT(
				"<"), GT(">"), LBR("("), RBR(")"), LSQ("["), RSQ("]"), LCRL("{"), RCRL(
				"}"), HYPHEN("-"), EQ("="), ASTERISK("*"), COLON(":"), SEMICOLON(
				";"), TGRAPH("TGraph"), SCHEMA("Schema"), GRAPHCLASS(
				"GraphClass"), ABSTRACT("abstract"), VERTEXCLASS("VertexClass"), EDGECLASS(
				"EdgeClass"), FROM("from"), TO("to"), ROLE("role"), AGGREGATION(
				"aggregation"), NONE("none"), SHARED("shared"), COMPOSITE(
				"composite"), ENUMDOMAIN("EnumDomain"), RECORDDOMAIN(
				"RecordDomain"), COMMENT("Comment"), PACKAGE("Package"), GRAPH(
				"Graph"), LIST("List"), LIST2(".LIST"), SET("Set"), SET2(".Set"), MAP(
				"Map"), MAP2(".Map"), TEXT(null), INT(null), STRING(null), EOF(
				null);

		String text;

		private Token(String text) {
			this.text = text;
		}

		@Override
		public String toString() {
			return text == null ? super.toString() : text;
		}
	}

	private static final int BUFFER_SIZE = 65536;
	private static final int LEXEM_SIZE = 1024;

	private InputStream in;
	private int line;
	private int la;
	private int putBackChar;
	private char[] lexem;
	private int lexemPos;
	private StringBuilder lexemBuilder;
	private static TgTokenRecognizer rec = new TgTokenRecognizer();

	private byte[] buffer;
	private int bufferSize;
	private int bufferPos;
	private String filename;

	public TgLexer(InputStream is, String filename) throws GraphIOException {
		this.filename = filename;
		in = is;
		buffer = new byte[BUFFER_SIZE];
		lexem = new char[LEXEM_SIZE];
		init();
	}

	public TgLexer(String s) throws GraphIOException {
		buffer = s.getBytes(Charset.forName("US-ASCII"));
		bufferSize = buffer.length;
		init();
	}

	private final void init() throws GraphIOException {
		putBackChar = -1;
		line = 1;
		la = read();
	}

	public String getLocation() {
		if (filename == null) {
			return "line " + getLine() + ": ";
		} else {
			return getFilename() + " line " + getLine() + ": ";
		}
	}

	public int getLine() {
		return line;
	}

	public String getFilename() {
		return filename;
	}

	private final int read() throws GraphIOException {
		int ch;
		if (putBackChar >= 0) {
			ch = putBackChar;
			putBackChar = -1;
		} else {
			if (bufferPos < bufferSize) {
				ch = buffer[bufferPos++];
			} else {
				if (in != null) {
					try {
						bufferSize = in.read(buffer);
					} catch (IOException e) {
						throw new GraphIOException(getLocation()
								+ "Caught IOException", e);
					}
					bufferPos = 0;
				}
				if (bufferPos < bufferSize) {
					ch = buffer[bufferPos++];
				} else {
					ch = -1;
				}
			}
		}
		if (ch == '\n') {
			++line;
		}
		return ch;
	}

	public final static boolean isWs(int c) {
		return (c == ' ') || (c == '\n') || (c == '\r') || (c == '\t');
	}

	public final static boolean isSeparator(int c) {
		return (c == ';') || (c == '<') || (c == '>') || (c == '(')
				|| (c == ')') || (c == '{') || (c == '}') || (c == ':')
				|| (c == '[') || (c == ']') || (c == ',') || (c == '=');
	}

	public final static boolean isDelimiter(int c) {
		return (c == ' ') || (c == ';') || (c == '\n') || (c == '\r')
				|| (c == '<') || (c == '>') || (c == '(') || (c == ')')
				|| (c == '{') || (c == '}') || (c == ':') || (c == '[')
				|| (c == ']') || (c == ',') || (c == '=') || (c == '\t')
				|| (c == -1);
	}

	public final Token nextToken() throws GraphIOException {
		// skip whitespace and consecutive single line comments
		while (true) {
			// skip whitespace
			while (isWs(la)) {
				la = read();
			}
			if (la != '/') {
				break;
			}
			// skip single line comments
			la = read();
			if ((la >= 0) && (la == '/')) {
				// single line comment, skip to the end of the current line
				while ((la >= 0) && (la != '\n')) {
					la = read();
				}
			} else {
				putBackChar = la;
				if (la == '\n') {
					--line;
				}
				la = '/';
				break;
			}
		}
		// build token
		lexemPos = 0;
		lexemBuilder = null;
		rec.reset();
		if (isSeparator(la)) {
			rec.next(la);
			append(la);
			la = read();
		} else if (la == '"') {
			readUtfString();
			return Token.STRING;
		} else {
			if (la >= 0) {
				while (!isDelimiter(la)) {
					rec.next(la);
					append(la);
					la = read();
				}
			} else {
				return Token.EOF;
			}
		}
		return rec.getToken();
	}

	private final void append(int c) {
		if (lexemPos < LEXEM_SIZE) {
			lexem[lexemPos++] = (char) c;
		} else {
			if (lexemBuilder == null) {
				lexemBuilder = new StringBuilder();
			}
			lexemBuilder.append(new String(lexem, 0, lexemPos));
			lexem[0] = (char) c;
			lexemPos = 1;
		}
	}

	public final String getLexem() {
		if (lexemBuilder != null) {
			if (lexemPos > 0) {
				lexemBuilder.append(new String(lexem, 0, lexemPos));
				lexemPos = 0;
			}
			return lexemBuilder.toString();
		}
		return new String(lexem, 0, lexemPos);
	}

	public final long getLong() {
		return rec.getValue();
	}

	public final int getInt() {
		return (int) (rec.getValue());
	}

	private final void readUtfString() throws GraphIOException {
		int startLine = line;
		la = read();
		LOOP: while ((la >= 0) && (la != '"')) {
			if ((la < 32) || (la > 127)) {
				throw new GraphIOException(getLocation()
						+ "Invalid character '" + (char) la
						+ "' in string in line " + line);
			}
			if (la == '\\') {
				la = read();
				if (la < 0) {
					break LOOP;
				}
				switch (la) {
				case '\\':
					la = '\\';
					break;
				case '"':
					la = '"';
					break;
				case 'n':
					la = '\n';
					break;
				case 'r':
					la = '\r';
					break;
				case 't':
					la = '\t';
					break;
				case 'u':
					la = read();
					if (la < 0) {
						break LOOP;
					}
					String unicode = "" + (char) la;
					la = read();
					if (la < 0) {
						break LOOP;
					}
					unicode += (char) la;
					la = read();
					if (la < 0) {
						break LOOP;
					}
					unicode += (char) la;
					la = read();
					if (la < 0) {
						break LOOP;
					}
					unicode += (char) la;
					try {
						la = Integer.parseInt(unicode, 16);
					} catch (NumberFormatException e) {
						throw new GraphIOException(getLocation()
								+ "Invalid unicode escape sequence '\\u"
								+ unicode + "' in line " + line);
					}
					break;
				default:
					throw new GraphIOException(getLocation()
							+ "Invalid escape sequence in string in line "
							+ line);
				}
			}
			append(la);
			la = read();
		}
		if (la < 0) {
			throw new GraphIOException(getLocation()
					+ "Unterminated string starting in line " + startLine);
		}
		la = read();
	}
}
