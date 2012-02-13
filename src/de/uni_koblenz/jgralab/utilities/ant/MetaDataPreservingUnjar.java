/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2012 Institute for Software Technology
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
package de.uni_koblenz.jgralab.utilities.ant;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Expand;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.PatternSet;
import org.apache.tools.ant.types.resources.FileResource;

public class MetaDataPreservingUnjar extends Task {
	private Set<File> jarFiles;
	private String metaDir;
	private File dest;

	// private Expand unjar;

	public MetaDataPreservingUnjar() {
		jarFiles = new HashSet<File>();
		metaDir = "META-INF";
		// unjar = new Expand();
	}

	public void setDest(File dest) {
		this.dest = dest;
	}

	public void addConfiguredFileset(FileSet files) {
		Iterator<?> fileIterator = files.iterator();
		while (fileIterator.hasNext()) {
			Object current = fileIterator.next();
			if (current instanceof FileResource) {
				File currentFile = ((FileResource) current).getFile();
				jarFiles.add(currentFile);
			}
		}
	}

	@Override
	public void execute() {
		// set exclude pattern
		PatternSet exclude = new PatternSet();
		exclude.setExcludes(metaDir + "/**");
		for (File currentFile : jarFiles) {
			// perform a normal unjar without meta info
			Expand unjarClasses = new Expand();
			unjarClasses.setSrc(currentFile);
			unjarClasses.setDest(dest);
			unjarClasses.addPatternset(exclude);
			unjarClasses.execute();

			// extract meta info to meta subdir
			// define and prepare target directory
			File metaSubDir = new File(dest.getAbsolutePath() + File.separator
					+ metaDir + File.separator + currentFile.getName());
			// System.out.println(metaSubDir);
			metaSubDir.mkdirs();

			// set include pattern for unjar
			PatternSet include = new PatternSet();
			include.setIncludes(metaDir + "/**");

			// create and configure unjar task
			Expand unjar = new Expand();
			unjar.setSrc(currentFile);
			unjar.setDest(metaSubDir);
			unjar.addPatternset(include);
			unjar.execute();
		}
	}
}
