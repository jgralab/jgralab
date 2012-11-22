/**
 * 
 */
package de.uni_koblenz.jgralab.greql.funlib.graph;

import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql.evaluator.InternalGreqlEvaluator;
import de.uni_koblenz.jgralab.greql.evaluator.fa.DFA;
import de.uni_koblenz.jgralab.greql.funlib.Description;
import de.uni_koblenz.jgralab.greql.funlib.FunLib;
import de.uni_koblenz.jgralab.greql.funlib.Function;
import de.uni_koblenz.jgralab.greql.funlib.NeedsEvaluatorArgument;

@NeedsEvaluatorArgument
public class Path extends Function {

	@Description(params = { "v1", "pd", "v2" }, description = "Returns the shortest path between v1 and v2 matching the path description pd.", categories = Category.GRAPH)
	public de.uni_koblenz.jgralab.greql.types.Path evaluate(
			InternalGreqlEvaluator evaluator, Vertex v1, DFA dfa, Vertex v2) {
		de.uni_koblenz.jgralab.greql.types.PathSystem ps = (de.uni_koblenz.jgralab.greql.types.PathSystem) FunLib
				.apply("pathSystem", evaluator, v1, dfa);
		return ps.extractPath(v2);
	}
}
