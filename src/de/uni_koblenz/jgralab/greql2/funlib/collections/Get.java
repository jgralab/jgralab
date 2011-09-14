package de.uni_koblenz.jgralab.greql2.funlib.collections;

import java.util.ArrayList;

import org.pcollections.PMap;
import org.pcollections.POrderedSet;
import org.pcollections.PVector;

import de.uni_koblenz.jgralab.greql2.funlib.Function;

public class Get extends Function {

	public Get() {
		super(
				"Returns the value associated with KEY in MAP, or the element of INDEX in COLLECTION.\n"
						+ "Shorthand notation: myMap[KEY] or myCollection[INDEX]",
				Category.COLLECTIONS_AND_MAPS);
	}

	public <T> T evaluate(PVector<T> l, Integer i) {
		return i < 0 || i >= l.size() ? null : l.get(i);
	}

	public <T> T evaluate(POrderedSet<T> l, Integer i) {
		return i < 0 || i >= l.size() ? null : l.get(i);
	}

	public <K, V> V evaluate(PMap<K, V> m, K key) {
		return m.get(key);
	}

	@Override
	public long getEstimatedCosts(ArrayList<Long> inElements) {
		return inElements.get(0);
	}

	@Override
	public long getEstimatedCardinality(int inElements) {
		return inElements;
	}
}
