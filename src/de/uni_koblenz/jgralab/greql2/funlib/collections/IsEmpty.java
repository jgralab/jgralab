package de.uni_koblenz.jgralab.greql2.funlib.collections;

import org.pcollections.PMap;
import org.pcollections.PSet;
import org.pcollections.PVector;

import de.uni_koblenz.jgralab.greql2.funlib.Function;

public class IsEmpty extends Function {

	public IsEmpty() {
		super("Returns true, iff the given collection is empty.", 2, 1, 0.01,
				Category.STATISTICS);
	}

	public <T> Boolean evaluate(PVector<T> c) {
		return c.isEmpty();
	}

	public <T> Boolean evaluate(PSet<T> c) {
		return c.isEmpty();
	}

	public <K, V> Boolean evaluate(PMap<K, V> c) {
		return c.isEmpty();
	}
}
