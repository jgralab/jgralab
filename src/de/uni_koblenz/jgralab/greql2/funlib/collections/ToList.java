package de.uni_koblenz.jgralab.greql2.funlib.collections;

import java.util.ArrayList;

import org.pcollections.ArrayPSet;
import org.pcollections.ArrayPVector;
import org.pcollections.PCollection;
import org.pcollections.PVector;

import de.uni_koblenz.jgralab.greql2.funlib.Function;

public class ToList extends Function {

	public ToList() {
		super("Converts a collection into a List.",
				Category.COLLECTIONS_AND_MAPS);
	}

	public <T> PVector<T> evaluate(PCollection<T> l) {
		if (l instanceof ArrayPVector) {
			return (PVector<T>) l;
		}
		if (l instanceof ArrayPSet) {
			return ((ArrayPSet<T>) l).toPVector();
		}
		PVector<T> result = ArrayPVector.empty();
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
