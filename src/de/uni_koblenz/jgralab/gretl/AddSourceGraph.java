package de.uni_koblenz.jgralab.gretl;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.gretl.parser.TokenTypes;
import de.uni_koblenz.jgralab.impl.ConsoleProgressFunction;

public class AddSourceGraph extends Transformation<Graph> {
	private String alias = null;
	private String graphFile = null;

	public AddSourceGraph(Context c, String alias, String graphFileName) {
		super(c);
		this.alias = alias;
		graphFile = graphFileName;
	}

	public AddSourceGraph(Context c, String graphFileName) {
		this(c, null, graphFileName);
	}

	public static AddSourceGraph parseAndCreate(ExecuteTransformation et) {
		String alias = null;
		if (et.tryMatchGraphAlias()) {
			alias = et.matchGraphAlias();
			// System.out.println("Matched alias " + alias);
		}
		String graphFile = et.match(TokenTypes.STRING).value;
		// System.out.println("Matched file " + graphFile);
		return new AddSourceGraph(et.context, alias, graphFile);
	}

	@Override
	protected Graph transform() {
		if (alias != null) {
			if (context.getSourceGraph(alias) != null) {
				return context.getSourceGraph(alias);
			}
		} else if (context.getSourceGraph() != null) {
			return context.getSourceGraph();
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
