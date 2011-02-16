package de.uni_koblenz.jgralabtest.utilities.tg2dot;

import java.io.IOException;

import org.junit.Test;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.ProgressFunction;
import de.uni_koblenz.jgralab.WorkInProgress;
import de.uni_koblenz.jgralab.utilities.tg2dot.Tg2Dot;

@WorkInProgress(responsibleDevelopers = "mmce@uni-koblenz.de", description = "More test have to be included. Every static method should be tested. Additionally the class itself should be tested.")
public class Tg2DotTest {

	@Test
	public void convertGraph() throws GraphIOException {
		Graph g = GraphIO.loadGraphFromFileWithStandardSupport(
				"testit/testgraphs/greqltestgraph.tg", (ProgressFunction) null);

		Tg2Dot.convertGraph(g, "testit/testoutput.dot", false);
	}

	@Test
	public void convertGraph2Svg() throws GraphIOException,
			InterruptedException, IOException {
		Graph g = GraphIO.loadGraphFromFileWithStandardSupport(
				"testit/testgraphs/greqltestgraph.tg", (ProgressFunction) null);

		Tg2Dot.convertGraphToSvg(g, "testit/testoutput.svg", false);
	}

}
