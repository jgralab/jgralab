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

/**
 * This class implements a call to TgSchema2Java as custom ant task.<br />
 * It has several parameters that can be set in ant. These parameters are
 * described here briefly.<br />
 * <ul>
 * <li><code>schema</code> corresponds to the cli option -s . The filename of
 * the tg file, containing the schema is set using this parameter. If more than
 * one schema should be generated, a nested fileset can be used instead. If this
 * parameter is set and a nested fileset is present, both will be taken.</li>
 * <li><code>path</code> the source location of Java's base package. The schema
 * will be generated into this path. If multiple schema files have to be
 * generated into different base package locations (e.g. "nomal" schemas to
 * "src" and test schemas to "testit"), the task must be called multiple times,
 * once for each base package location.</li>
 * <li><code>implementationMode</code> takes a comma separated list of possible
 * implementation modes. If unset, all implementations will be generated.
 * Possible implementation modes are currently "standard", "transaction" and
 * "savemem".</li>
 * <li><code>subtypeFlag</code> corresponds to the cli option -f . If set,
 * separate methods with subtype flag will be created.</li>
 * <li><code>withoutTypes</code> corresponds to the cli option -w . If set, no
 * type specific methods are created in the classes.</li>
 *</ul>
 * 
 * @author ist@uni-koblenz.de
 * 
 */
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

		String[] values = value.toLowerCase().split(",");
		for (String v : values) {
			if (v.equals("transaction")) {
				executeObject.setTransactionSupport(true);
			} else if (v.equals("standard")) {
				executeObject.setStandardSupport(true);
			} else if (v.equals("savemem")) {
				executeObject.setSavememSupport(true);
			} else {
				throw new BuildException(
						"Invalid value for implementation mode: "
								+ v
								+ "\nOnly \"transaction\",\"standard\" and \"savemem\" are supported.");
			}
		}
	}

	public void addConfiguredFileset(FileSet files) {
		@SuppressWarnings("unchecked")
		Iterator fileIterator = files.iterator();
		while (fileIterator.hasNext()) {
			Object current = fileIterator.next();
			if (current instanceof FileResource) {
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
