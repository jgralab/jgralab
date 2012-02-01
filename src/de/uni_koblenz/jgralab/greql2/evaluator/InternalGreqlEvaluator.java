package de.uni_koblenz.jgralab.greql2.evaluator;

import de.uni_koblenz.jgralab.greql2.schema.Greql2Vertex;

public interface InternalGreqlEvaluator {
	// TODO [greqlevaluator] create internal interface

	public Object setGlobalVariable(String varName, Object value);

	public Object getGlobalVariableValue(String varName);

	public Object setLocalEvaluationResult(Greql2Vertex vertex, Object value);

	public Object getLocalEvaluationResult(Greql2Vertex vertex);

	public Object removeLocalEvaluationResult(Greql2Vertex vertex);

}
