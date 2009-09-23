/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2009 Institute for Software Technology
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

package de.uni_koblenz.jgralab.impl;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.schema.GraphClass;
import de.uni_koblenz.jgralab.schema.Schema;

/**
 * TODO add comment
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public abstract class GraphElementImpl implements GraphElement {
	protected int id;

	public GraphElementImpl(Graph graph) {
		assert graph != null;
		this.graph = (GraphImpl) graph;
	}

	protected GraphImpl graph;

	public Graph getGraph() {
		return graph;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.AttributedElement#getGraphClass()
	 */
	public GraphClass getGraphClass() {
		return (GraphClass) graph.getAttributedElementClass();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.AttributedElement#getSchema()
	 */
	public Schema getSchema() {
		return graph.getSchema();
	}

	/**
	 * Changes the graph version of the graph this element belongs to. Should be
	 * called whenever the graph is changed, all changes like adding, creating
	 * and reordering of edges and vertices or changes of attributes of the
	 * graph, an edge or a vertex are treated as a change.
	 */
	public void graphModified() {
		graph.graphModified();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.GraphElement#getId()
	 */
	@Override
	public int getId() {
		return id;
	}

	/**
	 * sets the id field of this graph element
	 * 
	 * @param id
	 *            an id
	 */
	protected abstract void setId(int id);
}
