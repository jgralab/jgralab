package de.uni_koblenz.jgralab.greql.evaluator;

import java.util.HashMap;
import java.util.Map;

import de.uni_koblenz.jgralab.greql.GreqlEnvironment;

public class GreqlEnvironmentAdapter implements GreqlEnvironment {

	/**
	 * Holds the variables that are defined via using, they are called bound or
	 * free variables
	 */
	private Map<String, Object> variableMap;

	public GreqlEnvironmentAdapter() {
		this(new HashMap<String, Object>());
	}

	public GreqlEnvironmentAdapter(Map<String, Object> greqlMapping) {
		variableMap = greqlMapping;
	}

	/**
	 * returns the changes variableMap
	 */
	@Override
	public synchronized Map<String, Object> getVariables() {
		return variableMap;
	}

	@Override
	public synchronized Object getVariable(String name) {
		return variableMap == null ? null : variableMap.get(name);
	}

	@Override
	public synchronized void setVariables(Map<String, Object> varMap) {
		variableMap = varMap;
	}

	@Override
	public synchronized Object setVariable(String varName, Object value) {
		if (variableMap == null) {
			variableMap = new HashMap<String, Object>();
		}
		return variableMap.put(varName, value);
	}

	@Override
	public synchronized Object removeVariable(String varName) {
		return variableMap == null ? null : variableMap.remove(varName);
	}

}
