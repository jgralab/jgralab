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

public class PreserveOtherMetaInfo extends Task {
	private Set<File> jarFiles;
	private String metaDir;
	private FileSet files;

	public PreserveOtherMetaInfo() {
		jarFiles = new HashSet<File>();
	}

	public void setMetaDir(String metaDir) {
		this.metaDir = metaDir;
	}

	public void addConfiguredFileset(FileSet files) {
		this.files = files;
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
		for (File currentFile : jarFiles) {
			// define and prepare target directory
			File metaSubDir = new File(metaDir + File.separator
					+ currentFile.getName());
			System.out.println(metaSubDir);
			metaSubDir.mkdirs();

			// set include pattern for unjar
			PatternSet include = new PatternSet();
			include.setIncludes("META-INF/**");

			// create and configure unjar task
			Expand unjar = new Expand();
			unjar.addFileset(files);
			unjar.setDest(metaSubDir);
			unjar.addPatternset(include);

			unjar.execute();
		}
	}
}
