package de.uni_koblenz.jgralabtest.algolib;

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
