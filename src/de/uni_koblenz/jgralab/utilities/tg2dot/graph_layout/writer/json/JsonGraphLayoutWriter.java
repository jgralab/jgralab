package de.uni_koblenz.jgralab.utilities.tg2dot.graph_layout.writer.json;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.jackson.JsonGenerationException;

import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.utilities.common.json.JsonWriter;
import de.uni_koblenz.jgralab.utilities.tg2dot.graph_layout.GraphLayout;
import de.uni_koblenz.jgralab.utilities.tg2dot.graph_layout.definition.Definition;
import de.uni_koblenz.jgralab.utilities.tg2dot.graph_layout.definition.ElementDefinition;
import de.uni_koblenz.jgralab.utilities.tg2dot.graph_layout.definition.TypeDefinition;
import de.uni_koblenz.jgralab.utilities.tg2dot.graph_layout.writer.AbstractGraphLayoutWriter;

/**
 * Provides a the functionality to write a GraphLayout as Json-file.
 * 
 * @author ist@uni-koblenz.de
 */
public class JsonGraphLayoutWriter extends AbstractGraphLayoutWriter {

	/**
	 * JsonWriter providing convenience functions to write a Json-file.
	 */
	private JsonWriter writer;

	/**
	 * Constructs a JsonGraphLayoutWriter.
	 */
	public JsonGraphLayoutWriter() {
	}

	@Override
	protected void initializeResources(String filename) {
		writer = new JsonWriter(new File(filename));
	}

	@Override
	protected void writeConstants(GraphLayout layout)
			throws JsonGenerationException, IOException {
		for (Entry<String, String> entry : layout.getGlobalVariables()
				.entrySet()) {
			writer.field(entry.getKey(), entry.getValue());
		}
	}

	@Override
	protected void writeTypeDefinitions(GraphLayout layout)
			throws JsonGenerationException, IOException {

		writeTypeDefinitions(layout.getVertexTypeDefinitions());
		writeTypeDefinitions(layout.getEdgeTypeDefinitions());
	}

	@Override
	protected void writeTypeDefinitions(
			Map<AttributedElementClass, TypeDefinition> definitions)
			throws JsonGenerationException, IOException {
		for (Entry<AttributedElementClass, TypeDefinition> entry : definitions
				.entrySet()) {
			writeDefinition(entry.getKey().getQualifiedName(), entry.getValue());
		}
	}

	@Override
	protected void writeElementDefinitions(GraphLayout layout)
			throws JsonGenerationException, IOException {

		for (ElementDefinition definition : layout.getElementDefinitions()) {
			writeDefinition(definition.getGreqlString(), definition);
		}
	}

	/**
	 * Writes a definition as Json-object to the file.
	 */
	@Override
	protected void writeDefinition(String name, Definition definition)
			throws JsonGenerationException, IOException {
		writer.startObject(name);
		for (String attributeName : definition.getAttributeNames()) {
			writer.field(attributeName,
					definition.getAttributeValue(attributeName));
		}
		writer.endObject();
	}

	@Override
	protected void releaseResources() throws IOException {
		writer.close();
	}
}
