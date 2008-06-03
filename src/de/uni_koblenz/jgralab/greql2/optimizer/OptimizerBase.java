/**
 * 
 */
package de.uni_koblenz.jgralab.greql2.optimizer;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.schema.Greql2;
import de.uni_koblenz.jgralab.greql2.schema.IsDeclaredVarOf;
import de.uni_koblenz.jgralab.greql2.schema.Variable;

/**
 * Base class for all {@link Optimizer}s which defines some useful methods that
 * are needed in derived Classes.
 * 
 * @author Tassilo Horn (heimdall), 2007, Diploma Thesis
 * 
 */
public abstract class OptimizerBase implements Optimizer {

	String optimizerHeaderString() {
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

	/**
	 * Collect the {@link Variable}s that have no outgoing
	 * {@link IsDeclaredVarOf} edges and are located below <code>v</code>.
	 * 
	 * @param vertex
	 *            the root {@link Vertex} below which to look for undeclared
	 *            {@link Variable}s
	 * @return a {@link Set} of {@link Variable}s that have no outgoing
	 *         {@link IsDeclaredVarOf} edges and are located below
	 *         <code>v</code>
	 */
	protected Set<Variable> collectUndeclaredVariablesBelow(Vertex vertex) {
		// System.out.println("collectUndeclaredVariablesBelow(" + vertex +
		// ")");
		HashSet<Variable> undeclaredVars = new HashSet<Variable>();
		for (Variable var : OptimizerUtility.collectVariablesBelow(vertex)) {
			if (var.getFirstIsDeclaredVarOf(EdgeDirection.OUT) == null) {
				undeclaredVars.add(var);
			}
		}
		return undeclaredVars;
	}
}
