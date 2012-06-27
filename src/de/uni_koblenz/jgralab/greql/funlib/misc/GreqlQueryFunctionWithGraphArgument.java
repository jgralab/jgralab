package de.uni_koblenz.jgralab.greql.funlib.misc;

import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.greql.GreqlEnvironment;
import de.uni_koblenz.jgralab.greql.GreqlQuery;
import de.uni_koblenz.jgralab.greql.funlib.NeedsGraphArgument;
import de.uni_koblenz.jgralab.greql.schema.Greql2Expression;
import de.uni_koblenz.jgralab.greql.schema.IsBoundVarOf;
import de.uni_koblenz.jgralab.greql.schema.Variable;

@NeedsGraphArgument
public class GreqlQueryFunctionWithGraphArgument extends GreqlQueryFunction {

	public GreqlQueryFunctionWithGraphArgument(GreqlQuery query) {
		super(query);
	}

	public GreqlQueryFunctionWithGraphArgument(GreqlQuery query, long costs, long cardinality,
			double selectivity) {
		super(query, costs, cardinality, selectivity);
	}

	@Override
	protected void initialize(GreqlQuery query) {
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
