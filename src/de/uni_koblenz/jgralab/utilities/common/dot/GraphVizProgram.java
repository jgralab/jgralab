package de.uni_koblenz.jgralab.utilities.common.dot;

public class GraphVizProgram {

	/**
	 * Must end with "/". For example: "GraphViz/bin/"
	 */
	public String path;
	public GraphVizLayouter layouter;
	public GraphVizOutputFormat outputFormat;

	public GraphVizProgram() {
		path = "";
		layouter = GraphVizLayouter.DOT;
		outputFormat = GraphVizOutputFormat.PDF;
	}

	public GraphVizProgram path(String path) {
		this.path = path;
		return this;
	}

	public GraphVizProgram layouter(GraphVizLayouter layouter) {
		this.layouter = layouter;
		return this;
	}

	public GraphVizProgram outputFormat(GraphVizOutputFormat outputFormat) {
		this.outputFormat = outputFormat;
		return this;
	}

}
