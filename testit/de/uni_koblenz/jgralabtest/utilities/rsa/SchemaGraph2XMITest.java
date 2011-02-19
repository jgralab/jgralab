/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2011 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 * 
 * For bug reports, documentation and further information, visit
 * 
 *                         http://jgralab.uni-koblenz.de
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
package de.uni_koblenz.jgralabtest.utilities.rsa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.logging.Level;

import javax.xml.stream.XMLStreamException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.utilities.rsa.Rsa2Tg;
import de.uni_koblenz.jgralab.utilities.rsa.SchemaGraph2XMI;

public class SchemaGraph2XMITest {

	/**
	 * Position of the test schemas.
	 */
	private static String folder = "testit/de/uni_koblenz/jgralabtest/utilities/rsa/testschemas/";

	private Rsa2Tg r;

	{
		r = new Rsa2Tg();

		r.setUseFromRole(true);
		r.setRemoveUnusedDomains(true);
		r.setUseNavigability(true);
		r.setFilenameDot(null);
		r.setFilenameValidation(null);
		r.setFilenameSchemaGraph(null);
	}

	private static File folderWithTGs;

	private static File temp;

	@BeforeClass
	public static void setUp() {
		JGraLab.setLogLevel(Level.OFF);

		folderWithTGs = new File(folder);
		assert folderWithTGs.exists() : "The folder " + folderWithTGs
				+ " does not exist.";

		temp = new File(System.getProperty("java.io.tmpdir") + File.separator
				+ "SchemaGraph2XMI_TestOutput");
		if (!temp.exists()) {
			if (!temp.mkdir()) {
				fail(temp.getAbsoluteFile() + "could not be created.");
			}
		}
	}

	@AfterClass
	public static void tearDown() {
		if (!temp.delete()) {
			System.out.println(temp + " could not be deleted.");
		}
	}

	/**
	 * In this test case all EdgeClasses are created navigable from FROM to TO.
	 * 
	 * @throws GraphIOException
	 * @throws FileNotFoundException
	 * @throws XMLStreamException
	 */
	@Test
	public void testDefault() throws GraphIOException, FileNotFoundException,
			XMLStreamException {
		runTests(false);
	}

	/**
	 * In this test case all EdgeClasses are created bidirectional navigable.
	 * 
	 * @throws GraphIOException
	 * @throws FileNotFoundException
	 * @throws XMLStreamException
	 */
	@Test
	public void testBidirectional() throws GraphIOException,
			FileNotFoundException, XMLStreamException {
		runTests(true);
	}

	private void runTests(boolean createBidirectional) throws GraphIOException,
			FileNotFoundException, XMLStreamException {
		// check all schemas in folder
		for (String file : folderWithTGs.list()) {
			if (file.toLowerCase().endsWith(".tg")) {

				String originalTg = folderWithTGs.getAbsolutePath()
						+ File.separator + file;

				// create xmi
				String generatedXMI = temp.getAbsolutePath() + File.separator
						+ new File(file).getName() + ".xmi";
				if (createBidirectional) {
					SchemaGraph2XMI.main(new String[] { "-i", originalTg, "-o",
							generatedXMI, "-b" });
				} else {
					SchemaGraph2XMI.main(new String[] { "-i", originalTg, "-o",
							generatedXMI });
				}

				// create tg out of xmi
				String generatedTg = temp.getAbsolutePath() + File.separator
						+ new File(file).getName();
				r.setFilenameSchema(generatedTg);
				r.process(generatedXMI);

				// check generated tg
				compareTgs(originalTg, generatedTg);
			}
		}
	}

	/**
	 * Compares all nonempty lines of originalTg and generatedTg. The order of
	 * the lines is ignored. Attributes are sorted lexicographic.
	 * 
	 * @param originalTg
	 * @param generatedTg
	 * @param isReverted
	 */
	private void compareTgs(String originalTg, String generatedTg) {
		try {
			HashSet<String> originalTgContent = new HashSet<String>();
			HashSet<String> generatedTgContent = new HashSet<String>();

			// read lines of original Tg
			LineNumberReader reader = new LineNumberReader(new FileReader(
					originalTg));
			String line;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (!line.isEmpty()) {
					if (containesAttributes(line)) {
						line = sortAttributes(line);
					}
					originalTgContent.add(line);
				}
			}
			reader.close();

			// read lines of generated Tg
			reader = new LineNumberReader(new FileReader(generatedTg));
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (!line.isEmpty()) {
					if (containesAttributes(line)) {
						line = sortAttributes(line);
					}
					generatedTgContent.add(line);
				}
			}
			reader.close();

