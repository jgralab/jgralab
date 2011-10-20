package de.uni_koblenz.jgralab.greql2.funlib.collections;

import org.pcollections.PSet;
import org.pcollections.PVector;

import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.greql2.funlib.Function;

public class SubCollection extends Function {

	public SubCollection() {
		super(
				"Returns a sub collection starting at the given start index (including),\n"
						+ "and ending at the given end index (excluding).",
				Category.COLLECTIONS_AND_MAPS);
	}

	public <T> PVector<T> evaluate(PVector<T> coll, Integer startIndex,
			Integer endIndex) {
		return coll.subList(startIndex, endIndex);
	}

	public <T> PVector<T> evaluate(PVector<T> coll, Integer startIndex) {
		return evaluate(coll, startIndex, coll.size());
	}

	public <T> PSet<T> evaluate(PSet<T> coll, Integer startIndex,
			Integer endIndex) {
		if (startIndex > endIndex) {
			return null;
		}
		PSet<T> result = JGraLab.set();
		int idx = 0;
		for (T item : coll) {
			if (idx == endIndex) {
				break;
			}
			if ((idx >= startIndex) && (idx < endIndex)) {
				result = result.plus(item);
			}
			idx++;
		}
		return result;
	}

	public <T> PSet<T> evaluate(PSet<T> coll, Integer startIndex) {
		return evaluate(coll, startIndex, coll.size());
	}
}
