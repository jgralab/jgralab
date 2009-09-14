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

package de.uni_koblenz.jgralabtest.greql2.jvalue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.junit.BeforeClass;
import org.junit.Test;

import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueBag;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueCollection;
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
	public void equalsInteger() {
		JValue v1 = new JValue(1);
		JValue v2 = new JValue(1);
		assertTrue(v1.equals(v2));
		assertFalse(v1 == v2);
		assertTrue(v1.hashCode() == v2.hashCode());
	}

	@Test
	public void equalsNodes() {
		JValue v1 = new JValue(graph.getFirstNode());
		JValue v2 = new JValue(graph.getFirstNode());
		assertTrue(v1.equals(v2));
		assertFalse(v1 == v2);
		assertTrue(v1.hashCode() == v2.hashCode());
	}

	@Test
	public void equalsLinks() {
		JValue e1 = new JValue(graph.getFirstLinkInGraph());
		JValue e2 = new JValue(graph.getFirstLinkInGraph());
		assertTrue(e1.equals(e2));
		assertFalse(e1 == e2);
		assertTrue(e1.hashCode() == e2.hashCode());
	}

	@Test
	public void notEqualsNodes() {
		JValue v1 = new JValue(graph.getFirstNode());
		JValue v2 = new JValue(v1.toVertex().getNextVertex());
		assertFalse(v1.equals(v2));
		assertFalse(v1 == v2);
	}

	@Test
	public void notEqualsLinks() {
		JValue e1 = new JValue(graph.getFirstLinkInGraph());
		JValue e2 = new JValue(e1.toEdge().getNextEdge());
		assertFalse(e1.equals(e2));
		assertFalse(e1 == e2);
	}

	@Test
	public void equalsList() {
		JValueList l1 = new JValueList();
		l1.add(new JValue("one"));
		l1.add(new JValue("two"));
		l1.add(new JValue("three"));
		l1.add(new JValue("four"));
		l1.add(new JValue("five"));

		JValueList l2 = new JValueList();
		l2.add(new JValue("one"));
		l2.add(new JValue("two"));
		l2.add(new JValue("three"));
		l2.add(new JValue("four"));
		l2.add(new JValue("five"));

		assertTrue(l1.equals(l2));
		assertTrue(l1.hashCode() == l2.hashCode());
	}

	@Test
	public void notEqualsList() {
		JValueList l1 = new JValueList();
		l1.add(new JValue("one"));
		l1.add(new JValue("two"));
		l1.add(new JValue("three"));
		l1.add(new JValue("four"));
		l1.add(new JValue("five"));

		JValueList l2 = new JValueList();
		l2.add(new JValue("one"));
		l2.add(new JValue("three"));
		l2.add(new JValue("two"));
		l2.add(new JValue("four"));
		l2.add(new JValue("five"));

		assertFalse(l1.equals(l2));
	}

	@Test
	public void equalsBag() {
		JValueBag b1 = new JValueBag();
		b1.add(new JValue("one"));
		b1.add(new JValue("one"));
		b1.add(new JValue("two"));
		b1.add(new JValue("three"));
		b1.add(new JValue("three"));

		JValueBag b2 = new JValueBag();
		b2.add(new JValue("three"), 2);
		b2.add(new JValue("one"), 2);
		b2.add(new JValue("two"));

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
		col.add(new JValue("one"));
		col.add(new JValue("two"));
		Iterator<JValue> iter = col.iterator();
		if (iter.hasNext()) {
			iter.next();
		}
		col.add(new JValue("three"));
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
		col.add(new JValue("one"));
		col.add(new JValue("two"));
		col.add(new JValue("three"));
		col.add(new JValue("four"));

		other.add(new JValue("one"));
		other.add(new JValue("three"));

		col.removeAll(other);
		assertEquals(2, col.size());
		assertEquals(true, col.contains(new JValue("two")));
		assertEquals(true, col.contains(new JValue("four")));
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
		col.add(new JValue("one"));
		col.add(new JValue("two"));
		col.add(new JValue("three"));
		col.add(new JValue("four"));

		col.remove(new JValue("one"));
		col.remove(new JValue("three"));

		assertEquals(2, col.size());
		assertEquals(true, col.contains(new JValue("two")));
		assertEquals(true, col.contains(new JValue("four")));
	}

	// @Test
	// public void testRemoveOnTuple() {
	// tryRemove(new JValueTuple());
	// }

	@Test
	public void testRemoveOnBag() {
		tryRemove(new JValueBag());
		JValueBag bag = new JValueBag();
		bag.add(new JValue("one"));
		bag.add(new JValue("one"));
		bag.add(new JValue("one"));
		bag.add(new JValue("one"));
		bag.add(new JValue("two"));
		bag.add(new JValue("two"));
		bag.remove(new JValue("one"), 3);
		assertEquals(3, bag.size());
		assertEquals(2, bag.getQuantity(new JValue("two")));
		assertEquals(1, bag.getQuantity(new JValue("one")));
		bag.remove(new JValue("two"), 2);
		assertEquals(1, bag.size());
		assertEquals(0, bag.getQuantity(new JValue("two")));
		assertEquals(1, bag.getQuantity(new JValue("one")));
		bag.remove(new JValue("two"), 2);
		assertEquals(1, bag.size());
		assertEquals(0, bag.getQuantity(new JValue("two")));
		assertEquals(1, bag.getQuantity(new JValue("one")));
	}

	@Test
	public void testAddOnBag() {
		JValueBag bag = new JValueBag();
		bag.add(new JValue("one"), -1);
		assertEquals(0, bag.size());
		bag.add(new JValue("one"), 0);
		assertEquals(0, bag.size());
		bag.add(new JValue("one"), 4);
		assertEquals(4, bag.size());
		bag.add(new JValue("one"), -1);
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
		first.add(new JValue("one"));
		first.add(new JValue("two"));
		first.add(new JValue("three"));
		first.add(new JValue("four"));
		second.add(new JValue("three"));
		second.add(new JValue("four"));
		second.add(new JValue("five"));
		second.add(new JValue("six"));
		JValueSet diff = first.symmetricDifference(second);
		assertEquals(4, diff.size());
		assertTrue(diff.contains(new JValue("one")));
		assertTrue(diff.contains(new JValue("two")));
		assertTrue(diff.contains(new JValue("five")));
		assertTrue(diff.contains(new JValue("six")));
		assertFalse(diff.contains(new JValue("four")));
		assertFalse(diff.contains(new JValue("three")));
	}

	@Test
	public void testSymDifferenceBag() {
		JValueBag first = new JValueBag();
		JValueBag second = new JValueBag();
		first.add(new JValue("one"), 3);
		first.add(new JValue("two"), 2);
		first.add(new JValue("three"), 4);
		first.add(new JValue("four"), 3);
		second.add(new JValue("three"), 2);
		second.add(new JValue("four"), 4);
		second.add(new JValue("five"), 2);
		second.add(new JValue("six"), 7);
		JValueBag diff = first.symmetricDifference(second);
		assertEquals(17, diff.size());
		assertEquals(diff.getQuantity(new JValue("one")), 3);
		assertEquals(diff.getQuantity(new JValue("two")), 2);
		assertEquals(diff.getQuantity(new JValue("five")), 2);
		assertEquals(diff.getQuantity(new JValue("six")), 7);
		assertEquals(diff.getQuantity(new JValue("four")), 1);
		assertEquals(diff.getQuantity(new JValue("three")), 2);
	}

	@Test
	public void testDifferenceBag() {
		JValueBag first = new JValueBag();
		JValueBag second = new JValueBag();
		first.add(new JValue("one"), 3);
		first.add(new JValue("two"), 2);
		first.add(new JValue("three"), 4);
		first.add(new JValue("four"), 3);
		second.add(new JValue("three"), 2);
		second.add(new JValue("four"), 4);
		second.add(new JValue("five"), 2);
		second.add(new JValue("six"), 7);
		JValueBag diff = first.difference(second);
		assertEquals(7, diff.size());
		assertEquals(3, diff.getQuantity(new JValue("one")));
		assertEquals(2, diff.getQuantity(new JValue("two")));
		assertEquals(2, diff.getQuantity(new JValue("three")));
		assertEquals(0, diff.getQuantity(new JValue("four")));
		assertEquals(0, diff.getQuantity(new JValue("five")));
		assertEquals(0, diff.getQuantity(new JValue("six")));
	}

	@Test
	public void notEqualsBag() {
		JValueBag b1 = new JValueBag();
		b1.add(new JValue("one"));
		b1.add(new JValue("one"));
		b1.add(new JValue("two"));
		b1.add(new JValue("three"), 3);

		JValueBag b2 = new JValueBag();
		b2.add(new JValue("three"), 2);
		b2.add(new JValue("one"), 2);
		b2.add(new JValue("two"));

		assertFalse(b1.equals(b2));
	}

	@Test
	public void equalsMap() {
		JValueMap m1 = new JValueMap();
		m1.put(new JValue(1), new JValue("One"));
		m1.put(new JValue(2), new JValue("Two"));
		m1.put(new JValue(3), new JValue("Three"));
		m1.put(new JValue(4), new JValue("Four"));
		m1.put(new JValue(5), new JValue("Five"));

		JValueMap m2 = new JValueMap();
		m2.put(new JValue(2), new JValue("Two"));
		m2.put(new JValue(1), new JValue("One"));
		m2.put(new JValue(5), new JValue("Five"));
		m2.put(new JValue(3), new JValue("Three"));
		m2.put(new JValue(4), new JValue("Four"));

		assertTrue(m1.equals(m2));
		assertTrue(m1.hashCode() == m2.hashCode());
	}

	@Test
	public void equalsMap2() {
		JValueMap m1 = new JValueMap();
		m1.put(new JValue(l1), new JValue(n1));
		m1.put(new JValue(l2), new JValue(n2));
		m1.put(new JValue(l3), new JValue(n3));
		m1.put(new JValue(l4), new JValue(n4));

		JValueMap m2 = new JValueMap();
		m2.put(new JValue(l4), new JValue(n4));
		m2.put(new JValue(l2), new JValue(n2));
		m2.put(new JValue(l3), new JValue(n3));
		m2.put(new JValue(l1), new JValue(n1));

		assertTrue(m1.equals(m2));
		assertTrue(m1.hashCode() == m2.hashCode());
	}

	@Test
	public void notEqualsMap() {
		JValueMap m1 = new JValueMap();
		m1.put(new JValue(1), new JValue("One"));
		m1.put(new JValue(2), new JValue("Two"));
		m1.put(new JValue(3), new JValue("Three"));
		m1.put(new JValue(4), new JValue("Four"));
		m1.put(new JValue(5), new JValue("FIVE"));

		JValueMap m2 = new JValueMap();
		m2.put(new JValue(2), new JValue("Two"));
		m2.put(new JValue(1), new JValue("One"));
		m2.put(new JValue(5), new JValue("Five"));
		m2.put(new JValue(3), new JValue("Three"));
		m2.put(new JValue(4), new JValue("Four"));

		assertFalse(m1.equals(m2));
	}
}
