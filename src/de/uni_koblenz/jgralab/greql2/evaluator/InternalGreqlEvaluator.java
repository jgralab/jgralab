package de.uni_koblenz.jgralab.greql2.evaluator;

import de.uni_koblenz.jgralab.greql2.schema.Greql2Vertex;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;

public interface InternalGreqlEvaluator {
	// TODO [greqlevaluator] create internal interface

	public Object setBoundVariable(String varName, Object value);

	public Object getBoundVariableValue(String varName);

	public Object setLocalEvaluationResult(Greql2Vertex vertex, Object value);

	public Object getLocalEvaluationResult(Greql2Vertex vertex);

	public Object removeLocalEvaluationResult(Greql2Vertex vertex);

	/**
	 * @param name
	 * @return {@link AttributedElementClass} of the datagraph with the name
	 *         <code>name</code>
	 */
	public AttributedElementClass getKnownType(String name);

	public AttributedElementClass getAttributedElementClass(String name);

}
