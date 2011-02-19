/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2011 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 * 
 * For bug reports, documentation and further information, visit
 * 
 *                         http://jgralab.uni-koblenz.de
 * 
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 3 of the License, or (at your
 * option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see <http://www.gnu.org/licenses>.
 * 
 * Additional permission under GNU GPL version 3 section 7
 * 
 * If you modify this Program, or any covered work, by linking or combining
 * it with Eclipse (or a modified version of that program or an Eclipse
 * plugin), containing parts covered by the terms of the Eclipse Public
 * License (EPL), the licensors of this Program grant you additional
 * permission to convey the resulting work.  Corresponding Source for a
 * non-source form of such a combination shall include the source code for
 * the parts of JGraLab used as well as that of the covered work.
 */
package de.uni_koblenz.jgralab.utilities.common.json;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

/**
 * JsonReader is a event based Reader and provides throw extension several
 * methods for parsing. It uses on a token based Json-Parser to process a
 * Json-file.
 * 
 * @author ist@uni-koblenz.de
 */
public abstract class JsonReader {

	/**
	 * JsonParser for processing a Json-file. It is token-based.
	 */
	JsonParser parser;

	/**
	 * Counts the nestedDepth at the current position.
	 */
	private int nestingDepth;

	/**
	 * Returns the nesting depth at the current position.
	 * 
	 * @return
	 */
	public int getNestingDepth() {
		return nestingDepth;
	}

	/**
	 * Creates a JsonReader for reading a Json-file.
	 */
	public JsonReader() {
	}

	/**
	 * Starts the processing of the specified file.
	 * 
	 * @param path
	 *            Path to the Json-file.
	 */
	public void startProcessing(String path) throws FileNotFoundException {
		startProcessing(new File(path));
	}

	/**
	 * Starts the processing of the specified file.
	 * 
	 * @param path
	 *            Path to the Json-file.
	 */
	public void startProcessing(File path) throws FileNotFoundException {
		try {
			initializeAdditionalStates();
			initializeReader(path);
			iterateOverAllElements();
		} catch (JsonParseException e) {
			throw new RuntimeException("Error: " + e.getLocalizedMessage()
					+ "Error: Processing of the JSON-file stopped.", e);
		} catch (FileNotFoundException e) {
			throw e;
		} catch (IOException e) {
			throw new RuntimeException("Error: " + e.getLocalizedMessage()
					+ "Error: Processing of the JSON-file stopped.", e);
		}
	}

	/**
	 * Initializes the JsonReader and the underlying JsonParser.
	 * 
	 * @throws JsonParseException
	 * @throws IOException
	 */
	private void initializeReader(File sourceFile) throws JsonParseException,
			IOException {
		nestingDepth = 0;

		JsonFactory factory = new JsonFactory();
		parser = factory.createJsonParser(sourceFile);
	}

	/**
	 * Initializes all additional states.
	 */
	protected abstract void initializeAdditionalStates();

	/**
	 * Iterates of all token of the JsonParser and parses them.
	 * 
	 * @throws JsonParseException
	 * @throws IOException
	 */
	private void iterateOverAllElements() throws JsonParseException,
			IOException {
		JsonToken token;

		do {

			token = parser.nextToken();

			switch (token) {
			case START_OBJECT:
				// can only be the beginning of the document otherwise it would
				// be parsed by the method "parseCurrentFieldEventToken"
				nestingDepth++;
				startDocumentEvent();
				break;
			case END_OBJECT:
				nestingDepth--;
				if (nestingDepth == 0) {
					endDocumentEvent();
				} else {
					endObjectEvent();
				}
				break;
			case END_ARRAY:
				endArrayEvent();
				break;
			case FIELD_NAME:
				parseCurrentFieldEventToken(parser.getCurrentName());
				break;

			default:
				throw new RuntimeException(token + " has not been considert!");
			}
		} while (nestingDepth != 0);
	}

	/**
	 * Parses the current field event token.
	 * 
	 * @param name
	 *            Name of the current element.
	 * @throws JsonParseException
	 * @throws IOException
	 */
	private void parseCurrentFieldEventToken(String name)
			throws JsonParseException, IOException {
		JsonToken token = parser.nextToken();
		switch (token) {
		case START_OBJECT:
			nestingDepth++;
			startObjectEvent(name);
			break;
		case START_ARRAY:
			startArrayEvent(name);
		case VALUE_STRING:
			fieldEvent(name, parser.getText());
			break;
		case VALUE_NUMBER_INT:
			fieldEvent(name, parser.getIntValue());
			break;
		case VALUE_NUMBER_FLOAT:
			fieldEvent(name, parser.getFloatValue());
			break;
		case VALUE_FALSE:
			fieldEvent(name, false);
			break;
		case VALUE_TRUE:
			fieldEvent(name, true);
			break;
		case VALUE_NULL:
			fieldEvent(name);
			break;
		case NOT_AVAILABLE:
			throw new RuntimeException(
					"Attribute \"Not Available\" not implemented!");
		default:
			throw new RuntimeException(token + " has not been considert!");
		}
	}

	/**
	 * The document has started.
	 */
	protected abstract void startDocumentEvent();

	/**
	 * The document has ended.
	 */
	protected abstract void endDocumentEvent();

	/**
	 * A named object has started.
	 * 
	 * @param name
	 *            Name of the object.
	 */
	protected abstract void startObjectEvent(String name);

	/**
	 * The current object has ended.
	 */
	protected abstract void endObjectEvent();

	/**
	 * A named array has started.
	 * 
	 * @param name
	 *            Name of the array.
	 */
	protected abstract void startArrayEvent(String name);

	/**
	 * The current array has ended.
	 */
	protected abstract void endArrayEvent();

	/**
	 * A named empty field has occurred.
	 * 
	 * @param name
	 *            Name of the field.
	 */
	protected abstract void fieldEvent(String name);

	/**
	 * A named integer field has occurred.
	 * 
	 * @param name
	 *            Name of the field.
	 * @param value
	 *            Integer value of the field.
	 */
	protected abstract void fieldEvent(String name, int value);

	/**
	 * A named float field has occurred.
	 * 
	 * @param name
	 *            Name of the field.
	 * @param value
	 *            Float value of the field.
	 */
	protected abstract void fieldEvent(String name, float value);

	/**
	 * A named boolean field has occurred.
	 * 
	 * @param name
	 *            Name of the field.
	 * @param value
	 *            Boolean value of the field.
	 */
	protected abstract void fieldEvent(String name, boolean value);

	/**
	 * A named String field has occurred.
	 * 
	 * @param name
	 *            Name of the Field.
	 * @param value
	 *            String value of the Field.
	 */
	protected abstract void fieldEvent(String name, String value);
}
