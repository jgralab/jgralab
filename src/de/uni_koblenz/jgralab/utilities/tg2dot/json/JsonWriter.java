package de.uni_koblenz.jgralab.utilities.tg2dot.json;

import java.io.File;
import java.io.IOException;

import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonGenerator.Feature;
import org.codehaus.jackson.JsonParseException;

/**
 * JsonWriter provides several convenience methods to write a Json-file.
 * However, this class is not completely implemented.
 * 
 * @author ist@uni-koblenz.de
 */
public class JsonWriter {

	/**
	 * JsonGenerator for creating a Json-file.
	 */
	JsonGenerator generator;

	/**
	 * Constructs a JsonWriter for writing a Json-file.
	 */
	public JsonWriter(File path) {
		try {
			initializeWriter(path);
			startDocument();
		} catch (IOException e) {
			System.out.println("\nError: " + e.getLocalizedMessage());
			System.out
					.println("Error: Processing of the JSON-file stopped. IO");
			throw new RuntimeException(e);
		}
	}

	/**
	 * Initializes the JsonWriter and the underlying JsonGenerator.
	 * 
	 * @throws JsonParseException
	 * @throws IOException
	 */
	private void initializeWriter(File sourceFile) throws IOException {
		JsonFactory factory = new JsonFactory();

		factory.setGeneratorFeature(Feature.AUTO_CLOSE_JSON_CONTENT, false);
		factory.setGeneratorFeature(Feature.AUTO_CLOSE_TARGET, false);
		generator = factory.createJsonGenerator(sourceFile, JsonEncoding.UTF8);
		generator.setPrettyPrinter(new JsonPrettyPrinter());
	}

	/**
	 * Starts this document.
	 * 
	 * @throws IOException
	 * @throws JsonGenerationException
	 */
	private void startDocument() throws JsonGenerationException, IOException {
		generator.writeStartObject();
	}

	/**
	 * Ends a document.
	 * 
	 * @throws IOException
	 * @throws JsonGenerationException
	 */
	private void endDocument() throws JsonGenerationException, IOException {
		generator.writeEndObject();
	}

	/**
	 * Starts a named object.
	 * 
	 * @param name
	 *            Name of the object.
	 * @throws IOException
	 * @throws JsonGenerationException
	 */
	public void startObject(String name) throws JsonGenerationException,
			IOException {
		generator.writeObjectFieldStart(name);

	}

	/**
	 * Ends the current object.
	 * 
	 * @throws IOException
	 * @throws JsonGenerationException
	 */
	public void endObject() throws JsonGenerationException, IOException {
		generator.writeEndObject();
	}

	/**
	 * Starts a named array.
	 * 
	 * @param name
	 *            Name of the array.
	 * @throws IOException
	 * @throws JsonGenerationException
	 */
	public void startArray(String name) throws JsonGenerationException,
			IOException {
		generator.writeArrayFieldStart(name);
	}

	/**
	 * Ends the current array.
	 * 
	 * @throws IOException
	 * @throws JsonGenerationException
	 */
	public void endArray() throws JsonGenerationException, IOException {
		generator.writeEndArray();

	}

	/**
	 * Creates a named empty field.
	 * 
	 * @param name
	 *            Name of the field.
	 * @throws IOException
	 * @throws JsonGenerationException
	 */
	public void field(String name) throws JsonGenerationException, IOException {
		generator.writeNullField(name);
	}

	/**
	 * Creates a named integer field.
	 * 
	 * @param name
	 *            Name of the field.
	 * @param value
	 *            Integer value of the field.
	 * @throws IOException
	 * @throws JsonGenerationException
	 */
	public void field(String name, int value) throws JsonGenerationException,
			IOException {
		generator.writeNumberField(name, value);
	}

	/**
	 * Creates a named float field.
	 * 
	 * @param name
	 *            Name of the field.
	 * @param value
	 *            Float value of the field.
	 * @throws IOException
	 * @throws JsonGenerationException
	 */
	public void field(String name, float value) throws JsonGenerationException,
			IOException {
		generator.writeNumberField(name, value);
	}

	/**
	 * Creates a named boolean field.
	 * 
	 * @param name
	 *            Name of the field.
	 * @param value
	 *            Boolean value of the field.
	 * @throws IOException
	 * @throws JsonGenerationException
	 */
	public void field(String name, boolean value)
			throws JsonGenerationException, IOException {
		generator.writeBooleanField(name, value);
	}

	/**
	 * Creates a named String field.
	 * 
	 * @param name
	 *            Name of the Field.
	 * @param value
	 *            String value of the Field.
	 * @throws IOException
	 * @throws JsonGenerationException
	 */
	public void field(String name, String value)
			throws JsonGenerationException, IOException {
		generator.writeStringField(name, value);
	}

	/**
	 * Ends the document and closes the used {@link JsonGenerator}.
	 * 
	 * @throws IOException
	 */
	public void close() throws IOException {
		endDocument();
		generator.flush();
		generator.close();
	}
}
