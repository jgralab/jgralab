package de.uni_koblenz.jgralab.utilities.tgtree;

import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

class GraphElementCellRenderer extends DefaultTreeCellRenderer {

	private static final long serialVersionUID = -1698523886275339684L;

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean sel, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {

		super.getTreeCellRendererComponent(tree, value, sel, expanded,
				leaf, row, hasFocus);
		GraphElementTreeNode getn = (GraphElementTreeNode) value;
		setToolTipText(getn.getToolTipText());

		if (getn instanceof EdgeTreeNode) {
			setIcon(null);
		}

		return this;
	}
}