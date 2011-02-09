package de.uni_koblenz.jgralab.utilities.tgtree;

import java.util.ArrayList;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.Vertex;

class VertexTreeNode extends GraphElementTreeNode {
	private Vertex v;

	public VertexTreeNode(Vertex v, EdgeTreeNode parent) {
		super(parent);
		this.v = v;
	}

	@Override
	protected void init() {
		if (incs != null) {
			return;
		}
		incs = new ArrayList<GraphElementTreeNode>();
		for (Edge e : v.incidences()) {
			incs.add(new EdgeTreeNode(e, this));
		}
	}

	@Override
	public String toString() {
		return v.toString();
	}

	@Override
	protected GraphElement get() {
		return v;
	}
}