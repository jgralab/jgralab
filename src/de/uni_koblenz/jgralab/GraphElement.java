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

package de.uni_koblenz.jgralab;

/**
 * aggregates vertices and edges
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public interface GraphElement extends AttributedElement {

	/**
	 * returns the id of this graph element
	 * 
	 * @return the id of this graph element
	 */
	public int getId();

	/**
	 * returns the graph containing this graph element
	 * 
	 * @return the graph containing this graph element
	 */
	public Graph getGraph();

	/**
	 * Changes the graph version of the graph this element belongs to. Should be
	 * called whenever the graph is changed, all changes like adding, creating
	 * and reordering of edges and vertices or changes of attributes of the
	 * graph, an edge or a vertex are treated as a change.
	 */
	public void graphModified();

	/**
	 * returns true if this GraphElement is still present in the Graph (i.e. not
	 * deleted). This check is equivalent to getGraph().containsVertex(this) or
	 * getGraph().containsEdge(this).
	 */
	public boolean isValid();
}
