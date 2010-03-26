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

package de.uni_koblenz.jgralab.greql2.jvalue;

import java.util.logging.Logger;

public class JValueTuple extends JValueList {

	private static Logger logger = Logger
			.getLogger(JValueTuple.class.getName());

	/**
	 * Acts as a cache for the hash code value of this set out of performance
	 * considerations. Whenever this set is changed, storedHashCode is set to 0
	 * and gets updated as soon as the <code>hashCode()</code> method is called.
	 */
	private int storedHashCode = 0;

	/**
	 * Returns the hash code value for this tuple. To get the hash code of this
	 * tuple, new hash code values for every element of this multiset are
	 * calculated from a polynomial of 3rd order and finally summed up.
	 * 
	 * @return the hash code value for this tuple
	 */
	@Override
	public int hashCode() {
		if (storedHashCode == 0) {
			int elementHashCode = 0;
			int newHashCode = -1;
			int number = 1;

			for (JValue currentElement : itemList) {
				// System.out.println("Current element is: " + currentElement);
				elementHashCode = currentElement.hashCode();
				newHashCode += -1 + (3 + elementHashCode)
						* (7 + elementHashCode) * (11 + elementHashCode)
						* number * number * number;
				number++;
			}
			storedHashCode = newHashCode + this.getClass().hashCode();
		}
		return storedHashCode;
	}

	/**
	 * creates a new empty JValueList
	 */
	public JValueTuple() {
		super();
	}

	/**
	 * creates a new empty JValueList with given size
	 */
	public JValueTuple(int size) {
		super(size);
	}

	/**
	 * creates a new JValueList which contains all elements in the given
	 * collection
	 */
	public JValueTuple(JValueCollection collection) {
		super(collection);
	}

	/**
	 * returns true
	 */
	@Override
	public boolean isJValueTuple() {
		return true;
	}

	/**
	 * returns a reference to this tuple
	 */
	@Override
	public JValueTuple toJValueTuple() {
		return this;
	}

	/**
	 * adds a JValue to the collection
	 * 
	 * @param jValue
	 *            the JValue to be added
	 * @return true if successfull, false otherwise
	 */
	@Override
	public boolean add(JValue jValue) {
		if (jValue == null) {
			throw new RuntimeException("Nullllllllllllllllll");
		}
		storedHashCode = 0;
		return itemList.add(jValue);
	}

	/**
	 * the insertion of elements is not allowed in a tupel
	 * 
	 * @return false
	 */
	@Override
	public boolean insert(int position, JValue element) {
		storedHashCode = 0;
		logger.severe("JValueTupel doesn't support insertion  of elements");
		return false;
	}

	/**
	 * It's not allowed to remove an element from a tuple.
	 * 
	 * @throws UnsupportedOperationException
	 */
	@Override
	public boolean remove(JValue element) {
		throw new UnsupportedOperationException();
	}

	/**
	 * transforms this collection into a JValueList. Beware, the order of the
	 * elements is random, the are _not_ sorted
	 * 
	 * @return a JValueList which contains the same elements as this collecton,
	 *         duplicates won't be eliminated
	 */
	@Override
	public JValueList toJValueList() {
		return new JValueList(this);
	}

	/**
	 * accepts te given visitor to visit this jvalue
	 */
	@Override
	public void accept(JValueVisitor v) {
		v.visitTuple(this);
	}

}
