package de.uni_koblenz.jgralab.utilities.ant;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.resources.FileResource;

import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.utilities.tgschema2java.TgSchema2Java;

public class TgSchema2JavaTask extends Task {

	private TgSchema2Java executeObject;
	private Set<String> schemaLocation;
	private String commitPath;

	public TgSchema2JavaTask() {
		executeObject = new TgSchema2Java();
		executeObject.setTypeSpecificMethodSupport(true);
		executeObject.setMethodsForSubclassesSupport(false);
		schemaLocation = new HashSet<String>();
	}

	public void setPath(String value) {
		commitPath = value;
	}

	public void setSchema(String value) {
		schemaLocation.add(value);
	}

	public void setWithoutTypes(String value) {
		String v = value.toLowerCase();
		if (v.equals("true") || v.equals("yes")) {
			executeObject.setTypeSpecificMethodSupport(false);
		} else if (!(v.equals("false") || v.equals("no"))) {
			throw new BuildException("Invalid value for boolean field: "
					+ value);
		}
	}

	public void setSubtypeFlag(String value) {
		String v = value.toLowerCase();
		if (v.equals("true") || v.equals("yes")) {
			executeObject.setMethodsForSubclassesSupport(true);
		} else if (!(v.equals("false") || v.equals("no"))) {
			throw new BuildException("Invalid value for boolean field: "
					+ value);
		}
	}

	public void setImplementationMode(String value) {
		String v = value.toLowerCase();
		if (v.equals("transaction")) {
			executeObject.setTransactionSupportOnly();
		} else if (v.equals("standard")) {
			executeObject.setStandardSupportOnly();
		} else if (!v.equals("all")) {
			throw new BuildException(
					"Invalid value for implementation mode: "
							+ value
							+ "\nOnly \"transaction\",\"standard\" and \"all\" are allowed.");
		}
	}
	
	public void addConfiguredFileset(FileSet files){
		@SuppressWarnings("unchecked")
		Iterator fileIterator = files.iterator();
		while(fileIterator.hasNext()){
			Object current = fileIterator.next();
			if(current instanceof FileResource){
				File currentFile = ((FileResource) current).getFile();
				schemaLocation.add(currentFile.getAbsolutePath());
			}
		}
	}

	@Override
	public void execute() {
		executeObject.setCommitPath(commitPath);
		try {
			for (String currentTG : schemaLocation) {
				executeObject.loadSchema(currentTG);
				executeObject.execute();
			}
		} catch (GraphIOException e) {
			throw new BuildException(e);
		}
	}

}
