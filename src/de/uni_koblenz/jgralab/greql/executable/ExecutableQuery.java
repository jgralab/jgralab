package de.uni_koblenz.jgralab.greql.executable;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.greql.GreqlEnvironment;

public interface ExecutableQuery {

	public Object execute(Graph datagraph);

	public Object execute(Graph datagraph, GreqlEnvironment environment);

}
