package de.uni_koblenz.jgralab.greql2.funlib.collections;

import org.pcollections.PMap;

import de.uni_koblenz.jgralab.greql2.funlib.Function;

public class ContainsKey extends Function {

	public ContainsKey() {
		super("Returns true, iff the given map contains the given key.", 2, 1,
				0.2, Category.COLLECTIONS_AND_MAPS);
	}

	public <K, V> Boolean evaluate(PMap<K, V> map, K key) {
		return map.containsKey(key);
	}
}