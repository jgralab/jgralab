package de.uni_koblenz.jgralab.greql2.funlib.collections;

import java.util.ArrayList;

import org.pcollections.POrderedSet;
import org.pcollections.PVector;

import de.uni_koblenz.jgralab.greql2.funlib.Function;

public class Pos extends Function {

	public Pos() {
		super(
				"Returns the position of the first occurence of the given element in the given collection, "
						+ "or -1, if the element is not contained in the collection.",
				Category.COLLECTIONS_AND_MAPS);
	}

	public <T> Integer evaluate(PVector<T> l, T x) {
		return l.indexOf(x);
	}

	public <T> Integer evaluate(POrderedSet<T> l, T x) {
		return l.indexOf(x);
	}

	@Override
	public long getEstimatedCosts(ArrayList<Long> inElements) {
		return inElements.get(0);
	}
}
