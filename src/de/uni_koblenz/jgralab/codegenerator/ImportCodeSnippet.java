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

import java.util.SortedSet;
import java.util.TreeSet;

/**
 * enables the creation of import-statements as part of the created code
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class ImportCodeSnippet extends CodeSnippet {

	/**
	 * stores the import-statements
	 */
	private SortedSet<String> imports;

	/**
	 * creates an empty <code>ImportCodeSnippet</code> by initializing its
	 * SortedSet and by creating a <code>CodeSnippet</code> with a new line as
	 * its only content
	 */
	public ImportCodeSnippet() {
		this(null);
	}

	/**
	 * creates an empty <code>ImportCodeSnippet</code> and a new
	 * <code>CodeSnippet</code> with a new line at its start
	 * 
	 * @param parent
	 *            is passed to the <code>CodeSnippet</code>
	 */
	public ImportCodeSnippet(CodeList parent) {
		super(parent, true);
		imports = new TreeSet<String>();
	}

	/**
	 * adds <code>addedLines</code> to <code>this</code> if one of the Strings
	 * of <code>addedLines</code> has already been put into <code>this</code>,
	 * it will not be added again
	 * 
	 * @param addedLines
	 *            will be added to <code>this</code>
	 */
	@Override
	public void add(String... addedLines) {
		if (addedLines != null) {
			for (String line : addedLines) {
				imports.add(line);
			}
		}
	}

	/**
	 * <code>this</code> is only called from within the codegenerator,
	 * exceptions are therefore handled optimistically <code>this</code> expects
	 * its Strings to contain a "."
	 * 
	 * @param indent
	 *            defines how much each import-statement is to be interposed
	 * @return the content of <code>this</code> as import-statements, the
	 *         import- statements are sorted, meaning that imports from the same
	 *         package form an import-statement-block with one statement per
	 *         line, import-statements from different packages are divided by an
	 *         empty line between the statements every line is interposed
	 *         according to <code>indent</code> additionally the
	 *         import-statements are sorted alphabetically if <code>this</code>
	 *         is empty, an empty String will be returned
	 */
	@Override
	public String getCode(int indent) {
		lines.clear();
		String lastPackageName = null;
		for (String imp : imports) {
			String packageName = imp.substring(0, imp.indexOf('.'));
			if (lastPackageName != null && !lastPackageName.equals(packageName)) {
				lines.add("");
			}
			lines.add("import " + imp + ";");
			lastPackageName = packageName;
		}
		return super.getCode(indent);
	}

	/**
	 * clears the CodeSnippet by discarding all saved Strings clears the parent,
	 * <code>this</code>.getParent() will afterwards throw a
	 * NullPointerException if called
	 */
	@Override
	public void clear() {
		super.clear();
		imports.clear();
	}

	/**
	 * @return the number of import statements, which are currently saved
	 */
	@Override
	public int size() {
		return imports.size();
	}
}
