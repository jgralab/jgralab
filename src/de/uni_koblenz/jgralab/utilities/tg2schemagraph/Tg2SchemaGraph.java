package de.uni_koblenz.jgralab.utilities.tg2schemagraph;

import java.io.InputStream;

import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.grumlschema.SchemaGraph;
import de.uni_koblenz.jgralab.schema.Schema;

public class Tg2SchemaGraph {

	public Tg2SchemaGraph() {
	}

	public SchemaGraph process(String filename) throws GraphIOException {
		Schema2SchemaGraph s2sg = new Schema2SchemaGraph();
		Schema schema = GraphIO.loadSchemaFromFile(filename);
		return s2sg.convert2SchemaGraph(schema);
	}

	public SchemaGraph process(InputStream in) throws GraphIOException {
		Schema2SchemaGraph s2sg = new Schema2SchemaGraph();
		Schema schema = GraphIO.loadSchemaFromStream(in);
		return s2sg.convert2SchemaGraph(schema);
	}
}