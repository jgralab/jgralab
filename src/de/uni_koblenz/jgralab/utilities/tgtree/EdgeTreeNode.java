package de.uni_koblenz.jgralab.utilities.tgtree;

import java.util.ArrayList;
import java.util.Enumeration;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.schema.AggregationKind;

class EdgeTreeNode extends GraphElementTreeNode {

	private Edge e;

	public EdgeTreeNode(Edge e, VertexTreeNode parent) {
		super(parent);
		this.e = e;
	}

	public boolean isBackEdge() {
		if ((getParent() != null) && (getParent().getParent() != null)) {
			Edge pp = (Edge) ((GraphElementTreeNode) getParent().getParent())
					.get();
			if (pp.getNormalEdge() == e.getNormalEdge()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		int thisIdx = -1;
		int thatIdx = -1;

		if (getParent() != null) {
			thisIdx = getParent().getIndex(this) + 1;
		}

		VertexTreeNode child = (VertexTreeNode) getChildAt(0);
		Enumeration<GraphElementTreeNode> en = child.children();
		int i = 0;
		while (en.hasMoreElements()) {
			EdgeTreeNode etn = (EdgeTreeNode) en.nextElement();
			i++;
			if (etn.isBackEdge()) {
				thatIdx = i;
			}
		}

		String lfmt = "%"
				+ String.valueOf(
						(getParent() != null) ? getParent().getChildCount() : 2)
						.length() + "d";

		sb.append(String.format(lfmt, thisIdx));

		if (e.isNormal()) {
			if (e.getThatSemantics() != AggregationKind.NONE) {
				sb.append(" <>--> ");
			} else if (e.getThisSemantics() != AggregationKind.NONE) {
				sb.append(" --><> ");
			} else {
				sb.append(" ----> ");
			}
		} else {
			if (e.getThatSemantics() != AggregationKind.NONE) {
				sb.append(" <><-- ");
			} else if (e.getThisSemantics() != AggregationKind.NONE) {
				sb.append(" <--<> ");
			} else {
				sb.append(" <---- ");
			}
		}
		sb.append(thatIdx);
		sb.append(" | ");

		sb.append(e.toString());
		return sb.toString();
	}

	@Override
	public String getToolTipText() {
		StringBuilder sb = new StringBuilder();
		sb.append("<html>");
		String thisRole = e.getThisRole();
		String thatRole = e.getThatRole();
		if ((thisRole != null) && !thisRole.isEmpty()) {
			sb.append(thisRole);
		} else {
			sb.append("$noRole$");
		}
		sb.append(" ------- ");
		if ((thatRole != null) && !thatRole.isEmpty()) {
			sb.append(thatRole);
		} else {
			sb.append("$noRole$");
		}
		sb.append("<br/><br/>");
		sb.append(getAttributeString());
		sb.append("</html>");
		return sb.toString();
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