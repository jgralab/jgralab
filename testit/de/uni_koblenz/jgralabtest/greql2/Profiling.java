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
import java.util.Map;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.exception.Greql2Exception;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.parser.ManualGreqlParser;

public class Profiling {

	Map<String, JValue> boundVariables;

	protected Graph generateTestGraph() {
		Graph graph = null;
		try {
			int count = 65;
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
			// queryString = part1 + " i " + part2;
			graph = ManualGreqlParser.parse(queryString);
		} catch (Exception e) {
			System.out.println("Exception caught: " + e.toString());
			return null;
		}
		return graph;
	}

	public Profiling() {
		boundVariables = new HashMap<String, JValue>();
		boundVariables.put("nix", new JValue(133));
	}

	public void runSize() {
		System.out.println("Size");
		String queryString = "bag(tup(\"Nodes:\", count(from  v:V{} report v end)), tup(\"Edges:\", count(from  e:E{} report e end)))";
		Graph datagraph = generateTestGraph();
		try {
			System.out.println("Creating evaluator");
			GreqlEvaluator eval = new GreqlEvaluator(queryString, datagraph,
					boundVariables);
			System.out.println("Starting evaluation");
			eval.startEvaluation();
			try {
				System.out.println("Result of the evaluation was: "
						+ eval.getEvaluationResult().toString());
				System.out.println("Overall Evaluation took "
						+ eval.getOverallEvaluationTime() + " Milliseconds");
			} catch (Exception e) {
			}
		} catch (Greql2Exception e) {
			System.out.println("Caught exception");
			System.out.println(e.toString());
		}
	}

	public void runIdentifier() {
		System.out.println("Identifier");
		String queryString = "from i: V{Identifier} report i.name end";
		Graph datagraph = generateTestGraph();
		try {
			System.out.println("Creating evaluator");
			GreqlEvaluator eval = new GreqlEvaluator(queryString, datagraph,
					boundVariables);
			System.out.println("Starting evaluation");
			eval.startEvaluation();
			try {
				System.out.println("Result of the evaluation was: "
						+ eval.getEvaluationResult().toString());
				System.out.println("Overall Evaluation took "
						+ eval.getOverallEvaluationTime() + " Milliseconds");
			} catch (Exception e) {
			}
		} catch (Greql2Exception e) {
			System.out.println("Caught exception");
			System.out.println(e.toString());
		}
	}

	public void runSimpleTypes() {
		System.out.println("SimpleTypes");
		String queryString = "from s: V{Comprehension}, i: V{SimpleDeclaration} with i -->{IsSimpleDeclOf} -->{IsCompDeclOf} s report tup(i, s) end";
		Graph datagraph = generateTestGraph();
		try {
			System.out.println("Creating evaluator");
			GreqlEvaluator eval = new GreqlEvaluator(queryString, datagraph,
					boundVariables);
			System.out.println("Starting evaluation");
			eval.startEvaluation();
			try {
				// System.out.println("Result of the evaluation was: " +
				// eval.getEvaluationResult().toString());
				System.out.println("ResultSize was: "
						+ eval.getEvaluationResult().toCollection().size());
				System.out.println("Overall Evaluation took "
						+ eval.getOverallEvaluationTime() + " Milliseconds");
			} catch (Exception e) {
				System.out.println("Exception caught");
			}
		} catch (Greql2Exception e) {
			System.out.println("Caught exception");
			System.out.println(e.toString());
		}
	}

	public void runSimpleTypesOptimized() {
		System.out.println("SimpleTypes");
		String queryString = "from i: V{SimpleDeclaration}, s: V{Comprehension} with contains(i -->{IsSimpleDeclOf} -->{IsCompDeclOf}, s) report tup(i, s) end";
		Graph datagraph = generateTestGraph();
		try {
			System.out.println("Creating evaluator");
			GreqlEvaluator eval = new GreqlEvaluator(queryString, datagraph,
					boundVariables);
			System.out.println("Starting evaluation");
			eval.startEvaluation();
			try {
				// System.out.println("Result of the evaluation was: " +
				// eval.getEvaluationResult().toString());
				System.out.println("ResultSize was: "
						+ eval.getEvaluationResult().toCollection().size());
				System.out.println("Overall Evaluation took "
						+ eval.getOverallEvaluationTime() + " Milliseconds");
			} catch (Exception e) {
				System.out.println("Exception caught");
			}
		} catch (Greql2Exception e) {
			System.out.println("Caught exception");
			System.out.println(e.toString());
		}
	}

	public void saveGreqlGraph() {
		System.out.println("SimpleTypes");
		String queryString = "from s: V{Comprehension}, i: V{SimpleDeclaration} with i -->{IsSimpleDeclOf} -->{IsCompDeclOf} s report tup(i, s) end";
		Graph datagraph = generateTestGraph();
		try {
			System.out.println("Creating evaluator");
			GreqlEvaluator eval = new GreqlEvaluator(queryString, datagraph,
					boundVariables);
			System.out.println("Starting evaluation");
			eval.startEvaluation();
			// List<Graph> l = new Ar
			try {
				GraphIO.saveGraphToFile("greqlSyntaxGraph.tg", eval
						.getSyntaxGraph(), null);
			} catch (GraphIOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (Greql2Exception e) {
			System.out.println("Caught exception");
			System.out.println(e.toString());
		}
	}

	public static void main(String[] args) {
		Profiling prof = new Profiling();
		// prof.runSize();
		// prof.runIdentifier();
		prof.runSimpleTypesOptimized();
		// prof.saveGreqlGraph();
	}

}
