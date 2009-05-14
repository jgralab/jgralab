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

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.junit.Test;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.parser.Greql2Lexer;
import de.uni_koblenz.jgralab.greql2.parser.Greql2Parser;

public class SpeedTest extends GenericTests {

	@Override
	protected Graph getTestGraph() throws Exception {
		printTestFunctionHeader("GenerateTestGraph");

		int count = 10;
		String part1 = "from i:a report ";
		String part2 = " end where q:=a, l:=a, m:=a, n:=a, o:=a, p:=a, k:= a, j:=a, h := a, g := a, f:= a, e := a, d:=a, c:=a, b:=a, a:=4";
		String queryString = "";
		for (int i = 0; i < count; i++) { // 65
			queryString += part1;
		}
		queryString += " i, a, b, c, d, e ";
		for (int i = 0; i < count; i++) {
			queryString += part2;
		}
		System.out.println("QueryString is : " + queryString);
		Greql2Lexer lexer = new Greql2Lexer(new ANTLRStringStream(queryString));
		CommonTokenStream tokens = new CommonTokenStream();
		tokens.setTokenSource(lexer);
		Greql2Parser parser = new Greql2Parser(tokens);
		parser.greqlExpression();
		printTestFunctionFooter("GenerateTestGraph");
		return parser.getGraph();
	}

	// TODO: Although this is a quite complicated query, the parser shouldn't
	// need more than 10 seconds. But currently it needs infinite time...
	@Test(timeout = 10000)
	public void testCountFunctionEvaluation() throws Exception {
		printTestFunctionHeader("GraphSize");
		String queryString = "bag(tup(\"Nodes:\", count(from  v:V{} report v end)), tup(\"Edges:\", count(from  e:E{} report e end)))";
		Graph datagraph = getTestGraph();

		System.out.println("Creating evaluator");
		GreqlEvaluator eval = new GreqlEvaluator(queryString, datagraph,
				boundVariables);
		System.out.println("Starting evaluation");
		eval.startEvaluation();

		System.out.println("Result of the evaluation was: "
				+ eval.getEvaluationResult().toString());
		System.out.println("Overall evaluation took "
				+ eval.getOverallEvaluationTime() + " Milliseconds");
		printTestFunctionFooter("GraphSize");
	}

}
