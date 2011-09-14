package de.uni_koblenz.jgralab.greql2.funlib.collections;

import java.util.ArrayList;

import org.pcollections.ArrayPSet;
import org.pcollections.PSet;

import de.uni_koblenz.jgralab.greql2.funlib.Function;

public class Intersection extends Function {

	public Intersection() {
		super("Computes the intersection of two sets $a$ and $b$.",
				Category.COLLECTIONS_AND_MAPS);
	}

	public <T> PSet<T> evaluate(PSet<T> a, PSet<T> b) {
		if (a.isEmpty() || b.isEmpty()) {
			return ArrayPSet.empty();
		}
		PSet<T> result = ArrayPSet.empty();
		if (b.size() < a.size()) {
			for (T x : a) {
				if (b.contains(x)) {
					result = result.plus(x);
				}
			}
		} else {
			for (T x : b) {
				if (a.contains(x)) {
					result = result.plus(x);
				}
			}
		}
		return result;
	}

	@Override
	public long getEstimatedCosts(ArrayList<Long> inElements) {
		return inElements.get(0) + inElements.get(1);
	}

	@Override
	public long getEstimatedCardinality(int inElements) {
		return inElements;
	}
}
