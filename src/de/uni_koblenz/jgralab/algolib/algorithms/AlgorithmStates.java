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
package de.uni_koblenz.jgralab.algolib.algorithms;

public enum AlgorithmStates {
	/**
	 * In this state parameters may be changed and the algorithm can be started.
	 */
	INITIALIZED,

	/**
	 * In this state, no changes may be done to the algorithm. If it is
	 * supported, it can be interrupted using the method
	 * <code>Thread.interrupt()</code>.
	 */
	RUNNING,

	/**
	 * In this state the results can be obtained from the algorithm. For reusing
	 * the algorithm object, the method <code>reset</code> has to be called for
	 * reentering the state <code>INITIALIZED</code>.
	 */
	FINISHED,

	/**
	 * In this state the results can be obtained. Also a resuming of the
	 * algorithm with other parameters is possible. The results from the
	 * previous runs are used for the next run. It depends on the algorithm if
	 * this feature is possible, feasible and how it is exploited.
	 */
	STOPPED,

	/**
	 * In this state, the algorithm has been interrupted. No changes to
	 * parameters and no retrieval of results is possible. For reusing the
	 * algorithm object, the method <code>reset</code> has to be called for
	 * reentering the state <code>INITIALIZED</code>.
	 */
	CANCELED;
}
