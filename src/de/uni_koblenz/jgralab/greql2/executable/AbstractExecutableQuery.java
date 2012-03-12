package de.uni_koblenz.jgralab.greql2.executable;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.greql2.schema.Direction;

/**
 * Abstract base class for all executable queries. Provides some
 * common infrastructure methods.
 * @author dbildh
 *
 */
public abstract class AbstractExecutableQuery implements ExecutableQuery {

	protected static boolean checkDirection(Edge edge, Direction dir) {
		if (dir.get_dirValue().equals("IN")) {
			return edge.isNormal();
		} else {
			return !edge.isNormal();
		}
	}
	
	@Override
	public Object execute(Graph datagraph) {
		return execute(datagraph, null);
	}
	
	
	
	
	
}
