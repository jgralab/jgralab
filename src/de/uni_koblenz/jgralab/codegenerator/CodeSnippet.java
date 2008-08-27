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

import java.util.Vector;

public class CodeSnippet extends CodeBlock {
	protected Vector<String> lines;

	protected boolean wantsNewLine;
	
	public CodeSnippet() {
		this((CodeList)null);
	}
	
	public CodeSnippet(String... initialLines) {
		this(null, false, initialLines);
	}
	
	public CodeSnippet(boolean newLine, String... initialLines) {
		this(null, newLine, initialLines);
	}

	public CodeSnippet(CodeList parent, String... initialLines) {
		this(parent, false, initialLines);
	}

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

	public void setNewLine(boolean b) {
		wantsNewLine = b;
	}

	public void add(String... addedLines) {
		if (addedLines != null) {
			for (String line : addedLines) {
				lines.add(line);
			}
		}
	}

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
			if (line.length()>0) {
				buf.append(replaceVariables(line));
			}
			buf.append('\n');
		}
		return buf.toString();
	}
	
	@Override
	public void clear() {
		lines.clear();
	}

	@Override
	public int size() {
		return lines.size();
	}
}
