package de.uni_koblenz.jgralab.utilities;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.utilities.tg2dot.Tg2Dot;

/**
 * Some utility functions that come in handy when developing with JGraLab and
 * GReQL2.
 * 
 * @author Tassilo Horn (heimdall), 2007, Diploma Thesis
 * 
 */
public class Utility {

	/**
	 * Write the given {@link Graph} to the dot file specified by the given
	 * filename.
	 * 
	 * @param q
	 *            the {@link Graph}
	 * @param outfile
	 *            a string naming a file
	 */
	public static void convertGraphToDot(Graph q, String outfile) {
		Tg2Dot converter = new Tg2Dot();
		converter.setGraph(q);
		converter.setOutputFile(outfile);
		converter.setPrintEdgeAttributes(true);
		converter.setPrintReversedEdges(true);
		converter.setPrintRoleNames(true);
		converter.setShortenStrings(false);
		converter.setAbbreviateAttributeNames(true);
		converter.setRanksep(3.5);
		converter.setNodesep(0.2);
		converter.setFontsize(16);
		converter.printGraph();
		System.out.println("Written .dot file \"" + outfile + "\".");
	}
}
