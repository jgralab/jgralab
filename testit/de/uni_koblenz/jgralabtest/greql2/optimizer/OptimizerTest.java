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
package de.uni_koblenz.jgralabtest.greql2.optimizer;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.exception.OptimizerException;
import de.uni_koblenz.jgralab.greql2.funlib.Greql2FunctionLibrary;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.optimizer.CommonSubgraphOptimizer;
import de.uni_koblenz.jgralab.greql2.optimizer.ConditionalExpressionOptimizer;
import de.uni_koblenz.jgralab.greql2.optimizer.DefaultOptimizer;
import de.uni_koblenz.jgralab.greql2.optimizer.EarySelectionOptimizer;
import de.uni_koblenz.jgralab.greql2.optimizer.MergeSimpleDeclarationsOptimizer;
import de.uni_koblenz.jgralab.greql2.optimizer.Optimizer;
import de.uni_koblenz.jgralab.greql2.optimizer.OptimizerBase;
import de.uni_koblenz.jgralab.greql2.optimizer.PathExistenceOptimizer;
import de.uni_koblenz.jgralab.greql2.optimizer.PathExistenceToDirectedPathExpressionOptimizer;
import de.uni_koblenz.jgralab.greql2.optimizer.VariableDeclarationOrderOptimizer;
import de.uni_koblenz.jgralab.greql2.parser.GreqlParser;
import de.uni_koblenz.jgralab.greql2.schema.Greql2;
import de.uni_koblenz.jgralabtest.greql2.GenericTests;
import de.uni_koblenz.jgralabtest.greql2.testfunctions.IsPrime;

public class OptimizerTest extends GenericTests {

	static {
		Greql2FunctionLibrary.instance().registerUserDefinedFunction(
				IsPrime.class);
	}

	private Optimizer cso = new CommonSubgraphOptimizer();
	private Optimizer eso = new EarySelectionOptimizer();
	private Optimizer peo = new PathExistenceOptimizer();
	private Optimizer petdpeo = new PathExistenceToDirectedPathExpressionOptimizer();
	private Optimizer defo = new DefaultOptimizer();
	private Optimizer vdoo = new VariableDeclarationOrderOptimizer();
	private Optimizer csoAndMsdo = new CommonSubgraphAndMergeSDOptimizer();
	private Optimizer ceoAndCso = new CommonSubgraphAndConditionalExpressionOptimizer();

	private class CommonSubgraphAndMergeSDOptimizer extends OptimizerBase {
		private Optimizer msdo = new MergeSimpleDeclarationsOptimizer();

		@Override
		public boolean isEquivalent(Optimizer optimizer) {
			return false;
		}

		@Override
		public boolean optimize(GreqlEvaluator eval, Greql2 syntaxgraph)
				throws OptimizerException {
			boolean csoOptimized = cso.optimize(eval, syntaxgraph);
			return csoOptimized | msdo.optimize(eval, syntaxgraph);
		}
	};

	private class CommonSubgraphAndConditionalExpressionOptimizer extends
			OptimizerBase {
		private Optimizer ceo = new ConditionalExpressionOptimizer();

		@Override
		public boolean isEquivalent(Optimizer optimizer) {
			return false;
		}

		@Override
		public boolean optimize(GreqlEvaluator eval, Greql2 syntaxgraph)
				throws OptimizerException {
			boolean csoOptimized = ceo.optimize(eval, syntaxgraph);
			return csoOptimized | cso.optimize(eval, syntaxgraph);
		}
	};

	private void execTimedTest(String query, String name, Optimizer o)
			throws Exception {
		execTimedTest(query, name, o, getTestGraph());
	}

	private void execTimedTest(String query, String name) throws Exception {
		execTimedTest(query, name, getTestGraph());
	}

	private void execTimedTest(String query, String name, Graph datagraph)
			throws Exception {
		long start = System.currentTimeMillis();
		JValue v1 = evalTestQuery(name, query, datagraph);
		long mid = System.currentTimeMillis();
		JValue v2 = evalTestQuery(name + " (" + defo.getClass().getSimpleName()
				+ ")", query, defo, datagraph);
		long end = System.currentTimeMillis();
		assertEquals(v1, v2);
		double noOptTime = (mid - start) / 1000d;
		double optTime = (end - mid) / 1000d;
		System.out.println("Time with no optimization:      " + noOptTime
				+ "secs");
		System.out.println("Time with default optimization: " + optTime
				+ "secs");
	}

