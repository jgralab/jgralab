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
package de.uni_koblenz.jgralabtest.algolib.nonjunit;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.algorithms.GraphAlgorithm;
import de.uni_koblenz.jgralab.algolib.algorithms.search.SearchAlgorithm;
import de.uni_koblenz.jgralab.algolib.algorithms.search.visitors.DFSVisitor;
import de.uni_koblenz.jgralab.algolib.algorithms.search.visitors.SearchVisitorAdapter;
import de.uni_koblenz.jgralab.algolib.functions.IntFunction;

public class DebugSearchVisitor extends SearchVisitorAdapter implements
		DFSVisitor {

	public static String generateEdgeString(Edge e) {
		if (e == null) {
			return null;
		}
		StringBuilder out = new StringBuilder();
		out.append("(");
		out.append(e.getThis());
		out.append(")");
		if (!e.isNormal()) {
			out.append("<");
		}
		out.append("--");
		out.append("(");
		out.append(e);
		out.append(")");
		out.append("--");
		if (e.isNormal()) {
			out.append(">");
		}
		out.append("(");
		out.append(e.getThat());
		out.append(")");
		// return "(" + e.getThis() + ")" + "--" + "(" + e + ")" + "-->" + "("
		// + e.getThat() + ")";
		return out.toString();
	}

	protected IntFunction<Vertex> level;

	private void printIndent(Vertex v) {
		int amount = level.isDefined(v) ? level.get(v) : 0;
		for (int i = 0; i < amount; i++) {
			System.out.print("     ");
		}
	}

	@Override
	public void leaveTreeEdge(Edge e) {
		printIndent(e.getThis());
		System.out.println("Leaving tree edge " + generateEdgeString(e));
	}

	@Override
	public void leaveVertex(Vertex v) {
		printIndent(v);
		System.out.println("Leaving vertex " + v);
	}

	@Override
	public void visitBackwardArc(Edge e) {
		printIndent(e.getThis());
		System.out.println("Visiting backward arc " + generateEdgeString(e));
	}

	@Override
	public void visitCrosslink(Edge e) {
		printIndent(e.getThis());
		System.out.println("Visiting crosslink " + generateEdgeString(e));
	}

	@Override
	public void visitForwardArc(Edge e) {
		printIndent(e.getThis());
		System.out.println("Visiting forward arc " + generateEdgeString(e));
	}

	@Override
	public void visitFrond(Edge e) {
		printIndent(e.getThis());
		System.out.println("Visiting frond " + generateEdgeString(e));
	}

	@Override
	public void visitRoot(Vertex v) {
		printIndent(v);
		System.out.println("Visiting root " + v);
	}

	@Override
	public void visitTreeEdge(Edge e) {
		printIndent(e.getThis());
		System.out.println("Visiting tree edge " + generateEdgeString(e));
	}

	@Override
	public void visitEdge(Edge e) {
		printIndent(e.getThis());
		System.out.println("Visiting edge " + generateEdgeString(e));
	}

	@Override
	public void visitVertex(Vertex v) {
		printIndent(v);
		System.out.println("Visiting vertex " + v);
	}

	@Override
	public void reset() {
		level = algorithm.getInternalLevel();
	}

	@Override
	public void setAlgorithm(GraphAlgorithm alg) {
		super.setAlgorithm(((SearchAlgorithm) alg).withLevel());
		reset();
	}

}
