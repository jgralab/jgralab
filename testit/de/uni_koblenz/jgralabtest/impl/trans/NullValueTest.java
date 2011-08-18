/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2011 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 * 
 * For bug reports, documentation and further information, visit
 * 
 *                         http://jgralab.uni-koblenz.de
 * 
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 3 of the License, or (at your
 * option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see <http://www.gnu.org/licenses>.
 * 
 * Additional permission under GNU GPL version 3 section 7
 * 
 * If you modify this Program, or any covered work, by linking or combining
 * it with Eclipse (or a modified version of that program or an Eclipse
 * plugin), containing parts covered by the terms of the Eclipse Public
 * License (EPL), the licensors of this Program grant you additional
 * permission to convey the resulting work.  Corresponding Source for a
 * non-source form of such a combination shall include the source code for
 * the parts of JGraLab used as well as that of the covered work.
 */
package de.uni_koblenz.jgralabtest.impl.trans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pcollections.ArrayPMap;
import org.pcollections.ArrayPSet;
import org.pcollections.ArrayPVector;
import org.pcollections.PMap;
import org.pcollections.PSet;
import org.pcollections.PVector;

import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.impl.ConsoleProgressFunction;
import de.uni_koblenz.jgralab.trans.CommitFailedException;
import de.uni_koblenz.jgralabtest.schemas.record.BooleanType;
import de.uni_koblenz.jgralabtest.schemas.record.Hugo;
import de.uni_koblenz.jgralabtest.schemas.record.Node;
import de.uni_koblenz.jgralabtest.schemas.record.RecordTestGraph;
import de.uni_koblenz.jgralabtest.schemas.record.RecordTestSchema;

public class NullValueTest {

	private RecordTestGraph graph;
	Node node1;
	Node node2;
	String filename = "testit/testdata/record_testgraph.tg";

	@Before
	public void setUp() throws CommitFailedException {
		graph = RecordTestSchema.instance()
				.createRecordTestGraphWithTransactionSupport();
		graph.newTransaction();
		createTestGraph(true);
		graph.commit();

	}

	private void createTestGraph(boolean transactionSupport) {
		node1 = graph.createNode();
		node1.set_nodeMap(null);
		node1.set_testEnum(null);
		node1.set_testList(null);
		node1.set_testSet(null);
		node1.set_testString(null);
		node1.set_testRecord(null);
		node2 = graph.createNode();
		PMap<Integer, String> map = ArrayPMap.empty();
		map = map.plus(1, "Hugo").plus(100, "Volker").plus(30, "Andi");
		node2.set_nodeMap(map);
		node2.set_testEnum(Hugo.A);
		PVector<String> list = ArrayPVector.empty();
		list = list.plus("Hugo").plus("Lalala").plus("Mahdi");
		node2.set_testList(list);
		PSet<Integer> set = ArrayPSet.empty();
		set = set.plus(1).plus(3).plus(8);
		node2.set_testSet(set);
		node2.set_testString("Hugo");
		node2.set_testRecord(new BooleanType(true, true));
		graph.createLink(node1, node2);
	}

	@After
	public void tearDown() {
		// new File(filename).delete();
	}

	@Test
	public void testReadNullAttributes() throws CommitFailedException {
		graph.newReadOnlyTransaction();
		assertNull(node1.get_nodeMap());
		assertNull(node1.get_testEnum());
		assertNull(node1.get_testList());
		assertNull(node1.get_testRecord());
		assertNull(node1.get_testSet());
		assertNull(node1.get_testString());
		graph.commit();
	}

	@Test
	public void testNonNullAttributes() throws CommitFailedException {
		graph.newTransaction();
		PMap<Integer, String> map = ArrayPMap.empty();
		map = map.plus(1, "Hugo").plus(100, "Volker").plus(30, "Andi");
		assertEquals(map, node2.get_nodeMap());

		PVector<String> list1 = ArrayPVector.empty();
		list1 = list1.plus("Hugo").plus("Lalala").plus("Mahdi");
		assertEquals(list1, node2.get_testList());

		PSet<Integer> set1 = ArrayPSet.empty();
		set1 = set1.plus(1).plus(3).plus(8);
		assertEquals(set1, node2.get_testSet());

		assertEquals("Hugo", node2.get_testString());

		BooleanType type = new BooleanType(true, true);
		assertEquals(type, node2.get_testRecord());
		graph.commit();
	}

	@Test
	public void writeTest() throws CommitFailedException, GraphIOException {
		graph.newReadOnlyTransaction();
		try {
			GraphIO.saveGraphToFile(filename, graph,
					new ConsoleProgressFunction());
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		graph.commit();
	}

	@Test
	public void readTest() throws CommitFailedException, GraphIOException {
		graph = RecordTestSchema.instance().createRecordTestGraph();
		createTestGraph(false);
		GraphIO.saveGraphToFile(filename, graph, new ConsoleProgressFunction());
		try {
			graph = (RecordTestGraph) GraphIO
					.loadGraphFromFileWithTransactionSupport(filename,
							new ConsoleProgressFunction());
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

	}

}
