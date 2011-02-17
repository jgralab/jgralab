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
package de.uni_koblenz.jgralab.utilities.tg2dot.graph_layout.definition;

import java.util.List;

import de.uni_koblenz.jgralab.AttributedElement;

/**
 * A ElementDefinition is a description of a set of graph elements described by
 * GReQL-query and their style attributes.
 * 
 * @author ist@uni-koblenz.de
 */
public class ElementDefinition extends AbstractDefinition {

	/**
	 * GReQL-string describing the set of elements specified by this
	 * ElementDefinition.
	 */
	private String elementDefinitionQuery;

	/**
	 * List of all AttributedElements described by the GReQL-query stored in
	 * {@link ElementDefinition#elementDefinitionQuery}.
	 */
	protected List<AttributedElement> containedElements;

	/**
	 * 
	 */
	public ElementDefinition(String elementDefinitionQuery) {
		this.elementDefinitionQuery = elementDefinitionQuery;
	}

	/**
	 * Clones an ElementDefinition by initializing all data structures and
	 * copying all references.
	 * 
	 * @param definition
	 *            ElementDefinition, which is cloned.
	 */
	public ElementDefinition(ElementDefinition definition) {
		super(definition);
		elementDefinitionQuery = definition.elementDefinitionQuery;
	}

	/**
	 * Returns the GReQL-string describing the current set of
	 * {@link AttributedElement} this definition describes.
	 * 
	 * @return The GReQL-string.
	 */
	public String getGreqlString() {
		return elementDefinitionQuery;
	}

	/**
	 * Determines whether or not the given {@link AttributedElement} is included
	 * in this ElementDefinition.
	 * 
	 * @param element
	 *            AttributedElement in question.
	 * @return True, iff this definition contains the given element.
	 */
	public boolean hasElement(AttributedElement element) {
		return containedElements.contains(element);
	}

	/**
	 * Adds an AttributedElement to the list of contained elements.
	 * 
	 * @param element
	 *            {@link AttributedElement} to be added.
	 */
	public void add(AttributedElement element) {
		containedElements.add(element);
	}

	@Override
	public Definition clone() {
		return new ElementDefinition(this);
	}
}
