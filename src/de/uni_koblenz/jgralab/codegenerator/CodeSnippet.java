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

package de.uni_koblenz.jgralab.codegenerator;

import java.util.Vector;

/**
 * represents little parts of code code may be put into it in the form of
 * Strings and this code may be formated using new lines and tabulators
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class CodeSnippet extends CodeBlock {
	/**
	 * stores the lines of the code of <code>this</code>
	 */
	protected Vector<String> lines;

	/**
	 * defines if a new line should start the CodeSnippet
	 */
	protected boolean wantsNewLine;

	/**
	 * creates an empty <code>CodeSnippet</code>
	 */
	public CodeSnippet() {
		this((CodeList) null);
	}

	/**
	 * creates a <code>CodeSnippet</code> according to their order each
	 * initialLine will be put into the <code>CodeSnippet</code> with a new line
	 * at its end call with an empty String will result in a CodeSnippet
	 * consisting of one new line call with null results in an empty CodeSnippet
	 * is only called from within the codegenerator, therefore it handles
	 * exceptions optimistically
	 * 
	 * @param initialLines
	 *            the lines the <code>CodeSnippet</code> is composed of, none of
	 *            the lines equals <code>null</code>
	 */
	public CodeSnippet(String... initialLines) {
		this(null, false, initialLines);
	}

	/**
	 * creates a <code>CodeSnippet</code> which may start with a new line every
	 * initialLine is put into a new line of the <code>CodeSnippet</code> an
	 * empty String will result in a new, empty line is only called from within
	 * the codegenerator, therefore it handles exceptions optimistically
	 * 
	 * @param newLine
	 *            defines if previous to the first of the
	 *            <code>initialLines</code> a new line shall be inserted, must
	 *            not be <code>null</code>
	 * @param initialLines
	 *            the lines the <code>CodeSnippet</code> is composed of, none of
	 *            the lines equals <code>null</code>
	 */
	public CodeSnippet(boolean newLine, String... initialLines) {
		this(null, newLine, initialLines);
	}

	/**
	 * creates a <code>CodeSnippet</code> without a new line at the beginning
	 * and adds the <code>CodeSnippet</code> to <code>parent</code> is only
	 * called from within the codegenerator, therefore it handles exceptions
	 * optimistically
	 * 
	 * @param parent
	 *            a <code>CodeList</code>, to which the created
	 *            <code>CodeSnippet</code> is added, if it is not empty
	 * @param initialLines
	 *            are put into the <code>CodeSnippet</code>, every String will
	 *            be a new line, none of the lines equals <code>null</code>
	 */
	public CodeSnippet(CodeList parent, String... initialLines) {
		this(parent, false, initialLines);
	}

	/**
	 * creates a <code>CodeSnippet</code> containing <code>initialLines</code>
	 * and adds this <code>CodeSnippet</code> to <code>parent</code> is only
	 * called from within the codegenerator, therefore it handles exceptions
	 * optimistically
	 * 
	 * @param parent
	 *            a <code>CodeList</code>, to which the created
	 *            <code>CodeSnippet</code> is added, if it is not empty
	 * @param newLine
	 *            decides whether a new line is put in front of the first of the
	 *            <code>initialLines</code>, must not be <code>null</code>
	 * @param initialLines
	 *            amount of Strings, every String put into the
	 *            <code>CodeSnippet</code> with a new line command at its end,
	 *            none of the lines equals <code>null</code>
	 */
	public CodeSnippet(CodeList parent, boolean newLine, String... initialLines) {
		super(parent);
		wantsNewLine = newLine;
		this.lines = new Vector<String>();
		if (initialLines != null) {
			for (String line : initialLines) {
				lines.add(line);
			}
		}
	}

	/**
	 * setter for <code>b</code>
	 * 
	 * @param b
	 *            decides whether the String build out of <code>this</code>
	 *            should start with a new line
	 */
	public void setNewLine(boolean b) {
		wantsNewLine = b;
	}

	/**
	 * adds <code>addedLines</code> to <code>this</code> as long as
	 * <code>addedLines</code> is not <code>null</code>
	 * 
	 * @param addedLines
	 *            an amount of String, each of these Strings will be added into
	 *            a new line at the end of <code>this</code>
	 */
	public void add(String... addedLines) {
		if (addedLines != null) {
			for (String line : addedLines) {
				lines.add(line);
			}
		}
	}

	/**
	 * builds a String with a new line(="\n") at the end of each String the
	 * <code>CodeSnippet</code> consists of and <code>indentLevel</code>
	 * tabulators (="\t") in front of each of these Strings furthermore the
	 * resulting String may start with a new line if the corresponding property
	 * was set using <code>SetNewLine(boolean), 
	 * CodeSnippet(boolean,String...) or CodeSnippet(CodeList, boolean, String...)</code>
	 * 
	 * @return <code>this</code> as a String, the String is empty, if
	 *         <code>this</code> is empty
	 * @param indentLevel
	 *            defines the number of tabulators used in the beginning of each
	 *            line
	 */
	@Override
	public String getCode(int indentLevel) {
		if (lines.size() == 0) {
			return "";
		}
		StringBuilder buf = new StringBuilder();
		if (wantsNewLine) {
			buf.append("\n");
		}
		for (String line : lines) {
			for (int i = 0; i < indentLevel; ++i) {
				buf.append('\t');
			}
			if (line.length() > 0) {
				buf.append(replaceVariables(line));
			}
			buf.append('\n');
		}
		return buf.toString();
	}

	/**
	 * clears <code>this</code>
	 */
	@Override
	public void clear() {
		lines.clear();
	}

	/**
	 * @return the amount of lines <code>this</code> consists of
	 */
	@Override
	public int size() {
		return lines.size();
	}
}
