/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2014 Institute for Software Technology
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
package de.uni_koblenz.jgralab;

import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.VertexClass;

public interface GraphChangeListener {

	/**
	 * Called before a Vertex of class <code>vc</code> is created.
	 * 
	 * @param vc
	 *            the {@link VertexClass} of the new Vertex
	 */
	public void beforeCreateVertex(VertexClass vc);

	/**
	 * Called after Vertex <code>v</code> was created.
	 * 
	 * @param v
	 *            the new {@link Vertex}
	 */
	public void afterCreateVertex(Vertex v);

	/**
	 * Called before Vertex <code>v</code> is deleted.
	 * 
	 * @param v
	 *            the {@link Vertex} to delete
	 */
	public void beforeDeleteVertex(Vertex v);

	/**
	 * Called after a Vertex of class <code>vc</code> was deleted.
	 * 
	 * @param vc
	 *            the {@link VertexClass} of the deleted {@link Vertex}
	 * @param finalDelete
	 *            <code>true</code> if no more vertices are deleted,
	 *            <code>false</code> if more vertices are queued to be deleted
	 *            during a cascading delete of composite child vertices
	 */
	public void afterDeleteVertex(VertexClass vc, boolean finalDelete);

	/**
	 * Called before an Edge of class <code>ec</code> is created.
	 * 
	 * @param ec
	 *            the {@link EdgeClass} of the new {@link Edge}
	 * @param alpha
	 *            start {@link Vertex} for new Edge
	 * @param omega
	 *            end Vertex for new Edge
	 */
	public void beforeCreateEdge(EdgeClass ec, Vertex alpha, Vertex omega);

	/**
	 * Called after the Edge <code>e</code> was created.
	 * 
	 * @param e
	 *            the new {@link Edge}
	 */
	public void afterCreateEdge(Edge e);

	/**
	 * Called before the Edge <code>e</code> is deleted.
	 * 
	 * @param e
	 *            the {@link Edge} to delete
	 */
	public void beforeDeleteEdge(Edge e);

	/**
	 * Called after an Edge of class <code>ec</code> was deleted.
	 * 
	 * @param ec
	 *            the {@link EdgeClass} of the deleted Edge
	 */
	public void afterDeleteEdge(EdgeClass ec, Vertex oldAlpha, Vertex oldOmega);

	/**
	 * Called before Edge <code>inc</code> is put before <code>other</code>.
	 * 
	 * @param inc
	 * @param other
	 */
	public void beforePutIncidenceBefore(Edge inc, Edge other);

	/**
	 * Called after Edge <code>inc</code> is put before <code>other</code>.
	 * 
	 * @param inc
	 * @param other
	 */
	public void afterPutIncidenceBefore(Edge inc, Edge other);

	/**
	 * Called before Edge <code>inc</code> is put after <code>other</code>.
	 * 
	 * @param inc
	 * @param other
	 */
	public void beforePutIncidenceAfter(Edge inc, Edge other);

	/**
	 * Called after Edge <code>inc</code> is put after <code>other</code>.
	 * 
	 * @param inc
	 * @param other
	 */
	public void afterPutIncidenceAfter(Edge inc, Edge other);

	/**
	 * Called before start vertex of Edge <code>e</code> is changed.
	 * 
	 * @param e
	 *            the Edge that will change
	 * @param oldVertex
	 *            old start vertex
	 * @param newVertex
	 *            new start vertex
	 */
	public void beforeChangeAlpha(Edge e, Vertex oldVertex, Vertex newVertex);

	/**
	 * Called after start vertex of Edge <code>e</code> was changed.
	 * 
	 * @param e
	 *            the Edge that changed
	 * @param oldVertex
	 *            old start vertex
	 * @param newVertex
	 *            new start vertex
	 */
	public void afterChangeAlpha(Edge e, Vertex oldVertex, Vertex newVertex);

	/**
	 * Called before end vertex of Edge <code>e</code> is changed.
	 * 
	 * @param e
	 *            the Edge that will change
	 * @param oldVertex
	 *            old ed vertex
	 * @param newVertex
	 *            new ed vertex
	 */
	public void beforeChangeOmega(Edge e, Vertex oldVertex, Vertex newVertex);

	/**
	 * Called after end vertex of Edge <code>e</code> was changed.
	 * 
	 * @param e
	 *            the Edge that changed
	 * @param oldVertex
	 *            old end vertex
	 * @param newVertex
	 *            new end vertex
	 */
	public void afterChangeOmega(Edge e, Vertex oldVertex, Vertex newVertex);

	/**
	 * Called before attribute <code>attributeName</code> of
	 * <code>element</code> is changed.
	 * 
	 * @param element
	 * @param attributeName
	 * @param oldValue
	 * @param newValue
	 */
	public <AEC extends AttributedElementClass<AEC, ?>> void beforeChangeAttribute(
			AttributedElement<AEC, ?> element, String attributeName,
			Object oldValue, Object newValue);

	/**
	 * Called after attribute <code>attributeName</code> of <code>element</code>
	 * was changed.
	 * 
	 * @param element
	 * @param attributeName
	 * @param oldValue
	 * @param newValue
	 */
	public <AEC extends AttributedElementClass<AEC, ?>> void afterChangeAttribute(
			AttributedElement<AEC, ?> element, String attributeName,
			Object oldValue, Object newValue);

	/**
	 * @return the Graph that owns this GraphChangeListener.
	 */
	public Graph getGraph();
}
