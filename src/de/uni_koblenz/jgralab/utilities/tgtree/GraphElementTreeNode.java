package de.uni_koblenz.jgralab.utilities.tgtree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

import javax.swing.tree.TreeNode;

import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.schema.Attribute;

abstract class GraphElementTreeNode implements TreeNode {
	protected ArrayList<GraphElementTreeNode> incs;
	protected GraphElementTreeNode parent;

	protected abstract void init();

	protected abstract GraphElement get();

	protected GraphElementTreeNode(GraphElementTreeNode parent) {
		this.parent = parent;
	}

	@Override
	public Enumeration<GraphElementTreeNode> children() {
		init();
		return Collections.enumeration(incs);
	}

	@Override
	public boolean getAllowsChildren() {
		// TODO Auto-generated method stub
		init();
		return false;
	}

	@Override
	public TreeNode getChildAt(int childIndex) {
		init();
		return incs.get(childIndex);
	}

	@Override
	public int getChildCount() {
		init();
		return incs.size();
	}

	@Override
	public int getIndex(TreeNode node) {
		init();
		return incs.indexOf(node);
	}

	@Override
	public TreeNode getParent() {
		return parent;
	}

	@Override
	public boolean isLeaf() {
		init();
		return incs.isEmpty();
	}

	public abstract String getToolTipText();

	public String getAttributeString() {
		if (get().getAttributedElementClass().getAttributeList().isEmpty()) {
			return "$noAttrs$";
		}
		StringBuilder sb = new StringBuilder();
		for (Attribute attr : get().getAttributedElementClass()
				.getAttributeList()) {
			sb.append(attr.getName());
			sb.append(" = ");
			sb.append(get().getAttribute(attr.getName()));
			sb.append("<br>");
		}
		return sb.toString();
	}
}