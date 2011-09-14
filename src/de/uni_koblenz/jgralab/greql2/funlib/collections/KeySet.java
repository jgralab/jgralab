package de.uni_koblenz.jgralab.greql2.funlib.collections;

import org.pcollections.PMap;
import org.pcollections.PSet;

import de.uni_koblenz.jgralab.greql2.funlib.Function;

public class KeySet extends Function {

	public KeySet() {
		super("Returns the set of keys of the $map$.",
				Category.COLLECTIONS_AND_MAPS);
	}

	public <K, V> PSet<K> evaluate(PMap<K, V> map) {
		return (PSet<K>) map.keySet();
	}

	@Override
	public long getEstimatedCardinality(int inElements) {
		return inElements;
	}
}
