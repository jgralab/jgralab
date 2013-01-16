/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2013 Institute for Software Technology
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

package de.uni_koblenz.jgralab.greql.optimizer;

import de.uni_koblenz.jgralab.greql.GreqlQuery;
import de.uni_koblenz.jgralab.greql.OptimizerInfo;
import de.uni_koblenz.jgralab.greql.exception.OptimizerException;

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
	 * @param query
	 *            the current {@link GreqlQuery}
	 * @return <code>true</code> if a transformation was done,
	 *         <code>false</code> if this {@link Optimizer} couldn't do
	 *         anything.
	 * @throws OptimizerException
	 *             on failures while optimization
	 */
	public boolean optimize(GreqlQuery query) throws OptimizerException;

	/**
	 * @return true, if this optimizer and the given one are logical equivalent,
	 *         that means, if the optimization result will be the same
	 */
	public boolean isEquivalent(Optimizer optimizer);

	/**
	 * @return the {@link OptimizerInfo} of this Optimizer.
	 */
	public OptimizerInfo getOptimizerInfo();
}
