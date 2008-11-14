/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2008 Institute for Software Technology
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
