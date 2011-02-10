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
import java.util.Enumeration;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.GraphElement;

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
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("%3d", thisIdx));
		sb.append('/');
		sb.append(String.format("%3d", thatIdx));
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
