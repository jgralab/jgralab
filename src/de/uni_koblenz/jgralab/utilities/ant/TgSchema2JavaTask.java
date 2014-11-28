/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2014 Institute for Software Technology
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

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.resources.FileResource;

import de.uni_koblenz.jgralab.exception.GraphIOException;
import de.uni_koblenz.jgralab.utilities.tgschema2java.TgSchema2Java;

/**
 * This class implements a call to TgSchema2Java as custom ant task.<br />
 * It has several parameters that can be set in ant. These parameters are
 * described here briefly.<br />
 * <ul>
 * <li><code>schemaFile</code> corresponds to the cli option -s . The filename
 * of the tg file, containing the schema is set using this parameter. If more
 * than one schema should be generated, a nested fileset can be used instead. If
 * this parameter is set and a nested fileset is present, both will be taken.</li>
 * <li><code>sourcePath</code> the source location of Java's base package. The
 * schema will be generated into this path. If multiple schema files have to be
 * generated into different base package locations (e.g. "nomal" schemas to
 * "src" and test schemas to "testit"), the task must be called multiple times,
 * once for each base package location.</li>
 * <li><code>implementationMode</code> takes a comma separated list of possible
 * implementation modes. If unset, all implementations will be generated.
 * Possible implementation modes are currently "standard", "transaction" and
 * "db".</li>
 * <li><code>subtypeFlag</code> corresponds to the cli option -f . If set,
 * separate methods with subtype flag will be created.</li>
 * <li><code>withoutTypes</code> corresponds to the cli option -w . If set, no
 * type specific methods are created in the classes.</li>
 * </ul>
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
		schemaLocation = new HashSet<>();
	}

	public void setSourcePath(String value) {
		commitPath = value;
	}

	public void setSchemaFile(String value) {
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

	public void addConfiguredFileset(FileSet files) {
		Iterator<?> fileIterator = files.iterator();
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
		if (schemaLocation == null) {
			throw new BuildException("Schema file not set.");
		}
		if (commitPath == null) {
			throw new BuildException("Source path not set.");
		}
		executeObject.setCommitPath(commitPath);
		try {
			for (String currentTG : schemaLocation) {
				executeObject.loadSchema(currentTG);
				DeleteGeneratedSchemaTask.deleteGeneratedSchema(commitPath,
						executeObject.getSchema());
				executeObject.execute();
			}
		} catch (GraphIOException e) {
			e.printStackTrace();
			throw new BuildException(e);
		}
	}

}
