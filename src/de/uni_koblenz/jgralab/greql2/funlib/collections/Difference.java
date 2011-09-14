package de.uni_koblenz.jgralab.greql2.funlib.collections;

import java.util.ArrayList;

import org.pcollections.ArrayPMap;
import org.pcollections.ArrayPSet;
import org.pcollections.PMap;
import org.pcollections.PSet;

import de.uni_koblenz.jgralab.greql2.funlib.Function;

public class Difference extends Function {

	public Difference() {
		super("Computes the difference of two sets (or maps) $a$ and $b$.",
				Category.COLLECTIONS_AND_MAPS);
	}

	public <T> PSet<T> evaluate(PSet<T> a, PSet<T> b) {
		if (b.isEmpty()) {
			if (a instanceof ArrayPSet) {
				return a;
			} else {
				PSet<T> result = ArrayPSet.empty();
				return result.plusAll(a);
			}
		} else {
			if (a instanceof ArrayPSet) {
				return a.minusAll(b);
			} else {
				PSet<T> result = ArrayPSet.empty();
				return result.plusAll(a).minusAll(b);
			}
		}
	}

	public <K, V> PMap<K, V> evaluate(PMap<K, V> a, PMap<K, V> b) {
		if (b.isEmpty()) {
			if (a instanceof ArrayPMap) {
				return a;
			} else {
				PMap<K, V> result = ArrayPMap.empty();
				return result.plusAll(a);
			}
		} else {
			if (a instanceof ArrayPMap) {
				return a.minusAll(b.keySet());
			} else {
				PMap<K, V> result = ArrayPMap.empty();
				return result.plusAll(a).minusAll(b.keySet());
			}

		}
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
