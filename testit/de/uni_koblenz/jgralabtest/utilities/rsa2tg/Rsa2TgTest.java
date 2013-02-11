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
package de.uni_koblenz.jgralabtest.utilities.rsa2tg;

import java.io.IOException;
import java.util.logging.Level;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.exception.GraphIOException;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.utilities.rsa2tg.Rsa2Tg;
import de.uni_koblenz.jgralabtest.utilities.tg2schemagraph.CompareSchemaWithSchemaGraph;

public class Rsa2TgTest {

	private static String folder = "testit/testschemas/rsa-xmi/";

	private Rsa2Tg r;

	{
		r = new Rsa2Tg();

		r.setUseFromRole(true);
		r.setRemoveUnusedDomains(true);
		r.setUseNavigability(true);
	}

	@BeforeClass
	public static void setUp() {
		JGraLab.setLogLevel(Level.OFF);
	}

	public void testASchema(String filename) throws GraphIOException,
			IOException, SAXException, ParserConfigurationException,
			XMLStreamException {
		// Loads the SchemaGraph
		java.io.File file = new java.io.File(folder + filename);
		String tgFilename = file.getPath() + ".rsa.tg";
		r.setFilenameDot(null);
		r.setFilenameValidation(null);
		r.setFilenameSchema(tgFilename);
		r.setFilenameSchemaGraph(null);
		r.process(file.getPath());

		// Converts the SchemaGraph to a Schema
		Schema schema = GraphIO.loadSchemaFromFile(tgFilename);

		// Compares the SchemaGraph with the created Schema
		new CompareSchemaWithSchemaGraph().compare(schema, r.getSchemaGraph());
	}

	@Test
	public void testgrUML_M3() throws GraphIOException, IOException,
			SAXException, ParserConfigurationException, XMLStreamException {
		testASchema("grUML-M3.xmi");
	}

	@Test
	public void testOsmSchema() throws GraphIOException, IOException,
			SAXException, ParserConfigurationException, XMLStreamException {
		testASchema("OsmSchema.xmi");
	}

	// @Test
	// public void testSoamig() throws GraphIOException, IOException,
	// SAXException, ParserConfigurationException, XMLStreamException {
	// testASchema("soamig.xmi");
	// }
}
