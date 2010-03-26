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

package de.uni_koblenz.jgralabtest.greql2.jvalue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.junit.BeforeClass;
import org.junit.Test;

import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueBag;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueCollection;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueImpl;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueList;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueMap;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueSet;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueTuple;
import de.uni_koblenz.jgralabtest.schemas.minimal.Link;
import de.uni_koblenz.jgralabtest.schemas.minimal.MinimalGraph;
import de.uni_koblenz.jgralabtest.schemas.minimal.MinimalSchema;
import de.uni_koblenz.jgralabtest.schemas.minimal.Node;

/**
 * @author Tassilo Horn <horn@uni-koblenz.de>
 * 
 */
public class JValueTest {

	private static MinimalGraph graph;
	private static Node n1, n2, n3, n4;
	private static Link l1, l2, l3, l4;

	@BeforeClass
	public static void setupClass() {
		graph = MinimalSchema.instance().createMinimalGraph(10, 10);
		n1 = graph.createNode();
		n2 = graph.createNode();
		n3 = graph.createNode();
		n4 = graph.createNode();
		l1 = graph.createLink(n1, n2);
		l2 = graph.createLink(n2, n1);
		l3 = graph.createLink(n3, n4);
		l4 = graph.createLink(n4, n1);
	}

	@Test
	public void testValueFromSet() {
		HashSet<Integer> set = new HashSet<Integer>();
		set.add(1);
		set.add(2);
		set.add(3);
		JValue v = JValueImpl.fromObject(set);
		assertTrue(v.isCollection());
		JValueSet s = v.toCollection().toJValueSet();
		assertTrue(s.contains(new JValueImpl(1)));
		assertTrue(s.contains(new JValueImpl(2)));
		assertTrue(s.contains(new JValueImpl(3)));
		assertEquals(3, s.size());
	}

	@Test
	public void equalsInteger() {
		JValue v1 = new JValueImpl(1);
		JValue v2 = new JValueImpl(1);
		assertTrue(v1.equals(v2));
		assertFalse(v1 == v2);
		assertTrue(v1.hashCode() == v2.hashCode());
	}

	@Test
	public void equalsNodes() {
		JValue v1 = new JValueImpl(graph.getFirstNode());
		JValue v2 = new JValueImpl(graph.getFirstNode());
		assertTrue(v1.equals(v2));
		assertFalse(v1 == v2);
		assertTrue(v1.hashCode() == v2.hashCode());
	}

	@Test
	public void equalsLinks() {
		JValue e1 = new JValueImpl(graph.getFirstLinkInGraph());
		JValue e2 = new JValueImpl(graph.getFirstLinkInGraph());
		assertTrue(e1.equals(e2));
		assertFalse(e1 == e2);
		assertTrue(e1.hashCode() == e2.hashCode());
	}

	@Test
	public void notEqualsNodes() {
		JValue v1 = new JValueImpl(graph.getFirstNode());
		JValue v2 = new JValueImpl(v1.toVertex().getNextVertex());
		assertFalse(v1.equals(v2));
		assertFalse(v1 == v2);
	}

	@Test
	public void notEqualsLinks() {
		JValue e1 = new JValueImpl(graph.getFirstLinkInGraph());
		JValue e2 = new JValueImpl(e1.toEdge().getNextEdge());
		assertFalse(e1.equals(e2));
		assertFalse(e1 == e2);
	}

	@Test
	public void equalsList() {
		JValueList l1 = new JValueList();
		l1.add(new JValueImpl("one"));
		l1.add(new JValueImpl("two"));
		l1.add(new JValueImpl("three"));
		l1.add(new JValueImpl("four"));
		l1.add(new JValueImpl("five"));

		JValueList l2 = new JValueList();
		l2.add(new JValueImpl("one"));
		l2.add(new JValueImpl("two"));
		l2.add(new JValueImpl("three"));
		l2.add(new JValueImpl("four"));
		l2.add(new JValueImpl("five"));

		assertTrue(l1.equals(l2));
		assertTrue(l1.hashCode() == l2.hashCode());
	}

	@Test
	public void notEqualsList() {
		JValueList l1 = new JValueList();
		l1.add(new JValueImpl("one"));
		l1.add(new JValueImpl("two"));
		l1.add(new JValueImpl("three"));
		l1.add(new JValueImpl("four"));
		l1.add(new JValueImpl("five"));

		JValueList l2 = new JValueList();
		l2.add(new JValueImpl("one"));
		l2.add(new JValueImpl("three"));
		l2.add(new JValueImpl("two"));
		l2.add(new JValueImpl("four"));
		l2.add(new JValueImpl("five"));

		assertFalse(l1.equals(l2));
	}

