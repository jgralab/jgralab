/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2008 Institute for Software Technology
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
 * TODO add comment
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public abstract class CodeBlock {
	private Map<String, String> variables;

	private Stack<String> replacementStack;

	private CodeList parent;

	protected int additionalIndent;

	protected CodeBlock() {
		this(null);
	}

	protected CodeBlock(CodeList parent) {
		additionalIndent = 0;
		variables = new HashMap<String, String>();
		replacementStack = new Stack<String>();
		if (parent != null) {
			parent.add(this);
		}
	}

	public abstract String getCode(int indentLevel);

	public abstract void clear();

	public abstract int size();

	public void addVariables(Map<String, String> map) {
		for (String key : map.keySet()) {
			variables.put(key, map.get(key));
		}
	}

	public void setVariable(String name, String value) {
		variables.put(name, value);
	}

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

	public String getCode() {
		return getCode(0);
	}

	public CodeList getParent() {
		return parent;
	}

	protected void setParent(CodeList newParent) {
		parent = newParent;
	}
}
