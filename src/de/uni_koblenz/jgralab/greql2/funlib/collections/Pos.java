package de.uni_koblenz.jgralab.greql2.funlib.collections;

import java.util.ArrayList;

import org.pcollections.POrderedSet;
import org.pcollections.PVector;

import de.uni_koblenz.jgralab.greql2.funlib.Function;

public class Pos extends Function {

	public Pos() {
		super(
				"Determines the position of the first occurence of $x$ in a collection $l$. Returns -1 if $x$ was not found.",
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
