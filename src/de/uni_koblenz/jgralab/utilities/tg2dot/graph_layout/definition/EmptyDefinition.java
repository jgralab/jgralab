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

import java.util.HashSet;
import java.util.Set;

/**
 * An EmptyDefinition provides a description of an {@link Definition} which
 * couldn't be transformed from a {@link TemporaryDefinitionStruct} to a
 * {@link TypeDefinition} or an {@link ElementDefinition}.
 * 
 * @author ist@uni-koblenz.de
 */
public class EmptyDefinition implements Definition {

	/**
	 * An empty set.
	 */
	private static Set<String> emptySet = new HashSet<String>();

	/**
	 * The {@link TemporaryDefinitionStruct} this EmptyDefinition is constructed
	 * from.
	 */
	public TemporaryDefinitionStruct struct;

	/**
	 * Constructs an EmptyDefinition from a {@link TemporaryDefinitionStruct}.
	 * 
	 * @param struct
	 *            A {@link TemporaryDefinitionStruct}.
	 */
	protected EmptyDefinition(TemporaryDefinitionStruct struct) {
		this.struct = struct;
	}

	@Override
	public void addNonExistingAttributes(Definition spec) {
	}

	@Override
	public String getAttributeValue(String name) {
		return null;
	}

	@Override
	public Set<String> getAttributeNames() {
		return emptySet;
	}

	@Override
	public void overwriteAttributes(Definition spec) {
	}

	@Override
	public void setAttribute(String name, String value) {
	}

	@Override
	public Definition clone() {
		return this;
	}
}
