/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2009 Institute for Software Technology
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

import java.util.HashSet;
import java.util.Iterator;

/**
 * JValueSet implements a mathematic set of JValue-Objects. This includes
 * methods for union, difference, intersection etc. It is based on the class
 * <code>AbstractMathSet</code> and <code>HashMathSet</code> from the package
 * MathCollection which was developed in a project at the Institute for
 * Intelligent Systems at the University of Stuttgart
 * (http://www.iis.uni-stuttgart.de) under guidance of Dietmar Lippold
 * (dietmar.lippold@informatik.uni-stuttgart.de).
 * 
 * @author ist@uni-koblenz.de
 */
public class JValueSet extends JValueCollection implements Cloneable {

	/**
	 * A 'wrapper' iterator class that uses <code>HashSet.iterator()</code>
	 * while accounting for the additional <code>storedHashCode</code>
	 * attribute.
	 */
	private class JValueSetIterator implements Iterator<JValue> {
		private Iterator<JValue> myIterator;

		public JValueSetIterator() {
			myIterator = itemHashSet.iterator();
		}

		public boolean hasNext() {
			return myIterator.hasNext();
		}

		public JValue next() {
			return myIterator.next();
		}

		public void remove() {
			storedHashCode = 0;
			myIterator.remove();
		}
	}

	/**
	 * The backing instance of <code>HashSet<code> where the elements of this
	 * set are stored.
	 */
	private HashSet<JValue> itemHashSet;

	/**
	 * Constructs a new, empty mathematical set; the backing
	 * <code>HashSet</code> instance has default initial capacity (16) and load
	 * factor (0.75).
	 */
	public JValueSet() {
		itemHashSet = new HashSet<JValue>();
	}

	/**
	 * Constructs a new mathematical set containing the elements in the
	 * specified collection. The <code>HashSet</code> is created with default
	 * load factor (0.75) and an initial capacity sufficient to contain the
	 * elements in the specified collection.
	 * 
	 * @param collection
	 *            the collection whose elements are to be placed into this set.
	 * @throws NullPointerException
	 *             if the specified collection is null.
	 */
	public JValueSet(JValueCollection collection) {
		itemHashSet = new HashSet<JValue>(collection.size(), 0.75f);
		addAll(collection);
	}

	/**
	 * Constructs a new, empty mathematical set; the backing
	 * <code>HashSet</code> instance has the specified initial capacity and
	 * default load factor, which is 0.75.
	 * 
	 * @param initialCapacity
	 *            the initial capacity of the hash set.
	 * @throws IllegalArgumentException
	 *             if the initial capacity is less than zero.
	 */
	public JValueSet(int initialCapacity) {
		itemHashSet = new HashSet<JValue>(initialCapacity);
	}

	/**
	 * Constructs a new, empty mathematical set; the backing
	 * <code>HashSet</code> instance has the specified initial capacity and the
	 * specified load factor.
	 * 
	 * @param initialCapacity
	 *            the initial capacity of the hash set.
	 * @param loadFactor
	 *            the load factor of the hash set.
	 * @throws IllegalArgumentException
	 *             if the initial capacity is less than zero, or if the load
	 *             factor is nonpositive.
	 */
	public JValueSet(int initialCapacity, float loadFactor) {
		itemHashSet = new HashSet<JValue>(initialCapacity, loadFactor);
	}

	/**
	 * Returns an iterator over the elements in this mathematical set. The
	 * elements are returned in no particular order, this in includes that two
	 * calls of iterator() won't neccessary return the same order of elements.
	 * 
	 * @return an Iterator over the elements in this set.
	 * @see java.util.ConcurrentModificationException
	 */
	@Override
	public Iterator<JValue> iterator() {
		return new JValueSetIterator();
	}

	@Override
	public boolean isJValueSet() {
		return true;
	}

