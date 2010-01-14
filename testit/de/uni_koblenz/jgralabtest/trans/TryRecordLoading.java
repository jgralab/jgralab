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
	 */
	public static void main(String[] args) {
		// create graph without transaction support
		RecordTestGraph graph = RecordTestSchema.instance()
				.createRecordTestGraph();
		Node node = graph.createNode();
		node.set_nodeMap(new HashMap<Integer, String>());
		node.set_testRecord(graph.createBooleanType(true, false));
		graph.createLink(node, node);

		// save graph to file
		String filename = "./testit/testgraphs/record.tg";
		try {
			GraphIO
					.saveGraphToFile(filename, graph,
							new ProgressFunctionImpl());
		} catch (GraphIOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// load graph with transaction support
		try {
			RecordTestGraph graphTS = RecordTestSchema.instance()
					.loadRecordTestGraphWithTransactionSupport(filename,
							new ProgressFunctionImpl());
		} catch (GraphIOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("Success!");

	}

}
