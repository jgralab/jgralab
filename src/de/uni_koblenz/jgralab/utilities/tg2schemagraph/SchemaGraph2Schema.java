package de.uni_koblenz.jgralab.utilities.tg2schemagraph;

import de.uni_koblenz.jgralab.WorkInProgress;
import de.uni_koblenz.jgralab.grumlschema.SchemaGraph;
import de.uni_koblenz.jgralab.grumlschema.structure.Schema;
import de.uni_koblenz.jgralab.schema.impl.SchemaImpl;

/**
 * TODO: - Code - Comments - Test
 * 
 * @author mmce, Eckhard Großmann
 */
@WorkInProgress(responsibleDevelopers = "mmce, Eckhard Großmann")
public class SchemaGraph2Schema {

	private de.uni_koblenz.jgralab.schema.Schema schema;

	public SchemaGraph2Schema() {
	}

	public de.uni_koblenz.jgralab.schema.Schema convert(SchemaGraph schemaGraph) {

		createSchema(schemaGraph);

		return null;
	}

	private void createSchema(SchemaGraph schemaGraph) {

		Schema gSchema = schemaGraph.getFirstSchema();

		String name = gSchema.getName();
		String packagePrefix = gSchema.getPackagePrefix();

		schema = new SchemaImpl(name, packagePrefix);
	}

}
