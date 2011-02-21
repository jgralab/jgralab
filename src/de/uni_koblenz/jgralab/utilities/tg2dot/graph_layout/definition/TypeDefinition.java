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
package de.uni_koblenz.jgralab.utilities.tg2dot.graph_layout.definition;

import de.uni_koblenz.jgralab.schema.AttributedElementClass;

/**
 * A TypeDefinition is a description of graph elements of a specific schema type
 * and their style attributes.
 * 
 * @author ist@uni-koblenz.de
 */
public class TypeDefinition extends AbstractDefinition {

	/**
	 * The {@link AttributedElementClass} this definition describes.
	 */
	private AttributedElementClass typeClass;

	/**
	 * Constructs a TypeDefinition for a {@link AttributedElementClass} and
	 * initializes all data structures.
	 * 
	 * @param type
	 *            {@link AttributedElementClass} this definition describes.
	 */
	public TypeDefinition(AttributedElementClass type) {
		super();

		typeClass = type;
	}

	/**
	 * Clones an existing TypeDefinition by initializing new data structures and
	 * copying all used references.
	 * 
	 * @param definition
	 *            TypeDefinition, which should be cloned.
	 */
	public TypeDefinition(TypeDefinition definition) {
		super(definition);
		typeClass = definition.typeClass;
	}

	/**
	 * Returns the AttributedElementClass this TypeDefinition defines.
	 * 
	 * @return Associated {@link AttributedElementClass}.
	 */
	public AttributedElementClass getTypeClass() {
		return typeClass;
	}

	@Override
	public TypeDefinition clone() {
		return new TypeDefinition(this);
	}
}