	private void execTimedTest(String query, String name, Optimizer o,
			Graph datagraph) throws Exception {
		long start = System.currentTimeMillis();
		JValue v1 = evalTestQuery(name, query, datagraph);
		long mid1 = System.currentTimeMillis();
		JValue v2 = evalTestQuery(name + " (" + o.getClass().getSimpleName()
				+ ")", query, o, datagraph);
		long mid2 = System.currentTimeMillis();
		JValue v3 = evalTestQuery(name + " (" + defo.getClass().getSimpleName()
				+ ")", query, defo, datagraph);
		long end = System.currentTimeMillis();
		assertEquals(v1, v2);
		assertEquals(v1, v3);
		double noOptTime = (mid1 - start) / 1000d;
		double o1Time = (mid2 - mid1) / 1000d;
		double o2Time = (end - mid2) / 1000d;
		System.out.println("Time with no optimization: " + noOptTime + "secs");
		System.out.println("Time with optimization ("
				+ o.getClass().getSimpleName() + "): " + o1Time + "secs");
		System.out.println("Time with optimization ("
				+ defo.getClass().getSimpleName() + "): " + o2Time + "secs");
	}

	@Test
	public void testCommonSubgraphOptimizer0() throws Exception {
		String query = "from w : list(2..10), x : list(2..10), y : list(2..10), z : list(1..2) "
				+ "     with isPrime(x + z) and x * x > y and z > x * x "
				+ "     reportBag w, x, y, z end";
		execTimedTest(query, "CommonSubgraphOptimizer0()", cso);
	}

	@Test
	public void testCommonSubgraphOptimizer1() throws Exception {
		String query = "from w : list(2..10), x : list(2..10), y : list(2..10), z : list(1..2) "
				+ "     with isPrime(x + z) and (x + z) * z > y and x * x > y and (x + z) * z > x * x "
				+ "     report isPrime(x + y), x + z, (x + z) * z, x * x, x * w * w, w * w end";
		execTimedTest(query, "CommonSubgraphOptimizer1()", cso);
	}

	@Test
	public void testCommonSubgraphOptimizer2() throws Exception {
		String query = "from a : from x : list(1..10) with isPrime(x) reportSet x end, "
				+ "          b : from x : list(1..10) with isPrime(x) reportSet x end, "
				+ "          c : from x : list(1..10) with isPrime(x) reportSet x end "
				+ "     with forall i : a @ isPrime(i)          "
				+ "     report a, b, c end";
		execTimedTest(query, "CommonSubgraphOptimizer2()", cso);
	}

	@Test
	public void testCommonSubgraphOptimizer3() throws Exception {
		String query = "from x : list(1..10),                "
				+ "          y : list(1..10)"
				+ "     with isPrime(x+y) and theElement(from a : list(1..10),"
				+ "                                           b : list(1..10)"
				+ "                                      with isPrime(a+b)"
				+ "                                      reportSet true end) "
				+ "     report x, y end";
		execTimedTest(query, "CommonSubgraphOptimizer3()", cso);
	}

	@Test
	public void testEarlySelectionOptimizer1() throws Exception {
		String query = "from x, y : list(1..10),"
				+ "          z : list(11..20)"
				+ "     with isPrime(x) and isPrime(x + y) and isPrime(x+z) and isPrime(z)"
				+ "     reportSet x, y, z end";
		execTimedTest(query, "EarlySelectionOptimizer1()", eso);
	}

	@Test
	public void testEarlySelectionOptimizer2() throws Exception {
		String query = "from x : list(1..10),"
				+ "          y : list(11..20),"
				+ "          z : list(21..30)"
				+ "     with isPrime(x) and isPrime(y) and isPrime(z) and (x > 2 or x < 8)"
				+ "     reportSet x, y, z end";
		execTimedTest(query, "EarlySelectionOptimizer2()", eso);
	}

	@Test
	public void testEarlySelectionOptimizer3() throws Exception {
		String query = "from x : list(1..5)                  "
				+ "     with isPrime(x)"
				+ "     reportSet x, from y : list(21..25),"
				+ "                       z : list(21..30)"
				+ "                  with isPrime(y+x) and isPrime(z+x)"
				+ "                  reportSet y, z end" + "     end";
		execTimedTest(query, "EarlySelectionOptimizer3()", eso);
	}

