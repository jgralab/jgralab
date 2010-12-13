/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2010 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
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

package de.uni_koblenz.jgralab;

/**
 * can be implemented by a user to build his/her own progress function, example:
 * jgralab.impl.ProgressFunctionImpl.java
 * 
 * @author ist@uni-koblenz.de
 */
public interface ProgressFunction {

	/**
	 * method for initialization of progress function
	 * 
	 * @param totalElements
	 *            total number of elements which are to be processed
	 */
	public void init(long totalElements);

	/**
	 * Called during processing elements.
	 * 
	 * @param processedElements
	 *            the current number of elements which have been processed
	 */
	public void progress(long processedElements);

	/**
	 * Called after completion of all elements.
	 */
	public void finished();

	/**
	 * Specifies the number of processed elements after which a call to
	 * progress() occurs.
	 * 
	 * @return the interval (number of processed elements) which specifies how
	 *         often the progress bar is updated
	 */
	public long getUpdateInterval();

}
