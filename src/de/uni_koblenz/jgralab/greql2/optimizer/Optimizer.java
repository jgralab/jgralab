/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2007 Institute for Software Technology
 *               University of Koblenz-Landau, Germany
 *
 *               ist@uni-koblenz.de
 *
 * Please report bugs to http://serres.uni-koblenz.de/bugzilla
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package de.uni_koblenz.jgralab.greql2.optimizer;

import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.exception.OptimizerException;
import de.uni_koblenz.jgralab.greql2.schema.*;

/**
 * This interface should be implemented by all optimizers, that could be used
 * with GReQL 2.
 * 
 * @author Daniel Bildhauer <dbildh@uni-koblenz.de> Summer 2006, Diploma Thesis
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
