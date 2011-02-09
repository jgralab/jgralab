/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2010 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
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

	public String getClipboardText() {
		return get().getAttributedElementClass().getQualifiedName();
	}

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
