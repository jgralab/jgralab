package de.uni_koblenz.jgralab.utilities.tgtree;

import java.util.ArrayList;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.schema.AggregationKind;

class EdgeTreeNode extends GraphElementTreeNode {

	private Edge e;

	public EdgeTreeNode(Edge e, VertexTreeNode parent) {
		super(parent);
		this.e = e;
	}

	@Override
	public String toString() {
		String arrow = null;
		if (e.isNormal()) {
			if (e.getThatSemantics() != AggregationKind.NONE) {
				arrow = "<>--> ";
			} else if (e.getThisSemantics() != AggregationKind.NONE) {
				arrow = "--><> ";
			} else {
				arrow = "--> ";
			}
		} else {
			if (e.getThatSemantics() != AggregationKind.NONE) {
				arrow = "<><-- ";
			} else if (e.getThisSemantics() != AggregationKind.NONE) {
				arrow = "<--<> ";
			} else {
				arrow = "<-- ";
			}
		}
		return arrow + e.toString();
	}

	@Override
	protected void init() {
		incs = new ArrayList<GraphElementTreeNode>();
		incs.add(new VertexTreeNode(e.getThat(), this));
	}

	@Override
	protected GraphElement get() {
		return e;
	}

}