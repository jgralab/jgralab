package de.uni_koblenz.jgralab.utilities.tg2dot.graph_layout;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;

import de.uni_koblenz.jgralab.utilities.tg2dot.graph_layout.TemporaryGraphLayoutReader;
import de.uni_koblenz.jgralab.utilities.tg2dot.graph_layout.definition.TemporaryDefinitionStruct;

/**
 * Reads a graph layout in and produces a list of TemporaryDefinitionStructs and
 * global variables.
 * 
 * @author ist@uni-koblenz.de
 */
public interface TemporaryGraphLayoutReader {

	/**
	 * Returns a temporary definition list of all read definitions.
	 * 
	 * @return Temporary definition list;
	 */
	public List<TemporaryDefinitionStruct> getDefinitionList();

	/**
	 * Returns a list of all read global variables.
	 * 
	 * @return List of global variables.
	 */
	public Map<String, String> getGlobalVariables();

	/**
	 * Starts the processing of the specified file.
	 * 
	 * @param path
	 *            Path to the graph layout file.
	 */
	public void startProcessing(String path) throws FileNotFoundException;

	/**
	 * Starts the processing of the specified file.
	 * 
	 * @param file
	 *            Path to the graph layout file.
	 * @throws FileNotFoundException
	 */
	public void startProcessing(File file) throws FileNotFoundException;
}