	/**
	 * Returns the hash code value for this jvalue-set. To get the hash code of
	 * this set, new hash code values for every element of this mathematical set
	 * are calculated from a polynomial of 3rd order and finally summed up. This
	 * ensures that <code>s1.equals(s2)</code> implies that
	 * <code>s1.hashCode()==s2.hashCode()</code> for any two mathematical sets
	 * <code>s1</code> and <code>s2</code>, as required by the general contract
	 * of <code>Object.hashCode()</code>.
	 * <p>
	 * 
	 * This implementation first checks whether a cached hash code value is
	 * available. If not (i.e. <code>storedHashCode</code> is zero), the hash
	 * code gets calculated using the algorithm described above.
	 * 
	 * @return the hash code value for this set.
	 */
	@Override
	public int hashCode() {
		if (storedHashCode == 0) {
			int elementHashCode = 0;
			int newHashCode = -1;

			for (JValue value2 : this) {
				elementHashCode = value2.hashCode();
				newHashCode += -1 + (3 + elementHashCode)
						* (7 + elementHashCode) * (11 + elementHashCode);
			}
			storedHashCode = newHashCode + JValueSet.class.hashCode();
		}
		return storedHashCode;
	}

	/**
	 * Returns a shallow copy of this <code>HashMathSet</code> instance: the
	 * elements themselves are not cloned.
	 * 
	 * @return a shallow copy of this set.
	 */
	@Override
	public Object clone() {
		JValueSet copy = new JValueSet(this);
		copy.storedHashCode = this.storedHashCode;
		return copy;
	}

	/**
	 * Returns <code>true</code> if this set contains no elements.
	 * 
	 * @return <code>true</code> if this set contains no elements,
	 *         <code>false</code> otherwise.
	 */
	@Override
	public boolean isEmpty() {
		return itemHashSet.isEmpty();
	}

	/**
	 * Returns the number of elements in this set (its cardinality).
	 * 
	 * @return the number of elements in this set (its cardinality).
	 */
	@Override
	public int size() {
		return itemHashSet.size();
	}

	/**
	 * Removes all elements from this set.
	 */
	@Override
	public void clear() {
		itemHashSet.clear();
		this.storedHashCode = 0;
	}

	/**
	 * Returns <code>true</code> if this set contains the specified element.
	 * 
	 * @param element
	 *            element whose presence in this set is to be tested.
	 * @return <code>true</code> if this set contains the specified element,
	 *         <code>false</code> otherwise.
	 */
	@Override
	public boolean contains(JValue element) {
		return itemHashSet.contains(element);
	}

