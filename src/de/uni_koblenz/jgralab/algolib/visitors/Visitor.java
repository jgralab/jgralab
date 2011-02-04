/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2010 Institute for Software Technology
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
