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
