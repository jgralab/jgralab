/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2013 Institute for Software Technology
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

/**
 * Provides empty implementations as convenience base class for custom
 * {@link GraphChangeListener}s.
 * 
 * @author ist@uni-koblenz.de
 */
public class GraphChangeAdapter implements GraphChangeListener {
	protected Graph graph;

	public GraphChangeAdapter(Graph graph) {
		this.graph = graph;
	}

	@Override
	public void beforeCreateVertex(VertexClass vc) {
	}

	@Override
	public void afterCreateVertex(Vertex v) {
	}

	@Override
	public void beforeDeleteVertex(Vertex v) {
	}

	@Override
	public void afterDeleteVertex(VertexClass vc, boolean finalDelete) {
	}

	@Override
	public void beforeCreateEdge(EdgeClass ec, Vertex alpha, Vertex omega) {
	}

	@Override
	public void afterCreateEdge(Edge e) {
	}

	@Override
	public void beforeDeleteEdge(Edge e) {
	}

	@Override
	public void afterDeleteEdge(EdgeClass ec, Vertex oldAlpha, Vertex oldOmega) {
	}

	@Override
	public void beforePutIncidenceBefore(Edge inc, Edge other) {
	}

	@Override
	public void afterPutIncidenceBefore(Edge inc, Edge other) {
	}

	@Override
	public void beforePutIncidenceAfter(Edge inc, Edge other) {
	}

	@Override
	public void afterPutIncidenceAfter(Edge inc, Edge other) {
	}

	@Override
	public void beforeChangeAlpha(Edge e, Vertex oldVertex, Vertex newVertex) {
	}

	@Override
	public void afterChangeAlpha(Edge e, Vertex oldVertex, Vertex newVertex) {
	}

	@Override
	public void beforeChangeOmega(Edge e, Vertex oldVertex, Vertex newVertex) {
	}

	@Override
	public void afterChangeOmega(Edge e, Vertex oldVertex, Vertex newVertex) {
	}

	@Override
	public <AEC extends AttributedElementClass<AEC, ?>> void beforeChangeAttribute(
			AttributedElement<AEC, ?> element, String attributeName,
			Object oldValue, Object newValue) {
	}

	@Override
	public <AEC extends AttributedElementClass<AEC, ?>> void afterChangeAttribute(
			AttributedElement<AEC, ?> element, String attributeName,
			Object oldValue, Object newValue) {
	}

	@Override
	public Graph getGraph() {
		return graph;
	}

}
