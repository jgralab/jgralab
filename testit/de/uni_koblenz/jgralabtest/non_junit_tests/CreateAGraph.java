package de.uni_koblenz.jgralabtest.non_junit_tests;

import java.io.DataOutputStream;

import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralabtest.schemas.minimal.MinimalGraph;
import de.uni_koblenz.jgralabtest.schemas.minimal.MinimalSchema;

public class CreateAGraph {
	public static void main(String[] args) throws GraphIOException {
		MinimalSchema schema = MinimalSchema.instance();
		MinimalGraph graph = schema.createMinimalGraph();
		GraphIO.saveGraphToStream(new DataOutputStream(System.out), graph, null);
	}
}
