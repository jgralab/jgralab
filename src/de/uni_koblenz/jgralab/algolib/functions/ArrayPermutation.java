package de.uni_koblenz.jgralab.algolib.functions;

import java.util.Arrays;
import java.util.Iterator;

import de.uni_koblenz.jgralab.algolib.functions.pairs.IntDomainPair;

public class ArrayPermutation<RANGE> implements Permutation<RANGE> {

	private abstract class ArrayIterator<T> implements Iterator<T> {

		protected int i;

		protected ArrayIterator() {
			i = 1;
		}

		@Override
		public boolean hasNext() {
			return i < length;
		}

		@Override
		public abstract T next();

		@Override
		public void remove() {
			throw new UnsupportedOperationException(
					"This iterator does not allow the removal of objects.");
		}

	}

	private RANGE[] values;
	private int length;

	public ArrayPermutation(RANGE[] values) {
		this.values = values;
		int i = 1;
		while (i < values.length && values[i] != null) {
			i++;
		}
		length = i - 1;

	}

	@Override
	public RANGE get(int parameter) {
		return values[parameter];
	}

	@Override
	public boolean isDefined(int parameter) {
		assert (parameter > 0 && parameter < length ? values[parameter] != null
				: true);
		return parameter > 0 && parameter < length;
	}

	@Override
	public void add(RANGE value) {
		throw new UnsupportedOperationException(
				"This permutation is immutable.");
	}

	@Override
	public String toString() {
		return Arrays.toString(values);
	}

	public RANGE[] getArray() {
		return values;
	}

	@Override
	public Iterable<RANGE> getRangeElements() {
		return new Iterable<RANGE>() {
			@Override
			public Iterator<RANGE> iterator() {
				return new ArrayIterator<RANGE>() {
					@Override
					public RANGE next() {
						return values[i++];
					}
				};
			}
		};
	}

	@Override
	public int length() {
		return length;
	}

	@Override
	public Iterator<IntDomainPair<RANGE>> iterator() {
		return new ArrayIterator<IntDomainPair<RANGE>>() {
			@Override
			public IntDomainPair<RANGE> next() {
				return new IntDomainPair<RANGE>(i, values[i++]);
			}
		};
	}

}
