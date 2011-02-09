/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2010 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 * 
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 3 of the License, or (at your
 * option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see <http://www.gnu.org/licenses>.
 * 
 * Additional permission under GNU GPL version 3 section 7
 * 
 * If you modify this Program, or any covered work, by linking or combining
 * it with Eclipse (or a modified version of that program or an Eclipse
 * plugin), containing parts covered by the terms of the Eclipse Public
 * License (EPL), the licensors of this Program grant you additional
 * permission to convey the resulting work.  Corresponding Source for a
 * non-source form of such a combination shall include the source code for
 * the parts of JGraLab used as well as that of the covered work.
 */
package de.uni_koblenz.jgralab.algolib.functions;

import java.util.Arrays;
import java.util.Iterator;

import de.uni_koblenz.jgralab.algolib.functions.entries.PermutationEntry;

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
	public Iterator<PermutationEntry<RANGE>> iterator() {
		return new ArrayIterator<PermutationEntry<RANGE>>() {
			@Override
			public PermutationEntry<RANGE> next() {
				return new PermutationEntry<RANGE>(i, values[i++]);
			}
		};
	}

}
