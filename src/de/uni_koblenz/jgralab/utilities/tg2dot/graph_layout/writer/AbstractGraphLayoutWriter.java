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
package de.uni_koblenz.jgralab.utilities.tg2dot.graph_layout.writer;

import java.io.IOException;
import java.util.Map;

import org.codehaus.jackson.JsonGenerationException;

import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.utilities.tg2dot.graph_layout.GraphLayout;
import de.uni_koblenz.jgralab.utilities.tg2dot.graph_layout.definition.Definition;
import de.uni_koblenz.jgralab.utilities.tg2dot.graph_layout.definition.ElementDefinition;
import de.uni_koblenz.jgralab.utilities.tg2dot.graph_layout.definition.TypeDefinition;
import de.uni_koblenz.jgralab.utilities.tg2dot.graph_layout.writer.json.JsonGraphLayoutWriter;

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
