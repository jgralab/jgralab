/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2010 Institute for Software Technology
 *               University of Koblenz-Landau, Germany
 * 
 *               ist@uni-koblenz.de
 * 
 * Please report bugs to http://serres.uni-koblenz.de/bugzilla
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package de.uni_koblenz.jgralab.algolib.algorithms.search.visitors;

import java.util.ArrayList;
import java.util.List;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmTerminatedException;
import de.uni_koblenz.jgralab.algolib.visitors.Visitor;

public class DFSVisitorComposition extends SearchVisitorComposition implements
		DFSVisitor {

	private List<DFSVisitor> visitors;

	public DFSVisitorComposition(){
		visitors = new ArrayList<DFSVisitor>();
	}

	@Override
	public void addVisitor(Visitor visitor) {
		super.addVisitor(visitor);
		if (visitor instanceof DFSVisitor) {
			if (!visitors.contains(visitor)) {
				visitors.add((DFSVisitor) visitor);
			}
		}
	}

	@Override
	public void removeVisitor(Visitor visitor) {
		super.removeVisitor(visitor);
		if (visitor instanceof DFSVisitor) {
			visitors.remove(visitor);
		}
	}

	@Override
	public void clearVisitors() {
		super.clearVisitors();
		visitors.clear();
	}

	@Override
	public void leaveTreeEdge(Edge e) throws AlgorithmTerminatedException {
		int n = visitors.size();
		for (int i = 0; i < n; i++) {
			visitors.get(i).leaveTreeEdge(e);
		}
	}

	@Override
	public void leaveVertex(Vertex v) throws AlgorithmTerminatedException {
		int n = visitors.size();
		for (int i = 0; i < n; i++) {
			visitors.get(i).leaveVertex(v);
		}
	}

	@Override
	public void visitBackwardArc(Edge e) throws AlgorithmTerminatedException {
		int n = visitors.size();
		for (int i = 0; i < n; i++) {
			visitors.get(i).visitBackwardArc(e);
		}
	}

	@Override
	public void visitCrosslink(Edge e) throws AlgorithmTerminatedException {
		int n = visitors.size();
		for (int i = 0; i < n; i++) {
			visitors.get(i).visitCrosslink(e);
		}
	}

	@Override
	public void visitForwardArc(Edge e) throws AlgorithmTerminatedException {
		int n = visitors.size();
		for (int i = 0; i < n; i++) {
			visitors.get(i).visitForwardArc(e);
		}
	}
}
