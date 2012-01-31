package de.uni_koblenz.jgralab.impl.generic;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.ImplementationType;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.impl.GraphFactoryImpl;
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
		@SuppressWarnings("unchecked")
		V vertex = (V) new GenericVertexImpl(vc, id, g);
		return vertex;
	}

	@Override
	public <E extends Edge> E createEdge(EdgeClass ec, int id, Graph g,
			Vertex alpha, Vertex omega) {
		assert schema == ec.getSchema();
		@SuppressWarnings("unchecked")
		E edge = (E) new GenericEdgeImpl(ec, id, g, alpha, omega);
		return edge;
	}
}
