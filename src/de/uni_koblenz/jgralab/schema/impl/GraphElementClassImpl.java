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

package de.uni_koblenz.jgralab.schema.impl;

import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.schema.GraphClass;
import de.uni_koblenz.jgralab.schema.GraphElementClass;
import de.uni_koblenz.jgralab.schema.Package;

public abstract class GraphElementClassImpl<SC extends GraphElementClass<SC, IC>, IC extends GraphElement<SC, IC>>
		extends AttributedElementClassImpl<SC, IC> implements
		GraphElementClass<SC, IC> {

	protected GraphClass graphClass;

	/**
	 * delegates its constructor to the generalized class
	 * 
	 * @param qn
	 *            the unique identifier of the element in the schema
	 */
	protected GraphElementClassImpl(String simpleName, Package pkg,
			GraphClass graphClass) {
		super(simpleName, pkg, graphClass.getSchema());
		this.graphClass = graphClass;
	}

	@Override
	public GraphClass getGraphClass() {
		return graphClass;
	}

	public String getDescriptionString() {
		StringBuilder output = new StringBuilder(this.getClass()
				.getSimpleName() + " '" + getQualifiedName() + "'");
		if (isAbstract()) {
			output.append(" (abstract)");
		}
		output.append(": \n");

		output.append("subClasses of '" + getQualifiedName() + "': ");

		for (SC aec : getAllSubClasses()) {
			output.append("'" + aec.getQualifiedName() + "' ");
		}
		output.append("\nsuperClasses of '" + getQualifiedName() + "': ");
		for (SC aec : getAllSuperClasses()) {
			output.append("'" + aec.getQualifiedName() + "' ");
		}
		output.append("\ndirectSuperClasses of '" + getQualifiedName() + "': ");
		for (SC aec : getDirectSuperClasses()) {
			output.append("'" + aec.getQualifiedName() + "' ");
		}

		output.append(attributesToString());
		output.append("\n");

		return output.toString();
	}
}
