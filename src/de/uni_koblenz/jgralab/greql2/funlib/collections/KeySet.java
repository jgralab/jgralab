package de.uni_koblenz.jgralab.greql2.funlib.collections;

import org.pcollections.PMap;
import org.pcollections.PSet;

import de.uni_koblenz.jgralab.greql2.funlib.Function;

public class KeySet extends Function {

	public KeySet() {
		super("Returns the set of keys of the given map.",
				Category.COLLECTIONS_AND_MAPS);
	}

	public <K, V> PSet<K> evaluate(PMap<K, V> map) {
		try {
			return (PSet<K>) map.keySet();
		} catch (RuntimeException e) {
			e.printStackTrace();
			System.err.println("The offending map was " + map + "("
					+ map.getClass() + ")");
			System.err.println("The keySet: " + map.keySet() + "("
					+ map.keySet().getClass() + ")");
			throw e;
		}
	}

	@Override
	public long getEstimatedCardinality(int inElements) {
		return inElements;
	}
}
