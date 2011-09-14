package de.uni_koblenz.jgralab.greql2.funlib.collections;

import java.util.ArrayList;

import org.pcollections.ArrayPSet;
import org.pcollections.PCollection;
import org.pcollections.PSet;

import de.uni_koblenz.jgralab.greql2.funlib.Function;

public class ToSet extends Function {

	public ToSet() {
		super("Converts a collection into a Set (removes duplicates).",
				Category.COLLECTIONS_AND_MAPS);
	}

	public <T> PSet<T> evaluate(PCollection<T> l) {
		if (l instanceof ArrayPSet) {
			return (PSet<T>) l;
		}
		PSet<T> result = ArrayPSet.empty();
		return result.plusAll(l);
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
