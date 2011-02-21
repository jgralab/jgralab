/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2011 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 * 
 * For bug reports, documentation and further information, visit
 * 
 *                         http://jgralab.uni-koblenz.de
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
package de.uni_koblenz.jgralab.utilities.tgtree;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Image;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.schema.AggregationKind;

class GraphElementCellRenderer extends DefaultTreeCellRenderer {

	private static final long serialVersionUID = -1698523886275339684L;

	private static final Map<String, ImageIcon> ICON_CACHE = new HashMap<String, ImageIcon>();

	private static ImageIcon getIcon(String name) {
		ImageIcon icon = ICON_CACHE.get(name);
		if (icon == null) {
			icon = new ImageIcon(
					GraphElementCellRenderer.class.getResource("icons/" + name
							+ ".png"));
			// Scale the image to some smaller size
			icon.setImage(icon.getImage().getScaledInstance(24, 8,
					Image.SCALE_SMOOTH));
			ICON_CACHE.put(name, icon);
		}
		return icon;
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean sel, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {

		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf,
				row, hasFocus);
		GraphElementTreeNode getn = (GraphElementTreeNode) value;
		setToolTipText(getn.getToolTipText());
		setFont(new Font(Font.MONOSPACED, Font.PLAIN, tree.getFont().getSize()));
		if (getn instanceof EdgeTreeNode) {
			String iconName = null;
			Edge e = (Edge) ((EdgeTreeNode) getn).get();
			if (e.isNormal()) {
				if (e.getThatSemantics() == AggregationKind.SHARED) {
					iconName = "l_aggr_r";
				} else if (e.getThatSemantics() == AggregationKind.COMPOSITE) {
					iconName = "l_comp_r";
				} else if (e.getThisSemantics() == AggregationKind.SHARED) {
					iconName = "r_aggr_r";
				} else if (e.getThisSemantics() == AggregationKind.COMPOSITE) {
					iconName = "r_comp_r";
				} else {
					iconName = "edge_r";
				}
			} else {
				if (e.getThatSemantics() == AggregationKind.SHARED) {
					iconName = "l_aggr_l";
				} else if (e.getThatSemantics() == AggregationKind.COMPOSITE) {
					iconName = "l_comp_l";
				} else if (e.getThisSemantics() == AggregationKind.SHARED) {
					iconName = "r_aggr_l";
				} else if (e.getThisSemantics() == AggregationKind.COMPOSITE) {
					iconName = "r_comp_l";
				} else {
					iconName = "edge_l";
				}
			}
			setIcon(getIcon(iconName));
			EdgeTreeNode etn = (EdgeTreeNode) getn;
			if (etn.isBackEdge()) {
				setForeground(Color.LIGHT_GRAY);
			}
		} else if (getn instanceof VertexTreeNode) {
			setIcon(getIcon("vertex"));
		}

		return this;
	}
}
