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