	@Test
	public void equalsBag() {
		JValueBag b1 = new JValueBag();
		b1.add(new JValueImpl("one"));
		b1.add(new JValueImpl("one"));
		b1.add(new JValueImpl("two"));
		b1.add(new JValueImpl("three"));
		b1.add(new JValueImpl("three"));

		JValueBag b2 = new JValueBag();
		b2.add(new JValueImpl("three"), 2);
		b2.add(new JValueImpl("one"), 2);
		b2.add(new JValueImpl("two"));

		assertTrue(b1.equals(b2));
		assertTrue(b1.hashCode() == b2.hashCode());
	}

	@Test(expected = NoSuchElementException.class)
	public void testIterNextForBag() {
		tryIterNextException(new JValueBag());
	}

	@Test(expected = NoSuchElementException.class)
	public void testIterNextForSet() {
		tryIterNextException(new JValueSet());
	}

	@Test(expected = NoSuchElementException.class)
	public void testIterNextForTuple() {
		tryIterNextException(new JValueTuple());
	}

	@Test(expected = NoSuchElementException.class)
	public void testIterNextForList() {
		tryIterNextException(new JValueList());
	}

	private void tryIterNextException(JValueCollection col) {
		Iterator<JValue> iter = col.iterator();
		iter.next();
	}

	private void tryConcurrentModification(JValueCollection col) {
		col.add(new JValueImpl("one"));
		col.add(new JValueImpl("two"));
		Iterator<JValue> iter = col.iterator();
		if (iter.hasNext()) {
			iter.next();
		}
		col.add(new JValueImpl("three"));
		if (iter.hasNext()) {
			iter.next();
		}
	}

	@Test(expected = ConcurrentModificationException.class)
	public void testConcurrentModificationOnBag() {
		tryConcurrentModification(new JValueBag());
	}

	@Test(expected = ConcurrentModificationException.class)
	public void testConcurrentModificationOnSet() {
		tryConcurrentModification(new JValueSet());
	}

	@Test(expected = ConcurrentModificationException.class)
	public void testConcurrentModificationOnTuple() {
		tryConcurrentModification(new JValueTuple());
	}

	@Test(expected = ConcurrentModificationException.class)
	public void testConcurrentModificationOnList() {
		tryConcurrentModification(new JValueList());
	}

	// private void tryRemoveIter(JValueCollection col) {
	// col.add(new JValue("one"));
	// col.add(new JValue("two"));
	// Iterator<JValue> iter = col.iterator();
	// if (iter.hasNext())
	// iter.next();
	// if (iter.hasNext())
	// iter.next();
	// iter.remove();
	// assertEquals(1, col.size());
	// }

	// @Test
	// public void testRemoveOnTuple() {
	// tryRemove(new JValueTuple());
	// }

	@Test
	public void testRemoveIterOnBag() {
		tryRemove(new JValueBag());
	}

	@Test
	public void testRemoveIterOnSet() {
		tryRemove(new JValueSet());
	}

	@Test
	public void testRemoveIterOnList() {
		tryRemove(new JValueList());
	}

	private void tryRemoveAll(JValueCollection col, JValueCollection other) {
		col.add(new JValueImpl("one"));
		col.add(new JValueImpl("two"));
		col.add(new JValueImpl("three"));
		col.add(new JValueImpl("four"));

		other.add(new JValueImpl("one"));
		other.add(new JValueImpl("three"));

		col.removeAll(other);
		assertEquals(2, col.size());
		assertEquals(true, col.contains(new JValueImpl("two")));
		assertEquals(true, col.contains(new JValueImpl("four")));
	}

	@Test
	public void testRemoveAllOnList() {
		tryRemoveAll(new JValueList(), new JValueList());
	}

	@Test
	public void testRemoveAllOnBag() {
		tryRemoveAll(new JValueBag(), new JValueBag());
	}

	@Test
	public void testRemoveAllOnSet() {
		tryRemoveAll(new JValueSet(), new JValueSet());
	}

