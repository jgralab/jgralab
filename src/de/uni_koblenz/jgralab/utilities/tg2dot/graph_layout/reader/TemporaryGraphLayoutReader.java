package de.uni_koblenz.jgralab.utilities.tg2dot.graph_layout.reader;

import java.io.File;
import java.io.FileNotFoundException;

import de.uni_koblenz.jgralab.utilities.tg2dot.graph_layout.GraphLayout;
import de.uni_koblenz.jgralab.utilities.tg2dot.graph_layout.reader.TemporaryGraphLayoutReader;

/**
 * Reads a graph layout in and produces a list of TemporaryDefinitionStructs and
 * global variables.
 * 
 * @author ist@uni-koblenz.de
 */
public interface TemporaryGraphLayoutReader {

	/**
	 * Starts the processing of the specified file.
	 * 
	 * @param path
	 *            Path to the graph layout file.
	 */
	public void startProcessing(String path, GraphLayout graphLayoutFactory)
			throws FileNotFoundException;

	/**
	 * Starts the processing of the specified file.
	 * 
	 * @param file
	 *            Path to the graph layout file.
	 * @param graphLayoutFactory
	 * @throws FileNotFoundException
	 */
	public void startProcessing(File file, GraphLayout graphLayoutFactory)
			throws FileNotFoundException;
}
