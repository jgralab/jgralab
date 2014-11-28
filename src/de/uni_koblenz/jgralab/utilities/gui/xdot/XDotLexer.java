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
package de.uni_koblenz.jgralab.utilities.gui.xdot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author ist@uni-koblenz.de
 */
public class XDotLexer {
	private int line;
	private int col;
	private int la;
	private int putBackChar;
	private BufferedReader in;

	public enum Type {
		TEXT, SEPARATOR, STRING, EOF
	}

	public static final class Token {
		public Type type;
		public String text;

		private Token(Type t, String s) {
			type = t;
			text = s;
		}
	}

	public XDotLexer(InputStream is) throws IOException {
		in = new BufferedReader(new InputStreamReader(is, "UTF-8"));
		putBackChar = -1;
		line = 1;
		col = 0;
		la = read();
	}

	public final int getLine() {
		return line;
	}

	public final Token nextToken() throws IOException {
		skipWs();
		Type type = null;
		StringBuilder out = new StringBuilder();
		if (la < 0) {
			type = Type.EOF;
		} else if (la == '"') {
			readQuotedString(out);
			type = Type.STRING;
		} else if (la == '<') {
			readHtmlString(out);
			type = Type.STRING;
		} else if (isSeparator(la)) {
			out.append((char) la);
			type = Type.SEPARATOR;
			la = read();
		} else {
			if (la >= 0) {
				do {
					out.append((char) la);
					la = read();
				} while (!isWs(la) && !isSeparator(la) && (la >= 0));
			}
			type = Type.TEXT;
		}
		Token t = new Token(type, out.toString());
		// if (t.text.equals("{") || t.text.equalsIgnoreCase("}")
		// || t.text.equals(";")) {
		// System.out.println("'" + t.text + "'");
		// } else {
		// System.out.print("'" + t.text + "' ");
		// }
		return t;
	}

	private final int read() throws IOException {
		int ch;
		if (putBackChar >= 0) {
			ch = putBackChar;
			putBackChar = -1;
		} else {
			ch = in.read();
		}
		if (ch == '\n') {
			++line;
			col = 0;
			return ch;
		}
		// check for (possible repeated) escaped line ends
		while (ch == '\\') {
			ch = in.read();
			if (ch == '\r') {
				// check for windows line end (CR-LF)
				ch = in.read();
				if (ch == '\n') {
					++line;
					ch = in.read();
				} else if (ch != -1) {
					throw new IOException(
							"Expected line end CR/LF but found CR/ASCII " + ch);
				}
			} else if (ch == '\n') {
				// unix line end
				++line;
				ch = in.read();
			} else {
				putBackChar = ch;
				ch = '\\';
				return ch;
			}
		}
		++col;
		return ch;
	}

	private final void readQuotedString(StringBuilder out) throws IOException {
		int startLine = line;
		la = read();
		LOOP: while ((la >= 0) && (la != '"')) {
			if (la == '\\') {
				la = read();
				if (la < 0) {
					break LOOP;
				}
				if (la != '"') {
					out.append("\\");
				}
			}
			out.append((char) la);
			la = read();
		}
		if (la < 0) {
			throw new IOException(
					"Unterminated quoted string starting in line " + startLine);
		}
		la = read();
	}

	private final void readHtmlString(StringBuilder out) throws IOException {
		int startLine = line;
		la = read();
		while ((la >= 0) && (la != '>')) {
			out.append((char) la);
			la = read();
		}
		if (la < 0) {
			throw new IOException("Unterminated HTML string starting in line "
					+ startLine);
		}
		la = read();
	}

	private final static boolean isWs(int c) {
		return (c == ' ') || (c == '\n') || (c == '\t') || (c == '\r');
	}

	private final static boolean isSeparator(int c) {
		return (c == ';') || (c == '{') || (c == '}') || (c == '[')
				|| (c == ']') || (c == '=') || (c == ',');
	}

	private final void skipWs() throws IOException {
		// skip whitespace and consecutive single line comments
		do {
			// skip whitespace
			while (isWs(la)) {
				la = read();
			}
			// skip comments
			while (la == '/' || (la == '#' && col == 1)) {
				if (la == '#' && col == 1) {
					// single line CPP comment, skip to the end of the current
					// line
					while ((la >= 0) && (la != '\n')) {
						la = read();
					}
				} else {
					la = read();
					if ((la >= 0) && (la == '/')) {
						// single line comment, skip to the end of the current
						// line
						while ((la >= 0) && (la != '\n')) {
							la = read();
						}
					} else if ((la >= 0) && (la == '*')) {
						// multi line comment
						int startLine = line;
						do {
							while ((la >= 0) && (la != '*')) {
								la = read();
							}
							if (la >= 0) {
								la = read();
							}
						} while (la >= 0 && la != '/');
						if (la < 0) {
							throw new IOException(
									"Unterminated multi line comment starting in line "
											+ startLine);
						}
						la = read();
					} else {
						putback(la);
						la = '/';
						return;
					}
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

}
