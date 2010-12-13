/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2010 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
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
package de.uni_koblenz.jgralab.greql2.exception;

public class ParsingException extends Greql2Exception {

	private static final long serialVersionUID = 894099164202915776L;

	private String errorMessage;

	private String tokenString;

	private int offset;

	private int length;

	public ParsingException(String msg, String token, int offset, int length,
			String query) {
		super("Parsing error: " + msg + " at token '" + token
				+ "' at position (" + offset + "," + length + "): '"
				+ surrounding(query, offset, length) + "'");
		errorMessage = msg;
		this.tokenString = token;
		this.offset = offset;
		this.length = length;
	}

	private static String surrounding(String query, int off, int len) {
		if ((len < 0) || (off < 0)) {
			return "";
		}
		int s = off - 20;
		if (s < 0) {
			s = 0;
		}
		int e = off + len + 20;
		if (e > query.length()) {
			e = query.length();
		}
		int ol = off + len;
		if (ol > query.length()) {
			ol = query.length();
		}
		// System.out.println(off + ", " + len + ", " + s + ", " + e);
		String start = query.substring(s, off);
		String problematicPart = query.substring(off, ol);
		String end = query.substring(ol, e);
		return start + "‹" + problematicPart + "›" + end;
	}

	public int getOffset() {
		return offset;
	}

	public int getLength() {
		return length;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public String getToken() {
		return tokenString;
	}

}
