/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2010 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
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
package de.uni_koblenz.jgralab.utilities.tg2dot.json;

import java.io.File;
import java.io.FileNotFoundException;

import de.uni_koblenz.jgralab.utilities.tg2dot.graph_layout.AbstractTemporaryGraphLayoutReader;
import de.uni_koblenz.jgralab.utilities.tg2dot.graph_layout.TemporaryGraphLayoutReader;

/**
 * Reads a graph layout as Json-file in and produces a list of
 * TemporaryDefinitionStructs and global variables.
 * 
 * @author ist@uni-koblenz.de
 */
public class JsonTemporaryGraphLayoutReader extends AbstractTemporaryGraphLayoutReader implements
		TemporaryGraphLayoutReader {

	/**
	 * Internal JsonReader to process to graph layout from a json-file.
	 */
	private InternalJsonReader jsonReader;

	/**
	 * Creates a JsonGraphLayoutReader and initializes the internal JsonReader.
	 */
	public JsonTemporaryGraphLayoutReader() {
		jsonReader = new InternalJsonReader();
	}

	@Override
	public void startProcessing(String path) throws FileNotFoundException {
		jsonReader.startProcessing(path);
	}

	@Override
	public void startProcessing(File file) throws FileNotFoundException {
		jsonReader.startProcessing(file);
	}

	/**
	 * Internal JsonReader implementing the necessary behavior to parse a graph
	 * layout as Json-file and forward the necessary calls to its
	 * JsonGraphLayoutReader.
	 * 
	 * @author ist@uni-koblenz.de
	 */
	private final class InternalJsonReader extends JsonReader {

		/**
		 * Creates a GraphLayoutReader for reading graph layout as Json-files.
		 */
		public InternalJsonReader() {
		}

		@Override
		protected void initializeAdditionalStates() {
		}

		@Override
		protected void startDocumentEvent() {
		}

		@Override
		protected void endDocumentEvent() {
		}

		/**
		 * Processes a object event by creating a new temporary definition with
		 * the given name in case of a nested depth of 2 and adds it to the list
		 * of temporary definitions.
		 */
		@Override
		protected void startObjectEvent(String name) {
			if (nestedDepth != 2) {
				throw new RuntimeException("Already Processing an Object!");
			}
			definitionStarted(name);
		}

		@Override
		protected void endObjectEvent() {
			definitionEnded();
		}

		@Override
		protected void fieldEvent(String name) {
			// Empty fields are ignored!
		}

		@Override
		protected void fieldEvent(String name, int value) {
			processFieldEvent(name, Integer.toString(value));
		}

		@Override
		protected void fieldEvent(String name, float value) {
			processFieldEvent(name, Float.toString(value));
		}

		@Override
		protected void fieldEvent(String name, boolean value) {
			processFieldEvent(name, Boolean.toString(value));

		}

		@Override
		protected void fieldEvent(String name, String value) {
			processFieldEvent(name, value);
		}

		/**
		 * Processes every field event and distinguishes between global
		 * variables fields and definition attribute fields.
		 * 
		 * @param name
		 *            Name of the field.
		 * @param value
		 *            Value of the field.
		 */
		private void processFieldEvent(String name, String value) {

			switch (jsonReader.nestedDepth) {
			case 1:
				processGlobalVariable(name, value);
				break;
			case 2:
				processDefinitionAttribute(name, value);
				break;
			default:
				throw new RuntimeException(
						"A field has been declared at an unwanted nested depth. This shouldn't happen.");
			}
		}

		@Override
		protected void startArrayEvent(String name) {
			// Nothing to do, but an exception is thrown. Just in case.
			throw new RuntimeException(
					"An Array has been started. This shouldn't happen.");
		}

		@Override
		protected void endArrayEvent() {
			// Nothing to do.
		}
	}
}
