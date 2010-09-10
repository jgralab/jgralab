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
package de.uni_koblenz.jgralab.algolib.buffers;

import java.util.Arrays;
import java.util.Comparator;
import java.util.NoSuchElementException;


public class UnsortedPriorityQueueBuffer<T> extends
		DynamicArrayBuffer<T> implements Buffer<T> {

	private Comparator<T> comparator;

	public UnsortedPriorityQueueBuffer(int initialSize, Comparator<T> comparator) {
		super(initialSize);
		this.comparator = comparator;
	}

	@Override
	public T getNext() {
		if (filled == 0) {
			throw new NoSuchElementException("Buffer is empty");
		}
		int minIndex = indexOfMinimum();
		@SuppressWarnings("unchecked")
		T out = (T) data[minIndex];
		data[minIndex] = data[--filled];
		data[filled] = null;
		return out;
	}

	@SuppressWarnings("unchecked")
	private int indexOfMinimum() {
		assert (filled > 0);
		T min = (T) data[0];
		int out = 0;
		for (int i = 0; i < filled; i++) {
			if (comparator.compare((T) data[i], min) < 0) {
				min = (T) data[i];
				out = i;
			}
		}
		return out;
	}
	
	@Override
	public String toString(){
		return Arrays.toString(data);
	}

}
