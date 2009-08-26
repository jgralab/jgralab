package de.uni_koblenz.jgralabtest.utilities.schemagraph2schema;

import java.io.IOException;
import java.util.logging.Level;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;

import org.junit.Test;
import org.xml.sax.SAXException;

import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.utilities.rsa2tg.Rsa2Tg;
import de.uni_koblenz.jgralab.utilities.tg2schemagraph.SchemaGraph2Schema;
import de.uni_koblenz.jgralabtest.utilities.tg2schemagraph.CompareSchemaWithSchemaGraph;

public class SchemaGraph2SchemaTest {

	@Test
	public void t() throws GraphIOException, IOException, SAXException,
			ParserConfigurationException, XMLStreamException {
		String folder = "testit/testschemas/rsa-xmi/";
		String[] xmiFiles = { "grUML-M3.xmi", "OsmSchema.xmi", "test.xmi" };

		JGraLab.setLogLevel(Level.OFF);
		Rsa2Tg r = new Rsa2Tg();
		r.setUseFromRole(true);
		r.setRemoveUnusedDomains(true);
		r.setUseNavigability(true);
		r.setSuppressOutput(true);

		for (String xmiFileName : xmiFiles) {

			// Loads the SchemaGraph
			System.out.println("Testing with: " + folder + xmiFileName);
			System.out.print("Loading XMI and creating SchemaGraph ... ");
			r.process(folder + xmiFileName);
			System.out.println("\tdone");

			// Converts the SchemaGraph to a Schema
			System.out.print("Converting SchemaGraph to Schema ...");
			Schema schema = new SchemaGraph2Schema()
					.convert(r.getSchemaGraph());
			System.out.println("\t\tdone");

			// Compares the SchemaGraph with the created Schema
			System.out.print("Testing ...");
			new CompareSchemaWithSchemaGraph().compare(schema, r
					.getSchemaGraph());
			System.out.println("\t\t\t\t\tdone");
			System.out.println();
		}
		System.out.println("Fini.");
	}
}
