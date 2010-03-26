package de.uni_koblenz.jgralabtest.trans;

import java.util.Map;

import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.trans.CommitFailedException;
import de.uni_koblenz.jgralabtest.schemas.record.Node;
import de.uni_koblenz.jgralabtest.schemas.record.RecordTestGraph;
import de.uni_koblenz.jgralabtest.schemas.record.RecordTestSchema;

public class TryNullValueForMapDomain {

	/**
	 * @param args
	 * @throws GraphIOException
	 * @throws CommitFailedException 
	 */
	public static void main(String[] args) throws GraphIOException, CommitFailedException {
		// create graph without transaction support
		RecordTestGraph graph = RecordTestSchema.instance()
				.createRecordTestGraphWithTransactionSupport();
		graph.newTransaction();
		Node node = graph.createNode();
		node.set_nodeMap(null);
		graph.createLink(node, node);
		
		Map<Integer, String> theMap;
		try {
			theMap = node.get_nodeMap();
			System.out.println(theMap);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		graph.commit();
		
		graph.newReadOnlyTransaction();
		theMap = node.get_nodeMap();
		System.out.println(theMap);
		graph.commit();
		
		
		System.out.println("Success!");

	}

}
