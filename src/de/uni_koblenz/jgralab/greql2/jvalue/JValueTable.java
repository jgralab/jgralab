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

import java.util.Iterator;
import java.util.logging.Logger;

import de.uni_koblenz.jgralab.greql2.exception.JValueInvalidTypeException;

/**
 * A JValueTable is the Java "replacement" for CvTable (but in fact it has not
 * much common with it) It's based on bag of Tuples with an additional header
 * tuple. Theoretical, it's possible to store any kind of objects in the bag,
 * but to hold the implementation and usage clean and fast, ony tuple are
 * allowed
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class JValueTable extends JValueCollection {

	private static Logger logger = Logger
			.getLogger(JValueTable.class.getName());

	/**
	 * Returns the hash code value for this table.
	 * 
	 * @return the hash code value for this table.
	 */
	@Override
	public int hashCode() {
		return headerTuple.hashCode() + data.hashCode()
				+ this.getClass().hashCode();
	}

	/**
	 * This is the header-tuple of this table
	 */
	private JValueTuple headerTuple;

	/**
	 * This is the internal datastructure. Normaly, this is a bag, but the
	 * implementation of JValueTable is independent of the real object, so also
	 * list, set oder some other collection can be used
	 */
	private JValueCollection data;

	/**
	 * returns true if this Collection is a table
	 */
	@Override
	public boolean isJValueTable() {
		return true;
	}

	/**
	 * returns a reference to this table
	 */
	@Override
	public JValueTable toJValueTable() {
		return this;
	}

	/**
	 * returns this table as bag, that is the databag without the header tuple
	 */
	@Override
	public JValueBag toJValueBag() {
		return data.toJValueBag();
	}

	/**
	 * returns this table as set, that is the databag without the header tuple,
	 * duplicates will be eliminated
	 */
	@Override
	public JValueSet toJValueSet() {
		return data.toJValueSet();
	}

	/**
	 * returns this table as tuple, that is the databag as tuple without the
	 * header tuple
	 */
	@Override
	public JValueTuple toJValueTuple() {
		return data.toJValueTuple();
	}

	/**
	 * returns this table as list, that is the databag as list without the
	 * header tuple
	 */
	@Override
	public JValueList toJValueList() {
		return data.toJValueList();
	}

	/**
	 * returns this table as record, the header is accessible as
	 * record.tableHeader while all other elements are accessible as record.1,
	 * record.2 etc
	 */
	@Override
	public JValueRecord toJValueRecord() {
		JValueRecord rec = data.toJValueRecord();
		rec.add("tableHeader", headerTuple);
		return rec;
	}

	/**
	 * Returns the data collection
	 */
	public JValueCollection getData() {
		return data;
	}

	/**
	 * Sets the data
	 */
	public void setData(JValueCollection newData) {
		data = newData;
	}

	/**
	 * creates a new JValueTable with an empty header
	 */
	public JValueTable() {
		this(new JValueTuple());
	}

	/**
	 * creates a new JValueTable with an empty header
	 * 
	 * @param useSet
	 *            if it is true, the table will be based on a <b>set</b> instead
	 *            of a bag, so every element can exists only one in the table
	 */
	public JValueTable(boolean useSet) {
		this(new JValueTuple(), true);
	}

	/**
	 * creates a new JValueTable which is based on a bag and sets the given
	 * tuple as header of that table
	 */
	public JValueTable(JValueTuple header) {
		this(header, true);
	}

	/**
	 * creates a new JValueTable and sets the given tuple as header of that
	 * table
	 * 
	 * @param useSet
	 *            if it is true, the table will be based on a <b>set</b> instead
	 *            of a bag, so every element can exists only one in the table
	 */
	public JValueTable(JValueTuple header, boolean useSet) {
		super();
		headerTuple = header;
		if (useSet) {
			data = new JValueSet();
		} else {
			data = new JValueBag();
		}
	}

	/**
	 * creates a new JValueTable which contains each element of the given
	 * collection as seperate row. The table doesn't have a header
	 */
	public JValueTable(JValueCollection col) {
		super();
		data = new JValueSet();
		headerTuple = new JValueTuple();
		addAll(col);
	}

	/**
	 * @return the header tuple
	 */
	public JValueTuple getHeader() {
		return headerTuple;
	}

	/**
	 * sets the header tuple
	 */
	public void setHeader(JValueTuple newHeader) {
		headerTuple = newHeader;
	}

	/**
	 * prints the table as table
	 */
	public void printTable() {
		StringBuilder sb = new StringBuilder();
		sb
				.append("----------------------------------------------------------------------\n");
		Iterator<JValue> headIter = headerTuple.iterator();
		while (headIter.hasNext()) {
			sb.append("|");
			sb.append(headIter.next());
		}
		sb
				.append("\n----------------------------------------------------------------------\n");
		sb
				.append("\n----------------------------------------------------------------------\n");
		Iterator<JValue> rowIter = data.iterator();
		while (rowIter.hasNext()) {
			JValueTuple curTup = (JValueTuple) rowIter.next();
			Iterator<JValue> colIter = curTup.iterator();
			while (colIter.hasNext()) {
				sb.append("|");
				sb.append(colIter.next());
			}
			sb
					.append("\n----------------------------------------------------------------------\n");
		}
		sb
				.append("\n----------------------------------------------------------------------\n");
		logger.info(sb.toString());
	}

	/**
	 * adds the given Element to this Table. If the element is not a tuple, it
	 * will be encapsulated in a JValue. Because for JValueBag and JValueSet the
	 * elementorder is not fix, the given JValue is, even if it is a collection,
	 * not transformed into a tuple, instead of this its used as the first
	 * element of a new created tuple
	 */
	@Override
	public boolean add(JValue element) {
		if (element.isCollection()) {
			try {
				JValueCollection col = element.toCollection();
				if (col.isJValueTuple()) {
					JValueTuple tup = col.toJValueTuple();
					return data.add(tup);
				}
			} catch (Exception ex) {
				// This cannot happen
			}
		}
		JValueTuple tup = new JValueTuple();
		tup.add(element);
		return data.add(tup);
	}

	/**
	 * removes all elements from the datastructure, the header doesn't get
	 * cleaned
	 */
	@Override
	public void clear() {
		data.clear();
	}

	/**
	 * @return true if the datastructure contains no elements, false otherwise
	 */
	@Override
	public boolean isEmpty() {
		return data.isEmpty();
	}

	/**
	 * @return an iterator for the data structure
	 */
	@Override
	public Iterator<JValue> iterator() {
		return data.iterator();
	}

	/**
	 * @return the number of elements in the datastructure, that is the number
	 *         of tablerows
	 */
	@Override
	public int size() {
		return data.size();
	}

	/**
	 * @return true if the datastructure contains - the given tuple or - a tuple
	 *         which contains the same elements in the same order or - a tuple
	 *         which contains the given element as first element
	 */
	@Override
	public boolean contains(JValue elem) {
		if (elem.isCollection()) {
			try {
				JValueCollection col = elem.toCollection();
				if (col.isJValueTuple()) {
					JValueTuple tup = col.toJValueTuple();
					Iterator<JValue> iter = this.iterator();
					while (iter.hasNext()) {
						JValueTuple curTup = (JValueTuple) iter.next();
						if (curTup == tup) {
							return true;
						}
						if (curTup.size() == tup.size()) {
							int i = 0;
							while ((tup.get(i).equals(curTup.get(i)))
									&& (i < tup.size())) {
								i++;
							}
							if (i == tup.size()) {
								;
							}
							return true;
						}
					}
					return false;
				}
			} catch (Exception ex) {
				// This cannot happen
			}
		}
		Iterator<JValue> iter = this.iterator();
		while (iter.hasNext()) {
			JValueTuple curTup = (JValueTuple) iter.next();
			if (curTup.get(0) == elem) {
				return true;
			}
		}
		return false;
	}

	/**
	 * returns true, if this element and the given one are equal
	 */
	@Override
	public boolean equals(Object o) {
		if (o instanceof JValueTable) {
			JValueTable t = (JValueTable) o;
			if (t.size() != size()) {
				return false;
			}
			if (!t.getHeader().equals(getHeader())) {
				return false;
			}
			Iterator<JValue> iter = iterator();
			while (iter.hasNext()) {
				JValue current = iter.next();
				if (!t.getData().contains(current)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	/**
	 * Returns <code>true</code> if at least one value of the specified column
	 * appears twice. Otherwise, if every value of the column appears only once
	 * or if the specified column don't exist, <code>false</code> is returned.
	 * 
	 * @param colNumber
	 *            The column to check for duplicated entries. Index of first
	 *            column is 1.
	 */
	public boolean columnContainsDuplicateEntries(int colNumber) {
		if (this.size() < 1 || colNumber < 1) {
			return false;
		}

		JValueSet testSet = new JValueSet();
		Iterator<? extends JValue> tupleIter = data.iterator();
		while (tupleIter.hasNext()) {
			JValueTuple tuple = (JValueTuple) tupleIter.next();
			if (colNumber > tuple.size()) {
				return false; // not nice!! ckeck should be part of first if
			}
			// of method. need a getColumnCount() method?
			if (!testSet.add(tuple.get(colNumber - 1))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * removes the given JValueTuple. If the given element is not a tuple,
	 * nothing happens
	 */
	@Override
	public boolean remove(JValue elem) {
		return data.remove(elem);
	}

	/**
	 * accepts te given visitor to visit this jvalue
	 */
	@Override
	public void accept(JValueVisitor v) {
		v.visitTable(this);
	}

	@Override
	public Object toObject() throws JValueInvalidTypeException {
		return data.toObject();
	}

}
