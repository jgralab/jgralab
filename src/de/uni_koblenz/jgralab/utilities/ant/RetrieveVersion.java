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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

import org.apache.tools.ant.Task;

public class RetrieveVersion extends Task {
	public final String VERSION_FILENAME = "version.properties";
	private static final String MAJOR = "major";
	private static final String MINOR = "minor";
	private static final String MICRO = "micro";
	private static final String CODENAME = "codename";
	private static final String HEAD = "head";

	private final Properties prop = new Properties();
	protected String major;
	protected String minor;
	protected String micro;
	protected String head;
	protected String codename;

	private void checkExisting(File f) {
		if (!f.exists() || !f.canRead()) {
			throw new RuntimeException(f.getAbsolutePath() + " doesn't exist!");
		}
	}

	protected void getHeadRevision() {
		try {
			File headFile = new File(getProject().getBaseDir() + File.separator
					+ ".git" + File.separator + "HEAD");
			checkExisting(headFile);
			BufferedReader rb = null;
			String ref = null;
			try {
				rb = new BufferedReader(new FileReader(headFile));
				ref = rb.readLine();
				if (ref == null) {
					throw new RuntimeException("Unexpected EOF in " + headFile);
				}
				int idx = ref.indexOf(' ');
				ref = ref.substring(idx + 1).replace('/', File.separatorChar);
			} catch (IOException e) {
				throw new RuntimeException(e);
			} finally {
				if (rb != null) {
					rb.close();
				}
			}
			assert ref != null;

			File headRefFile = new File(getProject().getBaseDir()
					+ File.separator + ".git" + File.separator + ref);
			checkExisting(headRefFile);
			try {
				rb = new BufferedReader(new FileReader(headRefFile));
				head = rb.readLine();
			} catch (IOException e) {
				throw new RuntimeException(e);
			} finally {
				if (rb != null) {
					rb.close();
				}
			}
			if (head == null) {
				throw new RuntimeException(
						"Couldn't retrieve HEAD revision from " + headRefFile);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void execute() {
		readProperties();
		getProject().setNewProperty("version",
				major + "." + minor + "." + micro);
		getProject().setNewProperty("version.major", major);
		getProject().setNewProperty("version.minor", minor);
		getProject().setNewProperty("version.micro", micro);
		getProject().setNewProperty("version.head", head);
		getProject().setNewProperty("version.codename", codename);
	}

	protected void readProperties() {
		File propertyFile = new File(getProject().getBaseDir(),
				VERSION_FILENAME);
		try {
			prop.load(new FileInputStream(propertyFile));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		major = prop.getProperty(MAJOR, "0");
		minor = prop.getProperty(MINOR, "0");
		micro = prop.getProperty(MICRO, "0");
		head = prop.getProperty(HEAD, "<unknown>");
		codename = prop.getProperty(CODENAME, "?");
	}

	protected void saveProperties() {
		File propertyFile = new File(getProject().getBaseDir(),
				VERSION_FILENAME);
		prop.put(MAJOR, major);
		prop.put(MINOR, minor);
		prop.put(MICRO, micro);
		prop.put(HEAD, head);
		prop.put(CODENAME, codename);
		try {
			prop.store(new FileOutputStream(propertyFile),
					"JGraLab version information");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected void writePomFile() {
		try {
			// write a new POM file
			PrintWriter w = new PrintWriter(new OutputStreamWriter(
					new FileOutputStream("pom.xml"), "UTF-8"));
			w.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			w.println("<project>");
			w.println("\t<modelVersion>4.0.0</modelVersion>");
			w.println("\t<groupId>de.uni-koblenz.ist</groupId>");
			w.println("\t<artifactId>jgralab</artifactId>");
			w.println("\t<version>" + major + "." + minor + "." + micro
					+ "</version>");
			w.println("\t<name>JGraLab</name>");
			w.println("\t<description>");
			w.println("\t\tA high-performance TGraph library.");
			w.println("\t\tSee https://github.com/jgralab/jgralab");
			w.println("\t</description>");
			w.println("\t<repositories>");
			w.println("\t\t<repository>");
			w.println("\t\t\t<id>clojars</id>");
			w.println("\t\t\t<url>http://clojars.org/repo/</url>");
			w.println("\t\t</repository>");
			w.println("\t</repositories>");
			w.println("</project>");
			w.close();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}
