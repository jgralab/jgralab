/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2010 Institute for Software Technology
 *               University of Koblenz-Landau, Germany
 * 
 *               ist@uni-koblenz.de
 * 
 * Please report bugs to http://serres.uni-koblenz.de/bugzilla
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package de.uni_koblenz.jgralabtest.tools;

import java.util.NoSuchElementException;
import java.util.Random;

public class RandomBufferGeneric<T> {

	private Object[] data;
	private int initialSize;
	private int filled;
	private Random rnd;

	public RandomBufferGeneric(int initialSize) {
		this.initialSize = initialSize;
		data = new Object[initialSize];
		filled = 0;
		rnd = new Random();
	}

	private void expand() {
		// System.out.println("Expanding");
		Object[] newData = new Object[data.length + initialSize];
		for (int i = 0; i < data.length; i++) {
			newData[i] = data[i];
		}
		data = newData;
	}

	public T getNext() {
		if (filled == 0) {
			throw new NoSuchElementException("Buffer is empty");
		}
		int position = rnd.nextInt(filled);
		@SuppressWarnings("unchecked")
		T out = (T) data[position];
		data[position] = data[--filled];
		data[filled] = null;
		return out;
	}

	public boolean isEmpty() {
		return filled == 0;
	}

	public void put(T element) {
		if (filled == data.length) {
			expand();
		}
		data[filled++] = element;
	}

}
