package de.uni_koblenz.jgralab.greql2.evaluator;

import java.lang.ref.SoftReference;

import de.uni_koblenz.jgralab.greql2.parser.GreqlParser;
import de.uni_koblenz.jgralab.greql2.schema.Greql2Graph;

public class Query {
	private final String queryText;
	private SoftReference<Greql2Graph> queryGraph;

	public Query(String queryText) {
		this(queryText, false);
	}

	public Query(String queryText, boolean optimize) {
		this.queryText = queryText;
	}

	public Greql2Graph getQueryGraph() {
		Greql2Graph result = queryGraph.get();
		if (queryGraph == null || result == null) {
			result = GreqlParser.parse(queryText);
			queryGraph = new SoftReference<Greql2Graph>(result);
		}
		return result;
	}

	public String getQueryText() {
		return queryText;
	}
}
