/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2011 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 * 
 * For bug reports, documentation and further information, visit
 * 
 *                         http://jgralab.uni-koblenz.de
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

package de.uni_koblenz.jgralab.greql2.optimizer;

import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.exception.OptimizerException;
import de.uni_koblenz.jgralab.greql2.schema.Greql2;

/**
 * This interface should be implemented by all optimizers, that could be used
 * with GReQL 2.
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public interface Optimizer {

	/**
	 * Optimizes the given GReQL 2 syntaxgraph. The given syntaxgraph is
	 * optimized after the method finished.
	 * 
	 * @param eval
	 *            the GreqlEvaluator, which calls this method
	 * @param syntaxgraph
	 *            The GReQL 2 syntaxgraph to optimize
	 * @return <code>true</code> if a transformation was done,
	 *         <code>false</code> if this {@link Optimizer} couldn't do
	 *         anything.
	 * @throws OptimizerException
	 *             on failures while optimization
	 */
	public boolean optimize(GreqlEvaluator eval, Greql2 syntaxgraph)
			throws OptimizerException;

	/**
	 * @return true, if this optimizer and the given one are logical equivalent,
	 *         that means, if the optimization result will be the same
	 */
	public boolean isEquivalent(Optimizer optimizer);

}
