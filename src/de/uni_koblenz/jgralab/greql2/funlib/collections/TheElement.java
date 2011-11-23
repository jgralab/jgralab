package de.uni_koblenz.jgralab.greql2.funlib.collections;

import org.pcollections.POrderedSet;
import org.pcollections.PVector;

import de.uni_koblenz.jgralab.greql2.funlib.Function;

public class TheElement extends Function {

	public TheElement() {
		super(
				"Returns the only element in the given collection. "
						+ "If the collection is empty or contains more than one element, "
						+ "an exception is thrown.",
				Category.COLLECTIONS_AND_MAPS);
	}

	public <T> T evaluate(PVector<T> c) {
		return c.size() == 1 ? c.get(0) : null;
	}

	public <T> T evaluate(POrderedSet<T> c) {
		return c.size() == 1 ? c.get(0) : null;
	}
}
