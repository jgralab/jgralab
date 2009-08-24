package de.uni_koblenz.jgralabtest.utilities.sg2tg;

import java.io.IOException;
import java.util.logging.Level;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.xml.sax.SAXException;

import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.utilities.rsa2tg.Rsa2Tg;
import de.uni_koblenz.jgralabtest.utilities.tg2schemagraph.CompareSchemaWithSchemaGraph;

public class Rsa2TgTest {

	@Test
	public void t() throws GraphIOException, IOException, SAXException,
			ParserConfigurationException {
		String folder = "testit/testschemas/rsa-xmi/";
		String[] xmiFiles = { "grUML-M3.xmi", "OsmSchema.xmi", "test.xmi",
				"java-schema.xmi" };
		// String[] tgFiles = { "GrumlSchema.rsa.tg", "OsmSchema.rsa.tg",
		// "Test.rsa.tg" };

		JGraLab.setLogLevel(Level.OFF);
		Rsa2Tg r = new Rsa2Tg();
		r.setUseFromRole(true);
		r.setRemoveUnusedDomains(true);
		r.setUseNavigability(true);

		for (int i = 0; i < xmiFiles.length; i++) {

			// Loads the SchemaGraph
			System.out.println("Testing with: " + folder + xmiFiles[i]);
			System.out
					.print("Loading XMI, creating SchemaGraph and creating TG-file... ");
			r.process(folder + xmiFiles[i]);
			System.out.println("\tdone");

			de.uni_koblenz.jgralab.grumlschema.structure.Schema gSchema = r
					.getSchemaGraph().getFirstSchema();

			// Converts the SchemaGraph to a Schema
			System.out.print("Loading Schema from File ...");
			Schema schema = GraphIO.loadSchemaFromFile(folder
					+ gSchema.getName() + ".rsa.tg");
			System.out.println("\t\t\t\t\tdone");

			// Compares the SchemaGraph with the created Schema
			System.out.print("Testing ...");
			new CompareSchemaWithSchemaGraph().compare(schema, r
					.getSchemaGraph());
			System.out.println("\t\t\t\t\t\t\tdone");
			System.out.println();
		}
		System.out.println("Fini.");
	}
}