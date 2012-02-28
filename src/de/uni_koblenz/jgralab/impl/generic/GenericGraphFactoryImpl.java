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
package de.uni_koblenz.jgralab.impl.generic;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.ImplementationType;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.impl.GraphFactoryImpl;
import de.uni_koblenz.jgralab.impl.InternalGraph;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.GraphClass;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.VertexClass;

public class GenericGraphFactoryImpl extends GraphFactoryImpl {
	public GenericGraphFactoryImpl(Schema s) {
		super(s, ImplementationType.GENERIC);
	}

	@Override
	public void setGraphImplementationClass(GraphClass gc,
			Class<? extends Graph> implementationClass) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setVertexImplementationClass(VertexClass vc,
			Class<? extends Vertex> implementationClass) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setEdgeImplementationClass(EdgeClass ec,
			Class<? extends Edge> implementationClass) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <G extends Graph> G createGraph(GraphClass gc, String id, int vMax,
			int eMax) {
		assert schema == gc.getSchema();
		@SuppressWarnings("unchecked")
		G graph = (G) new GenericGraphImpl(gc, id, vMax, eMax);
		graph.setGraphFactory(this);
		return graph;
	}

	@Override
	public <V extends Vertex> V createVertex(VertexClass vc, int id, Graph g) {
		assert schema == vc.getSchema();
		if (!((InternalGraph) g).isLoading() && (g.hasECARuleManager())) {
			g.getECARuleManager().fireBeforeCreateVertexEvents(vc);
		}
		@SuppressWarnings("unchecked")
		V vertex = (V) new GenericVertexImpl(vc, id, g);
		if (!((InternalGraph) g).isLoading() && (g.hasECARuleManager())) {
			g.getECARuleManager().fireAfterCreateVertexEvents(vertex);
		}
		return vertex;
	}

	@Override
	public <E extends Edge> E createEdge(EdgeClass ec, int id, Graph g,
			Vertex alpha, Vertex omega) {
		assert schema == ec.getSchema();
		if (!((InternalGraph) g).isLoading() && (g.hasECARuleManager())) {
			g.getECARuleManager().fireBeforeCreateEdgeEvents(ec);
		}
		@SuppressWarnings("unchecked")
		E edge = (E) new GenericEdgeImpl(ec, id, g, alpha, omega);
		if (!((InternalGraph) g).isLoading() && (g.hasECARuleManager())) {
			g.getECARuleManager().fireAfterCreateEdgeEvents(edge);
		}
		return edge;
	}
}
