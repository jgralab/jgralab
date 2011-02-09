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

import java.util.ArrayList;
import java.util.HashMap;
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
public abstract class AbstractTemporaryGraphLayoutReader implements
		TemporaryGraphLayoutReader {

	/**
	 * Current temporary definition struct, which is read in.
	 */
	private TemporaryDefinitionStruct definition;

	/**
	 * List of all temporary definitions.
	 */
	private List<TemporaryDefinitionStruct> definitionList;

	/**
	 * List of all global variables.
	 */
	private Map<String, String> globalVariables;

	/**
	 * Creates a AbstractGraphLayoutReader for reading a graph layout and
	 * initializes all data structures.
	 */
	public AbstractTemporaryGraphLayoutReader() {
		initilizeStates();
	}

	/**
	 * Initialization all data structures to read in graph layouts as
	 * {@link TemporaryDefinitionStruct}s.
	 */
	protected void initilizeStates() {
		definitionList = new ArrayList<TemporaryDefinitionStruct>();
		globalVariables = new HashMap<String, String>();
	}

	/**
	 * Returns a temporary definition list of all read definitions.
	 * 
	 * @return Temporary definition list;
	 */
	@Override
	public List<TemporaryDefinitionStruct> getDefinitionList() {
		return definitionList;
	}

	/**
	 * A new definition with a name has been started.
	 * 
	 * @param definitionName
	 *            Name of the definition.
	 */
	protected void definitionStarted(String definitionName) {
		definition = new TemporaryDefinitionStruct();
		definition.name = definitionName;
		definitionList.add(definition);
	}

	/**
	 * The current definition has ended.
	 */
	protected void definitionEnded() {
		definition = null;
	}

	/**
	 * Checks, if this given String could be a global variable.
	 * 
	 * @param string
	 *            String to check.
	 * @return Returns true, iff the String is naming a global variable.
	 */
	protected boolean isGlobalVariable(String string) {
		// TODO Auto-generated method stub
		return string.charAt(0) == '@';
	}

	/**
	 * Processes a global variable field event and adds the variable to the
	 * global variable list.
	 * 
	 * @param name
	 *            Name of the field.
	 * @param value
	 *            Value of the field.
	 */
	public void processGlobalVariable(String name, String value) {
		if (name.startsWith("@")) {
			globalVariables.put(removeFirstChar(name), value);
		} else {
			throw new RuntimeException("Field " + name
					+ " does not have a '@' as prefix. Delete it or add an @.");
		}
	}

	/**
	 * Processes a definition attribute field by adding an attribute to the
	 * current temporary definition.
	 * 
	 * @param name
	 *            Name of the field.
	 * @param value
	 *            Value of the field.
	 */
	public void processDefinitionAttribute(String name, String value) {
		definition.addAttribute(name, value);
	}

	/**
	 * Removes the first character of a String.
	 * 
	 * @param name
	 *            String with an unwanted first character.
	 * @return String without the first character.
	 */
	private String removeFirstChar(String name) {
		return name.substring(1, name.length());
	}

	/**
	 * Returns a list of all read global variables.
	 * 
	 * @return List of global variables.
	 */
	@Override
	public Map<String, String> getGlobalVariables() {
		return globalVariables;
	}
}
