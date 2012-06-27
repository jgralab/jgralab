package de.uni_koblenz.jgralab.greql.executable;

import java.util.Map;

import de.uni_koblenz.jgralab.Graph;

public interface ExecutableQuery {

	
	public Object execute(Graph datagraph);
	
	public Object execute(Graph datagraph, Map<String, Object> boundVariables);
	
}
