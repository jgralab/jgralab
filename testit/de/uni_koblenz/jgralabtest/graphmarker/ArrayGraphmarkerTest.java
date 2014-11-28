/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2014 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 *
 * For bug reports, documentation and further information, visit
 *
 *                         https://github.com/jgralab/jgralab
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
package de.uni_koblenz.jgralabtest.graphmarker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collection;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import de.uni_koblenz.jgralab.ImplementationType;
import de.uni_koblenz.jgralab.graphmarker.ArrayVertexMarker;
import de.uni_koblenz.jgralabtest.instancetest.InstanceTest;
import de.uni_koblenz.jgralabtest.schemas.minimal.MinimalGraph;
import de.uni_koblenz.jgralabtest.schemas.minimal.MinimalSchema;
import de.uni_koblenz.jgralabtest.schemas.minimal.Node;

@RunWith(Parameterized.class)
public class ArrayGraphmarkerTest extends InstanceTest {

	public ArrayGraphmarkerTest(ImplementationType implementationType,
			String dbURL) {
		super(implementationType, dbURL);
	}

	@Parameters
	public static Collection<Object[]> configure() {
		return getParameters();
	}

	private static class TestMarkerObject {
		public int intValue;
		public double doubleValue;
		public String stringValue;
		public int[] arrayValue;

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + Arrays.hashCode(arrayValue);
			long temp;
			temp = Double.doubleToLongBits(doubleValue);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			result = prime * result + intValue;
			result = prime * result
					+ ((stringValue == null) ? 0 : stringValue.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			TestMarkerObject other = (TestMarkerObject) obj;
			if (!Arrays.equals(arrayValue, other.arrayValue)) {
				return false;
			}
			if (Double.doubleToLongBits(doubleValue) != Double
					.doubleToLongBits(other.doubleValue)) {
				return false;
			}
			if (intValue != other.intValue) {
				return false;
			}
			if (stringValue == null) {
				if (other.stringValue != null) {
					return false;
				}
			} else if (!stringValue.equals(other.stringValue)) {
				return false;
			}
			return true;
		}
	}

	protected final int V = 4; // initial max vertex count
	protected final int E = 4; // initial max edge count

	private MinimalGraph g;
	private ArrayVertexMarker<TestMarkerObject> marker;

	private Node v1;
	private Node v2;

	@Before
	public void setup() {
		switch (implementationType) {
		case STANDARD:
			g = MinimalSchema.instance().createMinimalGraph(
					ImplementationType.STANDARD, null, V, E);
			break;
		default:
			fail("Implementation " + implementationType
					+ " not yet supported by this test.");
		}
		v1 = g.createNode();
		v2 = g.createNode();
		g.createLink(v1, v2);
		marker = new ArrayVertexMarker<>(g);
	}

	@After
	public void tearDown() throws InterruptedException {
		g = null;
		marker = null;
		System.gc();
		Thread.sleep(10);
	}

	// omitted test for "isMarked"; it is assumed to be correct ;-)

	@Test
	public void testIsMarked() {

		assertFalse(marker.isMarked(v1));
		assertFalse(marker.isMarked(v2));

		marker.mark(v1, new TestMarkerObject());

		assertTrue(marker.isMarked(v1));
		assertFalse(marker.isMarked(v2));

		// test if treating null as error value works properly
		marker.mark(v2, null);

		assertTrue(marker.isMarked(v1));
		assertFalse(marker.isMarked(v2));
	}

	@Test
	public void testIsEmpty() {
		// test if empty marker is really empty
		assertTrue(marker.isEmpty());

		marker.mark(v1, new TestMarkerObject());

		// test if filled marker is not empty
		assertFalse(marker.isEmpty());
	}

	@Test
	public void testClear() {

		// test if clear works for empty marker
		assertTrue(marker.size() == 0);
		assertTrue(marker.isEmpty());

		marker.clear();

		assertTrue(marker.size() == 0);
		assertTrue(marker.isEmpty());

		marker.mark(v1, new TestMarkerObject());
		marker.mark(v2, new TestMarkerObject());

		assertTrue(marker.size() == 2);
		assertFalse(marker.isEmpty());

		marker.clear();

		assertTrue(marker.size() == 0);
		assertTrue(marker.isEmpty());
	}

	@Test
	public void testMark() {
		assertFalse(marker.isMarked(v1));
		assertFalse(marker.isMarked(v2));

		marker.mark(v1, new TestMarkerObject());

		assertTrue(marker.isMarked(v1));
		assertFalse(marker.isMarked(v2));

		marker.mark(v2, new TestMarkerObject());

		assertTrue(marker.isMarked(v1));
		assertTrue(marker.isMarked(v2));

	}

	@Test
	public void testGetMark() {
		TestMarkerObject mark = new TestMarkerObject();
		mark.arrayValue = new int[] { 1, 8, 90 };
		mark.doubleValue = 23.988;
		mark.intValue = -129;
		mark.stringValue = "Hugo";

		TestMarkerObject mark2 = new TestMarkerObject();
		mark2.arrayValue = new int[] { 2, 8, 90 };
		mark2.doubleValue = 23.98;
		mark2.intValue = -19;
		mark2.stringValue = "Hgo";

		assertNull(marker.getMark(v1));
		assertNull(marker.getMark(v2));

		// test if old mark is returned
		TestMarkerObject old1 = marker.mark(v1, mark);
		TestMarkerObject old2 = marker.mark(v2, mark2);

		assertEquals(mark, marker.getMark(v1));
		assertEquals(mark2, marker.getMark(v2));
		assertNull(old1);
		assertNull(old2);

		old1 = marker.mark(v1, mark2);
		assertEquals(old1, mark);

	}

	@Test
	public void testSize() {
		assertEquals(0, marker.size());
		marker.mark(v1, new TestMarkerObject());
		assertEquals(1, marker.size());
		marker.mark(v2, new TestMarkerObject());
		assertEquals(2, marker.size());
		marker.removeMark(v1);
		assertEquals(1, marker.size());
		marker.removeMark(v2);
		assertEquals(0, marker.size());
		assertTrue(marker.isEmpty());
	}

	@Test
	public void testremoveMark() {
		assertFalse(marker.removeMark(v1));
		assertFalse(marker.removeMark(v2));
		marker.mark(v1, new TestMarkerObject());
		marker.mark(v2, new TestMarkerObject());

		assertTrue(marker.isMarked(v1));
		assertTrue(marker.removeMark(v1));
		assertFalse(marker.isMarked(v1));
		assertFalse(marker.removeMark(v1));

		assertTrue(marker.isMarked(v2));
		assertTrue(marker.removeMark(v2));
		assertFalse(marker.isMarked(v2));
		assertFalse(marker.removeMark(v2));
	}

	@Test
	public void testMaxVertexCountIncreased() {
		assertEquals(V, marker.maxSize());
		for (int i = 0; i < V; i++) {
			g.createNode();
		}
		g.createNode();
		assertEquals(2 * V, marker.maxSize());
	}

}
