package de.uni_koblenz.jgralab.utilities.common.dot;

public enum GraphVizOutputFormat {

	POSTSCRIPT("ps"), SVG("svg"), SVG_ZIPPED("svgz"), PNG("png"), GIF("gif"), PDF(
			"pdf");

	public String name;

	GraphVizOutputFormat(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}
}
