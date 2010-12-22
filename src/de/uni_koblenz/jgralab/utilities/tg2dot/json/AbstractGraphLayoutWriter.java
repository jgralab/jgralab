package de.uni_koblenz.jgralab.utilities.tg2dot.json;

import java.io.IOException;
import java.util.Map;

import org.codehaus.jackson.JsonGenerationException;

import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.utilities.tg2dot.graph_layout.GraphLayout;
import de.uni_koblenz.jgralab.utilities.tg2dot.graph_layout.definition.Definition;
import de.uni_koblenz.jgralab.utilities.tg2dot.graph_layout.definition.ElementDefinition;
import de.uni_koblenz.jgralab.utilities.tg2dot.graph_layout.definition.TypeDefinition;

/**
 * Provides a the functionality to write a GraphLayout into a file.
 * 
 * @author ist@uni-koblenz.de
 */
public abstract class AbstractGraphLayoutWriter {

	/**
	 * Writes a GraphLayout to a Json-file.
	 * 
	 * @param filename
	 *            Json-file name.
	 * @param layout
	 *            GraphLayout, which is written out.
	 */
	public static void writeLayoutToAJsonFile(String filename,
			GraphLayout layout) {
		JsonGraphLayoutWriter writer = new JsonGraphLayoutWriter();
		try {
			writer.startProcessing(filename, layout);
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Writes a definition to the file.
	 * 
	 * @param name
	 *            Name of the Definition.
	 * @param definition
	 *            Definition, which is written out.
	 * @throws JsonGenerationException
	 * @throws IOException
	 */
	protected abstract void writeDefinition(String name, Definition definition)
			throws JsonGenerationException, IOException;

	/**
	 * Writes all {@link ElementDefinition}s to the file.
	 * 
	 * @param layout
	 *            GraphLayout, which is written out.
	 * @throws JsonGenerationException
	 * @throws IOException
	 */
	protected abstract void writeElementDefinitions(GraphLayout layout)
			throws JsonGenerationException, IOException;

	/**
	 * Writes all TypeDefinitions to the file.
	 * 
	 * @param definitions
	 *            Map of {@link AttributedElementClass} as key and
	 *            {@link TypeDefinition} as values.
	 * @throws JsonGenerationException
	 * @throws IOException
	 */
	protected abstract void writeTypeDefinitions(
			Map<AttributedElementClass, TypeDefinition> definitions)
			throws JsonGenerationException, IOException;

	/**
	 * Writes all TypeDefinitions to the file.
	 * 
	 * @param layout
	 *            GraphLayout, which is written out.
	 * @throws JsonGenerationException
	 * @throws IOException
	 */
	protected abstract void writeTypeDefinitions(GraphLayout layout)
			throws JsonGenerationException, IOException;

	/**
	 * Writes all constants to the file.
	 * 
	 * @param layout
	 *            GraphLayout, which is written out.
	 * @throws JsonGenerationException
	 * @throws IOException
	 */
	protected abstract void writeConstants(GraphLayout layout)
			throws JsonGenerationException, IOException;

	/**
	 * Starts the processing of the GraphLayout to the file.
	 * 
	 * @param layout
	 *            GraphLayout, which is written out.
	 * @throws JsonGenerationException
	 * @throws IOException
	 */
	public void startProcessing(String filename, GraphLayout layout)
			throws JsonGenerationException, IOException {
		initializeResources(filename);
		writeConstants(layout);
		writeTypeDefinitions(layout);
		writeElementDefinitions(layout);
		releaseResources();
	}

	/**
	 * Releases all used resources.
	 * 
	 * @throws IOException
	 */
	protected abstract void releaseResources() throws IOException;

	/**
	 * Initializes all resources.
	 * 
	 * @param filename
	 *            File name.
	 */
	protected abstract void initializeResources(String filename);
}