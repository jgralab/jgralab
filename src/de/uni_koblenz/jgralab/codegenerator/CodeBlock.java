/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2010 Institute for Software Technology
 *               University of Koblenz-Landau, Germany
 * 
 *               ist@uni-koblenz.de
 * 
 * Please report bugs to http://serres.uni-koblenz.de/bugzilla
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package de.uni_koblenz.jgralab.codegenerator;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * basic class to create and change all kind of code segments offering the
 * necessary methods offers the possibilities to create code out of the stored
 * Strings, store variables and their values, influence the relationship between
 * different <code>CodeBlock</code>s and others
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public abstract class CodeBlock {
	/**
	 * a map storing variables of <code>this</code> with their corresponding
	 * values
	 */
	private Map<String, String> variables;

	/**
	 * stores the lines of code
	 */
	private Stack<String> replacementStack;

	/**
	 * stores the parental <code>CodeList</code> of this, if a circle is to be
	 * created, by (indirectly) setting <code>this</code> as its own
	 * <code>parent</code> a new <code>CodeList</code> with the old content will
	 * be the <code>parent</code> of <code>this</code> f.ex. if A is
	 * <code>parent</code> of B, B is <code>parent</code> of C, C is about to be
	 * set as B´s <code>parent</code> than the <code>parent</code> of the
	 * <code>parent</code> of B, which is the <code>parent</code> of C, will be
	 * reset as the a <code>CodeList</code> combining A and the old B
	 */
	private CodeList parent;

	/**
	 * a number defining the number of tabulators that are to be set in front of
	 * every line of the content of <code>this</code>
	 */
	protected int additionalIndent;

	/**
	 * creates an empty CodeBlock with <code>null</code> as the parent
	 */
	protected CodeBlock() {
		this(null);
	}

	/**
	 * creates an empty CodeBlock
	 * 
	 * @param parent
	 *            the <code>CodeList</code> that will be set as the parent of
	 *            <code>this</code>
	 */
	protected CodeBlock(CodeList parent) {
		additionalIndent = 0;
		variables = new HashMap<String, String>();
		replacementStack = new Stack<String>();
		if (parent != null) {
			parent.add(this);
		}
	}

	/**
	 * 
	 * @param indentLevel
	 *            defines how many tabulators are set in the beginning of each
	 *            line
	 * @return the content of <code>this</code> as a String, new lines and
	 *         tabulators are set accordingly
	 */
	public abstract String getCode(int indentLevel);

	/**
	 * clears <code>this</code>
	 */
	public abstract void clear();

	/**
	 * 
	 * @return the size of <code>this</code>, where size means the number of
	 *         elements on the <code>replacementStack</code>
	 */
	public abstract int size();

	/**
	 * puts the content of <code>map</code> into <code>variables</code>
	 * 
	 * @param map
	 *            contains variables and their values
	 */
	public void addVariables(Map<String, String> map) {
		variables.putAll(map);
	}

	/**
	 * puts the <code>name<code> and the corresponding <code>value<code> into
	 * <code>variables</code>
	 * 
	 * @param name
	 *            a variable and
	 * @param value
	 *            its value
	 */
	public void setVariable(String name, String value) {
		variables.put(name, value);
	}

	/**
	 * 
	 * @param name
	 * @return the value of <code>name<code>, if <code>name</code> is not among
	 *         the stored <code>variables</code> a message of the form
	 *         *UNDEFINED:name* is returned
	 */
	public String getVariable(String name) {
		if (replacementStack.contains(name)) {
			return name;
		}
		replacementStack.push(name);
		try {
			CodeBlock block = this;
			while (block != null) {
				if (block.variables.containsKey(name)) {
					return replaceVariables(block.variables.get(name));
				}
				block = block.parent;
			}
			return "*UNDEFINED:" + name + "*";
		} finally {
			replacementStack.pop();
		}
	}

	/**
	 * 
	 * @param line
	 *            a String that possibly contains a variable
	 * @return the <code>line</code> where a variable starting and ending with #
	 *         are replaced by their values stored in the map
	 *         <code>variables</code>, if no variable is part of
	 *         <code>line</code> than line will returned unchanged
	 * @throws RuntimeException
	 *             if an end marker (#) does not, as it is required, follow the
	 *             start marker (#) of <code>line</code>
	 */
	protected String replaceVariables(String line) {
		if (line != null) {
			StringBuilder sb = new StringBuilder(line);
			int b = sb.indexOf("#");
			while (b >= 0) {
				int e = sb.indexOf("#", b + 1);
				if (e < 0) {
					throw new RuntimeException(
							"missing end marker in string \"" + line + "\"");
				}
				String name = sb.substring(b + 1, e);
				String value = getVariable(name);
				sb.replace(b, e + 1, value);
				b = sb.indexOf("#");
			}
			line = sb.toString();
		}
		return line;
	}

	/**
	 * calls the getCode(int indent)-method of the corresponding class, with
	 * zero as an indent, thereby the resulting String varies accordingly
	 * 
	 * @return the content of <code>this</code> as a String
	 */
	public String getCode() {
		return getCode(0);
	}

	/**
	 * 
	 * @return the <code>parent</code> of <code>this</code>
	 */
	public CodeList getParent() {
		return parent;
	}

	/**
	 * sets <code>newParent</code> as <code>parent</code> of this
	 * 
	 * @param newParent
	 *            a <code>CodeList</code>
	 */
	protected void setParent(CodeList newParent) {
		parent = newParent;
	}
}
