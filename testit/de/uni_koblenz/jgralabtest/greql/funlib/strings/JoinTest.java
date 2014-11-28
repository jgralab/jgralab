package de.uni_koblenz.jgralabtest.greql.funlib.strings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;
import org.pcollections.PCollection;
import org.pcollections.PSet;
import org.pcollections.PVector;

import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.greql.funlib.FunLib;

public class JoinTest extends StringsTest {

	private List<PCollection<String>> pcollectionValues;
	private String[] delims = new String[] { ",", ", ", " ; ", "^", "" };

	@Before
	public void setUp() {
		pcollectionValues = new LinkedList<>();
		for (int i = 0; i < 30; i++) {
			pcollectionValues.add(createRandomPVector(i));
			pcollectionValues.add(createRandomPSet(i));
		}

	}

	private PVector<String> createRandomPVector(int length) {
		PVector<String> out = JGraLab.vector();
		long seed = System.nanoTime();
		Random rng = new Random(seed);
		for (int i = 0; i < length; i++) {
			int random = rng.nextInt(stringValues.length);
			out = out.plus(stringValues[random]);
		}
		return out;
	}

	private PSet<String> createRandomPSet(int draws) {
		PSet<String> out = JGraLab.set();
		long seed = System.nanoTime();
		Random rng = new Random(seed);
		for (int i = 0; i < draws; i++) {
			int random = rng.nextInt(stringValues.length);
			out = out.plus(stringValues[random]);
		}
		return out;
	}

	@Test
	public void testJoin() {
		for (PCollection<String> currentCollection : pcollectionValues) {
			for (String currentDelim : delims) {
				String expected = computeExpected(currentCollection,
						currentDelim);
				Object result = FunLib.apply("join", currentCollection,
						currentDelim);
				assertTrue(result instanceof String);
				assertEquals(expected, result);
			}
		}
	}

	private String computeExpected(PCollection<String> collection, String delim) {
		StringBuilder s = new StringBuilder();
		Iterator<String> iter = collection.iterator();
		if (iter.hasNext()) {
			s.append(iter.next());
			while (iter.hasNext()) {
				s.append(delim);
				s.append(iter.next());
			}
		}
		return s.toString();
	}

}
