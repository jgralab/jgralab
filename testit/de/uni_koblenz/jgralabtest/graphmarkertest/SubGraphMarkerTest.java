package de.uni_koblenz.jgralabtest.graphmarkertest;

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
import de.uni_koblenz.jgralab.graphmarker.BooleanGraphMarker;
import de.uni_koblenz.jgralab.graphmarker.SubGraphMarker;
import de.uni_koblenz.jgralab.trans.CommitFailedException;
import de.uni_koblenz.jgralabtest.instancetest.InstanceTest;
import de.uni_koblenz.jgralabtest.schemas.minimal.Link;
import de.uni_koblenz.jgralabtest.schemas.minimal.MinimalGraph;
import de.uni_koblenz.jgralabtest.schemas.minimal.MinimalSchema;
import de.uni_koblenz.jgralabtest.schemas.minimal.Node;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNull;

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

	public SubGraphMarkerTest(boolean transactionsEnabled) {
		super(transactionsEnabled);
	}

	@Before
	public void setUp() throws CommitFailedException {
		g = transactionsEnabled ? MinimalSchema.instance()
				.createMinimalGraphWithTransactionSupport(V, E) : MinimalSchema
				.instance().createMinimalGraph(V, E);
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
		createReadOnlyTransaction(g);
		assertAllMarkedCorrectly();
		for (int i = 0; i < VERTEX_COUNT; i++) {
			assertEquals(oldMarker.mark(nodes[i]), newMarker.mark(nodes[i]));
		}
		for (int i = 0; i < EDGE_COUNT; i++) {
			assertEquals(oldMarker.mark(links[i]), newMarker.mark(links[i]));
		}
		assertAllMarkedCorrectly();
		for (int i = 0; i < VERTEX_COUNT; i++) {
			assertEquals(oldMarker.removeMark(nodes[i]), newMarker
					.removeMark(nodes[i]));
		}
		for (int i = 0; i < EDGE_COUNT; i++) {
			assertEquals(oldMarker.removeMark(links[i]), newMarker
					.removeMark(links[i]));
		}
		assertAllMarkedCorrectly();
		commit(g);
	}

}
