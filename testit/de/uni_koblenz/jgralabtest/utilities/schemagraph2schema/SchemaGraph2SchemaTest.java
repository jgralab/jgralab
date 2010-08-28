package de.uni_koblenz.jgralabtest.utilities.schemagraph2schema;

import java.io.IOException;
import java.util.logging.Level;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.utilities.rsa.Rsa2Tg;
import de.uni_koblenz.jgralab.utilities.tg2schemagraph.SchemaGraph2Schema;
import de.uni_koblenz.jgralabtest.utilities.tg2schemagraph.CompareSchemaWithSchemaGraph;

public class SchemaGraph2SchemaTest {

	private static String folder = "testit/testschemas/rsa-xmi/";

	private Rsa2Tg r;

	{
		r = new Rsa2Tg();

		r.setUseFromRole(true);
		r.setRemoveUnusedDomains(true);
		r.setUseNavigability(true);
		r.setSuppressOutput(true);
	}

	@BeforeClass
	public static void setUp() {
		JGraLab.setLogLevel(Level.OFF);
	}

	@AfterClass
	public static void tearDown() {
		System.out.println("fini.");
	}

	public void test(String filename) throws GraphIOException, IOException,
			SAXException, ParserConfigurationException, XMLStreamException {
		try {
			r.process(filename);

			// Loads the SchemaGraph
			System.out.println("Testing with: " + folder + filename);
			System.out.print("Loading XMI and creating SchemaGraph ... ");
			r.setFilenameDot(null);
			r.setFilenameValidation(null);
			r.setFilenameSchema(null);
			r.setFilenameSchemaGraph(null);

			System.out.println("\tdone");

			// Converts the SchemaGraph to a Schema
			System.out.print("Converting SchemaGraph to Schema ...");
			Schema schema = new SchemaGraph2Schema()
					.convert(r.getSchemaGraph());
			System.out.println("\t\tdone");

			// Compares the SchemaGraph with the created Schema
			System.out.print("Testing ...");
			new CompareSchemaWithSchemaGraph().compare(schema,
					r.getSchemaGraph());
			System.out.println("\t\t\t\t\tdone");

		} finally {
			System.out.println("\n");
		}
	}

	@Test
	public void testgrUML_M3() throws GraphIOException, IOException,
			SAXException, ParserConfigurationException, XMLStreamException {
		test(folder + "grUML-M3.xmi");
	}

	@Test
	public void testOsmSchema() throws GraphIOException, IOException,
			SAXException, ParserConfigurationException, XMLStreamException {
		test(folder + "OsmSchema.xmi");
	}
}
