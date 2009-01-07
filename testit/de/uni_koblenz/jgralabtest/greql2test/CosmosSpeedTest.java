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

package de.uni_koblenz.jgralabtest.greql2test;

import java.util.HashMap;
import java.util.Map;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.exception.Greql2Exception;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;

public class CosmosSpeedTest {

	Map<String, JValue> boundVariables;

	public CosmosSpeedTest() {
		boundVariables = new HashMap<String, JValue>();
		boundVariables.put("nix", new JValue(133));
	}

	protected Graph loadCosmos() {
		try {
			// CosmosSchema schema = CosmosSchema.instance();
			// Graph g = schema.loadCosmos("cosmos-client.tg");
			// System.out.println("Graph has " + g.getVCount() +
			// " vertices and " + g.getECount() + " edges" );
			// return g;
			return null;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	public void runSimpleTypes() {
		System.out.println("SimpleTypes");
		String queryString = "from s: V{SimpleTypeSpecifier}, i: V{Identifier} with contains(s -->{IsDeclarationSpecifierIn} [<--{IsInitDeclaratorIn}] <--{IsDeclaratorIn} <--{IsDirectDeclaratorIn}+, i) report i.name, s.name end";
		Graph datagraph = loadCosmos();
		try {
			System.out.println("Creating evaluator");
			GreqlEvaluator eval = new GreqlEvaluator(queryString, datagraph,
					boundVariables,
					new de.uni_koblenz.jgralab.impl.ProgressFunctionImpl());
			System.out.println("Starting evaluation");
			eval.startEvaluation();
			try {
				// System.out.println("Result of the evaluation was: " +
				// eval.getEvaluationResult().toString());
				System.out.println("ResultSize was: "
						+ eval.getEvaluationResult().toCollection().size());
				System.out.println("Evaluation (+ optimization, parsing) took "
						+ eval.getOverallEvaluationTime() + " Milliseconds");
			} catch (Exception e) {
				System.out.println("Exception caught");
				e.printStackTrace();
			}
		} catch (Greql2Exception e) {
			System.out.println("Caught exception");
			System.out.println(e.toString());
		}
	}

	public void runIdentifierWithUsage() {
		System.out.println("IdentifierWithUsage");
		String queryString = "from i: V{Identifier} report i.name as \"Vertex\", degree{IsExprIn}(i) as \"Usages\" end";
		Graph datagraph = loadCosmos();
		try {
			System.out.println("Creating evaluator");
			GreqlEvaluator eval = new GreqlEvaluator(queryString, datagraph,
					boundVariables,
					new de.uni_koblenz.jgralab.impl.ProgressFunctionImpl());
			System.out.println("Starting evaluation");
			eval.startEvaluation();
			try {
				// System.out.println("Result of the evaluation was: " +
				// eval.getEvaluationResult().toString());
				System.out.println("ResultSize was: "
						+ eval.getEvaluationResult().toCollection().size());
				System.out.println("Overall evaluation took "
						+ eval.getOverallEvaluationTime() + " Milliseconds");
			} catch (Exception e) {
				System.out.println("Exception caught");
				e.printStackTrace();
			}
		} catch (Greql2Exception e) {
			System.out.println("Caught exception");
			System.out.println(e.toString());
		}
	}

	public void runFunctionsWithModules() {
		System.out.println("IdentifierWithUsage");
		String queryString = "from fd:V{FunctionDefinition}, i:V{Identifier}, f:V{SourceFile} with (fd -->{IsExternalDeclarationIn} -->{IsSourceUsageIn} <--{IsPrimarySourceFor}+ f) and i -->{IsDirectDeclaratorIn}+ -->{IsFunctionDeclaratorIn} fd report f.name as \"SourceFile\", i.name as \"Function\" end";
		Graph datagraph = loadCosmos();
		try {
			System.out.println("Creating evaluator");
			GreqlEvaluator eval = new GreqlEvaluator(queryString, datagraph,
					boundVariables,
					new de.uni_koblenz.jgralab.impl.ProgressFunctionImpl());
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
				e.printStackTrace();
			}
		} catch (Greql2Exception e) {
			System.out.println("Caught exception");
			System.out.println(e.toString());
		}
	}

	public void runFunctionsWithModulesOptimized() {
		System.out.println("FunctionsWithModulesOptimized");
		String queryString = "from fd:V{FunctionDefinition}, f:V{SourceFile}, i:V{Identifier} with contains(fd -->{IsExternalDeclarationIn} -->{IsSourceUsageIn} <--{IsPrimarySourceFor}+, f) and contains(fd <--{IsFunctionDeclaratorIn} <--{IsDirectDeclaratorIn}+, i) report f.name as \"SourceFile\", i.name as \"Function\" end";
		Graph datagraph = loadCosmos();
		try {
			System.out.println("Creating evaluator");
			GreqlEvaluator eval = new GreqlEvaluator(queryString, datagraph,
					boundVariables,
					new de.uni_koblenz.jgralab.impl.ProgressFunctionImpl());
			System.out.println("Starting evaluation");
			eval.startEvaluation();
			try {
				// System.out.println("Result of the evaluation was: " +
				// eval.getEvaluationResult().toString());
				System.out.println("ResultSize was: "
						+ eval.getEvaluationResult().toCollection().size());
				System.out.println("Evaluation took "
						+ eval.getOverallEvaluationTime() + " Milliseconds");
			} catch (Exception e) {
				System.out.println("Exception caught");
				e.printStackTrace();
			}
		} catch (Greql2Exception e) {
			System.out.println("Caught exception");
			System.out.println(e.toString());
		}
	}

	public void runFunctionsWithModulesRealOptimized() {
		System.out.println("FunctionsWithModulesRealOptimized");
		// String queryString =
		// "from " +
		// "  fd:V{FunctionDefinition}, " +
		// "  f:V{SourceFile}, " +
		// "  i:V{Identifier} " +
		// "with " +
		// "  contains(fd -->{IsExternalDeclarationIn} -->{IsSourceUsageIn} <--{IsPrimarySourceFor}+, f) "
		// +
		// "and " +
		// "  contains(fd <--{IsFunctionDeclaratorIn} <--{IsDirectDeclaratorIn}+, i) "
		// +
		// "report " +
		// "  f.name as \"SourceFile\", " +
		// "  i.name as \"Function\" " +
		// "end";
		String queryString = "from "
				+ "  function: from f:V{SourceFile}, fd:V{FunctionDefinition} "
				+ "            with contains(-->{IsExternalDeclarationIn} -->{IsSourceUsageIn} <--{IsPrimarySourceFor}+ f, fd) "
				+ "            report rec(def:fd, sf:f) end, "
				+ "  i:V{Identifier} "
				+ "with "
				+ "  contains(function.def <--{IsFunctionDeclaratorIn} <--{IsDirectDeclaratorIn}+, i) "
				+ "report " + "  function.sf.name as \"SourceFile\", "
				+ "  i.name as \"Function\" " + "end";
		Graph datagraph = loadCosmos();
		try {
			System.out.println("Creating evaluator");
			GreqlEvaluator eval = new GreqlEvaluator(queryString, datagraph,
					boundVariables,
					new de.uni_koblenz.jgralab.impl.ProgressFunctionImpl());
			System.out.println("Starting evaluation");
			eval.startEvaluation();
			try {
				// System.out.println("Result of the evaluation was: " +
				// eval.getEvaluationResult().toString());
				System.out.println("ResultSize was: "
						+ eval.getEvaluationResult().toCollection().size());
				System.out.println("Evaluation took "
						+ eval.getOverallEvaluationTime() + " Milliseconds");
			} catch (Exception e) {
				System.out.println("Exception caught");
				e.printStackTrace();
			}
		} catch (Greql2Exception e) {
			System.out.println("Caught exception");
			System.out.println(e.toString());
		}
	}

	public void runTableOptimized() {
		System.out.println("TableOptimized");
		/*
		 * the query below is the old unoptimized query that took about two days
		 * to compute (using a IBM ThinkPad with 2,2 Ghz and 1024 MB Ram)
		 */
		// String queryString =
		// "from caller: from callerDef:V{FunctionDefinition}, callerId:V{Identifier}"
		// +
		// "             with contains(callerDef <--{IsFunctionDeclaratorIn} <--{IsDirectDeclaratorIn}+, callerId) "
		// + "             report tup(callerDef, callerId.name) end "
		// + "     , "
		// + "     callee:V{Identifier} "
		// // +
		// "with (callee.name = \"connect_to_server\" or callee.name = \"debug_msg\") "
		// // + "report "
		// // + " caller[0], caller[1] "
		// + "reportTable "
		// + "    callee, "
		// + "    caller[1], "
		// + "    contains(caller[0] <--{IsCompoundStatementIn} "
		// +
		// "    (( <--{IsStmtIn}*) | (<--{IsDeclarationIn}+ <--{IsInitDeclaratorIn}<--{IsInitializationIn} )) "
		// + "    <--{IsExprIn}* <--{IsFunctionNameIn}, callee), "
		// + "    \"Caller\" "
		// + "end";
		String queryString = "from caller, callee: from callerDef:V{FunctionDefinition}, callerId:V{Identifier}"
				+ "             with contains(callerDef <--{IsFunctionDeclaratorIn} <--{IsDirectDeclaratorIn}+, callerId) "
				+ "             report rec(def:callerDef, identifier:callerId) end "
				+ "reportTable "
				+ "    callee.identifier.name, "
				+ "    caller.identifier.name, "
				+ "    contains(caller.def <--{IsCompoundStatementIn} "
				+ "    (( <--{IsStmtIn}*) | (<--{IsDeclarationIn}+ <--{IsInitDeclaratorIn}<--{IsInitializationIn} )) "
				+ "    <--{IsExprIn}* <--{IsFunctionNameIn}, callee.identifier), "
				+ "    \"Caller\" " + "end";
		Graph datagraph = loadCosmos();
		try {
			System.out.println("Creating evaluator");
			GreqlEvaluator eval = new GreqlEvaluator(queryString, datagraph,
					boundVariables,
					new de.uni_koblenz.jgralab.impl.ProgressFunctionImpl());
			System.out.println("Starting evaluation");
			eval.startEvaluation();
			try {
				// System.out.println("Result of the evaluation was: " +
				// eval.getEvaluationResult().toString());
				System.out.println("ResultSize was: "
						+ eval.getEvaluationResult().toCollection().size());
				System.out.println("Overall Evaluation took "
						+ eval.getOverallEvaluationTime() + " Milliseconds");
				System.out.println("Result is: ");
				eval.getEvaluationResult().toCollection().toJValueTable()
						.printTable();
				// System.out.println("-----------------------------------------------------------------------");
				// JValueBag r =
				// eval.getEvaluationResult().toCollection().toJValueBag();
				// Iterator<JValue> iter = r.iterator();
				// while (iter.hasNext())
				// System.out.println(iter.next().isVertex() + " , ");
				// System.out.println("-----------------------------------------------------------------------");
			} catch (Exception e) {
				System.out.println("Exception caught");
				e.printStackTrace();
			}
		} catch (Greql2Exception e) {
			System.out.println("Caught exception");
			System.out.println(e.toString());
		}
	}

	public void runTableOptimized2() {
		System.out.println("TableOptimized2");
		String queryString = "let functions := from funDef:V{FunctionDefinition}, funId:V{Identifier}"
				+ "             with contains(funDef <--{IsFunctionDeclaratorIn} <--{IsDirectDeclaratorIn}+, funId) "
				+ "             report rec(def:funDef, identifier:funId) end"
				+ "    , "
				+ "    freqCalledFunctions := from fqcallee:functions "
				+ "                          with outDegree{IsFunctionNameIn}(fqcallee.identifier) > 1"
				+ "                          report fqcallee end"
				+ "    , "
				+ "    freqFunctionCalls := from freqCallee:freqCalledFunctions, functionCall:V{FunctionCall} "
				+ "                         with contains( <--{IsFunctionNameIn} freqCallee.identifier , functionCall) "
				+ "                         report functionCall end"
				+ "    , "
				+ "    freqCallers:= from freqCaller:functions, functionCall:freqFunctionCalls "
				+ "                   with contains(freqCaller.def <--{IsCompoundStatementIn} "
				+ "             	    (( <--{IsStmtIn}*) | (<--{IsDeclarationIn}+ <--{IsInitDeclaratorIn}<--{IsInitializationIn} ))"
				+ "             	    <--{IsExprIn}*, functionCall) "
				+ "                   reportSet freqCaller end "
				+ "in "
				+ "from caller : freqCallers, "
				+ "     callee : freqCalledFunctions "
				+ "reportTable "
				+ "  caller.identifier.name, "
				+ "  callee.identifier.name, "
				+ "  contains(caller.def <--{IsCompoundStatementIn} "
				+ "  (( <--{IsStmtIn}*) | (<--{IsDeclarationIn}+ <--{IsInitDeclaratorIn}<--{IsInitializationIn} )) "
				+ "  <--{IsExprIn}* <--{IsFunctionNameIn}, callee.identifier), "
				+ " \"Callee\" end";
		Graph datagraph = loadCosmos();
		try {
			System.out.println("Creating evaluator");
			GreqlEvaluator eval = new GreqlEvaluator(queryString, datagraph,
					boundVariables,
					new de.uni_koblenz.jgralab.impl.ProgressFunctionImpl());
			System.out.println("Starting evaluation");
			eval.startEvaluation();
			try {
				System.out.println("ResultSize was: "
						+ eval.getEvaluationResult().toCollection().size());
				System.out.println("Overall Evaluation took "
						+ eval.getOverallEvaluationTime() + " Milliseconds");
				System.out.println("Result is: ");
				// System.out.println("-----------------------------------------------------------------------");
				// JValueBag r =
				// eval.getEvaluationResult().toCollection().toJValueBag();
				// Iterator<JValue> iter = r.iterator();
				// while (iter.hasNext())
				// System.out.println(iter.next() + " , ");
				// System.out.println("-----------------------------------------------------------------------");
				eval.getEvaluationResult().toCollection().toJValueTable()
						.printTable();
			} catch (Exception e) {
				System.out.println("Exception caught");
				e.printStackTrace();
			}
		} catch (Greql2Exception e) {
			System.out.println("Caught exception");
			System.out.println(e.toString());
		}
	}

	public static void main(String[] args) {
		System.out.println("Initializing CosmosSpeedTest");
		CosmosSpeedTest prof = new CosmosSpeedTest();
		// prof.runIdentifierWithUsage();
		// prof.runSimpleTypes();
		// prof.runFunctionsWithModulesOptimized();
		// prof.runFunctionsWithModulesRealOptimized();
		prof.runTableOptimized();
		// prof.runTableOptimized2();
	}

}
