package de.uni_koblenz.jgralabtest.trans;

import java.util.HashMap;

import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.impl.ProgressFunctionImpl;
import de.uni_koblenz.jgralabtest.schemas.record.Node;
import de.uni_koblenz.jgralabtest.schemas.record.RecordTestGraph;
import de.uni_koblenz.jgralabtest.schemas.record.RecordTestSchema;

public class TryRecordLoading {

	/**
	 * @param args
	 * @throws GraphIOException
	 */
	public static void main(String[] args) throws GraphIOException {
		// create graph without transaction support
		RecordTestGraph graph = RecordTestSchema.instance()
				.createRecordTestGraph();
		Node node = graph.createNode();
		node.set_nodeMap(new HashMap<Integer, String>());
		node.set_testRecord(graph.createBooleanType(true, false));
		graph.createLink(node, node);

		// save graph to file
		String filename = "./testit/testgraphs/record.tg";

		GraphIO.saveGraphToFile(filename, graph, new ProgressFunctionImpl());

		// load graph with transaction support

		RecordTestGraph graphTS = RecordTestSchema.instance()
				.loadRecordTestGraphWithTransactionSupport(filename,
						new ProgressFunctionImpl());

		System.out.println("Success!");

	}

}
