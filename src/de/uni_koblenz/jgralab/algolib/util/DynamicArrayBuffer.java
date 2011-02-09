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
package de.uni_koblenz.jgralab.algolib.util;

public abstract class DynamicArrayBuffer<T> implements Buffer<T> {

	private static final long MAX = Integer.MAX_VALUE;
	private static final long MIN = 1000000l;

	protected Object[] data;
	protected int initialSize;
	protected int filled;

	public DynamicArrayBuffer(int initialSize) {
		super();
		this.initialSize = initialSize;
		data = new Object[initialSize];
		filled = 0;
	}

	private void expand() {
		// System.out.println("Expanding");
		Object[] newData = new Object[expandSize(data.length)];
		System.arraycopy(data, 0, newData, 0, data.length);
		// for (int i = 0; i < data.length; i++) {
		// newData[i] = data[i];
		// }
		data = newData;
	}

	private int expandSize(int oldSize) {
		long length = oldSize;
		if (length < MIN) {
			length *= 2;
		} else if (length < 3 * MIN) {
			length *= 1.5;
		} else if (length < 6 * MIN) {
			length *= 1.25;
		}

		if (length >= MAX) {
			return Integer.MAX_VALUE;
		}
		return (int) length;
	}

	@Override
	public boolean isEmpty() {
		return filled == 0;
	}

	@Override
	public void put(T element) {
		if (filled == data.length) {
			expand();
		}
		data[filled++] = element;
	}

}
