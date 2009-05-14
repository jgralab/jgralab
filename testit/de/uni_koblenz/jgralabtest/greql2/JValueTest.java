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

package de.uni_koblenz.jgralabtest.greql2;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;

import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueBag;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueList;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueMap;
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
