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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Provides the general implementation for {@link Definition}.
 * 
 * @author ist@uni-koblenz.de
 */
public abstract class AbstractDefinition implements Definition {

	/**
	 * A map storing all attributes with their name as key and their GReQL-query
	 * as value.
	 */
	protected Map<String, String> attributes;

	/**
	 * Constructs an AbstractDefinition and initializes all data structures.
	 */
	protected AbstractDefinition() {

		attributes = new HashMap<String, String>();
	}

	/**
	 * Constructs an AbstractDefinition from a AbstractDefinition and copys all
	 * data structures.
	 */
	public AbstractDefinition(AbstractDefinition definition) {
		this();
		attributes = new HashMap<String, String>(definition.attributes);
	}

	@Override
	public String getAttributeValue(String name) {
		return attributes.get(name);
	}

	@Override
	public Set<String> getAttributeNames() {
		return attributes.keySet();
	}

	@Override
	public void setAttribute(String name, String value) {
		attributes.put(name, value);
	}

	// public abstract void validateAttributeNames();

	// TODO implement this mechanism to prevent wrong attribute to occur.
	public void validateAttributeNames(Set<String> allowedNames) {
		for (String attributeName : attributes.keySet()) {
			allowedNames.contains(attributeName);
		}
	}

	@Override
	public void addNonExistingAttributes(Definition spec) {
		for (String name : spec.getAttributeNames()) {
			if (attributes.containsKey(name)) {
				continue;
			}
			attributes.put(name, spec.getAttributeValue(name));
		}
	}

	@Override
	public void overwriteAttributes(Definition spec) {
		for (String name : spec.getAttributeNames()) {
			attributes.put(name, spec.getAttributeValue(name));
		}
	}

	@Override
	public abstract Definition clone();
}