	@Test
	public void testEarlySelectionOptimizer4() throws Exception {
		String query = "from x : list(1..5),                  "
				+ "          y : list(21..100)   "
				+ "     with isPrime(x) and isPrime(y)       "
				+ "          and isPrime(theElement(  "
				+ "                        from x : list(12..13),"
				+ "                             y : list(10..11)"
				+ "                        with isPrime(y) and isPrime(x+y)"
				+ "                        reportSet x+y end))"
				+ "     report x, y, from y : list(21..100),"
				+ "                       z : from a : list(10..20),"
				+ "                                b : list(30..60)"
				+ "                           with a * 3 = b and isPrime(a)"
				+ "                           reportSet a + b end"
				+ "                  with isPrime(y+x) and isPrime(z+x)"
				+ "                  reportSet y, z end                "
				+ "     end";
		execTimedTest(query, "EarlySelectionOptimizer4()", eso);
	}

	@Test
	public void testEarlySelectionOptimizer5() throws Exception {
		String query = "from x : list(1..10),                  "
				+ "          y : list(11..20)"
				+ "     reportBag from a : list(21..25),"
				+ "                    b : list(21..25)"
				+ "               with isPrime(x) and isPrime(y)"
				+ "               reportSet x, y end            "
				+ "     end              ";
		execTimedTest(query, "EarlySelectionOptimizer5()", eso);
	}

	@Test
	public void testEarlySelectionOptimizer6() throws Exception {
		String query = "from x, y : list(1..20)                  "
				+ "     with isPrime(x) and isPrime(y) and isPrime(x+y)"
				+ "     reportSet x, y end            ";
		execTimedTest(query, "EarlySelectionOptimizer6()", eso);
	}

	@Test
	public void testEarlySelectionOptimizer7() throws Exception {
		String query = "from a, b : list(1..10),               "
				+ "          c, d : list(11..20)"
				+ "     with isPrime(a) and isPrime(b) and a < b and c < d"
				+ "     report a, b, c, d end";
		execTimedTest(query, "EarlySelectionOptimizer7()", eso);
	}

	@Test
	public void testEarlySelectionOptimizer8() throws Exception {
		String query = "from a, b : list(1..10),     "
				+ "          c    : list(11..20)"
				+ "     with isPrime(a + b) and isPrime(a + c)"
				+ "     report a, b, c end";
		execTimedTest(query, "EarlySelectionOptimizer8()", eso);
	}

	@Test
	public void testEarlySelectionOptimizer9() throws Exception {
		String query = "from a : list(1..10)"
				+ "     reportSet from b : list(20..30),"
				+ "                    c : list(15..30)"
				+ "               with isPrime(a+b) and isPrime(c)"
				+ "               reportSet a, b end              "
				+ "     end";
		execTimedTest(query, "EarlySelectionOptimizer9()", eso);
	}

	@Test
	public void testEarlySelectionOptimizer11() throws Exception {
		String query = "from class      : V,          "
				+ "          superClass : V           "
				+ "     with count(children(superClass)) > 1                "
				+ "          and superClass -->+ class "
				+ "          and (exists mid, mid2 : V,                                  "
				+ "                      mid -->+ class, "
				+ "                      mid2 -->+ class "
				+ "                      @ mid <> mid2) "
				+ "     reportSet class                              "
				+ "     end";
		execTimedTest(query, "EarlySelectionOptimizer11", eso);
	}

	@Test
	public void testEarlySelectionWithQuantifiedExpression() throws Exception {
		String query = "exists a, b, c : list(1..10), isPrime(a), isPrime(b), isPrime(a+b) "
				+ "     @ a > 5 and a > b and a < c * c";
		execTimedTest(query, "EarlySelectionWithQuantifiedExpression()", eso);
	}

	@Test
	public void testEarlySelectionOptimizer10() throws Exception {
		String query = "from a, b, c : list(1..10)"
				+ "     with a > b and isPrime(a + c) and isPrime(b + c)"
				+ "     reportBag a, b, c, a > b, isPrime(a + c) end";
		execTimedTest(query, "EarlySelectionOptimizer10()", eso);
	}

