package de.uni_koblenz.jgralab.greql.evaluator;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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

	@Override
	public synchronized Set<String> getVariableNames() {
		return variableMap.keySet();
	}

	@Override
	public synchronized Object getVariable(String name) {
		return variableMap == null ? null : variableMap.get(name);
	}

	@Override
	public synchronized Object setVariable(String varName, Object value) {
		if (variableMap == null) {
			variableMap = new HashMap<>();
		}
		return variableMap.put(varName, value);
	}

	@Override
	public synchronized Object removeVariable(String varName) {
		return variableMap == null ? null : variableMap.remove(varName);
	}
}
