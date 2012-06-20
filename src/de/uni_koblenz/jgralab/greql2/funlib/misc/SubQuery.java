package de.uni_koblenz.jgralab.greql2.funlib.misc;

import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEnvironment;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEnvironmentAdapter;
import de.uni_koblenz.jgralab.greql2.evaluator.Query;
import de.uni_koblenz.jgralab.greql2.exception.GreqlException;
import de.uni_koblenz.jgralab.greql2.funlib.Function;
import de.uni_koblenz.jgralab.greql2.schema.Greql2Expression;
import de.uni_koblenz.jgralab.greql2.schema.IsBoundVarOf;
import de.uni_koblenz.jgralab.greql2.schema.Variable;

public class SubQuery extends Function {

	protected Query query;

	protected String[] parameterNames;

	public SubQuery(Query query) {
		super();
		initialize(query);
	}

	public SubQuery(Query query, long costs, long cardinality,
			double selectivity) {
		super(costs, cardinality, selectivity);
		initialize(query);
	}

	protected void initialize(Query query) {
		this.query = query;
		Greql2Expression greql2Expression = query.getQueryGraph()
				.getFirstGreql2Expression();
		parameterNames = new String[greql2Expression.getDegree(IsBoundVarOf.EC,
				EdgeDirection.IN)];
		int i = 0;
		for (IsBoundVarOf ibvo : greql2Expression
				.getIsBoundVarOfIncidences(EdgeDirection.IN)) {
			parameterNames[i++] = ((Variable) ibvo.getThat()).get_name();
		}
	}

	protected Object evaluateQuery(Object... values) {
		GreqlEnvironment environment = setUsedVariables(0, values);
		return query.evaluate(null, environment);
	}

	protected GreqlEnvironment setUsedVariables(int startIndex, Object[] values) {
		if (values.length != parameterNames.length) {
			StringBuilder sb = new StringBuilder();
			String delim = "";
			for (String name : parameterNames) {
				sb.append(delim).append(name);
				delim = ", ";
			}
			throw new GreqlException("There are " + values.length
					+ " values given but only " + parameterNames.length
					+ " parameters:\n" + sb.toString());
		}
		GreqlEnvironment environment = new GreqlEnvironmentAdapter();
		for (int i = startIndex; i < values.length; i++) {
			environment.setVariable(parameterNames[i], values[i]);
		}
		return environment;
	}

	public Object evaluate() {
		return evaluateQuery();
	}

	public Object evaluate(Object o1) {
		return evaluateQuery(o1);
	}

	public Object evaluate(Object o1, Object o2) {
		return evaluateQuery(o1, o2);
	}

	public Object evaluate(Object o1, Object o2, Object o3) {
		return evaluateQuery(o1, o2, o3);
	}

	public Object evaluate(Object o1, Object o2, Object o3, Object o4) {
		return evaluateQuery(o1, o2, o3, o4);
	}

	public Object evaluate(Object o1, Object o2, Object o3, Object o4, Object o5) {
		return evaluateQuery(o1, o2, o3, o4, o5);
	}

	public Object evaluate(Object o1, Object o2, Object o3, Object o4,
			Object o5, Object o6) {
		return evaluateQuery(o1, o2, o3, o4, o5, o6);
	}

	public Object evaluate(Object o1, Object o2, Object o3, Object o4,
			Object o5, Object o6, Object o7) {
		return evaluateQuery(o1, o2, o3, o4, o5, o6, o7);
	}

	public Object evaluate(Object o1, Object o2, Object o3, Object o4,
			Object o5, Object o6, Object o7, Object o8) {
		return evaluateQuery(o1, o2, o3, o4, o5, o6, o7, o8);
	}

	public Object evaluate(Object o1, Object o2, Object o3, Object o4,
			Object o5, Object o6, Object o7, Object o8, Object o9) {
		return evaluateQuery(o1, o2, o3, o4, o5, o6, o7, o8, o9);
	}

	public Object evaluate(Object o1, Object o2, Object o3, Object o4,
			Object o5, Object o6, Object o7, Object o8, Object o9, Object o10) {
		return evaluateQuery(o1, o2, o3, o4, o5, o6, o7, o8, o9, o10);
	}

	public Object evaluate(Object o1, Object o2, Object o3, Object o4,
			Object o5, Object o6, Object o7, Object o8, Object o9, Object o10,
			Object o11) {
		return evaluateQuery(o1, o2, o3, o4, o5, o6, o7, o8, o9, o10, o11);
	}

	public Object evaluate(Object o1, Object o2, Object o3, Object o4,
			Object o5, Object o6, Object o7, Object o8, Object o9, Object o10,
			Object o11, Object o12) {
		return evaluateQuery(o1, o2, o3, o4, o5, o6, o7, o8, o9, o10, o11, o12);
	}

	public Object evaluate(Object o1, Object o2, Object o3, Object o4,
			Object o5, Object o6, Object o7, Object o8, Object o9, Object o10,
			Object o11, Object o12, Object o13) {
		return evaluateQuery(o1, o2, o3, o4, o5, o6, o7, o8, o9, o10, o11, o12,
				o13);
	}

	public Object evaluate(Object o1, Object o2, Object o3, Object o4,
			Object o5, Object o6, Object o7, Object o8, Object o9, Object o10,
			Object o11, Object o12, Object o13, Object o14) {
		return evaluateQuery(o1, o2, o3, o4, o5, o6, o7, o8, o9, o10, o11, o12,
				o13, o14);
	}

	public Object evaluate(Object o1, Object o2, Object o3, Object o4,
			Object o5, Object o6, Object o7, Object o8, Object o9, Object o10,
			Object o11, Object o12, Object o13, Object o14, Object o15) {
		return evaluateQuery(o1, o2, o3, o4, o5, o6, o7, o8, o9, o10, o11, o12,
				o13, o14, o15);
	}

	public Object evaluate(Object o1, Object o2, Object o3, Object o4,
			Object o5, Object o6, Object o7, Object o8, Object o9, Object o10,
			Object o11, Object o12, Object o13, Object o14, Object o15,
			Object o16) {
		return evaluateQuery(o1, o2, o3, o4, o5, o6, o7, o8, o9, o10, o11, o12,
				o13, o14, o15, o16);
	}

	public Object evaluate(Object o1, Object o2, Object o3, Object o4,
			Object o5, Object o6, Object o7, Object o8, Object o9, Object o10,
			Object o11, Object o12, Object o13, Object o14, Object o15,
			Object o16, Object o17) {
		return evaluateQuery(o1, o2, o3, o4, o5, o6, o7, o8, o9, o10, o11, o12,
				o13, o14, o15, o16, o17);
	}

	public Object evaluate(Object o1, Object o2, Object o3, Object o4,
			Object o5, Object o6, Object o7, Object o8, Object o9, Object o10,
			Object o11, Object o12, Object o13, Object o14, Object o15,
			Object o16, Object o17, Object o18) {
		return evaluateQuery(o1, o2, o3, o4, o5, o6, o7, o8, o9, o10, o11, o12,
				o13, o14, o15, o16, o17, o18);
	}
}
