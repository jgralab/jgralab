package de.uni_koblenz.jgralabtest.utilities.tg2schemagraph;

import org.junit.Assert;
import org.junit.Test;

import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.grumlschema.SchemaGraph;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.utilities.tg2schemagraph.Schema2SchemaGraph;

public class TG2SchemaGraphTest {

	public void testSchema2SchemaGraph(String tgFile) {

		// Loads the Schema
		try {
			System.out.println("Testing with: " + tgFile);
			System.out.print("Loading TG and creating Schema ... ");
			Schema schema = GraphIO.loadSchemaFromFile(tgFile);
			System.out.println("\tdone");

			// Converts the Schema to a SchemaGraph
			System.out.print("Converting Schema to SchemaGraph ...");
			SchemaGraph schemaGraph = new Schema2SchemaGraph()
					.convert2SchemaGraph(schema);
			System.out.println("\tdone");

			// Compares the Schema with the created SchemaGraph
			System.out.print("Testing ...");
			new CompareSchemaWithSchemaGraph().compare(schema, schemaGraph);
			System.out.println("\t\t\t\tdone");
			System.out.println("Succesful!");
		} catch (GraphIOException e) {
			System.err.print("An ERROR occurred!");
			System.out.println("");
			Assert.fail(e.toString());
		} finally {
			System.out.println("");
		}

	}

	@Test
	public void testOsmSchema() {
		testSchema2SchemaGraph("testit/de/uni_koblenz/jgralabtest/utilities/tg2schemagraph/OsmSchema.rsa.tg");

	}

	@Test
	public void testGrumlSchema() {
		testSchema2SchemaGraph("testit/de/uni_koblenz/jgralabtest/utilities/tg2schemagraph/GrumlSchema.rsa.tg");
	}

	@Test
	public void testTest() {
		testSchema2SchemaGraph("testit/de/uni_koblenz/jgralabtest/utilities/tg2schemagraph/Test.rsa.tg");
	}

	@Test
	public void testTestSchema0() {
		testSchema2SchemaGraph("testit/de/uni_koblenz/jgralabtest/utilities/tg2schemagraph/testschema0.tg");
	}

	@Test
	public void testTestSchema1() {
		testSchema2SchemaGraph("testit/de/uni_koblenz/jgralabtest/utilities/tg2schemagraph/testschema1.tg");
	}

	@Test
	public void testCityMapSchema() {
		testSchema2SchemaGraph("testit/testschemas/citymapschema.tg");
	}

	@Test
	public void testConstraintSchema() {
		testSchema2SchemaGraph("testit/testschemas/ConstrainedSchema.tg");
	}

	@Test
	public void testMinimalSchema() {
		testSchema2SchemaGraph("testit/testschemas/MinimalSchema.tg");
	}

	@Test
	public void testVertexTestSchema() {
		testSchema2SchemaGraph("testit/testschemas/VertexTestSchema.tg");
	}

	@Test
	public void testMotorWayMapSchema() {
		testSchema2SchemaGraph("testit/testschemas/motorwaymapschema.tg");
	}
}