	private Graph pathExistenceOptimizerTestGraph = null;

	private Graph getPathExistenceOptimizerTestGraph() throws Exception {
		if (pathExistenceOptimizerTestGraph == null) {
			pathExistenceOptimizerTestGraph = createPathExistenceOptimizerTestGraph();
		}
		return pathExistenceOptimizerTestGraph;
	}

	private static Graph createPathExistenceOptimizerTestGraph()
			throws Exception {
		String query = "    from a : V{Variable},                              "
				+ "              b : V{BagComprehension}                       "
				+ "         with a -->* b                                      "
				+ "         reportBag from x : list(1..10),                    "
				+ "                        y : list(11..20)                    "
				+ "                   with isPrime(x * x + y - 1)              "
				+ "                   reportSet x * x + y - 1, x, y, a, b end, "
				+ "                   a, b end";
		Graph g = GreqlParser.parse(query);
		// parser.saveGraph("createdTestGraph.tg");
		return g;
	}

	@Test
	public void testPathExistenceOptimizer1() throws Exception {
		String query = "from a : V{Variable},             "
				+ "          b : V{SimpleDeclaration}     "
				+ "     with a --> b                      "
				+ "     reportBag a, b end";
		execTimedTest(query, "PathExistenceOptimizer1()", peo,
				getPathExistenceOptimizerTestGraph());
	}

	@Test
	public void testPathExistenceOptimizer2() throws Exception {
		String query = "from a : V{Variable},             "
				+ "          b : V{SimpleDeclaration}     "
				+ "     with b <-- a                      "
				+ "     reportBag a, b end";
		execTimedTest(query, "PathExistenceOptimizer2()", peo,
				getPathExistenceOptimizerTestGraph());
	}

	@Test
	public void testPathExistenceOptimizer3() throws Exception {
		String query = "from a : V{Variable},             "
				+ "          b : V{SimpleDeclaration},    "
				+ "          c : V{Declaration}           "
				+ "     with a --> b --> c or c <-- b <-- a or  a -->* c <-- b "
				+ "     reportBag a, b, c end";
		execTimedTest(query, "PathExistenceOptimizer3()", peo,
				getPathExistenceOptimizerTestGraph());
	}

	@Test
	public void testPathExistenceToDirectedPathExpOptimizer1() throws Exception {
		String query = "from a : V{Variable},             "
				+ "          b : V{SimpleDeclaration}     "
				+ "     with a --> b                      "
				+ "     reportBag a, b end";
		execTimedTest(query, "PathExistenceToDirectedPathExpOptimizer1()",
				petdpeo, getPathExistenceOptimizerTestGraph());
	}

	@Test
	public void testPathExistenceToDirectedPathExpOptimizer2() throws Exception {
		String query = "from a : V{Variable},             "
				+ "          b : V     "
				+ "     with a --> <>-- b                      "
				+ "     reportBag a, b end";
		execTimedTest(query, "PathExistenceToDirectedPathExpOptimizer2()",
				petdpeo, getPathExistenceOptimizerTestGraph());
	}

	@Test
	public void testPathExistenceToDirectedPathExpOptimizer3() throws Exception {
		String query = "from a, b : V             "
				+ "     with a --> <>-- b                      "
				+ "     reportBag a, b end";
		execTimedTest(query, "PathExistenceToDirectedPathExpOptimizer3()",
				petdpeo, getPathExistenceOptimizerTestGraph());
	}

	@Test
	public void testPathExistenceToDirectedPathExpOptimizer4() throws Exception {
		String query = "from a, b : V             "
				+ "     reportBag a, a --> <>-- b end";
		execTimedTest(query, "PathExistenceToDirectedPathExpOptimizer4()",
				petdpeo, getPathExistenceOptimizerTestGraph());
	}

	@Test
	public void testPathExistenceToDirectedPathExpOptimizer5() throws Exception {
		String query = "from a, b: V, c: V{Variable}             "
				+ "     with a (-->|--<>)+ c and c --> b"
				+ "     reportBag a, a --> <>-- b end";
		execTimedTest(query, "PathExistenceToDirectedPathExpOptimizer5()",
				petdpeo, getPathExistenceOptimizerTestGraph());
	}

