/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2013 Institute for Software Technology
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
package de.uni_koblenz.jgralabtest.utilities.argoumo2tg;

import java.io.FileNotFoundException;
import java.util.logging.Level;

import javax.xml.stream.XMLStreamException;

import org.junit.BeforeClass;
import org.junit.Test;

import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.exception.GraphIOException;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.utilities.argouml2tg.ArgoUml2Tg;
import de.uni_koblenz.jgralabtest.utilities.tg2schemagraph.CompareSchemaWithSchemaGraph;

public class ArgoUML2TgTest {

	private static String folder = "testit/testschemas/argoUML-xmi/";

	private static String outputFolder = "testit/testdata/";

	private ArgoUml2Tg r;

	{
		r = new ArgoUml2Tg();

		r.setUseFromRole(true);
		r.setRemoveUnusedDomains(true);
		r.setUseNavigability(true);
	}

	@BeforeClass
	public static void setUp() {
		JGraLab.setLogLevel(Level.OFF);
	}

	private void testASchema(String filename) throws GraphIOException,
			FileNotFoundException, XMLStreamException {
		// Loads the SchemaGraph
		java.io.File file = new java.io.File(folder + filename);
		String tgFilename = outputFolder
				+ filename.substring(0, filename.lastIndexOf('.'))
				+ ".argoUML.tg";
		r.setFilenameDot(null);
		r.setFilenameValidation(null);
		r.setFilenameSchema(tgFilename);
		r.setFilenameSchemaGraph(outputFolder
				+ filename.substring(0, filename.lastIndexOf('.'))
				+ "2.argoUML.tg");
		r.process(file.getPath());

		// Converts the SchemaGraph to a Schema
		Schema schema = GraphIO.loadSchemaFromFile(tgFilename);

		// Compares the SchemaGraph with the created Schema
		new CompareSchemaWithSchemaGraph().compare(schema, r.getSchemaGraph());
	}

	@Test
	public void testAttriutes() throws FileNotFoundException, GraphIOException,
			XMLStreamException {
		testASchema("testAttributes.xmi");
	}

	@Test
	public void testAttributesDefaultValues() throws FileNotFoundException,
			GraphIOException, XMLStreamException {
		testASchema("testAttributesDefaultValues.xmi");
	}

	@Test
	public void testComments() throws FileNotFoundException, GraphIOException,
			XMLStreamException {
		testASchema("testComments.xmi");
	}

	@Test
	public void testConstraints() throws FileNotFoundException,
			GraphIOException, XMLStreamException {
		testASchema("testConstraints.xmi");
	}

	@Test
	public void testEdgeClassAbstract() throws FileNotFoundException,
			GraphIOException, XMLStreamException {
		testASchema("testEdgeClassAbstract.xmi");
	}

	@Test
	public void testEdgeClassAggregation() throws FileNotFoundException,
			GraphIOException, XMLStreamException {
		testASchema("testEdgeClassAggregation.xmi");
	}

	@Test
	public void testEdgeClassCyclic() throws FileNotFoundException,
			GraphIOException, XMLStreamException {
		testASchema("testEdgeClassCyclic.xmi");
	}

	@Test
	public void testEdgeClassDirection() throws FileNotFoundException,
			GraphIOException, XMLStreamException {
		testASchema("testEdgeClassDirection.xmi");
	}

	@Test
	public void testEdgeClassGeneralization() throws FileNotFoundException,
			GraphIOException, XMLStreamException {
		testASchema("testEdgeClassGeneralization.xmi");
	}

	@Test
	public void testEdgeClassMultiplicities() throws FileNotFoundException,
			GraphIOException, XMLStreamException {
		testASchema("testEdgeClassMultiplicities.xmi");
	}

	@Test
	public void testEdgeClassRoles() throws FileNotFoundException,
			GraphIOException, XMLStreamException {
		testASchema("testEdgeClassRoles.xmi");
	}

	@Test
	public void testInheritance() throws FileNotFoundException,
			GraphIOException, XMLStreamException {
		testASchema("testInheritance.xmi");
	}

	@Test
	public void testGraphClass() throws FileNotFoundException,
			GraphIOException, XMLStreamException {
		testASchema("testGraphClass.xmi");
	}

	@Test
	public void testPackages() throws FileNotFoundException, GraphIOException,
			XMLStreamException {
		testASchema("testPackages.xmi");
	}

	@Test
	public void testVertexClass() throws FileNotFoundException,
			GraphIOException, XMLStreamException {
		testASchema("testVertexClass.xmi");
	}

	@Test(expected = RuntimeException.class)
	public void testAttributeTest() throws FileNotFoundException,
			GraphIOException, XMLStreamException {
		testASchema("attributeTest.xmi");
	}
}
