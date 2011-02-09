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
