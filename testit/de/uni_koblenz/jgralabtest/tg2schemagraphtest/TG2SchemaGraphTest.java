package de.uni_koblenz.jgralabtest.tg2schemagraphtest;

import org.junit.Test;

import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.grumlschema.SchemaGraph;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.utilities.tg2schemagraph.Schema2SchemaGraph;

public class TG2SchemaGraphTest {

	@Test
	public void testSchema2SchemaGraph() throws GraphIOException {

		String[] tgFiles = {
				"testit/de/uni_koblenz/jgralabtest/tg2schemagraphtest/OsmSchema.rsa.tg",
				"testit/de/uni_koblenz/jgralabtest/tg2schemagraphtest/GrumlSchema.rsa.tg",
				"testit/de/uni_koblenz/jgralabtest/tg2schemagraphtest/Test.rsa.tg",
				"testit/testschemas/citymapschema.tg",
				"testit/testschemas/ConstrainedSchema.tg",
				"testit/testschemas/MinimalSchema.tg",
				"testit/testschemas/VertexTestSchema.tg",
				"testit/de/uni_koblenz/jgralabtest/tg2schemagraphtest/testschema0.tg",
				"testit/de/uni_koblenz/jgralabtest/tg2schemagraphtest/testschema1.tg" };
		for (String filename : tgFiles) {
			System.out.println("Testing with: " + filename);
			System.out.print("Loading TG and creating Schema ... ");
			Schema schema = GraphIO.loadSchemaFromFile(filename);
			System.out.println("\tdone");

			System.out.print("Converting Schema to SchemaGraph ...");
			SchemaGraph schemaGraph = new Schema2SchemaGraph()
					.convert2SchemaGraph(schema);
			System.out.println("\tdone");

			System.out.print("Testing ...");
			new CompareSchemaWithSchemaGraph().compare(schema, schemaGraph);
			System.out.println("\t\t\t\tdone");
			System.out.println();
		}
	}
}
