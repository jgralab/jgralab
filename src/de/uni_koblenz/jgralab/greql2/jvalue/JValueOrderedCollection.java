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

/**
 * This class is base for all ordered JValue-collections like JValueList oder
 * JValueTupel. Beware, ordered is not the same as sorted!
 * 
 * @author ist@uni-koblenz.de
 * 
 */
abstract public class JValueOrderedCollection extends JValueCollection {

	/**
	 * @return true if this collection is ordered
	 */
	@Override
	public boolean isOrderedCollection() {
		return true;
	}

	/**
	 * inserts a JValue at the given position into the collection
	 * 
	 * @param index
	 *            the osition where the element should be inserted
	 * @param element
	 *            the JValue to be inserted
	 * @return true if successfull, false otherwise
	 */
	abstract public boolean insert(int index, JValue element);

	/**
	 * replaces the element at position index with the given newElement
	 * 
	 * @param index
	 *            the position of the element which should be replaced
	 * @param newElement
	 *            the element which should replace the old one
	 * @return true if successfull, false otherwise
	 */
	abstract public boolean replace(int index, JValue newElement);

	/**
	 * @return the index of the given element in this collectin or -1 if the
	 *         collection doesn't contain the element
	 */
	abstract public int indexOf(JValue element);

	/**
	 * returns the element at position index
	 */
	abstract public JValue get(int index);

	/**
	 * replaces the old element the given newElement
	 * 
	 * @param oldElement
	 *            the element which should be replaced
	 * @param newElement
	 *            the element which should replace the old one
	 * @return true if successfull, false otherwise
	 */
	public boolean replace(JValue oldElement, JValue newElement) {
		int index = indexOf(oldElement);
		if (index >= 0) {
			replace(index, newElement);
			return true;
		}
		return false;
	}
}
