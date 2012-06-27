/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2012 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 *
 * For bug reports, documentation and further information, visit
 *
 *                         https://github.com/jgralab/jgralab
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
/**
 * 
 */
package de.uni_koblenz.jgralab.greql.executable;

import de.uni_koblenz.jgralab.Vertex;

public class VertexStateNumberQueue {

	protected static int initialSize = 100;

	public Vertex currentVertex = null;

	public int currentState = 0;

	int size = initialSize;

	Vertex[] vertices = null;

	int[] states = null;

	int last = 0;

	int first = 0;

	public VertexStateNumberQueue() {
		vertices = new Vertex[initialSize];
		states = new int[initialSize];
		size = initialSize;
	}

	public void put(Vertex v, int s) {
		if (last == first + size - 1) {
			resize();
		}
		vertices[last % size] = v;
		states[last % size] = s;
		last++;
	}

	public boolean hasNext() {
		if (first == last) {
			return false;
		}
		currentVertex = vertices[first % size];
		currentState = states[first % size];
		first++;
		return true;
	}

	private final void resize() {
		Vertex[] newVertices = new Vertex[size * 2];
		int[] newStates = new int[size * 2];

		for (int i = 0; i < size; i++) {
			newVertices[i] = vertices[(first + i) % size];
			newStates[i] = states[(first + i) % size];
		}
		states = newStates;
		vertices = newVertices;
		last = size - 1;
		first = 0;
		size *= 2;
	}
}
