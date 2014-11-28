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

import java.io.FileNotFoundException;
import java.util.logging.Level;

import javax.xml.stream.XMLStreamException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.utilities.rsa2tg.Rsa2Tg;

/**
 * This class implements a call to Rsa2Tg as custom ant task. <br />
 * It has several parameters that can be set in ant. These parameters are
 * described here briefly.<br />
 * <ul>
 * <li><code>xmiFile</code> corresponds to the cli option -i . It defines the
 * location of the xmi file that has been exported by RSA.</li>
 * <li><code>schemaFile</code> corresponds to the cli option -o . It defines the
 * location of the schema file that will contain the converted schema in
 * tg-format.</li>
 * <li><code>useFromRole</code> corresponds to the cli option -f . If this is
 * set, role names will be used to create names for unnamed edge classes.</li>
 * <li><code>removeUnusedDomains</code> corresponds to the cli option -u . If it
 * is set, the output schema is pruned of all usused domains.</li>
 * <li><code>removeComments</code> corresponds to the cli option -c . If it is
 * set, all comments are removed.</li>
 * <li><code>useNavigability</code> corresponds to the cli option -n . If this
 * is set, the navigability information will be interpreted as reading
 * direction, overriding the actual reading direction.</li>
 * <li><code>keepEmptyPackages</code> corresponds to the cli option -k . If this
 * is set, the generated schema will contain all empty packages defined in the
 * xmi model. The default behavior is removing them.</li>
 * <li><code>schemaGraphFile</code> corresponds to the cli option -s . If this
 * is set, the schema graph of the schema is also stored in the given tg-file.</li>
 * <li><code>reportFile</code> corresponds to the cli option -r . If this is
 * set, an additional validation report is created and stored in the given
 * html-file.</li>
 * <li><code>dotFile</code> corresponds to the cli option -e . If this is set,
 * the schema graph is visualized using dot. The dot information is stored in
 * the given dot-file.</li>
 * </ul>
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class Rsa2TgTask extends Task {

	private final Rsa2Tg executeObject;
	private String xmiFilename;
	private String schemaFilename;

	public Rsa2TgTask() {
		executeObject = new Rsa2Tg();
	}

	public void setXmiFile(String value) {
		xmiFilename = value;
	}

	public void setSchemaFile(String value) {
		schemaFilename = value;
	}

	public void setUseFromRole(String value) {
		String v = value.toLowerCase();
		if (v.equals("true") || v.equals("yes")) {
			executeObject.setUseFromRole(true);
		} else if (!(v.equals("false") || v.equals("no"))) {
			throw new BuildException("Invalid value for boolean field: "
					+ value);
		}
	}

	public void setRemoveUnusedDomains(String value) {
		String v = value.toLowerCase();
		if (v.equals("true") || v.equals("yes")) {
			executeObject.setRemoveUnusedDomains(true);
		} else if (!(v.equals("false") || v.equals("no"))) {
			throw new BuildException("Invalid value for boolean field: "
					+ value);
		}
	}

	public void setUseNavigability(String value) {
		String v = value.toLowerCase();
		if (v.equals("true") || v.equals("yes")) {
			executeObject.setUseNavigability(true);
		} else if (!(v.equals("false") || v.equals("no"))) {
			throw new BuildException("Invalid value for boolean field: "
					+ value);
		}
	}

	public void setKeepEmptyPackages(String value) {
		String v = value.toLowerCase();
		if (v.equals("true") || v.equals("yes")) {
			executeObject.setKeepEmptyPackages(true);
		} else if (!(v.equals("false") || v.equals("no"))) {
			throw new BuildException("Invalid value for boolean field: "
					+ value);
		}
	}

	public void setRemoveComments(String value) {
		String v = value.toLowerCase();
		if (v.equals("true") || v.equals("yes")) {
			executeObject.setRemoveComments(true);
		} else if (!(v.equals("false") || v.equals("no"))) {
			throw new BuildException("Invalid value for boolean field: "
					+ value);
		}
	}

	public void setSchemaGraphFile(String value) {
		executeObject.setFilenameSchemaGraph(value);
	}

	public void setReportFile(String value) {
		executeObject.setFilenameValidation(value);
	}

	public void setDotFile(String value) {
		executeObject.setFilenameDot(value);
	}

	@Override
	public void execute() {
		JGraLab.setLogLevel(Level.WARNING);
		if (schemaFilename == null) {
			throw new BuildException("No schema file given.");
		}
		if (xmiFilename == null) {
			throw new BuildException("No xmi file given.");
		}
		executeObject.setFilenameSchema(schemaFilename);
		try {
			executeObject.process(xmiFilename);
		} catch (FileNotFoundException e) {
			throw new BuildException(e);
		} catch (XMLStreamException e) {
			throw new BuildException(e);
		}
	}
}
