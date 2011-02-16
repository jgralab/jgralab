package de.uni_koblenz.jgralab.utilities.tg2image;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.utilities.common.dot.GraphVizProgram;
import de.uni_koblenz.jgralab.utilities.tg2dot.Tg2Dot;

/**
 * A convenience class to convert TGraphs to images.
 * 
 * @author ist@uni-koblenz.de
 */
public class Tg2Image {
	/**
	 * Converts a given Graph over the DOT format and a {@link GraphVizProgram}
	 * into an image to the provided path. Edges can be reversed for the hole
	 * graph or individually.
	 * 
	 * @param graph
	 *            Graph, which should be converted.
	 * @param program
	 *            A GraphVizProgram holding all needed parameters to executed
	 *            the selected GraphViz program.
	 * @param imageOutputPath
	 *            The image output path.
	 * @param reversedEdges
	 *            Flag to indicate the reversal of all edge directions.
	 * @param reversedEdgeTypes
	 *            Type of edges, which should be reversed.
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public static void convertGraph2ImageFile(Graph graph,
			GraphVizProgram program, String imageOutputPath,
			boolean reversedEdges,
			Class<? extends AttributedElement>... reversedEdgeTypes)
			throws InterruptedException, IOException {

		String executionString = String.format("%s%s -T%s -o%s", program.path,
				program.layouter, program.outputFormat, imageOutputPath);
		Tg2Dot.convertGraphPipeToProgram(graph, executionString, reversedEdges,
				reversedEdgeTypes);
	}

	/**
	 * Converts a given Graph over the DOT format and a {@link GraphVizProgram}
	 * into an {@link ImageIcon}. Edges can be reversed for the hole graph or
	 * individually.
	 * 
	 * @param graph
	 *            Graph, which should be converted.
	 * @param program
	 *            A GraphVizProgram holding all needed parameters to executed
	 *            the selected GraphViz program.
	 * @param reversedEdges
	 *            Flag to indicate the reversal of all edge directions.
	 * @param reversedEdgeTypes
	 *            Type of edges, which should be reversed.
	 * @return A loaded ImageIcon.
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public static ImageIcon convertGraph2ImageIcon(Graph graph,
			GraphVizProgram program, boolean reversedEdges,
			Class<? extends AttributedElement>... reversedEdgeTypes)
			throws InterruptedException, IOException {

		String executionString = String.format("%s%s -T%s", program.path,
				program.layouter, program.outputFormat);
		InputStream imageStream = Tg2Dot.convertGraphPipeToProgram(graph,
				executionString, reversedEdges, reversedEdgeTypes);

		return new ImageIcon(ImageIO.read(imageStream));
	}

	/**
	 * Converts a given Graph over the DOT format and a {@link GraphVizProgram}
	 * into a {@link BufferedInputStream}. Edges can be reversed for the hole
	 * graph or individually.
	 * 
	 * @param graph
	 *            Graph, which should be converted.
	 * @param program
	 *            A GraphVizProgram holding all needed parameters to executed
	 *            the selected GraphViz program.
	 * @param reversedEdges
	 *            Flag to indicate the reversal of all edge directions.
	 * @param reversedEdgeTypes
	 *            Type of edges, which should be reversed.
	 * @return
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public static BufferedInputStream convertGraph2ImageStream(Graph graph,
			GraphVizProgram program, boolean reversedEdges,
			Class<? extends AttributedElement>... reversedEdgeTypes)
			throws InterruptedException, IOException {

		String executionString = String.format("%s%s -T%s", program.path,
				program.layouter, program.outputFormat);
		return Tg2Dot.convertGraphPipeToProgram(graph, executionString,
				reversedEdges, reversedEdgeTypes);
	}
}
