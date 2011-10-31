package de.uni_koblenz.jgralab.greql2.funlib.collections;

import org.pcollections.PMap;

import de.uni_koblenz.jgralab.greql2.funlib.Function;

public class ContainsValue extends Function {

	public ContainsValue() {
		super("Returns true iff the $map$ contains the $value$.", 4, 1, 0.2,
				Category.COLLECTIONS_AND_MAPS);
	}

	public <K, V> Boolean evaluate(PMap<K, V> map, V value) {
		return map.containsValue(value);
	}
}
