package de.uni_koblenz.jgralabtest.non_junit_tests;

import org.pcollections.ArrayPVector;
import org.pcollections.PVector;

public class SubListTest {
	public static void main(String[] args) {
		PVector<Integer> v = ArrayPVector.empty();
		for (int i = 1; i <= 10; ++i) {
			v = v.plus(i);
		}
		System.out.println(v);
		v = v.subList(5, v.size());
		System.out.println(v);
		for (int i = 0; i < v.size(); ++i) {
			System.out.println(i + "\t" + v.get(i));
		}
		int i = 0;
		for (Integer x : v) {
			System.out.println(i + "\t" + x);
			++i;
		}
	}
}
