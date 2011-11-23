package de.uni_koblenz.jgralab.greql2.funlib.collections;

import org.pcollections.PMap;
import org.pcollections.PVector;

import de.uni_koblenz.jgralab.greql2.funlib.Function;

public class Values extends Function {

	public Values() {
		super("Returns the collection of values of the given map.",
				Category.COLLECTIONS_AND_MAPS);
	}

	public <K, V> PVector<V> evaluate(PMap<K, V> map) {
		return (PVector<V>) map.values();
	}

	@Override
	public long getEstimatedCardinality(int inElements) {
		return inElements;
	}
}
