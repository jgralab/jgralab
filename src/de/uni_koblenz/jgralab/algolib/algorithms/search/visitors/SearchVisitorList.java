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
package de.uni_koblenz.jgralab.algolib.algorithms.search.visitors;

import java.util.ArrayList;
import java.util.List;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmTerminatedException;
import de.uni_koblenz.jgralab.algolib.visitors.GraphVisitorList;
import de.uni_koblenz.jgralab.algolib.visitors.Visitor;

public class SearchVisitorList extends GraphVisitorList implements
		SearchVisitor {

	private List<SearchVisitor> visitors;

	public SearchVisitorList(){
		visitors = new ArrayList<SearchVisitor>();
	}

	@Override
	public void addVisitor(Visitor visitor) {
		super.addVisitor(visitor);
		if (visitor instanceof SearchVisitor) {
			if (!visitors.contains(visitor)) {
				visitors.add((SearchVisitor) visitor);
			}
		}
	}

	@Override
	public void removeVisitor(Visitor visitor) {
		super.removeVisitor(visitor);
		if (visitor instanceof SearchVisitor) {
			visitors.remove(visitor);
		}
	}

	@Override
	public void clearVisitors() {
		super.clearVisitors();
		visitors.clear();
	}

	@Override
	public void visitFrond(Edge e) throws AlgorithmTerminatedException {
		int n = visitors.size();
		for (int i = 0; i < n; i++) {
			visitors.get(i).visitFrond(e);
		}
	}

	@Override
	public void visitRoot(Vertex v) throws AlgorithmTerminatedException {
		int n = visitors.size();
		for (int i = 0; i < n; i++) {
			visitors.get(i).visitRoot(v);
		}
	}

	@Override
	public void visitTreeEdge(Edge e) throws AlgorithmTerminatedException {
		int n = visitors.size();
		for (int i = 0; i < n; i++) {
			visitors.get(i).visitTreeEdge(e);
		}
	}
}
