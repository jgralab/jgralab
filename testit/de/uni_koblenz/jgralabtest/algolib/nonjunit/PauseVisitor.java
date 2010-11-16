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

import java.util.Scanner;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.algorithms.search.visitors.DFSVisitorAdapter;

public class PauseVisitor extends DFSVisitorAdapter {

	private boolean firstRootVisited;
	private Scanner scanner;

	public PauseVisitor() {
		scanner = new Scanner(System.in);
	}

	private void pause() {
		scanner.nextLine();
	}

	@Override
	public void leaveTreeEdge(Edge e) {
		pause();
	}

	@Override
	public void leaveVertex(Vertex v) {
		pause();
	}

	@Override
	public void visitBackwardArc(Edge e) {
		pause();
	}

	@Override
	public void visitCrosslink(Edge e) {
		pause();
	}

	@Override
	public void visitForwardArc(Edge e) {
		pause();
	}

	@Override
	public void visitFrond(Edge e) {
		pause();
	}

	@Override
	public void visitRoot(Vertex v) {
		if (!firstRootVisited) {
			System.out
					.println("Pause visitor enabled, please press enter after each step.");
			firstRootVisited = true;
		}
	}

	@Override
	public void visitTreeEdge(Edge e) {
		pause();
	}

	@Override
	public void visitEdge(Edge e) {
		pause();
	}

	@Override
	public void visitVertex(Vertex v) {
		pause();
	}

	@Override
	public void reset() {
		firstRootVisited = false;
	}

}
