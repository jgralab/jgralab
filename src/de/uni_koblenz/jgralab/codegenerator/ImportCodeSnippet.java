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

import java.util.SortedSet;
import java.util.TreeSet;

/**
 * TODO add comment
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class ImportCodeSnippet extends CodeSnippet {

	private SortedSet<String> imports;

	public ImportCodeSnippet() {
		this(null);
	}

	public ImportCodeSnippet(CodeList parent) {
		super(parent, true);
		imports = new TreeSet<String>();
	}

	@Override
	public void add(String... addedLines) {
		if (addedLines != null) {
			for (String line : addedLines) {
				imports.add(line);
			}
		}
	}

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

	@Override
	public void clear() {
		super.clear();
		imports.clear();
	}

	@Override
	public int size() {
		return imports.size();
	}
}
