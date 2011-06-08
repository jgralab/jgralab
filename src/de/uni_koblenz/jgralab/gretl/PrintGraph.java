package de.uni_koblenz.jgralab.gretl;

import java.io.File;
import java.io.IOException;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.gretl.parser.TokenTypes;
import de.uni_koblenz.jgralab.utilities.tg2dot.Tg2Dot;
import de.uni_koblenz.jgralab.utilities.tg2dot.dot.GraphVizOutputFormat;

public class PrintGraph extends CountingTransformation {

	private File file;
	private Graph graph;

	protected PrintGraph(Context context, Graph g, File f) {
		super(context);
		this.file = f;
		this.graph = g;
	}

	public static PrintGraph parseAndCreate(ExecuteTransformation et) {
		String alias = Context.DEFAULT_SOURCE_GRAPH_ALIAS;
		if (et.tryMatchGraphAlias()) {
			alias = et.matchGraphAlias();
		}
		Graph g = null;
		if (alias.equals(Context.DEFAULT_TARGET_GRAPH_ALIAS)) {
			g = et.context.getTargetGraph();
		} else {
			g = et.context.getSourceGraph(alias);
		}
		File f = new File(et.match(TokenTypes.STRING).value);
		return new PrintGraph(et.context, g, f);
	}

	@Override
	protected Integer transform() {
		try {
			Tg2Dot.convertGraph(graph, file.getAbsolutePath(),
					GraphVizOutputFormat.PDF);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0;
	}

}