	private void tryRemove(JValueCollection col) {
		col.add(new JValueImpl("one"));
		col.add(new JValueImpl("two"));
		col.add(new JValueImpl("three"));
		col.add(new JValueImpl("four"));

		col.remove(new JValueImpl("one"));
		col.remove(new JValueImpl("three"));

		assertEquals(2, col.size());
		assertEquals(true, col.contains(new JValueImpl("two")));
		assertEquals(true, col.contains(new JValueImpl("four")));
	}

	// @Test
	// public void testRemoveOnTuple() {
	// tryRemove(new JValueTuple());
	// }

	@Test
	public void testRemoveOnBag() {
		tryRemove(new JValueBag());
		JValueBag bag = new JValueBag();
		bag.add(new JValueImpl("one"));
		bag.add(new JValueImpl("one"));
		bag.add(new JValueImpl("one"));
		bag.add(new JValueImpl("one"));
		bag.add(new JValueImpl("two"));
		bag.add(new JValueImpl("two"));
		bag.remove(new JValueImpl("one"), 3);
		assertEquals(3, bag.size());
		assertEquals(2, bag.getQuantity(new JValueImpl("two")));
		assertEquals(1, bag.getQuantity(new JValueImpl("one")));
		bag.remove(new JValueImpl("two"), 2);
		assertEquals(1, bag.size());
		assertEquals(0, bag.getQuantity(new JValueImpl("two")));
		assertEquals(1, bag.getQuantity(new JValueImpl("one")));
		bag.remove(new JValueImpl("two"), 2);
		assertEquals(1, bag.size());
		assertEquals(0, bag.getQuantity(new JValueImpl("two")));
		assertEquals(1, bag.getQuantity(new JValueImpl("one")));
	}

	@Test
	public void testAddOnBag() {
		JValueBag bag = new JValueBag();
		bag.add(new JValueImpl("one"), -1);
		assertEquals(0, bag.size());
		bag.add(new JValueImpl("one"), 0);
		assertEquals(0, bag.size());
		bag.add(new JValueImpl("one"), 4);
		assertEquals(4, bag.size());
		bag.add(new JValueImpl("one"), -1);
		assertEquals(4, bag.size());
	}

	@Test
	public void testRemoveOnSet() {
		tryRemove(new JValueSet());
	}

	@Test
	public void testRemoveOnList() {
		tryRemove(new JValueList());
	}

	// @Test
	// public void testRemoveAllTuple() {
	// JValueTuple tup = new JValueTuple();
	//
	// }

	@Test
	public void testSymDifferenceSet() {
		JValueSet first = new JValueSet();
		JValueSet second = new JValueSet();
		first.add(new JValueImpl("one"));
		first.add(new JValueImpl("two"));
		first.add(new JValueImpl("three"));
		first.add(new JValueImpl("four"));
		second.add(new JValueImpl("three"));
		second.add(new JValueImpl("four"));
		second.add(new JValueImpl("five"));
		second.add(new JValueImpl("six"));
		JValueSet diff = first.symmetricDifference(second);
		assertEquals(4, diff.size());
		assertTrue(diff.contains(new JValueImpl("one")));
		assertTrue(diff.contains(new JValueImpl("two")));
		assertTrue(diff.contains(new JValueImpl("five")));
		assertTrue(diff.contains(new JValueImpl("six")));
		assertFalse(diff.contains(new JValueImpl("four")));
		assertFalse(diff.contains(new JValueImpl("three")));
	}

	@Test
	public void testSymDifferenceBag() {
		JValueBag first = new JValueBag();
		JValueBag second = new JValueBag();
		first.add(new JValueImpl("one"), 3);
		first.add(new JValueImpl("two"), 2);
		first.add(new JValueImpl("three"), 4);
		first.add(new JValueImpl("four"), 3);
		second.add(new JValueImpl("three"), 2);
		second.add(new JValueImpl("four"), 4);
		second.add(new JValueImpl("five"), 2);
		second.add(new JValueImpl("six"), 7);
		JValueBag diff = first.symmetricDifference(second);
		assertEquals(17, diff.size());
		assertEquals(diff.getQuantity(new JValueImpl("one")), 3);
		assertEquals(diff.getQuantity(new JValueImpl("two")), 2);
		assertEquals(diff.getQuantity(new JValueImpl("five")), 2);
		assertEquals(diff.getQuantity(new JValueImpl("six")), 7);
		assertEquals(diff.getQuantity(new JValueImpl("four")), 1);
		assertEquals(diff.getQuantity(new JValueImpl("three")), 2);
	}

