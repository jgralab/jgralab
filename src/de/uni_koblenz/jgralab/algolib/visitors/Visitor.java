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
package de.uni_koblenz.jgralab.algolib.visitors;

import de.uni_koblenz.jgralab.algolib.algorithms.GraphAlgorithm;

public interface Visitor {
	
	/**
	 * If this visitor is used for computing results, this function resets this
	 * visitor. This includes reinitializing all variables needed for the
	 * computation of the result. If this visitor does not contain variables
	 * that need to be reinitialized, this method may do nothing.
	 */
	public void reset();

	/**
	 * Sets the graph algorithm this visitor is used by. This method is to be
	 * used for setting a field in the actual visitor object for allowing access
	 * to the intermediate results of the algorithm. This is required by many
	 * visitors for performing their own computations based on the computations
	 * made by the algorithm. The field of implementing visitor classes should
	 * use an explicit algorithm type and this method should perform a type
	 * check. This avoids unnecessary casts when accessing the visitor objects.
	 * 
	 * @param alg
	 *            the algorithm object that uses this visitor
	 * @throws IllegalArgumentException
	 *             if the given algorithm is incompatible with this visitor
	 */
	public void setAlgorithm(GraphAlgorithm alg);
}