	@Test
	public void testVariableDeclarationOrderOptimizer1() throws Exception {
		String query = "from a : list(1..10),                     "
				+ "          b : list(1..20)                      "
				+ "     with isPrime(a+b) and isPrime(b)        "
				+ "     reportSet a, b end";
		execTimedTest(query, "VariableDeclarationOrderOptimizer1()", vdoo);
	}

	@Test
	public void testVariableDeclarationOrderOptimizer2() throws Exception {
		String query = "from a : list(1..10),                     "
				+ "          b : list(1..20)                      "
				+ "     with isPrime(a + 1) and isPrime(b)            "
				+ "     reportSet a, b end";
		execTimedTest(query, "VariableDeclarationOrderOptimizer2()", vdoo);
	}

	@Test
	public void testVariableDeclarationOrderOptimizer3() throws Exception {
		String query = "from a : list(1..10),                     "
				+ "          b : list(1..20)                      "
				+ "     with isPrime(a + 1) and isPrime(b)        "
				+ "          and (exists! x : list(1..30), y : list(10..20), x+a<y+b @ isPrime(x+y)) "
				+ "     reportSet a, b end";
		execTimedTest(query, "VariableDeclarationOrderOptimizer3()", vdoo);
	}

	@Test
	public void testVariableDeclarationOrderOptimizer4() throws Exception {
		String query = "from a : V{Variable},                          "
				+ "              b : V,                                    "
				+ "              c : V                                     "
				+ "         with a --> c and b --> c and a <> b and inDegree(c) = 2 "
				+ "         reportSet a, b, c end";
		execTimedTest(query, "VariableDeclarationOrderOptimizer4()", vdoo);
	}

	@Test
	public void testVariableDeclarationOrderOptimizer5() throws Exception {
		String queryString = "from x:list(1..10), y:list(x..13), z:list(1..x) "
				+ "           with x <> 0 and y <> 0 and z <> 0 "
				+ "           report isPrime(z), isPrime(z*z), isPrime(z+z*z-1) end";
		execTimedTest(queryString, "VariableDeclarationOrderOptimizer5()", vdoo);
	}

	@Test
	public void testVariableDeclarationOrderOptimizer6() throws Exception {
		String queryString = "from x:list(1..10), y:list(x..13), z:list(1..x), a : set(1, 2, 3), b : z "
				+ "           with x <> 0 and y <> 0 and z <> 0 and (b <> z)"
				+ "           report isPrime(z), isPrime(z*z), isPrime(z+z*z-1), b end";
		execTimedTest(queryString, "VariableDeclarationOrderOptimizer6()", vdoo);
	}

	@Test
	public void testVariableDeclarationOrderOptimizer7() throws Exception {
		String queryString = "from x:list(1..10), y:list(x..13), z:list(y..20)   "
				+ "           with isPrime(y+z)  "
				+ "           report x, y, z end";
		execTimedTest(queryString, "VariableDeclarationOrderOptimizer7()", vdoo);
	}

	@Test
	public void testVariableDeclarationOrderOptimizer8() throws Exception {
		String queryString = "from x:V, y:x-->, z:y--> "
				+ "           with y<->+y and z<->+z  "
				+ "           report x, y, z end";
		execTimedTest(queryString, "VariableDeclarationOrderOptimizer8()",
				vdoo, getTestGraph());
	}

	@Test
	public void testConditionalExpressionOptimizer0() throws Exception {
		String query = "from u, v, w : set(true, false)     "
				+ "     with (u or v) and w and true and ((u and false) or (w or true))          "
				+ "     reportSet u, v, w end          ";
		execTimedTest(query, "ConditionalExpressionOptimizer0()", ceoAndCso);
	}

	@Test
	public void testConditionalExpressionOptimizer1() throws Exception {
		String query = "from u, v, w, x, y, z : set(true, false)     "
				+ "     with (u xor v) or (w and x and (y or z))       "
				+ "     reportSet u, v, w, x, y, z end          ";
		execTimedTest(query, "ConditionalExpressionOptimizer1()", ceoAndCso);
	}

	@Test
	public void testConditionalExpressionOptimizer2() throws Exception {
		String query = "from u, v, w, x, y, z : set(true, false)     "
				+ "     with (u xor v) or (w and x and (y or z)) or (y and z) or (u and z)      "
				+ "     reportSet u, v, w, x, y, z end          ";
		execTimedTest(query, "ConditionalExpressionOptimizer2()", ceoAndCso);
	}

