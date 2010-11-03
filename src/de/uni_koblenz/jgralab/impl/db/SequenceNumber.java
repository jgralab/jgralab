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
package de.uni_koblenz.jgralab.impl.db;

/**
 * Provides meta information on available number space for sequence numbers.
 * 
 * @author ultbreit@uni-koblenz.de
 */
public class SequenceNumber {

	/**
	 * Exponent to set regular distance.
	 */
	public static final long DISTANCE_EXPONENT = 32;

	/**
	 * Regular distance between sequence numbers of elements in a sequence.
	 */
	public static long REGULAR_DISTANCE = (long) Math.pow(2, 32);

	/**
	 * Default start sequence number for first element that is added to a
	 * sequence.
	 */
	public static final long DEFAULT_START_SEQUENCE_NUMBER = 0;

	/**
	 * Smallest available sequence number.
	 */
	public static long MIN_BORDER_OF_NUMBER_SPACE = Long.MIN_VALUE
			+ SequenceNumber.REGULAR_DISTANCE;

	/**
	 * Greatest available sequence number.
	 */
	public static long MAX_BORDER_OF_NUMBER_SPACE = Long.MAX_VALUE
			- SequenceNumber.REGULAR_DISTANCE;
}
