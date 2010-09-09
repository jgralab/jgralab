/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2010 Institute for Software Technology
 *               University of Koblenz-Landau, Germany
 * 
 *               ist@uni-koblenz.de
 * 
 * Please report bugs to http://serres.uni-koblenz.de/bugzilla
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package de.uni_koblenz.jgralabtest.trans;

import java.util.HashMap;

import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.impl.ConsoleProgressFunction;
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

		GraphIO.saveGraphToFile(filename, graph, new ConsoleProgressFunction());

		// load graph with transaction support

		RecordTestSchema.instance()
				.loadRecordTestGraphWithTransactionSupport(filename,
						new ConsoleProgressFunction());

		System.out.println("Success!");

	}

}
