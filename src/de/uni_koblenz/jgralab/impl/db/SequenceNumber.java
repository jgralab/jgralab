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
