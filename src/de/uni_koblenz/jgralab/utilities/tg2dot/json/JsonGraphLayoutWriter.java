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
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.jackson.JsonGenerationException;

import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.utilities.tg2dot.graph_layout.GraphLayout;
import de.uni_koblenz.jgralab.utilities.tg2dot.graph_layout.definition.Definition;
import de.uni_koblenz.jgralab.utilities.tg2dot.graph_layout.definition.ElementDefinition;
import de.uni_koblenz.jgralab.utilities.tg2dot.graph_layout.definition.TypeDefinition;

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
