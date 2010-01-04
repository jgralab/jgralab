/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2007 Institute for Software Technology
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

package de.uni_koblenz.jgralabtest.greql2;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.junit.Before;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueCollection;
import de.uni_koblenz.jgralab.greql2.optimizer.Optimizer;
import de.uni_koblenz.jgralab.greql2.parser.ManualGreqlParser;
import de.uni_koblenz.jgralab.greql2.schema.Greql2;
import de.uni_koblenz.jgralabtest.schemas.minimal.MinimalGraph;
import de.uni_koblenz.jgralabtest.schemas.minimal.MinimalSchema;
import de.uni_koblenz.jgralabtest.schemas.minimal.Node;

public class GenericTests {

	protected Map<String, JValue> boundVariables;

	Graph graph = null;
	Graph cyclicGraph = null;
	Graph tree = null;

	protected void printTestFunctionHeader(String functionName) {
		System.out
				.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		System.out.println("START     " + functionName);
		System.out
				.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
	}

	protected void printTestFunctionFooter(String functionName) {
		System.out
				.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
		System.out.println("END       " + functionName);
		System.out
				.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
	}

	@Before
	public void setUp() throws Exception {
		boundVariables = new HashMap<String, JValue>();
		boundVariables.put("nix", new JValue(133));
		boundVariables.put("FOO", new JValue("Currywurst"));
	}

	protected Graph getTestGraph() throws Exception {
		if (graph == null) {
			graph = createTestGraph();
		}
		return graph;
	}

	protected Graph getTestTree() throws Exception {
		if (tree == null) {
			tree = createTestTree();
		}
		return tree;
	}

	protected Graph getCyclicTestGraph() throws Exception {
		if (cyclicGraph == null) {
			cyclicGraph = createCyclicTestGraph();
		}
		return cyclicGraph;
	}

	protected Graph createTestGraph() throws Exception {
		String query = "from i:c report i end where d:=\"nada\", c:=b, b:=a, a:=\"Mensaessen\"";
		return ManualGreqlParser.parse(query);
	}

	protected Graph createCyclicTestGraph() throws Exception {
		MinimalSchema s = MinimalSchema.instance();
		MinimalGraph g = s.createMinimalGraph(10, 10);
		Node[] v = new Node[10];
		for (int i = 0; i < 10; i++) {
			v[i] = g.createNode();
		}
		for (int i = 0; i < 10; i++) {
			g.createLink(v[i], v[(i + 1) % 10]);
		}
		return g;
	}

	protected Graph createTestTree() throws Exception {
		// create a binary tree where v[0] is the root
		MinimalSchema s = MinimalSchema.instance();
		MinimalGraph g = s.createMinimalGraph(10, 10);
		Node[] v = new Node[15];
		for (int i = 0; i < 15; i++) {
			v[i] = g.createNode();
		}
		for (int i = 0; i < (v.length - 1) / 2; i++) {
			g.createLink(v[i], v[i * 2 + 1]);
			g.createLink(v[i], v[i * 2 + 2]);
			System.out.println("[" + i + ", " + (i * 2 + 1) + ", "
					+ (i * 2 + 2) + "]");
		}
		return g;
	}

	protected JValue evalTestQuery(String functionName, String query)
			throws Exception {
		return evalTestQuery(functionName, query, null, getTestGraph());
	}

	protected JValue evalTestQuery(String functionName, String query,
			Optimizer optimizer) throws Exception {
		return evalTestQuery(functionName, query, optimizer, getTestGraph());
	}

	protected JValue evalTestQuery(String functionName, String query,
			Graph datagraph) throws Exception {
		return evalTestQuery(functionName, query, null, datagraph);
	}

	protected JValue evalTestQuery(String functionName, String query,
			Optimizer optimizer, Graph datagraph) throws Exception {
		printTestFunctionHeader(functionName);
		GreqlEvaluator eval = new GreqlEvaluator(query, datagraph,
				boundVariables);
		eval.setUseSavedOptimizedSyntaxGraph(false);

		if (optimizer != null) {
			eval.setOptimize(true);
			eval.setOptimizer(optimizer);
		} else {
			eval.setOptimize(false);
		}

		// when optimizing turn on logging, too.
		eval.startEvaluation(eval.isOptimize());
		printTestFunctionFooter(functionName);

		JValue result = eval.getEvaluationResult();
		eval.printEvaluationTimes();
		return result;
	}

	protected JValue getNthValue(JValueCollection col, int n) {
		Iterator<JValue> iter = col.iterator();
		int i = 0;
		while (iter.hasNext()) {
			JValue value = iter.next();
			if (i == n) {
				return value;
			}
			i++;
		}
		return null;
	}

	protected void printResult(JValue result) throws Exception {
		System.out.println("Result is: " + result);
		if (result.isCollection()) {
			System.out.println("Collection size is: "
					+ result.toCollection().size());
		}
	}

}
