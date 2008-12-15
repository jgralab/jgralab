/**
 *
 */
package de.uni_koblenz.jgralab.greql2.optimizer;

import java.io.File;
import java.io.IOException;

import de.uni_koblenz.jgralab.greql2.schema.Greql2;

/**
 * Base class for all {@link Optimizer}s which defines some useful methods that
 * are needed in derived Classes.
 *
 * @author ist@uni-koblenz.de
 *
 */
public abstract class OptimizerBase implements Optimizer {

	protected String optimizerHeaderString() {
		return "*** " + this.getClass().getSimpleName() + ": ";
	}

	/**
	 * Print <code>graph</code> as dot-file with name <code>tmpFileName</code>
	 * as temporary file in /tmp/ (or where your systems tempdir is...)
	 *
	 * @param graph
	 * @param tmpFileName
	 */
	protected void printGraphAsDot(Greql2 graph, String tmpFileName) {
		try {
			de.uni_koblenz.jgralab.utilities.Utility.convertGraphToDot(graph,
					File.createTempFile(tmpFileName, ".dot").getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
