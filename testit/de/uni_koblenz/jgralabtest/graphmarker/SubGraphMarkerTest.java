/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2010 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
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
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.ImplementationType;
import de.uni_koblenz.jgralab.graphmarker.BooleanGraphMarker;
import de.uni_koblenz.jgralab.graphmarker.SubGraphMarker;
import de.uni_koblenz.jgralab.trans.CommitFailedException;
import de.uni_koblenz.jgralabtest.instancetest.InstanceTest;
import de.uni_koblenz.jgralabtest.schemas.minimal.Link;
import de.uni_koblenz.jgralabtest.schemas.minimal.MinimalGraph;
import de.uni_koblenz.jgralabtest.schemas.minimal.MinimalSchema;
import de.uni_koblenz.jgralabtest.schemas.minimal.Node;

@RunWith(Parameterized.class)
public class SubGraphMarkerTest extends InstanceTest {
	protected final int V = 4; // initial max vertex count
	protected final int E = 4; // initial max edge count
	protected final int VERTEX_COUNT = 10;
	protected final int EDGE_COUNT = 20;

	@Parameters
	public static Collection<Object[]> configure() {
		return getParameters();
	}

	private MinimalGraph g;
	private Node[] nodes;
	private Link[] links;
	private BooleanGraphMarker oldMarker;
	private SubGraphMarker newMarker;

	public SubGraphMarkerTest(ImplementationType implementationType) {
		super(implementationType);
	}

	@Before
	public void setUp() throws CommitFailedException {
		switch (implementationType) {
		case STANDARD:
			g = MinimalSchema.instance().createMinimalGraph(V, E);
			break;
		case TRANSACTION:
			g = MinimalSchema.instance()
					.createMinimalGraphWithTransactionSupport(V, E);
			break;
		case SAVEMEM:
			g = MinimalSchema.instance().createMinimalGraphWithSavememSupport(
					V, E);
			break;
		default:
			fail("Implementation " + implementationType
					+ " not yet supported by this test.");
		}
		createTransaction(g);

		Random rng = new Random(16L);
		oldMarker = new BooleanGraphMarker(g);
		newMarker = new SubGraphMarker(g);
		nodes = new Node[VERTEX_COUNT];
		for (int i = 1; i <= VERTEX_COUNT; i++) {
			nodes[i - 1] = g.createNode();
		}

		links = new Link[EDGE_COUNT];

		for (int i = 1; i <= EDGE_COUNT; i++) {
			int alphaID = rng.nextInt(VERTEX_COUNT) + 1;
			int omegaID = rng.nextInt(VERTEX_COUNT) + 1;
			links[i - 1] = g.createLink((Node) g.getVertex(alphaID), (Node) g
					.getVertex(omegaID));
		}

		commit(g);

		createReadOnlyTransaction(g);
		for (int i = 0; i < VERTEX_COUNT; i++) {
			if (rng.nextBoolean()) {
				oldMarker.mark(nodes[i]);
				newMarker.mark(nodes[i]);
			}
		}

		for (int i = 0; i < EDGE_COUNT; i++) {
			if (rng.nextBoolean()) {
				oldMarker.mark(links[i]);
				newMarker.mark(links[i]);
			}
		}
		commit(g);
	}

	public void assertAllMarkedCorrectly() throws CommitFailedException {
		createReadOnlyTransaction(g);
		Set<AttributedElement> oldSet = new HashSet<AttributedElement>();
		Set<AttributedElement> newSet = new HashSet<AttributedElement>();
		for (AttributedElement currentElement : oldMarker.getMarkedElements()) {
			oldSet.add(currentElement);
		}
		for (GraphElement currentElement : newMarker.getMarkedElements()) {
			newSet.add(currentElement);
		}
		assertEquals(oldSet, newSet);
		assertEquals(newSet, oldSet);

		commit(g);
	}

	@Test
	public void testGetMarkedElements() throws CommitFailedException {
		assertAllMarkedCorrectly();
		createReadOnlyTransaction(g);
		for (int i = 0; i < VERTEX_COUNT; i++) {
			assertEquals(oldMarker.mark(nodes[i]), newMarker.mark(nodes[i]));
		}
		for (int i = 0; i < EDGE_COUNT; i++) {
			assertEquals(oldMarker.mark(links[i]), newMarker.mark(links[i]));
		}
		commit(g);
		assertAllMarkedCorrectly();
		createReadOnlyTransaction(g);
		for (int i = 0; i < VERTEX_COUNT; i++) {
			assertEquals(oldMarker.removeMark(nodes[i]), newMarker
					.removeMark(nodes[i]));
		}
		for (int i = 0; i < EDGE_COUNT; i++) {
			assertEquals(oldMarker.removeMark(links[i]), newMarker
					.removeMark(links[i]));
		}
		commit(g);
		assertAllMarkedCorrectly();
	}

}
