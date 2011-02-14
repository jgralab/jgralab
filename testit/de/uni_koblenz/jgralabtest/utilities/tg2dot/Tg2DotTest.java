package de.uni_koblenz.jgralabtest.utilities.tg2dot;

import org.junit.Test;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.ProgressFunction;
import de.uni_koblenz.jgralab.utilities.tg2dot.Tg2Dot;

public class Tg2DotTest {

	@Test
	public void convertGraph() throws GraphIOException {
		Graph g = GraphIO.loadGraphFromFileWithStandardSupport(
				"testit/testgraphs/greqltestgraph.tg", (ProgressFunction) null);

		Tg2Dot.convertGraph(g, "testit/testoutput.dot", false);
	}
}
