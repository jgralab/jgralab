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

import de.uni_koblenz.jgralab.exception.GraphException;
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

	private InputStream in;
	private int line;
	private int la;
	private int putBackChar;
	private StringBuilder lexem;
	private static TgTokenRecognizer rec = new TgTokenRecognizer();

	private byte[] buffer;
	private int bufferSize;
	private int bufferPos;

	public TgLexer(InputStream is) throws IOException {
		in = is;
		buffer = new byte[65536];
		init();
	}

	public TgLexer(String s) throws IOException {
		buffer = s.getBytes(Charset.forName("US-ASCII"));
		bufferSize = buffer.length;
		init();
	}

	private final void init() throws IOException {
		putBackChar = -1;
		line = 1;
		bufferPos = 0;
		la = read();
	}

	public int getLine() {
		return line;
	}

	private final int read() throws IOException {
		int ch;
		if (putBackChar >= 0) {
			ch = putBackChar;
			putBackChar = -1;
		} else {
			if (bufferPos < bufferSize) {
				ch = buffer[bufferPos++];
			} else {
				if (in != null) {
					bufferSize = in.read(buffer);
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
		return (c == ' ') || (c == '\n') || (c == '\t') || (c == '\r');
	}

	public final static boolean isSeparator(int c) {
		return (c == ';') || (c == '<') || (c == '>') || (c == '(')
				|| (c == ')') || (c == '{') || (c == '}') || (c == ':')
				|| (c == '[') || (c == ']') || (c == ',') || (c == '=');
	}

	private final void skipWs() throws IOException {
		// skip whitespace and consecutive single line comments
		do {
			// skip whitespace
			while (isWs(la)) {
				la = read();
			}
			// skip single line comments
			if (la == '/') {
				la = read();
				if ((la >= 0) && (la == '/')) {
					// single line comment, skip to the end of the current line
					while ((la >= 0) && (la != '\n')) {
						la = read();
					}
				} else {
					putback(la);
					la = '/';
				}
			}
		} while (isWs(la));
	}

	private final void putback(int ch) {
		putBackChar = ch;
		if (ch == '\n') {
			--line;
		}
	}

	public final Token nextToken() throws GraphIOException {
		try {
			lexem = new StringBuilder();
			skipWs();
			rec.reset();
			if (la == '"') {
				readUtfString();
				return Token.STRING;
			} else if (isSeparator(la)) {
				rec.next(la);
				lexem.append((char) la);
				la = read();
			} else {
				if (la >= 0) {
					do {
						rec.next(la);
						lexem.append((char) la);
						la = read();
					} while (!isWs(la) && !isSeparator(la) && (la >= 0));
				} else {
					return Token.EOF;
				}
			}
			return rec.getToken();
		} catch (IOException e) {
			throw new GraphException(e);
		}
	}

	public final String getLexem() {
		return lexem.toString();
	}

	public final long getLong() {
		return rec.getValue();
	}

	public final int getInt() {
		return (int) (rec.getValue());
	}

	private final void readUtfString() throws IOException {
		int startLine = line;
		la = read();
		LOOP: while ((la >= 0) && (la != '"')) {
			if ((la < 32) || (la > 127)) {
				throw new IOException("Invalid character '" + (char) la
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
						throw new IOException(
								"Invalid unicode escape sequence '\\u"
										+ unicode + "' in line " + line);
					}
					break;
				default:
					throw new IOException(
							"Invalid escape sequence in string in line " + line);
				}
			}
			lexem.append((char) la);
			la = read();
		}
		if (la < 0) {
			throw new IOException("Unterminated string starting in line "
					+ startLine);
		}
		la = read();
	}
}
