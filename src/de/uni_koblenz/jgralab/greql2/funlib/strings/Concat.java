package de.uni_koblenz.jgralab.greql2.funlib.strings;

import org.pcollections.ArrayPVector;
import org.pcollections.PCollection;
import org.pcollections.PVector;

import de.uni_koblenz.jgralab.greql2.funlib.Function;

public class Concat extends Function {

	public Concat() {
		super("Concatenates Strings and Lists. Alternative use: a ++ b", 3, 1,
				1.0, Category.COLLECTIONS_AND_MAPS, Category.STRINGS);
	}

	public String evaluate(String a, Object b) {
		return a + b;
	}

	public String evaluate(Object a, String b) {
		return a + b;
	}

	public <T> PVector<T> evaluate(PCollection<T> a, PCollection<T> b) {
		if (a instanceof ArrayPVector) {
			return (PVector<T>) a.plusAll(b);
		} else {
			PVector<T> result = ArrayPVector.empty();
			return result.plusAll(a).plusAll(b);
		}
	}
}
