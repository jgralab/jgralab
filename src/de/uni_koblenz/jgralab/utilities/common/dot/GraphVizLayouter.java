package de.uni_koblenz.jgralab.utilities.common.dot;

public enum GraphVizLayouter {

	DOT("dot"), NEATO("neato"), TWOPI("twopi"), CIRCO("circo"), FDP("fdp"), SFDP(
			"sfdp");

	public String name;

	GraphVizLayouter(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}
}
