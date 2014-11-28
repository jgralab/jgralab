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
				null), UNSET_LITERAL("u");

		String lexeme;

		private Token(String text) {
			this.lexeme = text;
		}

		@Override
		public String toString() {
			return lexeme == null ? super.toString() : lexeme;
		}
	}

	private InputStream in; // inputstream, read in chunks of BUFFER_SIZE bytes
	private String filename; // filename of input stream, if any
	private int line; // current line number
	private int la; // look-ahead character
	private int putBackChar;

	private static TgTokenRecognizer rec = new TgTokenRecognizer();

	private static final int TEXT_SIZE = 1024;
	private char[] text; // buffer for short tokens (length <= TEXT_SIZE)
	private int textPos; // write position in text buffer
	private StringBuilder textBuilder; // builder for long tokens
										// (length>TEXT_SIZE)

	private static final int BUFFER_SIZE = 65536;
	private byte[] buffer; // read buffer
	private int bufferSize; // number of bytes in read buffer
	private int bufferPos; // read position

	/**
	 * Creates a TgLexer for input stream <code>is</code>, optionally specifying
	 * a <code>filename</code>.
	 * 
	 * @param is
	 *            an input stream
	 * @param filename
	 *            optional filename for exception messages (can be null)
	 * @throws GraphIOException
	 *             when the <code>is</code> could not be read
	 */
	public TgLexer(InputStream is, String filename) throws GraphIOException {
		this.filename = filename;
		in = is;
		buffer = new byte[BUFFER_SIZE];
		text = new char[TEXT_SIZE];
		putBackChar = -1;
		line = 1;
		la = read();
	}

	/**
	 * Creates a TgLexer for {@link String} <code>s</code>.
	 * 
	 * @param s
	 *            input string, must not be null
	 * @throws GraphIOException
	 *             (actually, this constructor won't throw a GraphIOException)
	 */
	public TgLexer(String s) throws GraphIOException {
		buffer = s.getBytes(Charset.forName("US-ASCII"));
		bufferSize = buffer.length;
		text = new char[TEXT_SIZE];
		putBackChar = -1;
		line = 1;
		la = read();
	}

	/**
	 * @return a human readable input position
	 */
	public String getLocation() {
		if (filename == null) {
			return "line " + getLine() + ": ";
		} else {
			return getFilename() + " line " + getLine() + ": ";
		}
	}

	/**
	 * @return the current line in the input stream
	 */
	public int getLine() {
		return line;
	}

	/**
	 * @return the filename processed, can be <code>null</code>
	 */
	public String getFilename() {
		return filename;
	}

	/**
	 * Read the next character.
	 * 
	 * @return next character as <code>int</code> value, or -1 on EOF
	 * @throws GraphIOException
	 */
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

	/**
	 * @param c
	 *            character code
	 * @return true iff the <code>c</code> is whitespace in TG format
	 */
	public final static boolean isWs(int c) {
		return (c == ' ') || (c == '\n') || (c == '\r') || (c == '\t');
	}

	/**
	 * @param c
	 *            character code
	 * @return true iff the <code>c</code> is a separator in TG format
	 */
	public final static boolean isSeparator(int c) {
		return (c == ';') || (c == '<') || (c == '>') || (c == '(')
				|| (c == ')') || (c == '{') || (c == '}') || (c == ':')
				|| (c == '[') || (c == ']') || (c == ',') || (c == '=');
	}

	/**
	 * @param c
	 *            character code
	 * @return true iff the <code>c</code> is a delimiter (whitepsace or
	 *         separator) in TG format
	 */
	public final static boolean isDelimiter(int c) {
		return (c == ' ') || (c == ';') || (c == '\n') || (c == '\r')
				|| (c == '<') || (c == '>') || (c == '"') || (c == '(')
				|| (c == ')') || (c == '{') || (c == '}') || (c == ':')
				|| (c == '[') || (c == ']') || (c == ',') || (c == '=')
				|| (c == '\t') || (c == -1);
	}

	/**
	 * Reads the next token from the input.
	 * 
	 * @return a {@link Token}, or {@link Token#EOF} when input is exhausted
	 * @throws GraphIOException
	 *             when input can't be read
	 */
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
		textPos = 0;
		textBuilder = null;
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

	/**
	 * Appends character <code>c</code> to the {@link #text} buffer, lazily
	 * using {@link #textBuilder} when the token length exceeds
	 * {@link #TEXT_SIZE}.
	 * 
	 * @param c
	 *            a character code
	 * 
	 */
	private final void append(int c) {
		if (textPos < TEXT_SIZE) {
			text[textPos++] = (char) c;
		} else {
			if (textBuilder == null) {
				textBuilder = new StringBuilder();
			}
			textBuilder.append(new String(text, 0, textPos));
			text[0] = (char) c;
			textPos = 1;
		}
	}

	/**
	 * @return the text of the current token
	 */
	public final String getText() {
		if (textBuilder != null) {
			if (textPos > 0) {
				textBuilder.append(new String(text, 0, textPos));
				textPos = 0;
			}
			return textBuilder.toString();
		}
		return new String(text, 0, textPos);
	}

	/**
	 * @return Value of current token as <code>long</code> value (only valid
	 *         when last token was {@link Token#INT})
	 */
	public final long getLong() {
		assert rec.getToken() == Token.INT;
		return rec.getValue();
	}

	/**
	 * @return Value of current token as <code>int</code> value (only valid when
	 *         last token was {@link Token#INT})
	 */
	public final int getInt() {
		assert rec.getToken() == Token.INT;
		return (int) (rec.getValue());
	}

	/**
	 * Reads a TG string and appends its contents to the {@link #text} buffer.
	 * 
	 * @throws GraphIOException
	 *             when input can't be read
	 */
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
