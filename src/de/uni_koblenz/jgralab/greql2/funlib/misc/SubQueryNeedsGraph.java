package de.uni_koblenz.jgralab.greql2.funlib.misc;

import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEnvironment;
import de.uni_koblenz.jgralab.greql2.evaluator.Query;
import de.uni_koblenz.jgralab.greql2.funlib.NeedsGraphArgument;
import de.uni_koblenz.jgralab.greql2.schema.Greql2Expression;
import de.uni_koblenz.jgralab.greql2.schema.IsBoundVarOf;
import de.uni_koblenz.jgralab.greql2.schema.Variable;

@NeedsGraphArgument
public class SubQueryNeedsGraph extends SubQuery {

	public SubQueryNeedsGraph(Query query) {
		super(query);
	}

	public SubQueryNeedsGraph(Query query, long costs, long cardinality,
			double selectivity) {
		super(query, costs, cardinality, selectivity);
	}

	@Override
	protected void initialize(Query query) {
		this.query = query;
		Greql2Expression greql2Expression = query.getQueryGraph()
				.getFirstGreql2Expression();
		parameterNames = new String[greql2Expression.getDegree(IsBoundVarOf.EC,
				EdgeDirection.IN) + 1];
		parameterNames[0] = "datagraph";
		int i = 1;
		for (IsBoundVarOf ibvo : greql2Expression
				.getIsBoundVarOfIncidences(EdgeDirection.IN)) {
			parameterNames[i++] = ((Variable) ibvo.getThat()).get_name();
		}
	}

	@Override
	protected Object evaluateQuery(Object... values) {
		GreqlEnvironment environment = setUsedVariables(1, values);
		return query.evaluate((Graph) values[0], environment);
	}

}
