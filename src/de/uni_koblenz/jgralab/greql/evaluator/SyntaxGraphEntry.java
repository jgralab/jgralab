/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2014 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 *
 * For bug reports, documentation and further information, visit
 *
 *                         https://github.com/jgralab/jgralab
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 3 of the License, or (at your
 * option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see <http://www.gnu.org/licenses>.
 *
 * Additional permission under GNU GPL version 3 section 7
 *
 * If you modify this Program, or any covered work, by linking or combining
 * it with Eclipse (or a modified version of that program or an Eclipse
 * plugin), containing parts covered by the terms of the Eclipse Public
 * License (EPL), the licensors of this Program grant you additional
 * permission to convey the resulting work.  Corresponding Source for a
 * non-source form of such a combination shall include the source code for
 * the parts of JGraLab used as well as that of the covered work.
 */

package de.uni_koblenz.jgralab.greql.evaluator;

import java.io.File;
import java.util.logging.Logger;

import de.uni_koblenz.jgralab.exception.GraphIOException;
import de.uni_koblenz.jgralab.greql.OptimizerInfo;
import de.uni_koblenz.jgralab.greql.optimizer.Optimizer;
import de.uni_koblenz.jgralab.greql.schema.GreqlExpression;
import de.uni_koblenz.jgralab.greql.schema.GreqlGraph;
import de.uni_koblenz.jgralab.greql.schema.GreqlSchema;

/**
 * This class is one entry in the Map from Query+CostModel+Optimizer to
 * List<OptimizedSyntaxGraph + UsedFlag>
 */
public class SyntaxGraphEntry {

	/**
	 * the SyntaxGraph this entry represents
	 */
	private final GreqlGraph syntaxGraph;

	/**
	 * @return the SyntaxGraph this entry represents
	 */
	public GreqlGraph getSyntaxGraph() {
		return syntaxGraph;
	}

	private static Logger logger = Logger.getLogger(SyntaxGraphEntry.class
			.getName());

	/**
	 * the optimizer that ist used to optimize this syntaxgraph
	 */
	private Optimizer optimizer;

	/**
	 * The GReQL2 query in text form.
	 */
	private String queryText;

	/**
	 * @return the optimzer that is used to optimize this syntaxgraph
	 */
	public Optimizer getOptimizer() {
		return optimizer;
	}

	/**
	 * Creates a new SyntaxGraphEntry
	 * 
	 * @param queryText
	 *            the GReQL query
	 * @param graph
	 *            the GReQL Syntaxgraph to store in this entry
	 * @param optimizer
	 *            the Optimizer that was used to optimize this syntaxgraph
	 */
	public SyntaxGraphEntry(String queryText, GreqlGraph graph,
			Optimizer optimizer) {
		this.queryText = queryText;
		syntaxGraph = graph;
		this.optimizer = optimizer;
	}

	/**
	 * Load a {@link SyntaxGraphEntry} from the given file.
	 * 
	 * @param fileName
	 *            the tg file where the {@link SyntaxGraphEntry} should be
	 *            loaded from
	 * @throws GraphIOException
	 *             if an error occurs while loading the {@link SyntaxGraphEntry}
	 *             from the given file
	 * @throws ClassNotFoundException
	 *             if the {@link SyntaxGraphEntry} saved in the file uses an
	 *             unknown {@link Optimizer} or {@link OptimizerInfo}
	 * @throws IllegalAccessException
	 *             if the {@link Optimizer} or {@link OptimizerInfo} of the
	 *             saved {@link SyntaxGraphEntry} is not accessible for some
	 *             reason
	 * @throws InstantiationException
	 *             if instantiation of the file's {@link SyntaxGraphEntry}
	 *             fails. If that's the case check if their class definition
	 *             contain a constructor with zero parameters.
	 */
	public SyntaxGraphEntry(File fileName) throws GraphIOException {
		syntaxGraph = GreqlSchema.instance().loadGreqlGraph(fileName.getPath());
		GreqlExpression g2e = syntaxGraph.getFirstGreqlExpression();
		try {
			queryText = (String) g2e.getAttribute("_queryText");
			String optimizerClass = (String) g2e.getAttribute("_optimizer");
			if (!optimizerClass.isEmpty()) {
				optimizer = (Optimizer) Class.forName(optimizerClass)
						.newInstance();
			}
			// Now delete the attribute values. They're not needed anymore.
			g2e.setAttribute("_queryText", null);
			g2e.setAttribute("_optimizer", null);
		} catch (Exception e) {
			e.printStackTrace();
			throw new GraphIOException(e.getMessage());
		}
	}

	/**
	 * Save this {@link SyntaxGraphEntry} to the given directory in the form
	 * '"queryText.hashCode()" + ".tg"'.
	 * 
	 * @param directory
	 *            the directory where this {@link SyntaxGraphEntry} should be
	 *            stored
	 * @throws GraphIOException
	 *             if an error occurs while saving this {@link SyntaxGraphEntry}
	 */
	public void saveToDirectory(File directory) throws GraphIOException {
		String optimizerClass = "";
		String optimizerClassSimple = "";

		GreqlExpression g2e = syntaxGraph.getFirstGreqlExpression();
		g2e.set_queryText(queryText);

		if (optimizer != null) {
			optimizerClass = optimizer.getClass().getName();
			optimizerClassSimple = optimizer.getClass().getSimpleName();
		}
		g2e.set_optimizer(optimizerClass);

		String fileName = directory.getPath() + File.separator
				+ queryText.hashCode() + "-" + optimizerClassSimple + ".tg";
		syntaxGraph.save(fileName);
		logger.info("Saved SyntaxGraphEntry to \"" + fileName + "\".");
	}

	/**
	 * This {@link SyntaxGraphEntry} equals the given object, if the given
	 * object's type is {@link SyntaxGraphEntry}, it has the same queryText, and
	 * its optimizer and costModel have the same type this
	 * {@link SyntaxGraphEntry}'s have.
	 * 
	 * (@see java.lang.Object#equals(java.lang.Object))
	 */
	@Override
	public boolean equals(Object o) {
		if (o instanceof SyntaxGraphEntry) {
			SyntaxGraphEntry e = (SyntaxGraphEntry) o;
			return queryText.equals(e.queryText)
					&& optimizer.getClass().equals(e.optimizer.getClass());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return queryText.hashCode() + optimizer.getClass().hashCode();
	}

	public String getQueryText() {
		return queryText;
	}

	@Override
	public String toString() {
		return "{SyntaxGraphEntry@" + syntaxGraph.hashCode() + ":"
				+ optimizer.getClass().getSimpleName() + "}";
	}
}