	/**
	 * Adds the specified element to this set if it is not already present.
	 * <p>
	 * 
	 * If the set gets altered, this implementation sets
	 * <code>storedHashCode</code> to 0 (representing an unavailable hash code
	 * value), which forces <code>hashCode()</code> to recalculate the actual
	 * hash code value.
	 * 
	 * @param element
	 *            element to be added to this set.
	 * @return <code>true</code> if the set did not already contain the
	 *         specified element, <code>false</code> otherwise.
	 */
	@Override
	public boolean add(JValue element) {
		if (itemHashSet.add(element)) {
			this.storedHashCode = 0;
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Removes the specified element from this set if it is present.
	 * <p>
	 * 
	 * If the set gets altered, this implementation sets
	 * <code>storedHashCode</code> to 0 (representing an unavailable hash code
	 * value), which forces <code>hashCode()</code> to recalculate the actual
	 * hash code value.
	 * 
	 * @param element
	 *            object to be removed from this set, if present.
	 * @return <code>true</code> if the set contained the specified element,
	 *         <code>false</code> otherwise.
	 */
	@Override
	public boolean remove(JValue element) {
		if (itemHashSet.remove(element)) {
			this.storedHashCode = 0;
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Compares the specified object with this set for equality. Returns
	 * <code>true</code> if the specified object is also a set, the two sets
	 * have the same size, and every element of the specified set is contained
	 * in this set.
	 * <p>
	 * 
	 * This implementation first checks if the given object is a
	 * <code>HashMathSet</code>. If so, the hash code values of this
	 * mathematical set and the specified <code>HashMathSet</code> are compared.
	 * Since the hash code values are being cached, this represents a quick
	 * solution if the sets aren't equal. However, if the hash code values are
	 * equal, it cannot be assumed that the sets themselves are equal, since
	 * different sets can have the same hash code value. In this case, the
	 * result of the super method <code>equals()</code> is returned.
	 * 
	 * @param o
	 *            object to be compared for equality with this set.
	 * @return <code>true</code> if the specified object is equal to this set,
	 *         <code>false</code> otherwise.
	 */
	@Override
	public boolean equals(Object o) {
		if (o instanceof JValueSet) {
			JValueSet other = (JValueSet) o;
			return itemHashSet.equals(other.itemHashSet);
		}
		return false;
	}

	/**
	 * Returns <code>true</code> if this mathematical set is a subset of the
	 * specified set. That is, if all elements of this mathematical set are also
	 * present in the specified set.
	 * 
	 * @param s
	 *            set to be checked for being a superset.
	 * @return <code>true</code> if this mathematical set is a subset of the
	 *         specifed set, <code>false</code> otherwise.
	 */
	public boolean isSubset(JValueSet s) {
		if (this.size() <= s.size()) {
			return s.containsAll(this);
		} else {
			return false;
		}
	}

	/**
	 * Returns the union with the specified set. This is a new
	 * <code>HashMathSet</code> containing all elements that are present in this
	 * mathematical set or in the specified set. This set and the given one are
	 * unchanged
	 * 
	 * @param s
	 *            set that is to be united with.
	 * @return the union with the specified set.
	 */
	public JValueSet union(JValueSet s) {
		JValueSet copy = new JValueSet(this);
		copy.addAll(s);
		return copy;
	}

	/**
	 * Returns the intersection with the specified set. This is a new
	 * <code>HashMathSet</code> containing all elements that are present in this
	 * mathematical set as well as in the specified set.
	 * 
	 * @param s
	 *            set that is to be intersected with.
	 * @return the intersection with the specified set.
	 */
	public JValueSet intersection(JValueSet s) {
		JValueSet resultingSet = new JValueSet(Math.min(s.size(), size()));
		JValue currentElement;

		if (s.size() > size()) {
			/*
			 * time complexity = O(min{|s|, |this|}) space complexity =
			 * O(|resultingSet|)
			 */
			for (Iterator<JValue> iter = iterator(); iter.hasNext();) {
				currentElement = iter.next();
				if (s.contains(currentElement)) {
					resultingSet.add(currentElement);
				}
			}
		} else {
			/*
			 * time complexity = O(|s|) space complexity = O(|resultingSet|)
			 */
			for (Iterator<JValue> iter = s.iterator(); iter.hasNext();) {
				currentElement = iter.next();
				if (this.contains(currentElement)) {
					resultingSet.add(currentElement);
				}
			}
		}
		return resultingSet;
	}

	/**
	 * Returns the asymmetric difference between this mathematical set and the
	 * specified set. This is a new <code>HashMathSet</code> containing all
	 * elements that are present in this mathematical set but not in the
	 * specified set.
	 * 
	 * @param s
	 *            set from what the difference is calculated.
	 * @return the difference with the specified set.
	 */
	public JValueSet difference(JValueSet s) {
		JValueSet resultingSet;
		JValue currentElement;
		/*
		 * time complexity = O(|this|) space complexity = O(|this|)
		 */
		resultingSet = new JValueSet(size());
		for (Iterator<JValue> iter = iterator(); iter.hasNext();) {
			currentElement = iter.next();
			if (!s.contains(currentElement)) {
				resultingSet.add(currentElement);
			}
		}
		return resultingSet;
	}

	/**
	 * Returns the symmetric difference between this mathematical set and the
	 * specified set. This is a new <code>HashMathSet</code> containing all
	 * elements that are present either in this mathematical set or in the
	 * specified set but not in both.
	 * 
	 * @param s
	 *            set from what the symmetric difference is calculated
	 * @return the symmetric difference with the specified set.
	 */
	public JValueSet symmetricDifference(JValueSet s) {
		JValueSet resultingSet;
		Iterator<JValue> iterSet;
		Iterator<JValue> iterThis;
		JValue currentElement;

		resultingSet = new JValueSet();
		for (iterThis = iterator(); iterThis.hasNext();) {
			currentElement = iterThis.next();
			if (!s.contains(currentElement)) {
				resultingSet.add(currentElement);
			}
		}
		for (iterSet = s.iterator(); iterSet.hasNext();) {
			currentElement = iterSet.next();
			if (!this.contains(currentElement)) {
				resultingSet.add(currentElement);
			}
		}

		return resultingSet;
	}

	/**
	 * Returns <code>true</code> if this mathematical set is a superset of the
	 * specified set. That is, if all elements of the specified set are also
	 * present in this mathematical set.
	 * <p>
	 * 
	 * This implementation first compares the sizes of this mathematical set and
	 * the specified set by invoking the <code>size</code> method on each. If
	 * this mathematical set is bigger than the specified set then each element
	 * of the specified set is checked for presence in this mathematical set.
	 * Otherwise, <code>false</code> is returned.
	 * 
	 * @param s
	 *            set to be checked for being a subset.
	 * @return <code>true</code> if this mathematical set is a superset of the
	 *         specifed set, <code>false</code> otherwise.
	 * @see JValueSet#isSubset
	 */
	public boolean isSuperset(JValueSet s) {
		if (this.size() >= s.size()) {
			return this.containsAll(s);
		} else {
			return false;
		}
	}

	/**
	 * Returns <code>true</code> if this mathematical set has no common elements
	 * with the specified set.
	 * <p>
	 * 
	 * This implementation determines which is the smaller of this set and the
	 * specified set by invoking the <code>size()</code> method on each. If this
	 * set has fewer elements, then the implementation iterates over this set,
	 * checking each element returned by the iterator in turn to see if it is
	 * contained in the specified set. If it is so contained, <code>false</code>
	 * is returned. If the specified set has fewer elements, then the
	 * implementation iterates over the specified set, returning
	 * <code>false</code> if it finds a common element.
	 * 
	 * @param s
	 *            set to be checked for common elements.
	 * @return <code>true</code> if this mathematical set has no common elements
	 *         with the specifed set, <code>false</code> otherwise.
	 */
	public boolean isDisjoint(JValueSet s) {
		Iterator<JValue> iter;

		if (this.size() < s.size()) {
			for (iter = this.iterator(); iter.hasNext();) {
				if (s.contains(iter.next())) {
					return false;
				}
			}
		} else {
			for (iter = s.iterator(); iter.hasNext();) {
				if (this.contains(iter.next())) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * replaces the old element the given newElement
	 * 
	 * @param oldElement
	 *            the element which should be replaced
	 * @param newElement
	 *            the element which should replace the old one
	 * @return true if successfull, false otherwise
	 */
	@Override
	public boolean replace(JValue oldElement, JValue newElement) {

		return true;
	}

	/**
	 * returns a pointer to the objec itself, very usefull to get a reference of
	 * the right type if you guess that a JValueCollection is a set Cleaner and
	 * faster than casting
	 */
	@Override
	public JValueSet toJValueSet() {
		return this;
	}

	/**
	 * accepts te given visitor to visit this jvalue
	 */
	@Override
	public void accept(JValueVisitor v) {
		v.visitSet(this);
	}

	@Override
	public Object toObject() {
		HashSet<Object> result = new HashSet<Object>(itemHashSet.size());
		for (JValue jv : itemHashSet) {
			result.add(jv.toObject());
		}
		return result;
	}
}
