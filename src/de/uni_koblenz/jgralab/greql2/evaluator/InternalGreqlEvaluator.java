package de.uni_koblenz.jgralab.greql2.evaluator;

public interface InternalGreqlEvaluator {
	// TODO [greqlevaluator] create internal interface

	public Object setGlobalVariable(String varName, Object value);

	public Object getGolabalVariableValue(String varName);

	public Object setLocalVariable(String varName, Object value);

	public Object getLocalVariableValue(String varName);

}
