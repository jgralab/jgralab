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
package de.uni_koblenz.jgralab.graphmarker;

import java.util.BitSet;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.Vertex;

/**
 * This class can be used to "colorize" graphs, it supports only two "colors",
 * that are "marked" or "not marked".
 * 
 * @author ist@uni-koblenz.de
 * 
 * @param <T>
 *            the name of the graph element class used by this
 *            <code>BitSetGraphMarker</code>
 */
public abstract class BitSetGraphMarker<T extends GraphElement> extends
		AbstractGraphMarker<T> {
	protected final BitSet marks;
	protected long version;

	/**
	 * Initializes a new BitSetGraphMarker with the given graph.
	 * 
	 * @param graph
	 */
	protected BitSetGraphMarker(Graph graph) {
		super(graph);
		marks = new BitSet();
	}

	/**
	 * Marks the given <code>graphElement</code>.
	 * 
	 * @param graphElement
	 *            the graph element to mark
	 * @return false if the given <code>graphElement</code> has already been
	 *         marked.
	 */
	public boolean mark(T graphElement) {
		assert (graphElement.getGraph() == graph);
		assert (graphElement.getId() <= (graphElement instanceof Vertex ? graph
				.getMaxVCount() : graph.getMaxECount()));
		boolean out = isMarked(graphElement);
		marks.set(graphElement.getId());
		version++;
		return !out;
	}

	@Override
	public boolean removeMark(T graphElement) {
		assert (graphElement.getGraph() == graph);
		assert (graphElement.getId() <= (graphElement instanceof Vertex ? graph
				.getMaxVCount() : graph.getMaxECount()));
		boolean out = isMarked(graphElement);
		marks.clear(graphElement.getId());
		version--;
		return out;
	}

	@Override
	public int size() {
		return marks.cardinality();
	}

	@Override
	public boolean isEmpty() {
		return marks.isEmpty();
	}

	@Override
	public void clear() {
		marks.clear();
	}

	@Override
	public boolean isMarked(T graphElement) {
		assert (graphElement.getGraph() == graph);
		assert (graphElement.getId() <= (graphElement instanceof Vertex ? graph
				.getMaxVCount() : graph.getMaxECount()));
		return marks.get(graphElement.getId());
	}

	@Override
	public void maxEdgeCountIncreased(int newValue) {
		// do nothing
	}

	@Override
	public void maxVertexCountIncreased(int newValue) {
		// do nothing
	}
}