	@Test
	public void testDifferenceBag() {
		JValueBag first = new JValueBag();
		JValueBag second = new JValueBag();
		first.add(new JValueImpl("one"), 3);
		first.add(new JValueImpl("two"), 2);
		first.add(new JValueImpl("three"), 4);
		first.add(new JValueImpl("four"), 3);
		second.add(new JValueImpl("three"), 2);
		second.add(new JValueImpl("four"), 4);
		second.add(new JValueImpl("five"), 2);
		second.add(new JValueImpl("six"), 7);
		JValueBag diff = first.difference(second);
		assertEquals(7, diff.size());
		assertEquals(3, diff.getQuantity(new JValueImpl("one")));
		assertEquals(2, diff.getQuantity(new JValueImpl("two")));
		assertEquals(2, diff.getQuantity(new JValueImpl("three")));
		assertEquals(0, diff.getQuantity(new JValueImpl("four")));
		assertEquals(0, diff.getQuantity(new JValueImpl("five")));
		assertEquals(0, diff.getQuantity(new JValueImpl("six")));
	}

	@Test
	public void notEqualsBag() {
		JValueBag b1 = new JValueBag();
		b1.add(new JValueImpl("one"));
		b1.add(new JValueImpl("one"));
		b1.add(new JValueImpl("two"));
		b1.add(new JValueImpl("three"), 3);

		JValueBag b2 = new JValueBag();
		b2.add(new JValueImpl("three"), 2);
		b2.add(new JValueImpl("one"), 2);
		b2.add(new JValueImpl("two"));

		assertFalse(b1.equals(b2));
	}

	@Test
	public void equalsMap() {
		JValueMap m1 = new JValueMap();
		m1.put(new JValueImpl(1), new JValueImpl("One"));
		m1.put(new JValueImpl(2), new JValueImpl("Two"));
		m1.put(new JValueImpl(3), new JValueImpl("Three"));
		m1.put(new JValueImpl(4), new JValueImpl("Four"));
		m1.put(new JValueImpl(5), new JValueImpl("Five"));

		JValueMap m2 = new JValueMap();
		m2.put(new JValueImpl(2), new JValueImpl("Two"));
		m2.put(new JValueImpl(1), new JValueImpl("One"));
		m2.put(new JValueImpl(5), new JValueImpl("Five"));
		m2.put(new JValueImpl(3), new JValueImpl("Three"));
		m2.put(new JValueImpl(4), new JValueImpl("Four"));

		assertTrue(m1.equals(m2));
		assertTrue(m1.hashCode() == m2.hashCode());
	}

	@Test
	public void equalsMap2() {
		JValueMap m1 = new JValueMap();
		m1.put(new JValueImpl(l1), new JValueImpl(n1));
		m1.put(new JValueImpl(l2), new JValueImpl(n2));
		m1.put(new JValueImpl(l3), new JValueImpl(n3));
		m1.put(new JValueImpl(l4), new JValueImpl(n4));

		JValueMap m2 = new JValueMap();
		m2.put(new JValueImpl(l4), new JValueImpl(n4));
		m2.put(new JValueImpl(l2), new JValueImpl(n2));
		m2.put(new JValueImpl(l3), new JValueImpl(n3));
		m2.put(new JValueImpl(l1), new JValueImpl(n1));

		assertTrue(m1.equals(m2));
		assertTrue(m1.hashCode() == m2.hashCode());
	}

	@Test
	public void notEqualsMap() {
		JValueMap m1 = new JValueMap();
		m1.put(new JValueImpl(1), new JValueImpl("One"));
		m1.put(new JValueImpl(2), new JValueImpl("Two"));
		m1.put(new JValueImpl(3), new JValueImpl("Three"));
		m1.put(new JValueImpl(4), new JValueImpl("Four"));
		m1.put(new JValueImpl(5), new JValueImpl("FIVE"));

		JValueMap m2 = new JValueMap();
		m2.put(new JValueImpl(2), new JValueImpl("Two"));
		m2.put(new JValueImpl(1), new JValueImpl("One"));
		m2.put(new JValueImpl(5), new JValueImpl("Five"));
		m2.put(new JValueImpl(3), new JValueImpl("Three"));
		m2.put(new JValueImpl(4), new JValueImpl("Four"));

		assertFalse(m1.equals(m2));
	}
}
