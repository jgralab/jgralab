package de.uni_koblenz.jgralab.greql2.funlib.collections;

import java.util.ArrayList;

import org.pcollections.PSet;

import de.uni_koblenz.jgralab.greql2.funlib.Function;

public class IsSubSet extends Function {

	public IsSubSet() {
		super("Returns true iff the set $a$ is subset of $b$.",
				Category.COLLECTIONS_AND_MAPS);
	}

	public <T> Boolean evaluate(PSet<T> a, PSet<T> b) {
		return b.containsAll(a);
	}

	@Override
	public long getEstimatedCosts(ArrayList<Long> inElements) {
		return inElements.get(0);
	}
}