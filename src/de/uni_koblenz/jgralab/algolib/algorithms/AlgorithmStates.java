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
