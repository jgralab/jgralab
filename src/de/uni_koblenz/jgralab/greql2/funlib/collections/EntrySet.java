package de.uni_koblenz.jgralab.greql2.funlib.collections;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;

import org.pcollections.ArrayPMap;
import org.pcollections.ArrayPSet;
import org.pcollections.PMap;
import org.pcollections.PSet;

import de.uni_koblenz.jgralab.greql2.funlib.Function;

public class EntrySet extends Function {

	public EntrySet() {
		super("Returns the set of entries of the $map$.",
				Category.COLLECTIONS_AND_MAPS);
	}

	public <K, V> PSet<PMap<String, Object>> evaluate(PMap<K, V> map) {
		PSet<PMap<String, Object>> result = ArrayPSet.empty();
		for (SimpleImmutableEntry<K, V> e : (ArrayPMap<K, V>) map) {
			PMap<String, Object> entry = ArrayPMap.empty();
			entry = entry.plus("key", e.getKey()).plus("value", e.getValue());
			result = result.plus(entry);
		}
		return result;
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
