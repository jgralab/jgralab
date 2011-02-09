package de.uni_koblenz.jgralab.utilities.tgtree;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

class TGraphTreeModel extends DefaultTreeModel {
	private static final long serialVersionUID = -8084955962460964630L;

	public TGraphTreeModel(TreeNode root) {
		super(root);
	}
}