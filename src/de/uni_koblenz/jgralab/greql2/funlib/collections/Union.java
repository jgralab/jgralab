package de.uni_koblenz.jgralab.greql2.funlib.collections;

import java.util.ArrayList;

import org.pcollections.ArrayPMap;
import org.pcollections.ArrayPSet;
import org.pcollections.PMap;
import org.pcollections.PSet;

import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.greql2.funlib.Function;

public class Union extends Function {

	public Union() {
		super(
				"Computes the union of two sets (or maps) $a$ and $b$.\n"
						+ "In case of common keys in maps, $b$'s entries will overwrite $a$'s entries.",
				Category.COLLECTIONS_AND_MAPS);
	}

	public <T> PSet<T> evaluate(PSet<T> a, PSet<T> b) {
		if (b.isEmpty()) {
			if (a instanceof ArrayPSet) {
				return a;
			} else {
				return JGraLab.set();
			}
		} else {
			if (a instanceof ArrayPSet) {
				return ((ArrayPSet<T>) a).plusAll(b);
			} else {
				return JGraLab.<T> set().plusAll(a).plusAll(b);
			}
		}
	}

	public <K, V> PMap<K, V> evaluate(PMap<K, V> a, PMap<K, V> b) {
		if (b.isEmpty()) {
			if (a instanceof ArrayPMap) {
				return a;
			} else {
				return JGraLab.map();
			}
		} else {
			if (a instanceof ArrayPMap) {
				return ((ArrayPMap<K, V>) a).plusAll(b);
			} else {
				return JGraLab.<K, V> map().plusAll(a).plusAll(b);
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
