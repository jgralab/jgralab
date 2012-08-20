package de.uni_koblenz.jgralabtest.greql.funlib.collections;

import static junit.framework.Assert.assertEquals;

import org.junit.Test;
import org.pcollections.PVector;

import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.greql.funlib.FunLib;

public class ConcatTest {

	@SuppressWarnings("unchecked")
	@Test
	public void testCollection() {
		PVector<Integer> l1 = JGraLab.<Integer> vector().plus(1).plus(2)
				.plus(3);
		PVector<Integer> l2 = JGraLab.<Integer> vector().plus(3).plus(4)
				.plus(5);
		PVector<Integer> l3 = l1.plusAll(l2);

		PVector<Integer> result = (PVector<Integer>) FunLib.apply("concat", l1,
				l2);
		assertEquals(l1.size() + l2.size(), result.size());
		assertEquals(l3, result);
		result = (PVector<Integer>) FunLib.apply("concat", l1,
				JGraLab.<Integer> vector());
		assertEquals(l1, result);
		result = (PVector<Integer>) FunLib.apply("concat",
				JGraLab.<Integer> vector(), l1);
		assertEquals(l1, result);
	}

	@Test
	public void testString() {
		String result = (String) FunLib.apply("concat", "abc", "def");
		assertEquals("abcdef", result);
	}
}
