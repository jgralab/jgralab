package de.uni_koblenz.jgralabtest.tg2schemagraphtest;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

import org.junit.Test;

import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.grumlschema.SchemaGraph;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.utilities.tg2schemagraph.Schema2SchemaGraph;

public class TG2SchemaGraphTest2 {

	@Test
	public void testSchema2SchemaGraph() {
		try {
			System.out.print("Loading TG and creating Schema ... ");
			Schema schema = GraphIO.loadSchemaFromFile("OsmSchema.rsa.tg");
			System.out.println("\tdone");

			System.out.print("Converting Schema to SchemaGraph ...");
			SchemaGraph schemaGraph = new Schema2SchemaGraph()
					.convert2SchemaGraph(schema);
			System.out.println("\tdone");

			System.out.print("Testing ...");
			assertEquals(new CompareSchemaWithSchemaGraph().compare(schema,
					schemaGraph), true);
			System.out.println("\t\t\tdone");

		} catch (GraphIOException ex) {
			System.out.println(ex);
			fail();
		}
	}

}
