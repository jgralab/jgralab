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

package de.uni_koblenz.jgralabtest.greql2;

import org.junit.Test;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.parser.GreqlParser;

public class SpeedTest extends GenericTests {

	protected Graph getTestGraph() throws Exception {
		printTestFunctionHeader("GenerateTestGraph");

		int count = 10;
		String part1 = "from i:a report ";
		String part2 = " end where q:=a, l:=a, m:=a, n:=a, o:=a, p:=a, k:= a, j:=a, h := a, g := a, f:= a, e := a, d:=a, c:=a, b:=a, a:=4";
		StringBuilder queryString = new StringBuilder();
		for (int i = 0; i < count; i++) { // 65
			queryString.append(part1);
		}
		queryString.append(" i, a, b, c, d, e ");
		for (int i = 0; i < count; i++) {
			queryString.append(part2);
		}
		String query = queryString.toString();
		System.out.println("QueryString is : " + query);
		Graph g = GreqlParser.parse(query);
		printTestFunctionFooter("GenerateTestGraph");
		return g;
	}

	// TODO: Although this is a quite complicated query, the parser shouldn't
	// need more than 10 seconds. But currently it needs infinite time...
	@Test(timeout = 10000)
	public void testCountFunctionEvaluation() throws Exception {
		printTestFunctionHeader("GraphSize");
		String queryString = "bag(tup(\"Nodes:\", count(from  v:V{} report v end)), tup(\"Edges:\", count(from  e:E{} report e end)))";
		Graph datagraph = getTestGraph();

		System.out.println("Creating evaluator");
		GreqlEvaluator eval = new GreqlEvaluator(queryString, datagraph, null);
		System.out.println("Starting evaluation");
		eval.startEvaluation();

		System.out.println("Result of the evaluation was: "
				+ eval.getEvaluationResult().toString());
		System.out.println("Overall evaluation took "
				+ eval.getOverallEvaluationTime() + " Milliseconds");
		printTestFunctionFooter("GraphSize");
	}

}
