package de.uni_koblenz.jgralab.greql;

import java.util.Map;

public interface GreqlEnvironment {

	/**
	 * returns the changes variableMap
	 */
	public Map<String, Object> getVariables();

	public Object getVariable(String name);

	/**
	 * deletes the previous variables.
	 * 
	 * @param varMap
	 */
	public void setVariables(Map<String, Object> varMap);

	public Object setVariable(String varName, Object value);
}