			// compare the content of both Tgs
			assertEquals(originalTg + " and " + generatedTg
					+ " have different length.", originalTgContent.size(),
					generatedTgContent.size());
			for (String s : originalTgContent) {
				assertTrue(s + " is not contained in the generated Tg "
						+ generatedTg, generatedTgContent.contains(s));
			}
			for (String s : generatedTgContent) {
				assertTrue(
						s
								+ " is contained in the generated Tg but not in the original Tg "
								+ originalTg, originalTgContent.contains(s));
			}

		} catch (FileNotFoundException e) {
			fail(e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			fail("An exception occurred while reading the file " + originalTg
					+ ":\n" + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Sorts the attributes lexicographically.
	 * 
	 * @param line
	 * @return
	 */
	private String sortAttributes(String line) {
		StringBuffer startOfLine = new StringBuffer();
		TreeSet<String> attributes = new TreeSet<String>();
		StringBuffer endOfLine = new StringBuffer();

		boolean isInAttributeDefinition = false;
		boolean isInDefaultValueDefinition = false;
		boolean areAttributesAlreadyFinished = false;

		StringBuffer currentAttribute = null;
		/*
		 * The default values of attributes are put into " ". If the first "
		 * occurs we know, that we now reach the definition of a default value.
		 * The problem is to differ between the " of the end of the default
		 * value definition and an \" occurring in the default value. For that
		 * reason prevChar was introduced. prevChar is set to \n because this
		 * cannot occur in a line.
		 */
		char prevChar = '\n';

		for (char c : line.toCharArray()) {
			if (areAttributesAlreadyFinished) {
				// the attributes are already read completely
				endOfLine.append(c);
			} else {
				// there are still some attributes to be recognized
				if (!isInAttributeDefinition) {
					// the first attribute is not reached yet
					startOfLine.append(c);
					if (c == '{') {
						// beginning of attribute definition is reached
						isInAttributeDefinition = true;
						currentAttribute = new StringBuffer();
					}
				} else {
					// the first attribute is or was reached
					if (!isInDefaultValueDefinition) {
						// we are currently not in the default value part
						if (c == ',') {
							// end of an attribute is reached
							attributes.add(currentAttribute.toString().trim());
							currentAttribute = new StringBuffer();
						} else if (c == '\"') {
							// the default value part is reached
							currentAttribute.append(c);
							isInDefaultValueDefinition = true;
						} else if (c == '}') {
							// the end of the last attribute is reached
							attributes.add(currentAttribute.toString().trim());
							endOfLine.append(c);
							areAttributesAlreadyFinished = true;
							isInAttributeDefinition = false;
						}
					} else {
						// the default value of an attribute is currently read
						currentAttribute.append(c);
						if (c == '\"' && prevChar != '\\') {
							// the end of the default value is reached
							isInDefaultValueDefinition = false;
							prevChar = '\n';
						} else if (c == '\\' && prevChar == '\\') {
							// the last and the current char is a \
							// (e.g. \\). The following char is not quoted.
							prevChar = '\n';
						} else {
							prevChar = c;
						}
					}
				}
			}
		}

		// put the line with the sorted attributes together again
		boolean isFirstAttribute = true;
		for (String attribute : attributes) {
			if (isFirstAttribute) {
				isFirstAttribute = false;
			} else {
				startOfLine.append(", ");
			}
			startOfLine.append(attribute);
		}
		startOfLine.append(endOfLine);

		return startOfLine.toString();
	}

	/**
	 * Checks if <code>line</code> contains attributes.
	 * 
	 * @param line
	 * @return
	 */
	private boolean containesAttributes(String line) {
		return !line.startsWith("Comment")
				&& line.contains("{")
				&& (!line.contains("[") || line.indexOf("{") < line
						.indexOf("["));
	}
}
