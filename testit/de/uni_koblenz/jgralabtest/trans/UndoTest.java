package de.uni_koblenz.jgralabtest.trans;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.impl.ProgressFunctionImpl;
import de.uni_koblenz.jgralab.trans.CommitFailedException;
import de.uni_koblenz.jgralab.trans.InvalidSavepointException;
import de.uni_koblenz.jgralab.trans.Savepoint;
import de.uni_koblenz.jgralab.trans.Transaction;
import de.uni_koblenz.jgralabtest.schemas.record.BooleanType;
import de.uni_koblenz.jgralabtest.schemas.record.Hugo;
import de.uni_koblenz.jgralabtest.schemas.record.Node;
import de.uni_koblenz.jgralabtest.schemas.record.RecordTestGraph;
import de.uni_koblenz.jgralabtest.schemas.record.RecordTestSchema;

public class UndoTest {

	private RecordTestGraph graph;
	Node node1;
	Node node2;
	String filename = "record_testgraph.tg";

	@Before
	public void setUp() throws CommitFailedException {
		graph = RecordTestSchema.instance()
				.createRecordTestGraphWithTransactionSupport();
		graph.newTransaction();
		graph.commit();

	}

	@After
	public void tearDown() {
		// new File(filename).delete();
	}

	@Test
	public void testSimpleUndo() throws CommitFailedException, InvalidSavepointException {
		Transaction trans = graph.newTransaction();
		assertNull(graph.getFirstNode());
		Savepoint sp = trans.defineSavepoint();
		assertNull(graph.getFirstNode());
		
		node2 = graph.createNode();
		Map<Integer, String> map = graph.createMap(
				Integer.class, String.class);
		map.put(1, "Hugo");
		map.put(100, "Volker");
		node2.set_nodeMap(map);
		node2.set_testEnum(Hugo.A);
		List<String> list = graph.createList(String.class);
		list.add("Hugo");
		list.add("Lalala");
		node2.set_testList(list);
		Set<Integer> set = graph.createSet(Integer.class);
		set.add(1);
		set.add(3);
		set.add(8);
		node2.set_testSet(set);
		node2.set_testString("Hugo");
		node2.set_testRecord(graph.createBooleanType(true, true));
		
		assertTrue(graph.getFirstNode() != null);
		assertTrue(graph.getFirstNode() == node2);
		
		trans.restoreSavepoint(sp);
		assertNull(graph.getFirstNode());
		
	}


}
