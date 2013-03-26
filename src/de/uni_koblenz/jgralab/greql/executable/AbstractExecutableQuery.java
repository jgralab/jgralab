package de.uni_koblenz.jgralab.greql.executable;

import java.util.BitSet;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.ProgressFunction;
import de.uni_koblenz.jgralab.greql.GreqlEnvironment;
import de.uni_koblenz.jgralab.greql.GreqlQuery;
import de.uni_koblenz.jgralab.greql.evaluator.GreqlEnvironmentAdapter;
import de.uni_koblenz.jgralab.greql.schema.Direction;
import de.uni_koblenz.jgralab.greql.schema.GReQLDirection;
import de.uni_koblenz.jgralab.greql.schema.GreqlExpression;
import de.uni_koblenz.jgralab.greql.schema.GreqlGraph;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.IncidenceClass;
import de.uni_koblenz.jgralab.schema.Schema;

/**
 * Abstract base class for all executable queries. Provides some common
 * infrastructure methods.
 * 
 * @author dbildh
 * 
 */
public abstract class AbstractExecutableQuery extends GreqlQuery implements
		ExecutableQuery {

	protected static boolean checkDirection(Edge edge, Direction dir) {
		if (dir.get_dirValue().equals(GReQLDirection.IN)) {
			return edge.isNormal();
		} else {
			return !edge.isNormal();
		}
	}

	protected BitSet bindIncidenceClassesToSchema(Schema schema,
			String[] edgeClasses, boolean[] fromOrTo) {
		assert edgeClasses.length == fromOrTo.length;
		BitSet result = new BitSet();
		for (int i = 0; i < edgeClasses.length; i++) {
			EdgeClass edgeClass = schema.getGraphClass().getEdgeClass(
					edgeClasses[i]);
			IncidenceClass ic = fromOrTo[i] ? edgeClass.getFrom() : edgeClass
					.getTo();
			result.set(ic.getIncidenceClassIdInSchema());
		}
		return result;
	}

	@Override
	public Object execute(Graph datagraph) {
		return execute(datagraph, new GreqlEnvironmentAdapter());
	}

	@Override
	public GreqlGraph getQueryGraph() {
		throw new UnsupportedOperationException(
				"This method is not available for generated queries.");
	}

	@Override
	public GreqlExpression getRootExpression() {
		throw new UnsupportedOperationException(
				"This method is not available for generated queries.");
	}

	@Override
	public Object evaluate() {
		return execute(null);
	}

	@Override
	public Object evaluate(Graph datagraph) {
		return execute(datagraph);
	}

	@Override
	public Object evaluate(Graph datagraph, GreqlEnvironment environment) {
		return execute(datagraph, environment);
	}

	@Override
	public Object evaluate(Graph datagraph, ProgressFunction progressFunction) {
		if (progressFunction != null) {
			throw new UnsupportedOperationException(
					"This method is not available for generated queries.");
		}
		return execute(datagraph);
	}

	@Override
	public Object evaluate(Graph datagraph, GreqlEnvironment environment,
			ProgressFunction progressFunction) {
		if (progressFunction != null) {
			throw new UnsupportedOperationException(
					"This method is not available for generated queries.");
		}
		return execute(datagraph, environment);
	}

}
