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
 
package de.uni_koblenz.jgralab;

/**
 * can be implemented by a user to build his/her own progress function,
 * example: jgralab.impl.ProgressFunctionImpl.java
 * @author Steffen Kahle
 */
public interface ProgressFunction {
	
	/**
	 * method for initialization of progress function
	 * @param steps number of total elements which are to be processed
	 */
	public void init(int steps);
	
	/**
	 * method which is called every interval
	 * @param progress the current number of element which have been processed
	 */
	public void progress(int progress);
	
	/**
	 * method to finish the progress bar
	 */
	public void finished();
	
	/**
	 * @return the interval how often the progress bar is being executed
	 */
	public int getInterval();

}
