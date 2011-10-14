package de.uni_koblenz.jgralab.gretl;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.gretl.Context.TransformationPhase;
import de.uni_koblenz.jgralab.gretl.parser.TokenTypes;
import de.uni_koblenz.jgralab.impl.ConsoleProgressFunction;

public class AddSourceGraph extends Transformation<Graph> {
	private String alias = null;
	private String graphFile = null;

	public AddSourceGraph(String alias, String graphFileName) {
		this.alias = alias;
		graphFile = graphFileName;
	}

	public AddSourceGraph(String graphFileName) {
		this(null, graphFileName);
	}

	public static AddSourceGraph parseAndCreate(ExecuteTransformation et) {
		String alias = null;
		if (et.tryMatchGraphAlias()) {
			alias = et.matchGraphAlias();
		}
		String graphFile = et.match(TokenTypes.STRING).value;
		return new AddSourceGraph(alias, graphFile);
	}

	@Override
	protected Graph transform() {
		if (context.phase == TransformationPhase.SCHEMA) {
			return null;
		}
		Graph g;
		try {
			g = GraphIO.loadGraphFromFileWithStandardSupport(graphFile,
					new ConsoleProgressFunction());
		} catch (Exception e) {
			throw new GReTLException(
					"Something went wrong loading source graph from "
							+ graphFile, e);
		}
		if (alias == null) {
			context.setSourceGraph(g);
		} else {
			context.addSourceGraph(alias, g);
		}
		return g;
	}

}
