package de.uni_koblenz.jgralab.utilities.tg2dot.json;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.PrettyPrinter;

/**
 * Implements several methods to control the pretty printing process of a
 * JsonProducer.
 * 
 * @author ist@uni-koblenz.de
 */
public class JsonPrettyPrinter implements PrettyPrinter {

	/**
	 * Stores the current depth in the nested data structure.
	 */
	private int nestesDepth;

	@Override
	public void beforeArrayValues(JsonGenerator arg0) throws IOException,
			JsonGenerationException {
		throw new RuntimeException("Not implemented!");
	}

	@Override
	public void beforeObjectEntries(JsonGenerator arg0) throws IOException,
			JsonGenerationException {
	}

	@Override
	public void writeArrayValueSeparator(JsonGenerator arg0)
			throws IOException, JsonGenerationException {
		throw new RuntimeException("Not implemented!");
	}

	@Override
	public void writeEndArray(JsonGenerator arg0, int arg1) throws IOException,
			JsonGenerationException {
		throw new RuntimeException("Not implemented!");
	}

	/**
	 * Marks the end of one object or scope.
	 */
	@Override
	public void writeEndObject(JsonGenerator arg0, int arg1)
			throws IOException, JsonGenerationException {

		nestesDepth--;
		newLine(arg0);
		intendate(arg0);
		arg0.writeRaw("}");
	}

	/**
	 * Is called, when a new Object is created and at least one Object already
	 * exists in the current scope.
	 */
	@Override
	public void writeObjectEntrySeparator(JsonGenerator arg0)
			throws IOException, JsonGenerationException {
		arg0.writeRaw(",\n");
		intendate(arg0);
	}

	/**
	 * Writes a colon between key and value of a key-value pair.
	 */
	@Override
	public void writeObjectFieldValueSeparator(JsonGenerator arg0)
			throws IOException, JsonGenerationException {
		arg0.writeRaw(" : ");
	}

	@Override
	public void writeRootValueSeparator(JsonGenerator arg0) throws IOException,
			JsonGenerationException {
		throw new RuntimeException("Not implemented!");
	}

	@Override
	public void writeStartArray(JsonGenerator arg0) throws IOException,
			JsonGenerationException {
		throw new RuntimeException("Not implemented!");
	}

	/**
	 * Is executed while an Object is started. Ensures the opening curly brace
	 * and the correct indentation of the coming element.
	 */
	@Override
	public void writeStartObject(JsonGenerator arg0) throws IOException,
			JsonGenerationException {
		arg0.writeRaw("{\n");
		nestesDepth++;
		intendate(arg0);
	}

	/**
	 * Ensures the correct indentation of according to the current nested depth.
	 * 
	 * @param arg0
	 *            The current used JsonGenerator.
	 * @throws JsonGenerationException
	 * @throws IOException
	 */
	private void intendate(JsonGenerator arg0) throws JsonGenerationException,
			IOException {
		for (int i = 0; i < nestesDepth; i++) {
			arg0.writeRaw("    ");
		}
	}

	/**
	 * Ensures a new line.
	 * 
	 * @param arg0
	 *            The current used JsonGenerator.
	 * @throws JsonGenerationException
	 * @throws IOException
	 */
	private void newLine(JsonGenerator arg0) throws JsonGenerationException,
			IOException {
		arg0.writeRaw('\n');
	}
}
