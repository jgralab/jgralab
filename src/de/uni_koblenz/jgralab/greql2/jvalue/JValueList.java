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
 
package de.uni_koblenz.jgralab.greql2.jvalue;


import java.util.ArrayList;
import java.util.Iterator;

public class JValueList extends JValueOrderedCollection {

	/**
	 * all items are stored in this arraylist
	 */
	protected ArrayList<JValue> itemList;
	
    /**
     * Acts as a cache for the hash code value of this list out of performance
     * considerations.
     * Whenever this list is changed, storedHashCode is set to 0 and gets updated
     * as soon as the <code>hashCode()</code> method is called.
     */
    private int storedHashCode = 0;
	
	/**
	 * creates a new empty JValueList
	 */
	public JValueList() {
		itemList = new ArrayList<JValue>();
	}
	
	/**
	 * creates a new empty JValueList with given size
	 */
	public JValueList(int size) {
		itemList = new ArrayList<JValue>(size);
	}
	
	/**
	 * creates a new JValueList which contains all elements in the given collection 
	 */
	public JValueList(JValueCollection collection) {
		itemList = new ArrayList<JValue>(collection.size());
		addAll(collection);
	}
	
	
	
    /**
     * Returns the hash code value for this list. To get the hash
     * code of this list, new hash code values for every element of
     * this multiset are calculated from a polynomial of 3rd order and
     * finally summed up.
     * This ensures that <code>s1.equals(s2)</code> implies that
     * <code>s1.hashCode()==s2.hashCode()</code> for any two multisets
     * <code>s1</code> and <code>s2</code>, as required by the general contract
     * of <code>Object.hashCode()</code>.
     *
     * @return  the hash code value for this list.
     */
    public int hashCode() {
    	if (storedHashCode == 0) {
        JValue currentElement;
        int elementHashCode = 0;
        int newHashCode = -1;

        for (Iterator<JValue> iter = this.toJValueSet().iterator(); iter.hasNext(); ) {
            currentElement = iter.next();
            elementHashCode = currentElement.hashCode();
            newHashCode += -1 + ( 3 + elementHashCode)
                              * ( 7 + elementHashCode)
                              * (11 + elementHashCode);
        }
        	newHashCode += this.getClass().hashCode();
        	storedHashCode = newHashCode;
    	}
    	return storedHashCode;
    }
	
	
	/**
	 * returns true
	 */
	public boolean isJValueList() {
		return true;
	}
	
	/**
	 * adds a JValue to the collection
	 * @param element the JValue to be added
	 * @return true if successfull, false otherwise
	 */
	public boolean add(JValue element) {
		storedHashCode = 0;
		return itemList.add(element);
	}
	
	/**
	 * inserts a JValue at the given position into the collection
	 * @param element the JValue to be inserted
	 * @return true if successfull, false otherwise
	 */
	public boolean insert(int position, JValue element) {
		storedHashCode = 0;
		itemList.add(position, element);
		if (itemList.get(position) == element)
			return true;
		else
			return false;
	}
	
	/**
	 * returns the element at position index
	 */
	public JValue get(int index) {
		return (JValue) itemList.get(index);
	}
	
	/**
	 * @return true if the collection contains the given element, false otherwise
	 */
	public boolean contains(JValue element) {
		return itemList.contains(element);
	}
	
	/**
	 * @return the index of the given element in this collectin or -1 if the 
	 * collection doesn't contain the element
	 */
	public int indexOf(JValue element) {
		return itemList.indexOf(element);
	}
	
	/**
	 * removes a JValue from the collection
	 * @param element the element to be removed
	 * @return true if successfull, false otherwise
	 */	
	public boolean remove(JValue element) {
		storedHashCode = 0;
		return itemList.remove(element);
	}
	
	/**
	 * replaces the element at position index with the given newElement
	 * @param index the position of the element which should be replaced
	 * @param newElement the element which should replace the old one
	 * @return true if successfull, false otherwise
	 */
	public boolean replace(int index, JValue newElement) {
		storedHashCode = 0;
		if (index > itemList.size())
			return false;
		itemList.set(index, newElement);
		return true;
	}
	
	/**
	 * 
	 * @return the number of elements in this collection
	 */
	public int size() {
		return itemList.size();
	}
	
    /**
     * removes all elements from this collection
     * 
     */
	public void clear() {
		storedHashCode = 0;
		itemList.clear();
	}
	
	/**
	 * @return true if this collection contains no elements, false otherwise
	 */
	public boolean isEmpty() {
		return itemList.isEmpty();
	}
	
	/**
	 * @return an Iterator to navigate through the collection
	 *
	 */
	public Iterator<JValue> iterator() {
		return itemList.iterator();
	}
	
	/**
	 * returns a pointer to the objec itself, very usefull to get a reference of 
	 * the right type if you guess that a JValueCollection is a list
	 * Cleaner and faster than casting
	 */
	public JValueList toJValueList() {
		return this;
	}
	
	/**
	 * accepts te given visitor to visit this jvalue
	 */
	public void accept(JValueVisitor v) throws Exception {
		v.visitList(this);
	}
	
}