	@Test
	public void testConditionalExpressionOptimizer3() throws Exception {
		String query = "from u, v : set(true, false)     "
				+ "     with u or v                             "
				+ "     reportSet u, v end          ";
		execTimedTest(query, "ConditionalExpressionOptimizer3()", ceoAndCso);
	}

	@Test
	public void testConditionalExpressionOptimizer4() throws Exception {
		String query = "from u, v, w, x, y, z : set(true, false)     "
				+ "     with ((u xor v) or (w and x and (y or z)) or (y and z) or (u and z) and ((u and x) or (y and w) and (u or v)))     "
				+ "          or ((u xor v) or (w and x and (y or z)) or (y and z) or (u and z) and ((u and x) or (y and w) and (u or v)))  "
				+ "          and ((u xor v) or (w and x and (y or z)) or (y and z) or (u and z) and ((u and x) or (y and w) and (u or v))) "
				+ "     reportSet u, v, w, x, y, z end          ";
		execTimedTest(query, "ConditionalExpressionOptimizer4()", ceoAndCso);
	}

	@Test
	public void testConditionalExpressionOptimizer5() throws Exception {
		String query = "from x, y, z : list(1..30)     "
				+ "     with isPrime(x) and not isPrime(y) and isPrime(z)"
				+ "     reportSet x, y, z end          ";
		execTimedTest(query, "ConditionalExpressionOptimizer5()", ceoAndCso);
	}

	@Test
	public void testConditionalExpressionOptimizer6() throws Exception {
		String query = "from x, y, z : list(1..30)     "
				+ "     with isPrime(x) and isPrime(y) and isPrime(z)"
				+ "     reportSet x, y, z end          ";
		execTimedTest(query, "ConditionalExpressionOptimizer6()", ceoAndCso);
	}

	@Test
	public void testOptimizer1() throws Exception {
		String query = "from x, y, z : list(1..30)     "
				+ "     with isPrime(x) and isPrime(z)"
				+ "     reportSet from a, b : list(x..z)             "
				+ "               with a + b = y                     "
				+ "                    and isPrime(y)                "
				+ "               report a, b, y end                 "
				+ "     end          ";
		execTimedTest(query, "Optimizer1()");
	}

	@Test
	public void testOptimizer2() throws Exception {
		String query = "from x, y, z : list(1..30)     "
				+ "     with isPrime(x) and isPrime(y) and isPrime(z)"
				+ "     reportSet x, y, z                            "
				+ "     end          ";
		execTimedTest(query, "Optimizer2()");
	}

	@Test
	public void testMergeSimpleDeclarationOptimizer1() throws Exception {
		// here all three SD should be merged
		String query = "from x : list(1..10),          "
				+ "          y : list(1..10),          "
				+ "          z : list(1..10)            "
				+ "     reportSet x, y, z                            "
				+ "     end          ";
		execTimedTest(query, "MergeSimpleDeclarationOptimizer1()", csoAndMsdo);
	}

	@Test
	public void testMergeSimpleDeclarationOptimizer2() throws Exception {
		// here the first and third SD may not be merged
		String query = "from x : list(1..10),          "
				+ "          y : list(1..11),          "
				+ "          z : list(1..10)            "
				+ "     reportSet x, y, z                            "
				+ "     end          ";
		execTimedTest(query, "MergeSimpleDeclarationOptimizer2()", csoAndMsdo);
	}

	@Test
	public void testMergeSimpleDeclarationOptimizer3() throws Exception {
		// here the first and second SD should be merged
		String query = "from x : list(1..11),          "
				+ "          y : list(1..11),          "
				+ "          z : list(1..10)            "
				+ "     reportSet x, y, z                            "
				+ "     end          ";
		execTimedTest(query, "MergeSimpleDeclarationOptimizer3()", csoAndMsdo);
	}

	@Test
	public void testMergeSimpleDeclarationOptimizer4() throws Exception {
		// here the second and third SD should be merged
		String query = "from x : list(1..10),          "
				+ "          y : list(1..11),          "
				+ "          z : list(1..11)            "
				+ "     reportSet x, y, z                            "
				+ "     end          ";
		execTimedTest(query, "MergeSimpleDeclarationOptimizer4()", csoAndMsdo);
	}

}
